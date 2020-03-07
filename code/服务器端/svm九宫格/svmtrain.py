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
import math
import warnings
warnings.filterwarnings("ignore")

features0=np.array(['Premax_1','Premin_1','Premean_1','Gaptime_1','dismax_1','dismin_1','dismean_1','spdmax_1','spdmin_1','spdmean_1',
                   'Accxmax_1','Accxmin_1','Accxmean_1', 'Accymax_1','Accymin_1','Accymean_1','Acczmax_1','Acczmin_1','Acczmean_1',
                   'Dirxmax_1','Dirxmin_1','Dirxmean_1', 'Dirymax_1','Dirymin_1','Dirymean_1','Dirzmax_1','Dirzmin_1','Dirzmean_1',
                  'Premax_2','Premin_2','Premean_2','Gaptime_2','dismax_2','dismin_2','dismean_2','spdmax_2','spdmin_2','spdmean_2',
                   'Accxmax_2','Accxmin_2','Accxmean_2', 'Accymax_2','Accymin_2','Accymean_2','Acczmax_2','Acczmin_2','Acczmean_2',
                   'Dirxmax_2','Dirxmin_2','Dirxmean_2', 'Dirymax_2','Dirymin_2','Dirymean_2','Dirzmax_2','Dirzmin_2','Dirzmean_2',
                  'Premax_3','Premin_3','Premean_3','Gaptime_3','dismax_3','dismin_3','dismean_3','spdmax_3','spdmin_3','spdmean_3',
                   'Accxmax_3','Accxmin_3','Accxmean_3', 'Accymax_3','Accymin_3','Accymean_3','Acczmax_3','Acczmin_3','Acczmean_3',
                   'Dirxmax_3','Dirxmin_3','Dirxmean_3', 'Dirymax_3','Dirymin_3','Dirymean_3','Dirzmax_3','Dirzmin_3','Dirzmean_3',
                  'Premax_4','Premin_4','Premean_4','Gaptime_4','dismax_4','dismin_4','dismean_4','spdmax_4','spdmin_4','spdmean_4',
                   'Accxmax_4','Accxmin_4','Accxmean_4', 'Accymax_4','Accymin_4','Accymean_4','Acczmax_4','Acczmin_4','Acczmean_4',
                   'Dirxmax_4','Dirxmin_4','Dirxmean_4', 'Dirymax_4','Dirymin_4','Dirymean_4','Dirzmax_4','Dirzmin_4','Dirzmean_4',
                  'Premax_5','Premin_5','Premean_5','Gaptime_5','dismax_5','dismin_5','dismean_5','spdmax_5','spdmin_5','spdmean_5',
                   'Accxmax_5','Accxmin_5','Accxmean_5', 'Accymax_5','Accymin_5','Accymean_5','Acczmax_5','Acczmin_5','Acczmean_5',
                   'Dirxmax_5','Dirxmin_5','Dirxmean_5', 'Dirymax_5','Dirymin_5','Dirymean_5','Dirzmax_5','Dirzmin_5','Dirzmean_5',
                  'Premax_6','Premin_6','Premean_6','Gaptime_6','dismax_6','dismin_6','dismean_6','spdmax_6','spdmin_6','spdmean_6',
                   'Accxmax_6','Accxmin_6','Accxmean_6', 'Accymax_6','Accymin_6','Accymean_6','Acczmax_6','Acczmin_6','Acczmean_6',
                   'Dirxmax_6','Dirxmin_6','Dirxmean_6', 'Dirymax_6','Dirymin_6','Dirymean_6','Dirzmax_6','Dirzmin_6','Dirzmean_6'])


