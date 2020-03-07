import csv
import numpy as np
from matplotlib import pyplot as plt
import pandas as pd
import os
from sklearn.model_selection import train_test_split
from sklearn.svm import SVC
from sklearn.preprocessing import StandardScaler
import random
from sklearn.externals import joblib
import tensorflow as tf
from sklearn.model_selection import train_test_split
from keras.models import Model, load_model
from keras.layers import Input, Dense
from keras.callbacks import ModelCheckpoint
from keras import regularizers
from keras import backend as K
features3=np.array(['Xord_1','Yord_1','Premax_1','Premin_1','Premean_1','Touchtime_1',
                       'Accxmax_1','Accxmin_1','Accxmean_1', 'Accymax_1','Accymin_1','Accymean_1','Acczmax_1','Acczmin_1','Acczmean_1',
                      'Xord_2','Yord_2','Premax_2','Premin_2','Premean_2','Touchtime_2','Gaptime_2',
                       'Accxmax_2','Accxmin_2','Accxmean_2', 'Accymax_2','Accymin_2','Accymean_2','Acczmax_2','Acczmin_2','Acczmean_2',
                      'Xord_3','Yord_3','Premax_3','Premin_3','Premean_3','Touchtime_3','Gaptime_3',
                       'Accxmax_3','Accxmin_3','Accxmean_3', 'Accymax_3','Accymin_3','Accymean_3','Acczmax_3','Acczmin_3','Acczmean_3',
                      'Xord_4','Yord_4','Premax_4','Premin_4','Premean_4','Touchtime_4','Gaptime_4',
                       'Accxmax_4','Accxmin_4','Accxmean_4', 'Accymax_4','Accymin_4','Accymean_4','Acczmax_4','Acczmin_4','Acczmean_4',
                      'Xord_5','Yord_5','Premax_5','Premin_5','Premean_5','Touchtime_5','Gaptime_5',
                       'Accxmax_5','Accxmin_5','Accxmean_5', 'Accymax_5','Accymin_5','Accymean_5','Acczmax_5','Acczmin_5','Acczmean_5',
                      'Xord_6','Yord_6','Premax_6','Premin_6','Premean_6','Touchtime_6','Gaptime_6',
                       'Accxmax_6','Accxmin_6','Accxmean_6', 'Accymax_6','Accymin_6','Accymean_6','Acczmax_6','Acczmin_6','Acczmean_6'])



features0=np.array(['Xord_1','Yord_1','Sizemax_1','Sizemin_1','Sizemean_1','Touchtime_1',
                       'Accxmax_1','Accxmin_1','Accxmean_1', 'Accymax_1','Accymin_1','Accymean_1','Acczmax_1','Acczmin_1','Acczmean_1',
                      'Xord_2','Yord_2','Sizemax_2','Sizemin_2','Sizemean_2','Touchtime_2','Gaptime_2',
                       'Accxmax_2','Accxmin_2','Accxmean_2', 'Accymax_2','Accymin_2','Accymean_2','Acczmax_2','Acczmin_2','Acczmean_2',
                      'Xord_3','Yord_3','Sizemax_3','Sizemin_3','Sizemean_3','Touchtime_3','Gaptime_3',
                       'Accxmax_3','Accxmin_3','Accxmean_3', 'Accymax_3','Accymin_3','Accymean_3','Acczmax_3','Acczmin_3','Acczmean_3',
                      'Xord_4','Yord_4','Sizemax_4','Sizemin_4','Sizemean_4','Touchtime_4','Gaptime_4',
                       'Accxmax_4','Accxmin_4','Accxmean_4', 'Accymax_4','Accymin_4','Accymean_4','Acczmax_4','Acczmin_4','Acczmean_4',
                      'Xord_5','Yord_5','Sizemax_5','Sizemin_5','Sizemean_5','Touchtime_5','Gaptime_5',
                       'Accxmax_5','Accxmin_5','Accxmean_5', 'Accymax_5','Accymin_5','Accymean_5','Acczmax_5','Acczmin_5','Acczmean_5',
                      'Xord_6','Yord_6','Sizemax_6','Sizemin_6','Sizemean_6','Touchtime_6','Gaptime_6',
                       'Accxmax_6','Accxmin_6','Accxmean_6', 'Accymax_6','Accymin_6','Accymean_6','Acczmax_6','Acczmin_6','Acczmean_6'])

