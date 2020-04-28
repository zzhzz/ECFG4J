import os
import shutil
import sys
import logging

logging.basicConfig(filename=f'fdg_{sys.argv[1]}.log', filemode='w', level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

start_dir = os.getcwd()

def reset(d):
    os.chdir(start_dir)
    shutil.rmtree(d)
    logging.info(f'Complete {d}')

def run(project, bug_id):
    dir_name = project + '_' + bug_id
    if not os.path.exists(dir_name):
        os.mkdir(dir_name)
    cmd = f'defects4j checkout -p {project} -v {bug_id}b -w {dir_name}'
    #build_cmd = 'mvn test-compile'
    build_cmd = 'defects4j compile'
    info_cmd = f'defects4j info -p {project} -b {bug_id} > info.txt'
    logger.info(cmd)
    res = os.system(cmd)
    if res != 0:
        logging.error('checkout failed!')
        reset(dir_name)
        return
    os.chdir(dir_name)
    res = os.system(build_cmd)
    if res != 0:
        logging.error('Build failed!')
        reset(dir_name)
        return
    os.system(info_cmd)
    black_list = []
    start = False
    flag = False
    method_list = []
    print(black_list)
    with open('defects4j.build.properties', 'r') as rfh:
        for line in rfh:
            if line.find('d4j.tests.trigger') != -1:
                list_str = line[line.find('=')+1:].replace('\n', '')
                method_list = list_str.split(',')
        method_list = list(map(lambda x: x.replace('\n', ''), method_list))
        method_list = list(filter(lambda x: len(x) > 0 and x not in black_list and x.find('::') != -1, method_list))
    cp = ['.', '../junit-4.10.jar'] #, 'target/classes', 'target/test-classes']
    cmd = 'defects4j export -p dir.bin.classes -o dir.class'
    os.system(cmd)
    cmd = 'defects4j export -p dir.bin.tests -o dir.test'
    os.system(cmd)
        
    with open('dir.class', 'r') as rfh:
        for path in rfh:
            cp.append(path.replace('\n', ''))
    with open('dir.test', 'r') as rfh:
        for path in rfh:
            cp.append(path.replace('\n', ''))
    def getAllLibs(path):
        lis = []
        for f in os.listdir(path):
            file_path = path + f
            if os.path.isfile(file_path):
                if f.endswith('.jar'):
                    lis.append(file_path)
            elif f == 'src':
                continue 
            else:
                lis.extend(getAllLibs(file_path + '/'))
        return lis

    pathes = ':'.join(cp)
        
    testCases = {}
    claz_list = ' '.join(method_list)
    with open('method_list.json', 'w') as fh:
        json.dump(method_list, fh)
    if len(method_list) > 0:
        output_dir = f'/home/zzhzz/Documents/ECFG4J/json_datas/{project}/{bug_id}'
        cmd = f'/home/zzhzz/.conda/envs/java1.8/bin/java -jar ../ECFG4J.jar {output_dir} {pathes} 2> err'
        r = os.system(cmd)
        print(len(method_list))
        if r != 0:
            print('Failed')
            quit()
    reset(dir_name)


import json
project = sys.argv[1]

with open('bugid-list.json', 'r') as rfh:
    id_list = json.load(rfh)

start = False

for bug_id in id_list[project]:
    if bug_id == '3000470':
        start = True
    if start:
        logger.info(f'{project} {bug_id} start')
        run(project, str(bug_id))
        logger.info(f'{project} {bug_id} complete') 
        quit()
        