features3=np.array(['Premax_1','Premin_1','Premean_1','Gaptime_1','dismax_1','dismin_1','dismean_1','spdmax_1','spdmin_1','spdmean_1',
                   'Dirxmax_1','Dirxmin_1','Dirxmean_1', 'Dirymax_1','Dirymin_1','Dirymean_1','Dirzmax_1','Dirzmin_1','Dirzmean_1',
                  'Premax_2','Premin_2','Premean_2','Gaptime_2','dismax_2','dismin_2','dismean_2','spdmax_2','spdmin_2','spdmean_2',
                   'Dirxmax_2','Dirxmin_2','Dirxmean_2', 'Dirymax_2','Dirymin_2','Dirymean_2','Dirzmax_2','Dirzmin_2','Dirzmean_2',
                  'Premax_3','Premin_3','Premean_3','Gaptime_3','dismax_3','dismin_3','dismean_3','spdmax_3','spdmin_3','spdmean_3',
                   'Dirxmax_3','Dirxmin_3','Dirxmean_3', 'Dirymax_3','Dirymin_3','Dirymean_3','Dirzmax_3','Dirzmin_3','Dirzmean_3',
                  'Premax_4','Premin_4','Premean_4','Gaptime_4','dismax_4','dismin_4','dismean_4','spdmax_4','spdmin_4','spdmean_4',
                   'Dirxmax_4','Dirxmin_4','Dirxmean_4', 'Dirymax_4','Dirymin_4','Dirymean_4','Dirzmax_4','Dirzmin_4','Dirzmean_4',
                  'Premax_5','Premin_5','Premean_5','Gaptime_5','dismax_5','dismin_5','dismean_5','spdmax_5','spdmin_5','spdmean_5',
                   'Dirxmax_5','Dirxmin_5','Dirxmean_5', 'Dirymax_5','Dirymin_5','Dirymean_5','Dirzmax_5','Dirzmin_5','Dirzmean_5',
                  'Premax_6','Premin_6','Premean_6','Gaptime_6','dismax_6','dismin_6','dismean_6','spdmax_6','spdmin_6','spdmean_6',
                   'Dirxmax_6','Dirxmin_6','Dirxmean_6', 'Dirymax_6','Dirymin_6','Dirymean_6','Dirzmax_6','Dirzmin_6','Dirzmean_6'])

features4=np.array(['Sizemax_1','Sizemin_1','Sizemean_1','Gaptime_1','dismax_1','dismin_1','dismean_1','spdmax_1','spdmin_1','spdmean_1',
                   'Dirxmax_1','Dirxmin_1','Dirxmean_1', 'Dirymax_1','Dirymin_1','Dirymean_1','Dirzmax_1','Dirzmin_1','Dirzmean_1',
                  'Sizemax_2','Sizemin_2','Sizemean_2','Gaptime_2','dismax_2','dismin_2','dismean_2','spdmax_2','spdmin_2','spdmean_2',
                   'Dirxmax_2','Dirxmin_2','Dirxmean_2', 'Dirymax_2','Dirymin_2','Dirymean_2','Dirzmax_2','Dirzmin_2','Dirzmean_2',
                  'Sizemax_3','Sizemin_3','Sizemean_3','Gaptime_3','dismax_3','dismin_3','dismean_3','spdmax_3','spdmin_3','spdmean_3',
                   'Dirxmax_3','Dirxmin_3','Dirxmean_3', 'Dirymax_3','Dirymin_3','Dirymean_3','Dirzmax_3','Dirzmin_3','Dirzmean_3',
                  'Sizemax_4','Sizemin_4','Sizemean_4','Gaptime_4','dismax_4','dismin_4','dismean_4','spdmax_4','spdmin_4','spdmean_4',
                   'Dirxmax_4','Dirxmin_4','Dirxmean_4', 'Dirymax_4','Dirymin_4','Dirymean_4','Dirzmax_4','Dirzmin_4','Dirzmean_4',
                  'Sizemax_5','Sizemin_5','Sizemean_5','Gaptime_5','dismax_5','dismin_5','dismean_5','spdmax_5','spdmin_5','spdmean_5',
                   'Dirxmax_5','Dirxmin_5','Dirxmean_5', 'Dirymax_5','Dirymin_5','Dirymean_5','Dirzmax_5','Dirzmin_5','Dirzmean_5',
                  'Sizemax_6','Sizemin_6','Sizemean_6','Gaptime_6','dismax_6','dismin_6','dismean_6','spdmax_6','spdmin_6','spdmean_6',
                   'Dirxmax_6','Dirxmin_6','Dirxmean_6', 'Dirymax_6','Dirymin_6','Dirymean_6','Dirzmax_6','Dirzmin_6','Dirzmean_6'])




