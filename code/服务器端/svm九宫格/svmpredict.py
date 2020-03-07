import csv
import numpy as np
from matplotlib import pyplot as plt
import pandas as pd
import os
from sklearn.svm import SVC
from sklearn.preprocessing import StandardScaler
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
        
#文件路径直接读取
def single_data_transision(fileaddr):	
    df=pd.read_excel(fileaddr)
    df.to_csv('temp.csv')#临时文件
    data=pd.read_csv('temp.csv')
    userid=-1
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
        colnames.append('tag')
        linenum=len(interval)
        for i in range(linenum):
            for j in range(len(ori_colnames)):
                temp_str =  ori_colnames[j] + "_" + str(i+1)
                colnames.append(temp_str)
        temp=np.array(temp).reshape(1,-1)        
        df=pd.DataFrame(temp,columns=colnames)
        df.to_csv('wait_judge.csv',index=False,sep=',')
        return 1
    else:
        return 0


def judge_user(person,model,input_user_excel,meanpath,varpath,judge_thre=0.99): 
    t=single_data_transision(input_user_excel)
    if(t==0):
        print('Sorry,file wrong type, please enter again,that will be all right')
        return -1
    df=pd.read_csv('wait_judge.csv',sep=',')
    X=np.zeros((df.shape[0],len(features3)))
    X[:,:]=df[features3].values
    mean=np.load(meanpath+str(person)+'/'+str(person)+'.npy')
    var=np.load(varpath+str(person)+'/'+str(person)+'.npy')
    X=(X-mean)/np.sqrt(var)
    y_prob=model.predict_proba(X)
    y_pred=(y_prob[0,1]>judge_thre).astype(np.int32)#阈值
    if(y_pred>0):
        print('True user!')
    else:
        print('Eve!')
    return y_pred

def mypredict(username,excel_filename):
    person=int(username)
    clf= joblib.load('./model/'+str(person)+'/'+str(person)+'.model')
    y_pred=judge_user(person,clf,excel_filename,'./mean/','./var/',judge_thre=0.3)
