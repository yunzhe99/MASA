package yy.lockdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import yy.lockdemo.CreateXls;
import yy.lockdemo.MainActivity;
import yy.lockdemo.FirstActivity;

public class LockView extends View implements SensorEventListener{
    Context context;
    private keys[] keys;
    private String[] keyValues; //按键参数
    private String pass; //密码集合
    private String right_password="1236987";
    public static int setpassword=0;//是否设置完密码
    private List<Point> trail; //已选中的点序列
    private Point touchPoint; // 当前触摸的点

    private int keyNumber = 3; //决定按钮数量
    private int confirmSize; //选择判断误差值

    private int defaultColor = Color.BLUE; //默认颜色
    private int defaultSelectColor = Color.WHITE; //选中颜色
    private int defaultErrorColor = Color.RED;     //错误颜色

    private int selectedColor;//当前选中颜色

    private int defaultWidth = 100; // 默认的宽度
    private int defaultHeight = 100;// 默认的高度
    private int defaultRadius = 100; //默认的半径

    private int minPassSize = 2; //最小的密码位数
    private ConfirmPassListener onConfirmPassListener;//密码确认监听
    private Paint mPaint;

    private Workbook wb;
    private WritableWorkbook wbook;//
    private WritableSheet sh;
    private Sheet sheet;
    CreateXls data_XLS=new CreateXls();
    private String excelPath;//文件存储路径

    String username = FirstActivity.getusername();//用户id
    private int key_board_mode=FirstActivity.get_keyboard_mode();//输入模式，0是录入模式，1是验证模式
    public static int recordtime=0;//录入了几次了，在录入信息模式下可以显示出来

    ArrayList <Float> Xord=new ArrayList<Float>();
    ArrayList <Float> Yord=new ArrayList<Float>();
    ArrayList <Float> Pressure=new ArrayList<Float>();
    ArrayList <Float> Presize=new ArrayList<Float>();
    ArrayList <Long> Touchtime=new ArrayList<Long>();//相对于first_timestamp的时间偏移
    ArrayList <Long> Gaptime=new ArrayList<Long>();
    ArrayList <Float> Distance=new ArrayList<Float>();//理想情况下是直线滑屏，计算与理想的距离
    private double now_distance=0;
    private int sampletimes=0;//屏幕事件采样次数，方便写入标识

    private long first_timestamp=0;//第一次按下的时间戳
    private long gap_timestamp=0;//此时划到按钮的时间戳

    private SensorManager sensorManager;//惯性传感器
    public static final int SENSOR_RATE_FASTEST=10;//10ms采样一次
    Timer timer=null;
    private int times=0;
    ArrayList <Integer> SeqList=new ArrayList<Integer>();//序号
    ArrayList <Float> AccList=new ArrayList<Float>();//加速度
    ArrayList <Float> GyrList=new ArrayList<Float>();//陀螺仪
    private float AccData[]=new float[3];//陀螺仪
    private float GyrData[]=new float[3];//加速度
    private int need_record_IMU=0;//没有开始采集的时候不采集为0，如果在采集就是1
    private int recode_state=0;//现在是不是正在记录

    private String Send_file_path = null;//发送的文件完整路径

    public LockView(Context context) {
        this(context, null);
    }

    public LockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context=context;
        init();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    //初始化
    private void init() {
        pass = "";
        recordtime=0;
        setpassword=0;
        trail = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(5);
        mPaint.setDither(true);
        selectedColor = defaultSelectColor;
        if(FirstActivity.get_keyboard_mode()==2)//验证模式
        {
            right_password=FirstActivity.get_right_password();
            setpassword=1;
        }
    }

    public String get_password(){
        return right_password;
    }

    public String get_Send_file_path()
    {
        return Send_file_path;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMod = MeasureSpec.getMode(widthMeasureSpec);
        int heightMod = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMod != MeasureSpec.EXACTLY) {
            if (widthMod == MeasureSpec.AT_MOST) {
                widthSize = Math.min(defaultWidth, widthSize);
            } else {
                widthSize = defaultWidth;
            }
        }
        if (heightMod != MeasureSpec.EXACTLY) {
            if (widthMod == MeasureSpec.AT_MOST) {
                heightSize = Math.min(defaultHeight, heightSize);
            } else {
                heightSize = defaultHeight;
            }
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //获取实际视图矩阵大小
        generateKeyView();
    }