def mkdir(path):
    folder = os.path.exists(path) 
    if not folder:                   #判断是否存在文件夹如果不存在则创建为文件夹
        os.makedirs(path)

def xlsx_to_csv_pd( xls_path, csv_path):
    for (dirpath,dirnames,filenames) in os.walk(xls_path):
        i=1
        for f in filenames:
            mkdir(csv_path+f.split('-')[0])         
            ori=dirpath+'/'+f
            tar=csv_path+f.split('-')[0]+'/' + f.split('-')[0]+'-'+str(i) + '.csv'
            if(os.path.getsize(ori)==0):#如果文件大小为0
                continue
            data_xls = pd.read_excel( ori , index_col=0)
            data_xls.to_csv(tar)
            i = i + 1

def process_csv( csv_name , userid ):
    data=pd.read_csv(csv_name)
    a=np.zeros(18,dtype=np.int32)
    a[0]=len(np.where(data.Xord==-2))
    a[1]=len(np.where(data.Yord==-2))
    a[2]=len(np.where(data.Pressure==-2))
    a[3]=len(np.where(data.Presize==-2))
    a[4]=len(np.where(data.Distance==-2))
    a[5]=len(np.where(data.Gaptime==-2))
	
    a[6]=len(np.where(data.ACCx==-2))
    a[7]=len(np.where(data.ACCy==-2))
    a[8]=len(np.where(data.ACCz==-2))
    a[9]=len(np.where(data.DIRx==-2))
    a[10]=len(np.where(data.DIRy==-2))
    a[11]=len(np.where(data.DIRz==-2))

    a[12]=len(np.where(data.ACCx==-6))
    a[13]=len(np.where(data.ACCy==-6))
    a[14]=len(np.where(data.ACCz==-6))
    a[15]=len(np.where(data.DIRx==-6))
    a[16]=len(np.where(data.DIRy==-6))
    a[17]=len(np.where(data.DIRz==-6))
    t=a[0]
    flag=1
    for i in range(12):
        t=t&a[i]
    if(t!=a[0]):
        flag=0
        linenum=0#不走下面了
        temp=[]
    t=a[12]
    for i in range(12,18):
        t=t&a[i]
    if(t!=a[12]):
        flag=0
        linenum=0#不走下面了
        temp=[]
            
    if(flag==1):
    #获取短列长度
        i=0
        shrt_len=0
        for i in range(0,len(data.Xord)):
		#print(i)
            if pd.isnull(data.Xord[i])==False:
                shrt_len=shrt_len+1

        shrt_len=shrt_len+1	
	
	#获取短列间隔
        interval = []
        for i in range(shrt_len):
            if data.Pressure[i] == -2:
                interval.append(i)

	#Pressure处理部分
        pre_max = []
        pre_min = []
        pre_mean = []
        temp=data.Pressure[0:interval[0]]
        pre_max.append(temp.max())
        pre_min.append(temp.min())
        pre_mean.append(temp.mean())
	
        for i in range( 0 , len(interval)-1 ):
            temp=data.Pressure[ interval[i]+1 : interval[i+1] ]
            #print(i)
            #print(temp)
            pre_max.append(temp.max())
            pre_min.append(temp.min())
            pre_mean.append(temp.mean())


	#Presize处理部分
        size_max = []
        size_min = []
        size_mean = []
        temp=data.Presize[0:interval[0]]
        size_max.append(temp.max())
        size_min.append(temp.min())
        size_mean.append(temp.mean())
	
        for i in range( 0 , len(interval)-1 ):
            temp=data.Presize[ interval[i]+1 : interval[i+1] ]
            #print(i)
            #print(temp)
            size_max.append(temp.max())
            size_min.append(temp.min())
            size_mean.append(temp.mean())


	#Distance处理部分
        dis_max = []
        dis_min = []
        dis_mean = []
        temp=data.Distance[0:interval[0]]
        if(temp.size==0):
            temp=np.array(0)
        dis_max.append(temp.max())
        dis_min.append(temp.min())
        dis_mean.append(temp.mean())
	
        for i in range( 0 , len(interval)-1 ):
            temp=data.Distance[ interval[i]+1 : interval[i+1] ]
		#print(i)
		#print(temp)
            if(temp.size==0):
                temp=np.array(0)
            dis_max.append(temp.max())
            dis_min.append(temp.min())
            dis_mean.append(temp.mean())

	
	#gaptime处理部分
        gap = []
        for i in range( 0 , len(interval) ):
            temp=data.Gaptime[ interval[i]+1 ]
            if(temp.size==0):
                temp=np.array(0)
            gap.append(temp)

	
	#speed处理部分
        spd_max = []
        spd_min = []
        spd_mean = []
	
        temp = []
        time = []

        for i in range( 0 , interval[0]-1 ):
		#print(i)
            if data.TimeStamp[i+1] != data.TimeStamp[i]:
                time = data.TimeStamp[i+1] - data.TimeStamp[i]
                x_offset = data.Xord[i+1] - data.Xord[i]
                y_offset = data.Yord[i+1] - data.Yord[i]
                s = x_offset*x_offset + y_offset*y_offset
                s = math.sqrt(s)
                v = s/time
                temp.append(v)
        if(len(temp)==0):
            temp=np.array([0])
        spd_max.append(max(temp))
        spd_min.append(min(temp))
        spd_mean.append(sum(temp)/len(temp))

	
	
        for i in range( 0 , len(interval)-1 ):
            temp = []

            for j in range( interval[i]+1 , interval[i+1]-1 ):
			#print(j)
			#print(data.Xord[j])
			
                if data.TimeStamp[j+1] != data.TimeStamp[j]:
                    time = data.TimeStamp[j+1] - data.TimeStamp[j]
                    x_offset = data.Xord[j+1] - data.Xord[j]
                    y_offset = data.Yord[j+1] - data.Yord[j]
                    s = x_offset*x_offset + y_offset*y_offset
                    s = math.sqrt(s)
                    v = s/time

                    temp.append(v)
            if(len(temp)==0):
                flag=0
                temp=np.array([0])
		#!!!!!!!!!!!!!!
            spd_max.append(max(temp))
            spd_min.append(min(temp))
            spd_mean.append(sum(temp)/len(temp))

	
	
	#长数据处理部分,首先获取间隔坐标
        start = []
        end = []
        for i in range(len(data.ACCx)):
        #print(i)
            if data.ACCx[i] == -6:
                start.append(i)
		
            if data.ACCx[i] == -2:
                end.append(i)

	
	#ACCx处理部分
        accx_max =[]
        accx_min =[]
        accx_mean =[]
	
        for i in range(len(start)):
            temp = []
            temp = data.ACCx[ start[i]+1:end[i] ]
            #print(temp)
            if(len(temp)==0):
                temp=np.array([0])
            accx_max.append(temp.max())
            accx_min.append(temp.min())
            accx_mean.append(temp.mean())
	
	
	#ACCy处理部分
        accy_max =[]
        accy_min =[]
        accy_mean =[]
	
        for i in range(len(start)):
            temp = []
            temp = data.ACCy[ start[i]+1:end[i] ]
            #print(temp)
            if(len(temp)==0):
                temp=np.array([0])
            accy_max.append(temp.max())
            accy_min.append(temp.min())
            accy_mean.append(temp.mean())
	
	#ACCz处理部分
        accz_max =[]
        accz_min =[]
        accz_mean =[]
	
        for i in range(len(start)):
            temp = []
            temp = data.ACCz[ start[i]+1:end[i] ]
            #print(temp)
            if(temp.size==0):
                temp=np.array([0])
            accz_max.append(temp.max())
            accz_min.append(temp.min())
            accz_mean.append(temp.mean())

	
        #DIRx处理部分
        DIRx_max =[]
        DIRx_min =[]
        DIRx_mean =[]
	
        for i in range(len(start)):
            temp = []
            temp = data.DIRx[ start[i]+1:end[i] ]
            #print(temp)
            if(len(temp)==0):
                temp=np.array([0])
            DIRx_max.append(temp.max())
            DIRx_min.append(temp.min())
            DIRx_mean.append(temp.mean())
	
	#DIRy处理部分
        DIRy_max =[]
        DIRy_min =[]
        DIRy_mean =[]
	
        for i in range(len(start)):
            temp = []
            temp = data.DIRy[ start[i]+1:end[i] ]
            #print(temp)
            if(len(temp)==0):
                temp=np.array([0])
            DIRy_max.append(temp.max())
            DIRy_min.append(temp.min())
            DIRy_mean.append(temp.mean())
	
	#DIRz处理部分
        DIRz_max =[]
        DIRz_min =[]
        DIRz_mean =[]
	
        for i in range(len(start)):
            temp = []
            temp = data.DIRz[ start[i]+1:end[i] ]
            #print(temp)
            if(len(temp)==0):
                temp=np.array([0])
            DIRz_max.append(temp.max())
            DIRz_min.append(temp.min())
            DIRz_mean.append(temp.mean())
	#数据整合
        temp = []
        temp.append(userid)#用户名
	#print('userid is ',userid)
	
        for i in range(len(interval)):
            temp.append( pre_max[i] )
            temp.append( pre_min[i] ) 
            temp.append( pre_mean[i] )
			
            temp.append( size_max[i] )
            temp.append( size_min[i] ) 
            temp.append( size_mean[i] )
		
            temp.append( dis_max[i] )
            temp.append( dis_min[i] )
            temp.append( dis_mean[i] )
                    
            temp.append( gap[i] )
		
            temp.append( spd_max[i] )
            temp.append( spd_min[i] )
            temp.append( spd_mean[i] )
		
            temp.append( accx_max[i] )
            temp.append( accx_min[i] )
            temp.append( accx_mean[i] )
		
            temp.append( accy_max[i] )
            temp.append( accy_min[i] )
            temp.append( accy_mean[i] )
		
            temp.append( accz_max[i] )
            temp.append( accz_min[i] )
            temp.append( accz_mean[i] )
		
            temp.append( DIRx_max[i] )
            temp.append( DIRx_min[i] )
            temp.append( DIRx_mean[i] )
		
            temp.append( DIRy_max[i] )
            temp.append( DIRy_min[i] )
            temp.append( DIRy_mean[i] )
		
            temp.append( DIRz_max[i] )
            temp.append( DIRz_min[i] )
            temp.append( DIRz_mean[i] )
    if(flag==0):
        
        return temp,0
    else:
        return temp,len(interval)

