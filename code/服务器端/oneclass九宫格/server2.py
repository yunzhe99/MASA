import socket
import time
import threading
import pandas as pd
import csv
import os
import numpy as np
import socketserver
import oneclasstrain
import oneclasspredict


def mkdir(path):
    folder = os.path.exists(path) 
    if not folder:                   #判断是否存在文件夹如果不存在则创建为文件夹
        os.makedirs(path)

def excel2csv(filename):
    df = pd.read_excel(filename)
    target_filename = filename.replace("excel", "csv").replace("xls", "csv")
    df.to_csv(target_filename,index = False)
    print("Save as %s" % target_filename)

# 生成csv
def genarate_csv_and_mark(filename):
    global keydown_time_list
    global Recording
    excel2csv(filename)
    Recording = True



SOCKET_PORT = 12345#默认服务器端口
Recording = True
user_password_csv='user_password.csv'#默认存储用户名和密码的

class Myserver(socketserver.BaseRequestHandler):
    
    def handle(self):
        global Recording
        Recording = True
        print("client connect!")
        conn = self.request
        size = 0
        train_or_predict=0
        username=''
        password=''
        while True:
            op_code = conn.recv(1)
            if op_code and op_code != bytes(0):
                pass
            else:
                continue

            op_code = int.from_bytes(op_code, byteorder='big')
            # print(op_code)

            # 根据op_code的值来执行相应的动作
            if op_code == 0xF0:

                # 计算延迟
                conn.sendall(bytes(str(int(time.time()*1000)), encoding="utf-8"))
                pass

            elif op_code == 0xF1:

                # 接受数据
                if size > 0:
                    current_size = 0
                    if(train_or_predict==1):#如果是训练，放在这个文件夹
                        mkdir('./excel/')
                        mkdir('./excel/'+username+'/')
                        mkdir('./csv/'+username+'/')
                        full_file_name='./excel/'+username+'/'+file_name
                    else:#如果是预测，放在待预测文件夹
                        mkdir('./wait_predict/')
                        mkdir('./wait_predict/'+username+'/')
                        full_file_name='./wait_predict/'+username+'/'+file_name
                    with open(full_file_name, 'wb') as f:
                        while True:
                            if current_size == size:
                                break
                            data = conn.recv(1024)
                            f.write(data)
                            current_size += len(data)

                    conn.sendall((0xE1).to_bytes(1, byteorder='big'))  # 通知客户机传输结束
                    print("Save file: %s" % file_name)

                    #genarate_csv_and_mark(full_file_name)
                else:  # 错误处理
                    pass
            
            # 传文件名
            elif op_code == 0xF2:
                file_name=''
                file_name=str(conn.recv(1024), encoding="utf-8")
                conn.sendall((0xE3).to_bytes(1, byteorder='big'))
                Recording = True
                
            # 文件大小
            elif op_code == 0xF3:
                       
                size = int(str(conn.recv(1024), encoding="utf-8"))
                print("file size %d" % size)
                conn.sendall((0xE3).to_bytes(1, byteorder='big'))  # 允许客户机开始传输

                Recording = False

            elif op_code == 0xF4:
                #传送完毕，告诉结果
                if(train_or_predict==1):#1表示训练，在这里训练
                    if(os.path.exists(user_password_csv)):#如果存在用户和密码对应表，则查表
                        df=pd.read_csv(user_password_csv)#如果存在则读取现有表，否则建立新表
                        df=df.append({'username':username,'password':password},ignore_index=True)
                        df.to_csv(user_password_csv,index=False)
                    else:
                        df=pd.DataFrame(columns=('username','password'))
                        df=df.append({'username':username,'password':password},ignore_index=True)
                        df.to_csv(user_password_csv,index=False)                 
                    print('\nuser %s finish trainning\n'%username)
                    conn.sendall((0xE4).to_bytes(1, byteorder='big'))  # 告诉客户端训练完毕                   
                    #在这里开始训练....防止客户等太久
                    oneclasstrain.mytrain(username)                   
                    del df
                else:#2表示验证
                    df=pd.read_csv(user_password_csv)#读取密码
                    find_flag=0
                    for index,row in df.iterrows():
                        if(str(row['username'])==username):
                            if(str(row['password'])==password):
                                find_flag=1
                                #密码正确了，才使用MVSA进行验证，否则密码都错了
                                judge=oneclasspredict.mypredict(username,full_file_name)
                                if(judge==1):
                                    print('\nuser %s is true user!\n'%username)                                                           
                                    conn.sendall((0xE5).to_bytes(1, byteorder='big'))  # 把结果告诉客户端，是真的
                                else:
                                    print('\nuser %s is fake user!\n'%username)
                                    conn.sendall((0xE6).to_bytes(1, byteorder='big'))  # 把结果告诉客户端，是假的
                            else:
                                print('\nuser %s is fake user!\n'%username)
                                conn.sendall((0xE6).to_bytes(1, byteorder='big'))  # 把结果告诉客户端，是假的
                            break
                    if(find_flag==0):
                        print('\nuser %s is fake user!\n'%username)
                        conn.sendall((0xE6).to_bytes(1, byteorder='big'))  # 把结果告诉客户端，是假的
                    del df
                #如果是假用户，返回0xE6

            #九宫格预测时，先发送正确的密码到手机端,先发送预测标志0xFE，然后发送用户名0xFC，然后请求发送密码
            elif op_code ==0xF5:
                df=pd.read_csv(user_password_csv)#读取密码
                for index,row in df.iterrows():
                    if(str(row['username'])==username):
                        conn.sendall(bytes(str(row['password'])+'\n',encoding='utf-8'))  # 把密码告诉客户端，客户端设置，并记录
                        #print('密码%s已发送'%str(row['password']))
                del df                
                
            #用户名
            elif op_code == 0xFC:
                username=''
                username=str(conn.recv(1024), encoding="utf-8")
                if(train_or_predict==1):#训练模式
                    print('user name is '+username)
                    mkdir('./excel/'+username+'/')
                    conn.sendall((0xE3).to_bytes(1, byteorder='big'))
                else:
                    df=pd.read_csv(user_password_csv)#用户
                    flag=0
                    for index,row in df.iterrows():
                        if(str(row['username'])==username):
                            flag=1
                    del df
                    if(flag==1):
                        conn.sendall((0xE3).to_bytes(1, byteorder='big'))
                        print('user name is '+username)
                    else:
                        conn.sendall((0xE9).to_bytes(1, byteorder='big'))#用户不存在
                        print('user %s is not exist'%username)

            #真实密码
            elif op_code == 0xFD:
                password=''
                password=str(conn.recv(1024), encoding="utf-8")
                print('password is '+password)
                conn.sendall((0xE3).to_bytes(1, byteorder='big'))

            #是训练还是预测
            elif op_code == 0xFE:
                train_or_predict = int(str(conn.recv(1024), encoding="utf-8"))
                conn.sendall((0xE3).to_bytes(1, byteorder='big'))  # 允许客户机开始传输
                if(train_or_predict==1):
                    print('训练')
                else:
                    print('预测')
                
                Recording = True


            elif op_code == 0xFF:
                # 客户端关闭，结束通信
                print("client close")
                #print(conn.addr)
                break



if __name__ == "__main__":

    server = socketserver.ThreadingTCPServer(("10.133.167.197", SOCKET_PORT), Myserver)
    server.serve_forever()


