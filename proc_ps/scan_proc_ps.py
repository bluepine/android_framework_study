import sys
import os
import shlex, subprocess

def clean_hex(num):
    '''
    get rid of the L at the end of hex string!
    '''
    h = hex(num)
    if h[-1] == 'L':
        h = h[:-1]
    return h

def run_cmd(cmd):
    '''
    return stdout in a line list
    '''
    print cmd
    args  = shlex.split(cmd)
    output = subprocess.check_output(args)
    lines = output.split('\r\n')
    for line in lines:
        line.strip()
    while '' in lines:
        lines.remove('')
    return lines

def dump_process(pid, entry):
    '''
    currently supported enties:
    USER
    PPID
    VSIZE
    RSS
    PC
    STATE
    NAME
    task
    fd
    environ
    auxv
    status
    limits
    sched
    comm
    cmdline
    stat
    statm
    maps
    cwd
    exe
    smaps
    wchan
    stack
    cgroup
    task/sched
    task/stat
    task/wchan
    task/stack
    task/status
    task/comm
    '''
    ret = ''
    if entry in ['USER', 'PPID', 'VSIZE', 'RSS', 'PC', 'STATE', 'NAME']:
        cmd = 'adb shell ps '+str(pid)
        lines = run_cmd(cmd)
        lineno = 0
        entries = ['USER', 'PID', 'PPID', 'VSIZE', 'RSS', 'WCHAN', 'PC', 'STATE', 'NAME']
        values = []
        for line in lines:
            lineno = lineno + 1
            if lineno == 1:
                continue
            elif lineno == 2:
                values = line.split(' ')
                while '' in values:
                    values.remove('')
            break;
        if len(values) == 0:
            print 'process '+str(pid)+' does\'t exist'
            return None
        for i in range(len(entries)):
            if entry == entries[i]:
                return values[i]
    #dump threads
    if entry in ['task/sched', 'task/stat', 'task/wchan', 'task/stack', 'task/status', 'task/comm']:
        entry = entry.split('/')
        entry = entry[1]
        threads = adb_ls('/proc/'+str(pid)+'/task')
        if threads == None:
            print 'process '+str(pid)+' does\'t exist'
            return None
        for thread in threads:
            cmd = 'adb shell cat /proc/'+str(pid)+'/task/'+thread+'/'+entry
            lines = run_cmd(cmd)
            ret = '\n'.join(lines)
        return ret

    #default: cat in proc fs
    if entry in ['exe', 'cwd', 'root']:
        cmd = 'adb shell ls -l /proc/'+str(pid)+'/'+entry
    elif entry in ['task', 'fd', 'mem', 'clear_refs']:
        cmd = 'adb shell ls /proc/'+str(pid)+'/'+entry
    else:
        cmd = 'adb shell cat /proc/'+str(pid)+'/'+entry
    lines = run_cmd(cmd)
    return '\n'.join(lines)

def adb_ls(path):
    """
    return the list of directories/files under path.
    if path points to a file, return the entry returned by ls
    if path doesn't exist, return None.    
    """
    cmd = 'adb shell ls '+path
    lines = run_cmd(cmd)
    lineno = 0
    entries = []
    for line in lines:
        line = line.strip()
        lineno = lineno + 1
        if lineno == 1:
            if line.find('No such file or directory') >= 0:
                print path + ' doesn\'t exist'
                entries = None
                break
        entries.append(line)
    return entries

def scan_pid_with_ps(pid):
    cmd = 'adb shell ps '+str(pid)
    lines = run_cmd(cmd)
    lineno = 0
    entries = ['USER', 'PID', 'PPID', 'VSIZE', 'RSS', 'WCHAN', 'PC', 'STATE', 'NAME']
    values = []
    for line in lines:
        lineno = lineno + 1
        if lineno == 1:
            continue
        elif lineno == 2:
            values = line.split(' ')
            while '' in values:
                values.remove('')
            break;
    if len(values) == 0:
        print str(pid) + ' does\'t exist'
        return
    else:
        for i in range(len(entries)):
            print entries[i].strip(), values[i].strip()

def get_pid_list():
    cmd = 'adb shell ps'
    lines = run_cmd(cmd)
    lineno = 0
    pid_list = []
    for line in lines:
        lineno = lineno + 1
        if lineno == 1:
            continue
        values = line.split(' ')
        while '' in values:
            values.remove('')
        pid_list.append(int(values[1]))
    return pid_list