def mkdir(path):
    folder = os.path.exists(path) 
    if not folder:                   #判断是否存在文件夹如果不存在则创建为文件夹
        os.makedirs(path)

###########################################################################################################################
#输入:person 用户id，是一个整数
#输入:basedir  存放着用户的数据的文件夹，例如./excel/320/
#输入:dstdir 转换为csv后的文件存放路径，文件路径结构与excel一致，自动创建文件夹；用户数据放在这里面以自己的id命名的子文件夹下
#作用：xls转换成csv，同时去掉大小为0KB的文件
#例如：excel_to_csv(person,basedir='../excel/'+str(person)+'/',dstdir='./csv/')
###########################################################################################################################
def excel_to_csv(person,basedir='./excel/',dstdir='./csv/'):
    mkdir(dstdir)
    for (dirpath,dirnames,filenames) in os.walk(basedir):
        index=1
        for f in filenames:
            subdir=dstdir+str(person)+'/'#放到dstdir中的用户子文件夹下
            mkdir(subdir)
            fsize = os.path.getsize(dirpath+'/'+f)
            if(fsize==0):#转CSV的时候过滤掉大小为0的，会造成错误
                continue
            df=pd.read_excel(dirpath+'/'+f)
            df.to_csv(subdir+'/'+str(person)+'-'+str(index)+'.csv')#格式需要特殊设计
            index=index+1

