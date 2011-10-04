/* Song Wei */
#define _FILE_OFFSET_BITS 64
#define _XOPEN_SOURCE 500
#include <sys/mman.h>
#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/ptrace.h>
#define _FILE_OFFSET_BITS 64
#define _XOPEN_SOURCE 500
/* ATTENTION: using this program to probe process memory might have side effects on the target process */
int main(int argc, char * argv[]){
  char *p;
  int pid;
  int r;
  int status;
  long start;
  long offset;
  long length;
  int wfd;
  const char * dump_file;
  int ret = -1;

  if(argc != 5 && argc != 4){
    printf("usage: pm_dump <pid> <start in hex> <length in hex> [dump file]\n");
    return -1;
  }
  if (1 != sscanf(argv[1], "%d", &pid)){
    printf("invalid length: %s\n", argv[1]);
    return -1;
  }
  if (1 != sscanf(argv[2], "0x%x", &start)){
    printf("invalid length: %s\nplease remeber to add 0x prefix\n", argv[2]);
    return -1;
  }
  if (1 != sscanf(argv[3], "0x%x", &length)){
    printf("invalid length: %s\nplease remeber to add 0x prefix\n", argv[3]);
    return -1;
  }
  if(argc == 5){
    dump_file = argv[4];
  }else{
    dump_file = NULL;
  }
  p = mmap(NULL, length, PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);
  if(p == MAP_FAILED){
    printf("mmap failed: %s\n", strerror(errno));
    return -1;
  }
  //  printf("opening %s\n", path);
  //we have to attch to the process to stop it first.
  if ((ptrace(PTRACE_ATTACH, pid, 0, 0)) != 0) {
    printf("failed to attach to process %d: %s\n",pid, strerror(errno));
    return -1;
  }
  r = wait( &status );
  if(r != pid){
    printf("stopped pid:%d, waiting pid:%d, they don't match\nerror msg:%s\n", r, pid, strerror(errno));
    goto detach;
  }
  if(WIFEXITED( status )){
    printf("process:%d exited with code %d, game over\n", pid, WEXITSTATUS(status));
    return -1;
  }

  if(dump_file){
    wfd = open(dump_file, O_WRONLY | O_CREAT);
    if(wfd < 0){
      printf("error opening %s\n", dump_file);
      return -1;
    }
  }else{
    wfd = -1;
  }
  //  printf("hi\n");
  offset = 0;
  while(offset < length){
    //printf("reading 0x%x bytes from 0x%x\n", length_to_read, start);
    //printf("hi\n");
    *((long *)(p+offset)) = ptrace(PTRACE_PEEKTEXT, pid, (void *)(start+offset), NULL);
    if(wfd <0){
      printf("0x%x\n", *((long *)(p+offset)));
    }
    offset += sizeof(long);
  }
  if(wfd >= 0){
    if(length != write(wfd, p, length)){
      printf("write failed\n");
      goto close_file;
    }
  }

  ret = 0;  
 detach:
  //let go
  if ((ptrace(PTRACE_DETACH, pid, 0, 0)) != 0) {
    printf("failed to detach process %d\n",pid);
  }
 close_file:
  if(wfd >= 0){
    close(wfd);
  }
  munmap(p, length);
  //printf("pid %d, start 0x%x, length 0x%x\n", pid, start, length);
  
  return ret;
}