def data_transision(excel_root_pathname = './excel/',csv_root_pathname = './csv/',single_root_pathname='./single/'):	
    mkdir(csv_root_pathname)
    #分别写入到CSV
    xlsx_to_csv_pd(excel_root_pathname , csv_root_pathname )
    #csv头标签
    ori_colnames=[  'Premax', 'Premin', 'Premean',
            'Sizemax', 'Sizemin', 'Sizemean',
		    'dismax', 'dismin', 'dismean',
		    'Gaptime', 
		    'spdmax', 'spdmin', 'spdmean', 
		    'Accxmax', 'Accxmin', 'Accxmean', 
		    'Accymax', 'Accymin','Accymean', 
		    'Acczmax', 'Acczmin', 'Acczmean', 
		    'Dirxmax', 'Dirxmin','Dirxmean', 
		    'Dirymax', 'Dirymin', 'Dirymean', 
		    'Dirzmax', 'Dirzmin','Dirzmean']

    colnames = []
    mkdir(single_root_pathname)
    df_all=[]
    df_all=pd.DataFrame(df_all)
    #从csv中取得数据
    for (dirpath,dirnames,filenames) in os.walk(csv_root_pathname):
        final_matrix = []
        for f in filenames: 
            userid=f.split('-')[0]
            #遍历文件夹，获取数据
            linenum = 0
            [ temp , linenum ] = process_csv(dirpath + '/'+f ,userid)
            #初始化列名
            if(linenum==0):
                continue
            colnames = []
            colnames.append('tag')
            for i in range(linenum):
                for j in range(len(ori_colnames)):
                    temp_str =  ori_colnames[j] + "_" + str(i+1)
                    colnames.append(temp_str)
                #print(dirpath + '/'+f)
            final_matrix.append(temp)
        #写入数据
    df=pd.DataFrame(final_matrix,columns=colnames)
    mkdir(single_root_pathname+'/'+userid+'/')
    df.to_csv(single_root_pathname+'/'+userid+'/'+userid+'.csv',index=False,sep=',')