###########################################################################################################################
#输入:person 用户id，是一个整数
#输入:baseinidex 存放着用户的csv数据的文件夹，例如./csv/320/，是上面的函数写入的结果
#输入:basedir 把csv数据提取出来，如6次按键，每个样本就是6行数据，放到如./sum/的用户子文件夹下
#输入:fileaddr 把sum数据提取出来，整合到这个csv文件下，每个样本是一行
#作用：用户的所有真数据放到fileaddr中，为训练做准备
#例如:person=data_transision('./csv/'+str(person)+'/','./sum/','./single/')
###########################################################################################################################
def data_transision(person,baseindex,basedir,fileaddr):
    data=[]
    #读取数据
    index=0
    maxlen=300#先假设最长是300，3秒才按完
    a=np.zeros(18,dtype=np.int32)
    for (dirpath,dirnames,filenames) in os.walk(baseindex):
        for f in filenames:
            filepath=dirpath+'/'+f
            df=pd.read_csv(filepath,sep=',')
            if(df.shape[0]>maxlen):
                maxlen=df.shape[0]
            a[0]=len(np.where(df.Xord==-2))
            a[1]=len(np.where(df.Yord==-2))
            a[2]=len(np.where(df.Pressure==-2))
            a[3]=len(np.where(df.Presize==-2))
            a[4]=len(np.where(df.Touchtime==-2))
            a[5]=len(np.where(df.Gaptime==-2))              
            a[6]=len(np.where(df.ACCx==-2))
            a[7]=len(np.where(df.ACCy==-2))
            a[8]=len(np.where(df.ACCz==-2))
            a[9]=len(np.where(df.DIRx==-2))
            a[10]=len(np.where(df.DIRy==-2))
            a[11]=len(np.where(df.DIRz==-2))
            a[12]=len(np.where(df.ACCx==-6))
            a[13]=len(np.where(df.ACCy==-6))
            a[14]=len(np.where(df.ACCz==-6))
            a[15]=len(np.where(df.DIRx==-6))
            a[16]=len(np.where(df.DIRy==-6))
            a[17]=len(np.where(df.DIRz==-6))
            t=a[0]
            flag=1
            for i in range(12):
                t=t&a[i]
            if(t!=a[0]):
                flag=0
            t=a[12]
            for i in range(12,18):
                t=t&a[i]
            if(t!=a[12]):
                flag=0
            #判断最大的
            if(flag==1):
                data.append(df)
                if(df.shape[0]>maxlen):
                    maxlen=df.shape[0]
                index=index+1
            else:
                continue
            
    #有label个采样数据，每个数据有3个加速度，3个陀螺仪，每个初始化为200个0
    X=np.zeros((index,6,maxlen))

    for i in range(0,index):
        for j in range(0,6):
            if(j==0):
                X[i][j][0:len(data[i].ACCx)]=data[i].ACCx    
            elif(j==1):
                X[i][j][0:len(data[i].ACCy)]=data[i].ACCy
            elif(j==2):
                X[i][j][0:len(data[i].ACCz)]=data[i].ACCz
            elif(j==3):
                X[i][j][0:len(data[i].DIRx)]=data[i].DIRx 
            elif(j==4):
                X[i][j][0:len(data[i].DIRy)]=data[i].DIRy
            else:
                X[i][j][0:len(data[i].DIRz)]=data[i].DIRz

    #寻找-6.0和-2.0中间夹着的数据下标，单独掏出来，存放好
    strokeindex=np.zeros((index,6,2))#表示区间,直接索引int(strokeindex[i][j][0])第i个文件的第j个起始分割

    for i in range(index):
        #对其中一个文件的分析
        tagindex=0
        for k in range(0,maxlen):
                if(X[i][0][k] == -6.0 and X[i][3][k] == -6.0):#开始标记,k+1是内容
                    strokeindex[i][tagindex][0]=k+1
                elif(X[i][0][k] == -2.0 and X[i][3][k] == -2.0):#结束标记
                    strokeindex[i][tagindex][1]=k-1
                    tagindex=tagindex+1

    #获得每段论文中IMU数据的最大[0] 最小[1]和均值[2]
    #resize_IMU[0][2][1][1] 表示第0个数据的第[3]段的第1列的(ACCx)最小值
    resize_IMU=np.zeros((index,6,6,3))
    for i in range(0,index):
        for j in range(0,6):#6个数据
            for k in range(0,6):#6段，每段的最大最小值和均值
                if(strokeindex[i][k][0]<strokeindex[i][k][1]):
                    resize_IMU[i][k][j][0]=X[i][j][int(strokeindex[i][k][0]):int(strokeindex[i][k][1])].max()#最大
                    resize_IMU[i][k][j][1]=X[i][j][int(strokeindex[i][k][0]):int(strokeindex[i][k][1])].min()#最小
                    resize_IMU[i][k][j][2]=X[i][j][int(strokeindex[i][k][0]):int(strokeindex[i][k][1])].mean()#均值
                else:#发生了交叠
                    resize_IMU[i][k][j][0]=0
                    resize_IMU[i][k][j][1]=0
                    resize_IMU[i][k][j][2]=0
    #读取左半边的数据,假设最多80行=15*6
    #坐标的读到D里面,5列，不考虑size
    everytouch=20#每次按键至多20个记录，不可能更多了
    D=np.zeros((index,6,everytouch*6))
    for i in range(0,index):
        for j in range(0,6):
            if(j==0):
                D[i][j][0:len(data[i].Xord[~np.isnan(data[i].Xord)])] = data[i].Xord[~np.isnan(data[i].Xord)]
            elif(j==1):
                D[i][j][0:len(data[i].Yord[~np.isnan(data[i].Yord)])]=data[i].Yord[~np.isnan(data[i].Yord)]
            elif(j==2):
                D[i][j][0:len(data[i].Pressure[~np.isnan(data[i].Pressure)])]=data[i].Pressure[~np.isnan(data[i].Pressure)]
            elif(j==3):
                D[i][j][0:len(data[i].Touchtime[~np.isnan(data[i].Touchtime)])]=data[i].Touchtime[~np.isnan(data[i].Touchtime)]
            elif(j==4):
                D[i][j][0:len(data[i].Gaptime[~np.isnan(data[i].Gaptime)])]=data[i].Gaptime[~np.isnan(data[i].Gaptime)]
            elif(j==5):
                D[i][j][0:len(data[i].Presize[~np.isnan(data[i].Presize)])]=data[i].Presize[~np.isnan(data[i].Presize)]

                
    #处理坐标，非常简单，只有一个，但是如何知道是坐标？需要用标记
    #6次按键，每次的坐标
    ordinate=np.zeros((index,6,2))#每个样本有6次按键坐标,分别为X和Y
    pressure=np.zeros((index,6,3))#压力最大、最小、均值
    presize=np.zeros((index,6,3))#面积最大、最小、均值
    touchtime=np.zeros((index,6))
    gaptime=np.zeros((index,6))

    firstindex=[]
    lastindex=[]
    for i in range(index):#label个样本
        k=0#第几次按压
        for row in range(everytouch*6):#至多有这么多行
            if(k==0):
                firstindex.append(0)
                k=k+1
            elif(D[i][0][row]==-2 and D[i][3][row]==-2):#是否是分割点
                k=k+1
                lastindex.append(row-1)
                if(k<=6):
                    firstindex.append(row+1)
        for t in range(0,6):
            ordinate[i][t][0]=D[i][0][firstindex[t]]
            ordinate[i][t][1]=D[i][1][firstindex[t]]
            if(firstindex[t]==lastindex[t]):
                pressure[i][t][0]=D[i][2][firstindex[t]]
                pressure[i][t][1]=D[i][2][firstindex[t]]
                pressure[i][t][2]=D[i][2][firstindex[t]]
                presize[i][t][0]=D[i][5][firstindex[t]]
                presize[i][t][1]=D[i][5][firstindex[t]]
                presize[i][t][2]=D[i][5][firstindex[t]]
            else:
                pressure[i][t][0]=D[i][2][firstindex[t]:lastindex[t]].max()
                pressure[i][t][1]=D[i][2][firstindex[t]:lastindex[t]].min()
                pressure[i][t][2]=D[i][2][firstindex[t]:lastindex[t]].mean()
                presize[i][t][0]=D[i][5][firstindex[t]:lastindex[t]].max()
                presize[i][t][1]=D[i][5][firstindex[t]:lastindex[t]].min()
                presize[i][t][2]=D[i][5][firstindex[t]:lastindex[t]].mean()
            touchtime[i][t]=D[i][3][firstindex[t]]
            if(t==0):#第一次按启动的时候，很多用户会误解，所以不要这个数据,所以用0填充
                gaptime[i][t]=0
            else:
                gaptime[i][t]=D[i][4][firstindex[t]]
        firstindex=[]
        lastindex=[]

    subindex=1#每个人的内部索引
    #i=0#全局索引

    for i in range(index):
        if(i==0):
            mkdir(basedir+str(person)+'/')
        with open(basedir+str(person)+'/'+ str(person)+'-'+str(i+1)+'.csv',"w",newline='') as csvfile: 
            writer = csv.writer(csvfile)
            writer.writerow(['index','Xord','Yord','Premax','Premin','Premean','Sizemax','Sizemin','Sizemean','Touchtime','Gaptime',
                        'Accxmax','Accxmin','Accxmean', 'Accymax','Accymin','Accymean',
                         'Acczmax','Acczmin','Acczmean', 'Dirxmax','Dirxmin','Dirxmean',
                        'Dirymax','Dirymin','Dirymean','Dirzmax','Dirzmin','Dirzmean'])
            for j in range(0,6):#6次按键
                writer.writerow([j+1,ordinate[i][j][0],ordinate[i][j][1],pressure[i][j][0],pressure[i][j][1],pressure[i][j][2],presize[i][j][0],presize[i][j][1],presize[i][j][2],
                                touchtime[i][j],gaptime[i][j],resize_IMU[i][j][0][0],resize_IMU[i][j][0][1],
                                resize_IMU[i][j][0][2],resize_IMU[i][j][1][0],resize_IMU[i][j][1][1],resize_IMU[i][j][1][2],
                                resize_IMU[i][j][2][0],resize_IMU[i][j][2][1],resize_IMU[i][j][2][2],resize_IMU[i][j][3][0],
                                resize_IMU[i][j][3][1],resize_IMU[i][j][3][2],resize_IMU[i][j][4][0],resize_IMU[i][j][4][1],
                                resize_IMU[i][j][4][2],resize_IMU[i][j][5][0],resize_IMU[i][j][5][1],resize_IMU[i][j][5][2]])
        
        
    ###############################################################################
    ########################      把数据规整化，每行是一个样本    #################

    #读取数据
    index=0
    for (dirpath,dirnames,filenames) in os.walk(basedir+str(person)):
        for f in filenames:
            filepath=dirpath+'/'+f
            data[index]=pd.read_csv(filepath,sep=',')
            index=index+1

    ori_colnames=['Xord', 'Yord', 'Premax', 'Premin', 'Premean','Sizemax','Sizemin','Sizemean','Touchtime',
       'Gaptime', 'Accxmax', 'Accxmin', 'Accxmean', 'Accymax', 'Accymin',
       'Accymean', 'Acczmax', 'Acczmin', 'Acczmean', 'Dirxmax', 'Dirxmin',
       'Dirxmean', 'Dirymax', 'Dirymin', 'Dirymean', 'Dirzmax', 'Dirzmin',
       'Dirzmean']

    touchtimes=6#假设按6次
    batchsize=data[0].shape[1]-1

    #每个样本是一行，每个样本有(data[0].columns-1)*6+1列，加的‘1’是样本标记
    finaldata=np.zeros((index,batchsize*6+1))
    #检索每个样本
    for i in range(index):
        #每个样本中6行压缩在1个里面
        finaldata[i][0]=person#标记
        for j in range(0,touchtimes):
            finaldata[i][j*batchsize+1:(j+1)*batchsize+1]=data[i].iloc[j][1:(batchsize+1)]

    #存起来
    
    #i=0#全局索引
    colnames=np.empty((1,batchsize*6+1),dtype=object)
    colnames[0][0]='tag'#标记

    for i in range(0,len(ori_colnames)):
        for j in range(0,touchtimes):
            colnames[0][len(ori_colnames)*j+i+1]=ori_colnames[i]+'_'+str(j+1)
       
    df=pd.DataFrame(finaldata,columns=colnames[0])
    mkdir(fileaddr+str(person)+'/')
    df.to_csv(fileaddr+str(person)+'/'+str(person)+'.csv',index=False,sep=',')
    return person


