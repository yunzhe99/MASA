# MASA
此项目为2019年全国大学生信息安全竞赛获奖项目：一种基于机器学习的安卓多因素身份认证系统(Android multi-factor identity authentication system based on machine learning)

随着智能手机的广泛使用，手机身份认证的重要性日益重要。从图中可以看出，手机支付每年都在增多。因此手机身份认证越发重要。

![](http://www.liyunzhe.cn/usr/uploads/2020/03/440561948.png)

我们的系统旨在通过手机数据的采集来完成身份认证，下图为系统模块示意图。

![](http://www.liyunzhe.cn/usr/uploads/2020/03/2096364614.png)

## 数据采集

我们调用了安卓触屏接口和手机内置的加速度传感器进行数据采集。

加速度传感器示意图如下：

![](http://www.liyunzhe.cn/usr/uploads/2020/03/4230932734.png)

采集界面如下：

![](http://www.liyunzhe.cn/usr/uploads/2020/03/3461672350.png)

## 认证算法

我们的认证算法有两类：SVM和Autoencoder。

### SVM

SVM是最常用的分类算法之一了，这种情况下我们的问题就是一个二分类问题，在此不再赘述。这一类方法前人研究也很多，效果也很好，缺点是错误样本很难获取。

### Autoendocer

在这一类问题中，据我们所知，还没有使用自编码器的先例。

自编码器一般被用来实现异常侦测，这里也不意外。

![](http://www.liyunzhe.cn/usr/uploads/2020/03/2194074599.png)

大致就是，如果是异常样本，就不能很好的还原，从而实现了认证。但是存在的问题是对参数很敏感，需要用户手动调参。

## 实验结果

混淆矩阵如下，基本上都能检测出来。

![](http://www.liyunzhe.cn/usr/uploads/2020/03/3413406040.png)

自编码器的效果也和预期相符。

![](http://www.liyunzhe.cn/usr/uploads/2020/03/3468036059.png)

更多结果请参考文档部分。