def scan_pid_in_proc(pid):
    path = '/proc/'+str(pid)
    en = adb_ls(path)
    print en
    if(len(en)>1):
        for e in en:
            entry = path+'/'+e
            print '--------------------'+entry+'--------------------'
            if e in ['exe', 'cwd', 'root']:
                cmd = 'adb shell ls -l '+entry
                print cmd
                os.system(cmd)
            elif e == 'pagemap':
                print 'skip pagemap.'
            elif e == 'auxv':
                print 'elf interpreter info. skip.'
            elif e in ['task', 'fd', 'mem', 'clear_refs']:
                cmd = 'adb shell ls '+entry
                print cmd
                os.system(cmd)
            elif e in ['fdinfo', 'net']:
                suben = adb_ls(entry)
                for sube in suben:
                    cmd = 'adb shell cat '+entry+'/'+sube
                    print cmd
                    os.system(cmd)
            else:
                cmd = 'adb shell cat '+entry
                print cmd
                os.system(cmd)
                if e in ['environ', 'wchan']:
                    print '\n'

def dump_proccess_mem(pid, start, length, dumpfile):
    '''
    use a binary on target
    make sure libs/armeabi/pm_peek has been compiled!
    '''
    pm_peek = os.path.join(sys.path[0], '..', 'pm_peek', 'libs/armeabi/pm_peek')
    cmd = 'adb push '+pm_peek+' /cache/'
    run_cmd(cmd)
    cmd = 'adb shell chmod 777 /cache/pm_peek'
    run_cmd(cmd)
    cmd = 'adb shell rm /cache/memdump'
    run_cmd(cmd)
    cmd = 'adb shell /cache/pm_peek '+str(pid)+' '+clean_hex(start)+' '+clean_hex(length)+' /cache/memdump'
    r = run_cmd(cmd)
    r = '\n'.join(r)
    print r
    if r.find('fail') >= 0 or r.find('error') >= 0:
        return False
    cmd = 'adb pull /cache/memdump '+dumpfile
    run_cmd(cmd)
    return os.path.exists(dumpfile)

def parse_process_memmap(pid):
    '''
    return: list containing
    [start, end, perm, offset, path]
    '''
    map = []
    out = dump_process(pid, 'maps')
    if out.find('No such file or directory') >= 0:
        print out
        return []
    lines = out.split('\n')
    for line in lines:
        items = line.split(' ')
        while '' in items:
            items.remove('')
        #print items
        if len(items) != 6 or items[-1] == '(deleted)':
            continue
        r = items[0].split('-')
        map.append([int(r[0], 16), int(r[1], 16), items[1], int(items[2], 16), items[5]])
    return map

def process_addr_info(pid, addr):
    if addr % 4 != 0:
        print 'address must be word aligned!'
        return None
    m = parse_process_memmap(pid)
    for r in m:
        if addr >= r[0] and addr < r[1]:
            offset = r[3]+addr-r[0]
            print hex(r[0]), hex(r[1]), r[2], hex(r[3]), r[4]
            print hex(offset)+' into '+r[4]
            pm_peek = os.path.join(sys.path[0], '..', 'pm_peek', 'libs/armeabi/pm_peek')
            cmd = 'adb push '+pm_peek+' /cache/'
            run_cmd(cmd)
            cmd = 'adb shell chmod 777 /cache/pm_peek'
            run_cmd(cmd)
            cmd = 'adb shell rm /cache/memdump'
            run_cmd(cmd)
            cmd = 'adb shell /cache/pm_peek '+str(pid)+' '+clean_hex(addr)+' '+hex(4)
            out = run_cmd(cmd)
            value = int(out[0], 16)
            print 'word at address '+hex(addr), out[0]
            return (offset, r[4], value)
    print 'address not found in mmap info'
    return None

if __name__ == "__main__":
    if len(sys.argv) != 1:
        print __file__
        sys.exit();
    #scan_pid_in_proc(int(sys.argv[1]))
    pid_list = get_pid_list()
    # print pid_list
    for pid in pid_list:
        print '----------------------'
        # print dump_process(pid, 'status')
        # print dump_process(pid, 'exe')
        # print dump_process(pid, 'comm')
        # print dump_process(pid, 'PC')
        # print dump_process(pid, 'stack')
        # print dump_process(pid, 'task/stack')
        print dump_process(pid, 'task/comm')