def get_train_data(person,person_fileaddr,csvdatabase,traindata_fileaddr,meanpath,varpath):
    scaler=StandardScaler()
    truedata=pd.read_csv(person_fileaddr+str(person)+'.csv')#真的数据
    features=features3#特征
    trueuser_data=truedata[features]
    scaler.fit(trueuser_data.values)
    traindata=truedata
    traindata.to_csv(traindata_fileaddr,index=False)
    mkdir(meanpath+str(person)+'/')
    mkdir(varpath+str(person)+'/')
    np.save(meanpath+str(person)+'/'+str(person)+'.npy',scaler.mean_)
    np.save(varpath+str(person)+'/'+str(person)+'.npy',scaler.var_)
    #return scaler.mean_,scaler.var_


###########################################################################################################################
#输入:person 用户id，是一个整数
#输入:train_filename  训练数据放在这个文件里面，包括真用户数据，和不是ta的用户数据
#输入:modelpath 该用户模型存放路径，如./model/，模型会存放在该路径的子文件夹下
#输入:meanpath 该用户真数据的均值会放在该路径的用户子文件夹下
#输入:varpath 该用户真数据的方差会放在该路径的用户子文件夹下
#作用：训练
#例如:train_model(person,str(person)+'_train.csv','./model/','./mean/','./var/')
###########################################################################################################################
def train_model(person,train_filename,modelpath,meanpath,varpath,historypath):
    features=features3#按键行为识别是1/3特征，如果只有面积，是3号特征
    mkdir(modelpath)
    
    df=pd.read_csv(train_filename,sep=',')
    mean=np.load(meanpath+str(person)+'/'+str(person)+'.npy')
    var=np.load(varpath+str(person)+'/'+str(person)+'.npy')

    mask = (df['tag'] == person)
    data = df[features]
    
    # 正样本按照7:3切成训练集和测试集
    X_train, X_test = train_test_split(data[mask], test_size=0.3, random_state=666)
    X_train, X_valid = train_test_split(X_train, test_size=0.3, random_state=666)
    X_train=(X_train-mean)/np.sqrt(var)
    X_valid=(X_valid-mean)/np.sqrt(var)


    # 设置Autoencoder的参数
    input_dim = X_train.shape[1]
    encoding_dim = 35#按键是35，九宫格50
    num_epoch = 60
    batch_size = 5

    input_layer = Input(shape=(input_dim, ))
    encoder = Dense(encoding_dim, activation="tanh", 
                    activity_regularizer=regularizers.l1(10e-5))(input_layer)#10e-5
    encoder = Dense(int(encoding_dim / 2), activation="relu",activity_regularizer=regularizers.l1(10e-5))(encoder)
    decoder = Dense(int(encoding_dim / 2), activation='relu')(encoder)
    decoder = Dense(input_dim, activation='tanh')(decoder)
    autoencoder = Model(inputs=input_layer, outputs=decoder)
    autoencoder.compile(optimizer='adam', 
                        loss='mean_squared_error', 
                        metrics=['mae'])

    # 模型保存，并开始训练模型
    mkdir(modelpath+str(person))
    modelpath=modelpath+'/'+str(person)+'/'+str(person)+'.model'
    checkpointer = ModelCheckpoint(filepath=modelpath,
                                   verbose=0,
                                   save_best_only=True)
    history = autoencoder.fit(X_train, X_train,
                              epochs=num_epoch,
                              batch_size=batch_size,
                              shuffle=True,
                              validation_data=(X_valid, X_valid),
                              verbose=0, 
                              callbacks=[checkpointer]).history
    mkdir(historypath+str(person))
    np.save(historypath+str(person)+'/'+str(person)+'.history.npy',np.array(history['val_loss']))