def get_train_data(person,person_fileaddr,csvdatabase,traindata_fileaddr,fakeuser_percent,fakeuser_pernum,meanpath,varpath):
    scaler=StandardScaler()
    truedata=pd.read_csv(person_fileaddr+str(person)+'.csv')#真的数据
    features=features3#特征
    trueuser_data=truedata[features]
    scaler.fit(trueuser_data.values)
    #fakeuser_num=len([lists for lists in os.listdir(csvdatabase) if os.path.isdir(os.path.join(csvdatabase, lists))])
    for dirpath,dirnames,filenames in os.walk(csvdatabase):
        if(len(dirnames)!=0):
            fakeuser=set(dirnames)-set([person])
    fakeuser_num=int(fakeuser_percent*len(fakeuser))#假用户数目
    fakedata=[]
    if(fakeuser_num>0):#如果需要假用户
        choosed_fakeuser=random.sample(fakeuser,fakeuser_num)
        #print(choosed_fakeuser)
        for fakeuser in choosed_fakeuser:
            df=pd.read_csv(csvdatabase+fakeuser+'/'+fakeuser+'.csv')
            choose_rows=np.random.permutation(df.shape[0])[0:fakeuser_pernum]
            choosed_data=df.iloc[choose_rows]
            if(len(fakedata)==0):              
                fakedata=pd.DataFrame(choosed_data)
            else:
                fakedata=pd.concat([fakedata,choosed_data])
    traindata=pd.concat([truedata,fakedata])
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
def train_model(person,train_filename,modelpath,meanpath,varpath):
    features=features3#按键行为识别是1/3特征，如果只有面积，是3号特征
    mkdir(modelpath)
    
    df=pd.read_csv(train_filename,sep=',')
    y=np.zeros((1,df.shape[0]))#标签
    X_train=np.zeros((df.shape[0],len(features)))
    X_train[:,:]=df[features].values
    X_train=X_train.astype(np.float32)
    for row in range(0,df.shape[0]):
        if(int(df.tag[row])==person):
            y[0][row]=1
        else:
            y[0][row]=0
    y_train=y[0].astype(np.int32)
    mean=np.load(meanpath+str(person)+'/'+str(person)+'.npy')
    var=np.load(varpath+str(person)+'/'+str(person)+'.npy')
    X_train=(X_train-mean)/np.sqrt(var)
    clf=SVC(gamma='auto',probability=True)
    clf.fit(X_train,y_train)
    mkdir(modelpath+str(person))
    joblib.dump(clf, modelpath+'/'+str(person)+'/'+str(person)+'.model')