    //设置按键数量
    public void setKeyNumber(int keyNumber) {
        this.keyNumber = keyNumber;
        generateKeyView();
        //重置密码
        keyValues = null;
        invalidate();
    }

    //设置按键参数
    public void setKeyValues(@NonNull String... keyValues) {
        if (keyValues.length != keys.length)
            throw new IllegalArgumentException("You should set " + keys.length + " values!");
        this.keyValues = keyValues;
    }

    //构建按钮基本信息
    private void generateKeyView() {
        keys = new keys[keyNumber * keyNumber];
        int KeyLayoutSize; //按键布局大小
        boolean isHorizontal; //布局均衡方向
        int rightBoundary; //布局右边界
        if (getWidth() > getHeight()) {
            isHorizontal = false;
            KeyLayoutSize = getHeight();
        } else {
            isHorizontal = true;
            KeyLayoutSize = getWidth();
        }
        int keySize = KeyLayoutSize / keyNumber;
        confirmSize = keySize/ 4; //触摸确定距离为  按钮大小的三分之一
        defaultRadius = keySize / 3;
        Point pointStart;
        if (isHorizontal) {
            pointStart = new Point((int) getTranslationX() + ((getWidth() - KeyLayoutSize) / 2), (int) getTranslationY());
            rightBoundary = (int) (getWidth() + getTranslationX());
        } else {
            pointStart = new Point((int) getTranslationX(), (int) getTranslationY() + (getHeight() - KeyLayoutSize) / 2);
            rightBoundary = (int) (getTranslationX() + ((getWidth() - KeyLayoutSize) / 2 + KeyLayoutSize));
        }
        for (int i = 0; i < keys.length; i++) {
            if (pointStart.x >= rightBoundary) {
                //重置计算点坐标
                pointStart.x = rightBoundary - KeyLayoutSize;
                pointStart.y += keySize;
            }
            Rect bound = new Rect(pointStart.x, pointStart.y, pointStart.x + keySize, pointStart.y + keySize);
            keys[i] = new keys(bound, defaultRadius);
            pointStart.x = pointStart.x + keySize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawKeys(canvas);
        drawTrail(canvas);
    }

    //绘制线条
    private void drawTrail(Canvas canvas) {
        mPaint.setColor(defaultColor);
        Point last = null;
        for (Point point : trail) {
            if (last == null) {
                last = point;
                continue;
            }
            canvas.drawLine(last.x, last.y, point.x, point.y, mPaint);
            last = point;
        }
        if (touchPoint != null && last != null) {
            canvas.drawLine(last.x, last.y, touchPoint.x, touchPoint.y, mPaint);
        }
    }

    //绘制按钮
    private void drawKeys(Canvas canvas) {
        int i=0;
        for (LockView.keys key : keys) {
            if (key.isSelect) {
                mPaint.setColor(selectedColor);
            } else {
                mPaint.setColor(defaultColor);
            }
            canvas.drawCircle(key.bound.centerX(), key.bound.centerY(), key.radius, mPaint);
            i++;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean in_area=check_in_area(event.getX(), event.getY());//是否在区域

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                //resetKeyStatus();//清空pass
                if(in_area && MainActivity.get_process_begin()==0)//还没有按下开始按钮
                {
                    MainActivity.showtext("请先按下开始按钮");
                }
                else if(in_area && MainActivity.get_process_begin()==1)
                {
                    recode_state=1;//开始了！
                    MainActivity.showtext("正在滑屏...");
                    touchPoint = new Point((int) event.getX(), (int) event.getY());
                    sampletimes=0;//屏幕事件采样次数，方便写入标识
                    if(setpassword==1)
                    {
                        first_timestamp=System.currentTimeMillis();;//第一次按下的时间戳
                        times=0;
                        if(timer==null) {timer=new Timer();}
                        timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run() {
                                times++;
                                SeqList.add(times);
                                AccList.add(AccData[0]);AccList.add(AccData[1]);AccList.add(AccData[2]);
                                GyrList.add(GyrData[0]);GyrList.add(GyrData[1]);GyrList.add(GyrData[2]);
                            }
                        },0,SENSOR_RATE_FASTEST);
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(recode_state==1 && setpassword==1)//如果是在记录而且已经设置了密码
                {
                    checkInKeys(event.getX(), event.getY());
                    touchPoint.x = (int) event.getX();
                    touchPoint.y = (int) event.getY();
                    sampletimes++;
                    Xord.add(event.getX());
                    Yord.add(event.getY());
                    Pressure.add(event.getPressure());
                    Presize.add(event.getSize());
                    Touchtime.add(System.currentTimeMillis()-first_timestamp);//相对时间偏移
                    if(recode_state==1)//因为可能到了！
                    {
                        int i = Integer.valueOf((pass.substring(pass.length() - 1))).intValue() - 1;//目前的按钮
                        char ch = right_password.charAt(pass.length());
                        int j = (int) ch - (int) '0';
                        // MainActivity.showtext(String.valueOf(pass.length()) + " i = " + String.valueOf(i)+"  j = " + String.valueOf(j));
                        int now_x1center=keys[i].bound.left+keys[i].bound.right;
                        int now_y1center=keys[i].bound.top+keys[i].bound.bottom;
                        int now_x2center=keys[j-1].bound.left+keys[j-1].bound.right;
                        int now_y2center=keys[j-1].bound.top+keys[j-1].bound.bottom;

                        double  x1center=(double)now_x1center/2;
                        double  y1center=(double)now_y1center/2;
                        double k=0;
                        double b=0;

                        if(now_x1center==now_x2center)//斜率不存在，竖着的，距离就是|xn - x1ccenter|
                        {
                            now_distance=(double)Math.abs(event.getX()-x1center);
                            //MainActivity.showtext("距离是 "+ String.valueOf(now_distance));
                        }
                        else if(now_y1center==now_y2center)//如果是水平的
                        {
                            now_distance=(double)Math.abs(event.getY()-y1center);
                            //MainActivity.showtext("距离是 "+ String.valueOf(now_distance));
                        }
                        else//点斜式
                        {
                            k=(((double)now_y1center/2)-((double)now_y2center/2))/(((double)now_x1center/2)-((double)now_x2center/2));
                            b=(double)now_y1center/2 - k*(double)now_x1center/2;// b=y1-kx1
                            now_distance=Math.abs(k*event.getX()-event.getY()+b)/Math.sqrt(k*k+1);
                        }
                        //MainActivity.showtext("距离是 "+ String.valueOf(now_distance));
                        Distance.add((float)now_distance);//距离
                    }
                }
                else if(recode_state==1)
                {
                    checkInKeys(event.getX(), event.getY());
                    touchPoint.x = (int) event.getX();
                    touchPoint.y = (int) event.getY();
                }

                break;

            case MotionEvent.ACTION_UP://touchPoint是在这个块里声明的
                if(recode_state==1 && setpassword==1 && FirstActivity.get_keyboard_mode()==1)//如果是在滑屏，没有画满数字，到了这儿
                {
                    touchPoint = null;
                    MainActivity.showtext("");
                    MainActivity.reset_process_begin();
                    if (onConfirmPassListener != null && trail.size() >= minPassSize) {
                        onConfirmPassListener.onConfirm(pass);
                    }
                    recode_state=0;
                    timer.cancel();
                    timer=null;
                    if(!right_password.equals(pass))//如果密码错误
                    {
                        clean();
                    }
                    else{
                        onStop();//密码正确，停下来就行，省电
                    }
                }
                else if(recode_state==1 && setpassword==0 && FirstActivity.get_keyboard_mode()==1)
                {
                    touchPoint = null;
                    right_password=pass;
                    if(onConfirmPassListener != null) {
                        onConfirmPassListener.onConfirm("密码设置完毕");
                    }
                    recode_state=0;
                }
                else if(recode_state==1 && FirstActivity.get_keyboard_mode()==2)//验证模式
                {
                    //MainActivity.showtext("正确密码为"+right_password);
                    touchPoint = null;
                    if(onConfirmPassListener != null) {
                        onConfirmPassListener.onConfirm("验证密码输入完毕");
                    }
                    recode_state=0;
                }
                break;

        }

        invalidate();
        return true;
    }

    //判断是否选中
    private void checkInKeys(float x, float y) {
        for (int index = 0; index < keys.length; index++) {
            if (x > keys[index].bound.left + confirmSize && x < keys[index].bound.right - confirmSize) {
                if (y > keys[index].bound.top + confirmSize && y < keys[index].bound.bottom - confirmSize) {
                    //if (x > keys[index].bound.left && x < keys[index].bound.right ) {
                    //   if (y > keys[index].bound.top && y < keys[index].bound.bottom ) {
                    if (keys[index].isSelect)//如果这个键已经被按下了
                        continue;
                    else {
                        keys[index].isSelect = true;
                        if (keyValues != null) {
                            pass += keyValues[index];//密码多一位
                            //如果是在写了而不是设置密码，写入下一个分界线
                            if(setpassword==1)
                            {
                                if (pass.length()==1)//如果是第一次按下，还没有记录
                                {
                                    gap_timestamp = System.currentTimeMillis();//按在一个按钮的时间戳
                                    Gaptime.add((long) 0);//第一次按下add(0)
                                }
                                else//如果不是第一次按下
                                {
                                    gap_timestamp = System.currentTimeMillis() - gap_timestamp;//按在一个按钮的时间戳
                                    for (int i = 0; i < sampletimes - 1; i++) {
                                        Gaptime.add((long) -1);
                                    }
                                    /*写入分界线-2.0*/
                                    Xord.add((float) -2);
                                    Yord.add((float) -2);
                                    Pressure.add((float) -2);
                                    Presize.add((float) -2);
                                    Touchtime.add((long) -2);
                                    Gaptime.add((long) -2);
                                    Distance.add((float)-2);
                                    times++;
                                    SeqList.add(times);
                                    AccList.add((float) -2);
                                    AccList.add((float) -2);
                                    AccList.add((float) -2);
                                    GyrList.add((float) -2);
                                    GyrList.add((float) -2);
                                    GyrList.add((float) -2);
                                    sampletimes = 0;
                                    Gaptime.add(gap_timestamp);
                                    gap_timestamp=System.currentTimeMillis();
                                }
                                if (pass.length()<right_password.length())//如果还没有输完密码,要有下一个
                                {
                                    times++;
                                    SeqList.add(times);
                                    AccList.add((float) -6);
                                    AccList.add((float) -6);
                                    AccList.add((float) -6);
                                    GyrList.add((float) -6);
                                    GyrList.add((float) -6);
                                    GyrList.add((float) -6);
                                }
                                else if(pass.length()==right_password.length())
                                {
                                    if(recode_state==1)//如果是在滑屏
                                    {
                                        MainActivity.showtext("");
                                        recode_state=0;
                                        timer.cancel();
                                        timer=null;
                                        if(!right_password.equals(pass))//如果密码错误
                                        {
                                            if(onConfirmPassListener != null && trail.size() >= minPassSize) {
                                                onConfirmPassListener.onConfirm("密码不一致");
                                            }
                                            clean();
                                        }
                                        else
                                        {
                                            if(onConfirmPassListener != null && trail.size() >= minPassSize) {
                                                onConfirmPassListener.onConfirm("可以保存");
                                            }
                                            onStop();//密码正确，停下来就行，省电
                                        }

                                    }
                                }
                            }
                            else
                                onStop();

                        }
                        //增加轨迹
                        trail.add(new Point(keys[index].bound.centerX(), keys[index].bound.centerY()));
                    }
                }
            }
        }
    }
    //是否在按钮区域中
    private boolean check_in_area(float x, float y)
    {
        for (int index = 0; index < keys.length; index++)
            if (x > keys[index].bound.left + confirmSize && x < keys[index].bound.right - confirmSize)
                if (y > keys[index].bound.top + confirmSize && y < keys[index].bound.bottom - confirmSize)
                    return true;
        return false;
    }

    //重置按键状态
    public void resetKeyStatus() {
        pass = "";
        selectedColor = defaultSelectColor;
        trail.clear();
        for (LockView.keys key : keys) {
            key.isSelect = false;
        }
    }

    public void error() {
        shakeAnimation();
        selectedColor = defaultErrorColor;
    }


    public void setMinPassSize(int minPassSize) {
        this.minPassSize = minPassSize;
    }


    public void setOnConfirmPassListener(ConfirmPassListener onConfirmPassListener) {
        this.onConfirmPassListener = onConfirmPassListener;
    }

    //按钮类
    private class keys {
        int radius;
        Rect bound; // 详细位置信息
        boolean isSelect = false;

        public keys(Rect bound, int radius) {
            this.bound = bound;
            this.radius = radius;
        }
    }

    //动画重复
    public void shakeAnimation() {
        Animation translateAnimation = new TranslateAnimation(0, 5, 0, 5);
        translateAnimation.setInterpolator(new CycleInterpolator(5));
        translateAnimation.setDuration(500);
        startAnimation(translateAnimation);
    }

    //密码监听接口
    public interface ConfirmPassListener {
        void onConfirm(String pass);
    }

    public int get_centerX(int num){
        return (keys[num].bound.left+keys[num].bound.right)/2;
    }

    public int get_centerY(int num){
        return (keys[num].bound.top+keys[num].bound.bottom)/2;
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO 自动生成的方法存根

    }
    //传感器采样
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO 自动生成的方法存根
        int type = event.sensor.getType();
        float[] values = event.values;
        if(need_record_IMU==1) //如果需要采样
        {
            switch(type) {
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    AccData[0]=values[0];AccData[1]=values[1];AccData[2]=values[2];

                    break;
                case Sensor.TYPE_GYROSCOPE:
                    GyrData[0]=values[0]; GyrData[1]=values[1]; GyrData[2]=values[2];
                    break;
                default:
                    break;
            }
        }

    }