def mytrain(username):       
    person=int(username)
    #basedir是存放着要训练的用户的数据的文件夹,例如./excel/320/
    #1. dstdir是要把用户数据放到哪个大文件夹里面，会自动创建子文件夹，注意第2个参数是这个用户的excel数据的文件夹，文件夹里面就是用户数据没有子文件夹
    excel_to_csv(person,basedir='./excel/'+str(person)+'/',dstdir='./csv/')
    #2. 第一个参数是上面那个具体的文件夹，第2个参数是把csv转成小的暂时存放在哪里，第3个参数是CSV用户数据库，用户数据会存放进去【实验时，可以先把数据库里的一个用户删掉】
    #csvdatabase里面是每个用户数据是单独的一个表，用很多用户的信息，./sum/里面是每个样本一个表
    person=data_transision(person,'./csv/'+str(person)+'/','./sum/','./csvdatabase/')
    mkdir('./traindata/')
    #3. 第2个地址是用户数据库中这个用户的子文件夹，下一个参数是用户数据库，再下一个是这个用户生成的训练文件，后面是均值和方差存放在哪里
    get_train_data(person,'./csvdatabase/'+str(person)+'/','./csvdatabase/','./traindata/'+str(person)+'_train.csv','./mean/','./var/')
    #4. 训练
    train_model(person,'./traindata/'+str(person)+'_train.csv','./model/','./mean/','./var/','./history/')
    K.clear_session()
#mytrain('321')
