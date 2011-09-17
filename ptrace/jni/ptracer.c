#include <stdio.h>
#include <sys/ptrace.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <linux/user.h>
#include <signal.h>

int main(int argc, char ** argv[])
{  
  struct pt_regs u;
  pid_t child;
  pid_t wait_child;
  int status;
  int i;
  int steps;
  long w;
  siginfo_t sig;
  void * addr;
  if(argc != 2){
    printf("ptracer pid\n");
    return 0;
  }
  child = atoi(argv[1]);
  printf("tracing %d\n", child);
  steps=0;
  w = ptrace(PTRACE_ATTACH, child, NULL, NULL);
  printf("ptrace PTRACE_ATTACH returns:%d\n", w);
  if(w < 0){
    return 0;
  }
  while(1) {
    steps++;
    //printf("waiting for child %d\n", child);
    wait_child = wait( &status );
    if(wait_child != child){
      printf("child:%d, wait_child:%d, they don't match\n", child, wait_child);
      return 0;
    }
    if(WIFEXITED( status )){
      printf("child:%d exited with code %d\n", child, WEXITSTATUS(status));
      break;
    }
    /* if (WIFSTOPPED( status )){ */
    /*   printf( "Child has stopped due to signal %d\n", */
    /* 	      WSTOPSIG( status ) ); */
    /* } */
    /* if(WIFSIGNALED(status)){ */
    /*   printf( "Child %ld received signal %d\n", */
    /* 	      (long)child, */
    /* 	      WTERMSIG(status) ); */
    /* } */
    /* addr = (void*)32; */
    /* w = ptrace(PTRACE_PEEKTEXT, */
    /* 	       child, addr, */
    /* 	       NULL); */
    /* printf("content at address 0%x:%x\n", addr, w); */
    /* w = ptrace(PTRACE_GETSIGINFO, */
    /* 	       child, NULL, */
    /* 	       &sig); */
    /* printf("ptrace PTRACE_GETSIGINFO returns:%d\n", w); */
    /* printf("si_signo %d, si_code %d\n", sig.si_signo, sig.si_code); */
    memset(&u, 0, sizeof(u));
    w = ptrace(PTRACE_GETREGS,
	       child, NULL,
	       &u);
    if(w){
      printf("ptrace PTRACE_GETREGS returns:%d\n", w);
      goto detach;
    }
    /* for(i=0; i<18; i++){       */
    /*   printf("%08x ", u.uregs[i]); */
    /* } */
    printf("pc:0x%x\n", u.ARM_pc);
    if(steps<0){
      w = ptrace(PTRACE_SYSCALL,
      		 child, NULL,
      		 NULL);
      printf("ptrace PTRACE_SINGLESTEP returns:%d\n", w);
      /* w = ptrace(PTRACE_CONT, */
      /* 		 child, NULL, */
      /* 		 NULL); */
      /* if(w){ */
      /* 	printf("ptrace PTRACE_CONT returns:%d\n", w); */
      /* 	goto detach; */
      /* } */
    }else{
      break;
    }
  }
 detach:
  w = ptrace(PTRACE_DETACH, child, NULL, NULL);
  printf("ptrace PTRACE_DETACH returns:%d\n", w);
  return 0;
}