    public void onResume() //开始启动IMU
    {
        if(need_record_IMU==0)
        {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                    SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_FASTEST);
            need_record_IMU=1;
        }
    }

    protected void onStop() //关闭IMU
    {
        if(need_record_IMU==1) {
            need_record_IMU=0;
            sensorManager.unregisterListener(this);
        }
    }


    public void writetoxls() {
        CreateXls data_XLS=new CreateXls();
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        excelPath=data_XLS.getExcelDir()+ File.separator+username+File.separator+
                username + "-"+String.valueOf(hour)+"-"+String.valueOf(minute)+"-"+String.valueOf(second)+".xls";
        recordtime=recordtime+1;//记录到文件次数增加
        if(recordtime==1)//每次前清空一次
        {
            data_XLS.deletedir(username);
            data_XLS.makedir(username);//针对数字的文件夹
        }
        data_XLS.excelCreate(new File(excelPath));
        Send_file_path=data_XLS.getExcelDir()+ File.separator+username+File.separator;//文件所在路径
        WriteXls(Xord,Yord,Distance,Pressure,Presize,Touchtime,Gaptime,AccList,GyrList,SeqList);
        if(key_board_mode==1)//如果是在录入信息而不是验证信息
        {
            MainActivity.showtext("用户"+username+"已记录"+String.valueOf(recordtime)+"次");
        }
        clean();//每次保存了清空一次
    }

    public void clean() {
        onStop();
        Xord.clear();
        Yord.clear();
        Pressure.clear();
        Presize.clear();
        Touchtime.clear();
        Gaptime.clear();
        Distance.clear();
        SeqList.clear();
        AccList.clear();
        GyrList.clear();
        first_timestamp=0;
        sampletimes=0;
    }
    public void WriteXls(ArrayList<Float> Xord,ArrayList<Float> Yord,ArrayList<Float> Distance,ArrayList<Float> Pressure,
                         ArrayList<Float> Presize,ArrayList<Long> Touchtime,ArrayList<Long> Gaptime,
                         ArrayList<Float> accdata,ArrayList<Float> gyrdata,ArrayList<Integer> SeqList)
    {
        try {
            wb=Workbook.getWorkbook(new File(excelPath));//获取原始文档
            sheet=wb.getSheet(0);//得到一个工作对象
            wbook=Workbook.createWorkbook(new File(excelPath),wb);//根据book创建一个操作对象
            sh=wbook.getSheet(0);//得到一个工作薄
            //逐个写入x坐标数据到文件中去！
            for(int i=0;i<Xord.size();i++)
            {
                if  (Xord!=null && Xord.get(i)!=null)
                {
                    Label label=new Label(0,i+1,String.valueOf(Xord.get(i)));
                    sh.addCell(label);
                }
            }
            //逐个写入y坐标数据到文件中去！
            for(int i=0;i<Yord.size();i++)
            {
                if  (Yord!=null && Yord.get(i)!=null)
                {

                    Label label=new Label(1,i+1,String.valueOf(Yord.get(i)));
                    sh.addCell(label);

                }
            }
            //距离
            for(int i=0;i<Distance.size();i++)
            {
                if  (Distance!=null && Distance.get(i)!=null)
                {

                    Label label=new Label(2,i+1,String.valueOf(Distance.get(i)));
                    sh.addCell(label);

                }
            }
            //压力
            for(int i=0;i<Pressure.size();i++)
            {
                if  (Pressure!=null && Pressure.get(i)!=null)
                {

                    Label label=new Label(3,i+1,String.valueOf(Pressure.get(i)));
                    sh.addCell(label);

                }
            }
            //触摸面积
            for(int i=0;i<Presize.size();i++)
            {
                if  (Presize!=null && Presize.get(i)!=null)
                {

                    Label label=new Label(4,i+1,String.valueOf(Presize.get(i)));
                    sh.addCell(label);

                }
            }
            //触摸时间
            for(int i=0;i<Touchtime.size();i++)
            {
                if  (Touchtime!=null && Touchtime.get(i)!=null)
                {

                    Label label=new Label(5,i+1,String.valueOf(Touchtime.get(i)));
                    sh.addCell(label);

                }
            }
            //触摸间隔
            for(int i=0;i<Gaptime.size();i++)
            {
                if  (Gaptime!=null && Gaptime.get(i)!=null)
                {

                    Label label=new Label(6,i+1,String.valueOf(Gaptime.get(i)));
                    sh.addCell(label);

                }
            }

            //附加的惯性传感器的数据
            //时间先后序号
            for(int i=0,seqrow=1;i<SeqList.size();i++)
            {
                if  (SeqList!=null && SeqList.get(i)!=null)
                {
                    Label label=new Label(7,seqrow,String.valueOf(SeqList.get(i)));
                    sh.addCell(label);
                    seqrow++;
                }
            }

            //逐个写入加速度数据到文件中去！
            for(int i=0,acc_Row=1;i<accdata.size();)
            {
                if  (accdata!=null && accdata.get(i)!=null)
                {
                    for(int j=8;j<11;j++)
                    {
                        Label label=new Label(j,acc_Row,String.valueOf(accdata.get(i)));
                        sh.addCell(label);
                        i++;
                    }
                    acc_Row++;
                }
            }
            //逐个写入陀螺数据到文件中去！
            for(int i=0,gyr_Row=1;i<gyrdata.size();)
            {
                if  (gyrdata!=null && gyrdata.get(i)!=null)
                {
                    for(int j=11;j<14;j++)
                    {
                        Label label=new Label(j,gyr_Row,String.valueOf(gyrdata.get(i)));
                        sh.addCell(label);
                        i++;
                    }
                    gyr_Row++;
                }
            }
            //写入数据
            wbook.write();
            wbook.close();
        } catch (Exception e2){
            System.out.print(e2.toString()+"--");
            System.out.print("--异常--");
        }
    }
}
