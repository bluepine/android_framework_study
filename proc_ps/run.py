from scan_proc_ps import *
import os

# spid= 0
# pid_list = get_pid_list()
# for pid in pid_list:
#     print dump_process(pid, 'exe')
#     print dump_process(pid, 'NAME')

# print pid_list
pid = 143
out = dump_process(pid, 'PC')
print out
if out != None:
    addr_info = process_addr_info(pid, int(out, 16))
    print addr_info
    print dump_process(pid, 'maps')
    print dump_process(pid, 'task/comm')
    print dump_process(pid, 'task/stack')
    print dump_process(pid, 'task/sched')


# m = parse_process_memmap(pid)
# for r in m:
# #    print hex(r[0]), hex(r[1]), r[2], hex(r[3]), r[4]
# #    if r[2][2] == 'x':
#     if r[4] == addr_info[1] and r[2][2] == 'x':
#         print hex(r[0]), hex(r[1]), r[2], hex(r[3]), r[4]
#         dump_proccess_mem(pid, r[0], r[1]-r[0], 'memdump')

# print dump_process(pid, 'PC')

# for pid in pid_list:
# #    scan_pid_in_proc(pid)
# #     print '----------------------'
#     print dump_process(pid, 'status')
#     print dump_process(pid, 'exe')
#     print dump_process(pid, 'task/sched')
#     print dump_process(pid, 'maps')
    # print dump_process(pid, 'comm')
    # print dump_process(pid, 'PC')
    # print dump_process(pid, 'stack')
    #print dump_process(pid, 'task/stack')
    #print dump_process(pid, 'task/comm')