def mytrain(username):  
    person=int(username)
    #basedir是存放着要训练的用户的数据的文件夹
    #1. dstdir是要把用户数据放到哪个大文件夹里面，会自动创建子文件夹，注意第2个参数是这个用户的excel数据的文件夹，文件夹里面就是用户数据没有子文件夹
    #excel_to_csv(person,basedir='../excel/'+str(person)+'/',dstdir='./csv/')
    #2. 第一个参数是上面那个具体的文件夹，第2个参数是把csv转成小的暂时存放在哪里，第3个参数是CSV用户数据库，用户数据会存放进去【实验时，可以先把数据库里的一个用户删掉】
    #csvdatabase里面是每个用户数据是单独的一个表，用很多用户的信息，./sum/里面是每个样本一个表

    data_transision('./excel/'+str(person)+'/','./csv/','./csvdatabase/')
    #3. 第2个地址是用户数据库中这个用户的子文件夹，下一个参数是用户数据库，再下一个是这个用户生成的训练文件，后面是均值和方差存放在哪里
    mkdir('./traindata/')
    get_train_data(person,'./csvdatabase/'+str(person)+'/','./csvdatabase/','./traindata/'+str(person)+'_train.csv',0.5,4,'./mean/','./var/')
    #4. 训练
    train_model(person,'./traindata/'+str(person)+'_train.csv','./model/','./mean/','./var/')
