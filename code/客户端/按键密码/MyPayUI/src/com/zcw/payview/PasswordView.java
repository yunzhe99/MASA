package com.zcw.payview;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.zcw.payview.CreateXls;

import android.R.bool;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static java.lang.Thread.sleep;

//socket相关的包
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PasswordView extends RelativeLayout implements OnTouchListener,SensorEventListener{
	Context context;
	private String strPassword; // 当前输入的密码
	private String rightPassword;//第一次输入的密码，作为应该要输入的密码
	private TextView[] tvList; // 用数组保存6个TextView，为什么用数组？
	private LinearLayout ll_pwd; // 因为就6个输入框不会变了
	private GridView gridView; // 用GrideView布局键盘，其实并不是真正的键盘，只是模拟键盘的功能
	private ArrayList<Map<String, String>> valueList; // 有人可能有疑问，为何这里不用数组了？
														// 因为要用Adapter中适配，用数组不能往adapter中填充	
	private RelativeLayout rl_bottom;
	private int currentIndex = -1; // 用于记录当前输入密码格位置

	private PopupWindow popupWindow;

	private TextView input_state;
	private TextView inputnums;
    private TextView record_time;
    private Button input_begin;//开始记录
    private Button input_clear;//按错了清空记录
    private Button send_button;//什么时候开始发送
    private String excelPath;//文件存储路径
    String username = FirstActivity.getusername();//用户id
    private long touchTime;
    private long gapTime;
    private int firsttime=0;
    
    private int key_board_mode=FirstActivity.get_keyboard_mode();//输入模式，1是录入模式，2是验证模式
    private int recordtime=1;//录入了几次了，在录入信息模式下可以显示出来
    
    private SensorManager sensorManager;//惯性传感器
    private int processState=0;//是否有传感器数据，0表示没有
	//10ms延迟，100Hz采样率，因为这个速度要求太快了，10ms比较稳妥
    public static final int SENSOR_RATE_FASTEST=10;
    
    
    private Workbook wb;
    private WritableWorkbook wbook;//需要导入jxl工程或者包
    private WritableSheet sh;
    private Sheet sheet;
    CreateXls data_XLS=new CreateXls();//需要导入工程或者jxl包
    
    ArrayList <Float> Xord=new ArrayList<Float>();
    ArrayList <Float> Yord=new ArrayList<Float>();
    ArrayList <Float> Pressure=new ArrayList<Float>();
    ArrayList <Float> Presize=new ArrayList<Float>();
    ArrayList <Long> Touchtime=new ArrayList<Long>();
    ArrayList <Long> Gaptime=new ArrayList<Long>();
    
    Timer timer=null;
    private int times=0;
    ArrayList <Integer> SeqList=new ArrayList<Integer>();//序号
    ArrayList <Float> AccList=new ArrayList<Float>();//加速度
    ArrayList <Float> GyrList=new ArrayList<Float>();//陀螺仪
    private float AccData[]=new float[3];//陀螺仪
    private float GyrData[]=new float[3];//加速度
    
   
    private int touching=0;/*是否在触摸*/
    private int sampletimes=0;//采样次数
	private int send=0;//是否发送
    
	//socket相关
	private String server_address = FirstActivity.getserverip();//服务器地址
    private int server_port = Integer.valueOf(FirstActivity.getserverport());//服务器端口
    private Socket sk;//套接字
    private InputStream is;//服务器发过来的
    private OutputStream os;//发给服务器的
    private String Send_file_name = null;//发送的文件名
    private String Send_file_path = null;//发送的文件完整路径
    Map<String, byte[]> map = null;
    
    void clean() {
    	Xord.clear();
        Yord.clear();
        Pressure.clear();
        Presize.clear();
        Touchtime.clear();
    	Gaptime.clear(); 
    	SeqList.clear();
    	AccList.clear();
    	GyrList.clear();
    	firsttime=0;
    	onStop();	
    	times=0;
    }
    
    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        switch (event.getAction()) {
        //点击的开始位置
        case MotionEvent.ACTION_DOWN:
        	if(processState==0)//如果没有进入采样模式
        	{
        		break;
        	}  
        	touching=1;
        	firsttime++;
        	if(username.equals("")) {
        		firsttime=0;
        		if(!AccList.isEmpty())
        		{
        			clean();
        		}
        		Toast.makeText(context, "用户id不能为空",Toast.LENGTH_SHORT).show();
        	}
        	else {		
	            touchTime = System.currentTimeMillis();
	            times++;
	        	SeqList.add(times);
	        	AccList.add((float)-6);AccList.add((float)-6);AccList.add((float)-6);
	        	GyrList.add((float)-6);GyrList.add((float)-6);GyrList.add((float)-6);
	        	
	            if(firsttime==1)//第一次按键
	            	{          	
	            	times=0;
	            	//使用Timer采样IMU数据            	
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
	            gapTime=System.currentTimeMillis()-gapTime;
	            Gaptime.add(gapTime);
	            sampletimes++;
	            Xord.add(event.getX());
	        	Yord.add(event.getY());
	        	Pressure.add(event.getPressure());
	        	Presize.add(event.getSize());        	
	        	
        	}
            break;
			
        //触屏实时位置
        case MotionEvent.ACTION_MOVE:
        	sampletimes++;
        	Xord.add(event.getX());
        	Yord.add(event.getY());
        	Pressure.add(event.getPressure());
        	Presize.add(event.getSize());
            break;
			
        //离开屏幕的位置
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
        	touching=0;
            touchTime = System.currentTimeMillis()- touchTime;
            gapTime=System.currentTimeMillis();
            
            //完成记录
            Touchtime.add(touchTime);
            for(int i=0;i<sampletimes-1;i++)
            {
            	Touchtime.add((long)-1);
            	Gaptime.add((long)-1);           	
            }
              
            //写入分界线-2.0
            Xord.add((float) -2);
            Yord.add((float) -2);
            Pressure.add((float) -2);
            Presize.add((float) -2);
            Touchtime.add((long)-2);
        	Gaptime.add((long)-2);
        	times++;
        	SeqList.add(times);
        	AccList.add((float)-2);AccList.add((float)-2);AccList.add((float)-2);
        	GyrList.add((float)-2);GyrList.add((float)-2);GyrList.add((float)-2);
        	sampletimes=0;
			
        	/*//如果已经按下了6个键
            if(firsttime==6) { 
            	timer.cancel();
       	     	timer=null;
        		writetoxls();		
        		clean();
        		//inputnums.setText("密码是 "+strPassword);
        		input_begin.setEnabled(true);//可以再次开始采集
        		input_clear.setEnabled(false);//开始输入了，不能再开始了
            
        	}    
        	*/
        default:
            break;
        }
        return false;//继续响应
    }
	
	
	public PasswordView(Context context) {
		this(context, null);
	}

	public PasswordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		View view = View.inflate(context, R.layout.layout_popup_bottom, null);
		valueList = new ArrayList<Map<String, String>>();
		tvList = new TextView[6];
		rl_bottom = (RelativeLayout) view.findViewById(R.id.rl_bottom);/*装密码框和忘记密码的上面的布局*/
		ll_pwd = (LinearLayout) view.findViewById(R.id.ll_pwd);/*6位的密码框之一*/
		
		/*用来记录触屏信息的*/
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE); //必须要在一个context引用总调用
		input_state = (TextView) view.findViewById(R.id.input_state);
		inputnums=(TextView)view.findViewById(R.id.inputnums);
		record_time = (TextView) view.findViewById(R.id.record_time);
	    input_begin = (Button) view.findViewById(R.id.input_begin);
	    input_begin.setOnClickListener(new ButtonListener());
	    input_clear = (Button) view.findViewById(R.id.input_clear);
        input_clear.setOnClickListener(new ButtonListener());
        send_button = (Button) view.findViewById(R.id.send);
        send_button.setOnClickListener(new ButtonListener());   
        input_clear.setEnabled(false);//一开始没必要点击清空
		for (int i = 0; i < 6; i++) {
			TextView textView = new TextView(context);
			android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(
					0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			textView.setGravity(Gravity.CENTER);
			textView.setTransformationMethod(PasswordTransformationMethod.getInstance());
			textView.setTextSize(32);
			textView.setLayoutParams(layoutParams);
			ll_pwd.addView(textView);
			if (i != 5) {
				View view2 = new View(context);
				android.widget.LinearLayout.LayoutParams layoutParams1 = new android.widget.LinearLayout.LayoutParams(
						1,
						android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
						0);
				view2.setLayoutParams(layoutParams1);
				view2.setBackgroundColor(Color.parseColor("#999999"));
				ll_pwd.addView(view2);

			}
			tvList[i] = textView;
		}

		View contentView = LayoutInflater.from(context).inflate(
				R.layout.layout_popupdemo, null);// 定义后退弹出框
		gridView = (GridView) contentView.findViewById(R.id.gv_keybord);// 泡泡窗口的布局
		initData();

		addView(view); // 必须要，不然不显示控件
		popupWindow = new PopupWindow(contentView,
				ViewGroup.LayoutParams.MATCH_PARENT,// width

				ViewGroup.LayoutParams.WRAP_CONTENT);// higth
		popupWindow.setFocusable(false);
		popupWindow.setAnimationStyle(R.style.animation);
		
	}

	private class ButtonListener implements View.OnClickListener {
        public void onClick(View v) {
        	//原本是切换模式，现在用来清空
        	if(v.getId()==R.id.input_begin) {
        		/*mode=(mode+1)%4;*/
        		if(!AccList.isEmpty())//不能清空空的!
        		{
        			clean();
        		}
        		strPassword="";//密码清空
        		input_state.setText("正在输入...");
        		record_time.setText("");
	        	onResume();
	        	input_begin.setEnabled(false);//开始输入了，不能再开始了
	        	input_clear.setEnabled(true);
	        	gapTime=System.currentTimeMillis();
        	}
        	else if(v.getId()==R.id.input_clear)
        	{
        		if(!AccList.isEmpty())//不能清空空的!
        		{
        			clean();
        		}
        		strPassword="";//密码清空
        		input_state.setText("");
        		record_time.setText("");
        		inputnums.setText("");
        		currentIndex=-1;//置-1!!
        		for(int i=0;i<6;i++) {
					tvList[i].setText(""); 
				}
        		input_begin.setEnabled(true);//开始输入了，不能再开始了
        		input_clear.setEnabled(false);
        	}
        	/*else if(v.getId()==R.id.send)
        	{
        		input_state.setText("");
        		record_time.setText("");
        		inputnums.setText("");
        		if(recordtime==0 || firsttime!=0)//如果没有输入，不能发送 ;如果没有输入完，不能发送
        		{
        			clean();
        			Toast.makeText(context,"输入为空或未输入完！请重新输入",Toast.LENGTH_SHORT).show();
        		}
        		else 
        		{
        			input_begin.setEnabled(false);
            		input_clear.setEnabled(false);
        			if(key_board_mode==0)
        			{
        				Toast.makeText(context,"发送到服务器存储",Toast.LENGTH_SHORT).show();
        				send=1;
        			}
        			else
        			{
        				Toast.makeText(context,"发送到服务器验证",Toast.LENGTH_SHORT).show();
        			}
        		}
        	}*/
        }
    }
	//字节序转换
    public static byte[] intToByteArray(int a){
        return new byte[]{
                (byte)((a>>24)&0xFF),
                (byte)((a>>16)&0xFF),
                (byte)((a>>8)&0xFF),
                (byte)((a)&0xFF),
        };
    }
    public static int byteArrayToInt(byte[] b){
        return b[3]&0xFF|
                (b[2]&0xFF)<<8|
                (b[1]&0xFF)<<16|
                (b[0]&0xFF)<<24;
    }

    public static int byteArratToInt_8(byte[] b){
        return b[0]&0xFF;
    }
	
	//从socket中读取数据
    private Map<String, byte[]> read_data_from_socket(){
        Map<String, byte[]> map = new HashMap<String, byte[]>();
        try {
            is = sk.getInputStream();
            byte buffer[] = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                //String buf = new String(buffer, 0, len);
                break;
            }
            map.put("data",buffer);
            map.put("len", intToByteArray(len));
            return map;
        }catch (Exception e){
            e.printStackTrace();
            return map;
        }
    }
	
    private void network_init() throws Exception{
    	//input_state.setText("正在连接服务器....\n"+server_address+":"+server_port);
        //连接服务器端
        try {
            sk = new Socket(server_address, server_port);
            is = sk.getInputStream();
            os = sk.getOutputStream();
            //input_state.setText("连接成功");
            //Toast.makeText(context,"连接成功",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
        	input_state.setText("连接失败");
        	//Toast.makeText(context,"连接失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            sk.close();
            is.close();
            os.close();
        }
    }
    
	public void SendAllFile()
	{
		new Thread() {
            public void run() {
            	
                try {
                	network_init();
                    long file_size = 0;
                    int server_ret = 0;
                    //获得文件路径
                    File file_path = new File(Send_file_path);
                    File[] array = file_path.listFiles();   
                    int ifsend_username=0;
                    //遍历文件
                    for(int i=0;i<array.length;i++){   
                    	if(array[i].isFile())
                    	{
                    		File file=new File(array[i].getPath());                   		
                    		Send_file_name=array[i].getName();
                    		
                    		//获取文件长度
                    		if (file.exists() && file.isFile())
                    		{
                    			file_size = file.length();
                    			if(file_size==0)//如果是空文件，跳过
                    				continue;
                    		}                               
                            else
                                System.out.println("[Error] file cannot open");

                    		//发送文件夹内所有文件
                            try {

                                os = sk.getOutputStream();                   
                                //如果已经把用户信息传递了
                                if(ifsend_username==0)
                                {
                                	//训练还是预测？
                                	os.write((byte)0xFE);
                                	os.write(String.valueOf(key_board_mode).getBytes());
                                	os.flush(); 
                                	map = read_data_from_socket();
                                    server_ret = byteArratToInt_8(map.get("data"));
                                    
                                  //传输用户名
                                    os.write((byte)0xFC);
                                    os.write(username.getBytes());
                                    os.flush();         
                                    ifsend_username=1;
                                    map = read_data_from_socket();
                                    server_ret = byteArratToInt_8(map.get("data"));
                                    
                                	//传输密码
                                    os.write((byte)0xFD);
                                    os.write(rightPassword.getBytes());
                                    os.flush();         
                                    ifsend_username=1;
                                    map = read_data_from_socket();
                                    server_ret = byteArratToInt_8(map.get("data"));
                                }
                                else
                                {
                                	server_ret=0xE3;
                                }
                                
                                if(server_ret==0xE3) {
                                	//传输文件名
                                    os.write((byte)0xF2);
                                    os.write(Send_file_name.getBytes());
                                    os.flush();                        
                                    map = read_data_from_socket();
                                    server_ret = byteArratToInt_8(map.get("data"));
                                    
                                    if(server_ret==0xE3) {
                                    	os.write((byte)0xF3);
                                        // 传输文件大小
                                        os.write(String.valueOf(file_size).getBytes());
                                        //Log.d("File_size", String.valueOf(file_size));
                                        os.flush();
                                    }
                                   
                                }
                                                      
                              
                                map = read_data_from_socket();
                                server_ret = byteArratToInt_8(map.get("data"));

                                if(server_ret == 0xE3){ // 开始传输文件
                                    os.write((byte)0xF1);
                                    os.flush();

                                    FileInputStream fin = new FileInputStream(file);
                                    byte[] sendByte = new byte[1024];
                                    // dout.writeUTF(file.getName());
                                    int length = 0;
                                    while((length = fin.read(sendByte, 0, sendByte.length))>0){
                                        os.write(sendByte,0,length);
                                        os.flush();
                                    }

                                    map = read_data_from_socket();
                                    server_ret = byteArratToInt_8(map.get("data"));
          
                                    Send_file_path = null;
                                    Send_file_name = null;
                                                              
                                    fin.close();                                
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }              		
                    	}
                    }  
                    //input_state.setText("传送完毕");
                    
                    os.write((byte)0xF4);
                    os.flush();                    
                    map = read_data_from_socket();
                    server_ret = byteArratToInt_8(map.get("data"));
                    if(server_ret==0xE4)
                    {
                    	input_state.post(new Runnable() {

							@Override
							public void run() {
								input_state.setText("训练完毕");							
							}
                    		
                    	});
                    }
                    else if(server_ret==0xE5){
                    	input_state.post(new Runnable() {
							@Override
							public void run() {
								input_state.setText("你是真用户");							
							}
                    		
                    	});
                    }
                    else if(server_ret==0xE6){
                    	input_state.post(new Runnable() {

							@Override
							public void run() {
								input_state.setText("你是假用户");							
							}                 		
                    	});
                    }
                    
                    os.write((byte)0xFF);
                    os.flush();
                    sk.close();
                    is.close();
                    os.close();
                    
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
	}
	
	public interface OnPasswordInputFinish {
		void afterTrain();
		void validYes();
		void validNo();
	}
	//发送
	public void jump_on_send(final OnPasswordInputFinish pass) {
		send_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	input_state.setText("");
        		record_time.setText("");
        		inputnums.setText("");
        		if(recordtime==1 || firsttime!=0)//如果没有输入，不能发送 ;如果没有输入完，不能发送
        		{
        			clean();
        			Toast.makeText(context,"输入为空或未输入完！请重新输入",Toast.LENGTH_SHORT).show();
        		}
        		else 
        		{
        			input_begin.setEnabled(false);
            		input_clear.setEnabled(false);
        			/*if(key_board_mode==0)
        			{
        				Toast.makeText(context,"发送到服务器存储",Toast.LENGTH_SHORT).show();
        			}
        			else
        			{
        				Toast.makeText(context,"发送到服务器验证",Toast.LENGTH_SHORT).show();
        			}*/
        		}
        		SendAllFile();
            }
        });
		//在这里增加改变界面的代码，进行相应跳转
		//pass.onSend();
		input_state.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				if(input_state.getText().toString().equals("训练完毕")) {
					pass.afterTrain();
				}
				else if(input_state.getText().toString().equals("你是真用户")) {
					pass.validYes();
				}
				else if(input_state.getText().toString().equals("你是假用户")) {
					pass.validNo();
				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO 自动生成的方法存根
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO 自动生成的方法存根
				
			}
			
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		show();
	}

	public void show() {
		popupWindow.showAtLocation(rl_bottom, Gravity.BOTTOM, 0, 0); // 确定在界面中出现的位置
	}

	/**
	 * 加载模拟键盘上数据的代码
	 */
	private void initData() {
		/* 初始化按钮上应该显示的数字 */
		/*监听按键*/
		gridView.setOnTouchListener(this);
		
		for (int i = 1; i < 13; i++) {
			Map<String, String> map = new HashMap<String, String>();
			if (i < 10) {
				map.put("name", String.valueOf(i));
			} else if (i == 10) {
				map.put("name", "");
			} else if (i == 11) {
				map.put("name", String.valueOf(0));
			}else {
				map.put("name", "");
			}
			valueList.add(map);
		}
		gridView.setAdapter(adapter);
		
		
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < 11 && position != 9 && processState==1) { // 点击0~9按钮，且没有发生重复事件
					if (currentIndex >= -1 && currentIndex < 5) { // 判断输入位置————要小心数组越界
						currentIndex=currentIndex+1;
						tvList[currentIndex].setText(valueList.get(position).get("name"));
						strPassword += tvList[currentIndex].getText().toString().trim();
						inputnums.setText("当前第"+recordtime+"次，输入了 "+strPassword);
						//input_begin.setEnabled(false);//开始输入了，不能再开始了

						//如果已经按下了6个键
			            if(firsttime==6) { 
			            	timer.cancel();
			       	     	timer=null;
			       	     	if(recordtime==1)//如果是第一次，输入的密码作为正确密码，防止后面输入错误s 
			       	     	{
			       	     		rightPassword=strPassword;
			       	     		writetoxls();	
			       	     	}
			       	     	else//其它时候需要判断
			       	     	{
			       	     		if(rightPassword.equals(strPassword))//密码相等
			       	     		{
			       	     			writetoxls();
			       	     		}
			       	     		else//密码不一致
			       	     		{
			       	     			Toast.makeText(context, "密码不一致，重新输入",Toast.LENGTH_SHORT).show();
			       	     		}
			       	     			
			       	     	}
			        		clean();
			        		//inputnums.setText("密码是 "+strPassword);
			        		input_begin.setEnabled(true);//可以再次开始采集
			        		input_clear.setEnabled(false);//开始输入了，不能再开始了
			        		currentIndex=-1;//置-1!!
			        		for(int i=0;i<6;i++) {
								tvList[i].setText(""); 
			        		}
			            }					
					}
					
				} else {
					if (position == 11) { // 点击退格键，直接删完所有信息
						if(!AccList.isEmpty())//不能清空空的!
		        		{
		        			clean();
		        		}
		        		strPassword="";//密码清空
		        		input_state.setText("");
		        		record_time.setText("");
		        		inputnums.setText("");
		        		currentIndex=-1;//置-1!!
		        		for(int i=0;i<6;i++) {
							tvList[i].setText(""); 
						}
		        		input_begin.setEnabled(true);//开始输入了，不能再开始了
		        		input_clear.setEnabled(false);
					}
				}
			}
		});
		
	}


	/* 获取输入的密码 */
	public String getStrPassword() {
		return strPassword;
	}

	
	// GrideView的适配器
	BaseAdapter adapter = new BaseAdapter() {
		@Override
		public int getCount() {
			return valueList.size();
		}

		@Override
		public Object getItem(int position) {
			return valueList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = View.inflate(context, R.layout.item_gride, null);
				viewHolder = new ViewHolder();
				viewHolder.btnKey = (TextView) convertView
						.findViewById(R.id.btn_keys);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.btnKey.setText(valueList.get(position).get("name"));
			if (position == 9||position==11) {
				viewHolder.btnKey.setBackgroundDrawable(Utils.getStateListDrawable(context));
				viewHolder.btnKey.setEnabled(false);
			}
			if (position == 11) {
				viewHolder.btnKey.setBackgroundDrawable(Utils.getStateListDrawable(context));
			}

			return convertView;
		}
	};
	
	//创建文件夹、写入文件
	public void writetoxls() {
		CreateXls data_XLS=new CreateXls();
		Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
		
		int hour = c.get(Calendar.HOUR_OF_DAY); 
		int minute = c.get(Calendar.MINUTE); 
		int second = c.get(Calendar.SECOND); 
		
		excelPath=data_XLS.getExcelDir()+ File.separator+username+File.separator+
    			username + "-"+String.valueOf(hour)+"-"+String.valueOf(minute)+"-"+String.valueOf(second)+".xls";    
		if(recordtime==1)//如果是第一次，检查是否符合要求！
		{
			data_XLS.deletedir(username);
			data_XLS.makedir(username);//针对的用户的文件夹
		}
		data_XLS.excelCreate(new File(excelPath));
		WriteXls(Xord,Yord,Pressure,Presize,Touchtime,Gaptime,AccList,GyrList,SeqList);
		Send_file_path=data_XLS.getExcelDir()+ File.separator+username+File.separator;//文件所在路径
		
		recordtime=recordtime+1;//记录次数增加，有记录了	
		if(key_board_mode==0)//如果是在录入信息而不是验证信息
		{			
			record_time.setText("用户"+username+"已记录"+String.valueOf(recordtime-1)+"次");
		}
		input_state.setText("");
		
		clean();
    	//Toast.makeText(context,"记录完成",Toast.LENGTH_SHORT).show();
	}
	
	
	//存放控件
	public final class ViewHolder {
		public TextView btnKey;
	}

	 public void WriteXls(ArrayList<Float> Xord,ArrayList<Float> Yord,ArrayList<Float> Pressure,
			             ArrayList<Float> Presize,ArrayList<Long> Touchtime,ArrayList<Long> Gaptime,
			             ArrayList<Float> accdata,ArrayList<Float> gyrdata,ArrayList<Integer> SeqList)
		{
		try {
		wb=Workbook.getWorkbook(new File(excelPath));//获取原始文档
		sheet=wb.getSheet(0);//得到一个工作对象
		wbook=Workbook.createWorkbook(new File(excelPath),wb);//根据book创建一个操作对象
		sh=wbook.getSheet(0);//得到一个工作
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
		//压力
		for(int i=0;i<Pressure.size();i++)
		{
		    if  (Pressure!=null && Pressure.get(i)!=null)
		    {

		      Label label=new Label(2,i+1,String.valueOf(Pressure.get(i)));
		      sh.addCell(label);

		    }
		}
		//触摸面积
		for(int i=0;i<Presize.size();i++)
		{
		    if  (Presize!=null && Presize.get(i)!=null)
		    {

		      Label label=new Label(3,i+1,String.valueOf(Presize.get(i)));
		      sh.addCell(label);

		    }
		}
		//触摸时间
		for(int i=0;i<Touchtime.size();i++)
		{
		    if  (Touchtime!=null && Touchtime.get(i)!=null)
		    {

		      Label label=new Label(4,i+1,String.valueOf(Touchtime.get(i)));
		      sh.addCell(label);

		    }
		}
		//触摸间隔
		for(int i=0;i<Gaptime.size();i++)
		{
		    if  (Gaptime!=null && Gaptime.get(i)!=null)
		    {

		      Label label=new Label(5,i+1,String.valueOf(Gaptime.get(i)));
		      sh.addCell(label);

		    }
		}
		
		//附加的惯性传感器的数据
		//时间先后序号
		for(int i=0,seqrow=1;i<SeqList.size();i++)
		{
		    if  (SeqList!=null && SeqList.get(i)!=null)
		    {
		     Label label=new Label(6,seqrow,String.valueOf(SeqList.get(i)));
		     sh.addCell(label);
		     seqrow++;
		    }
		}
		
		//逐个写入加速度数据到文件中去！		
		for(int i=0,acc_Row=1;i<accdata.size();)
		{
		    if  (accdata!=null && accdata.get(i)!=null)
		    {
		        for(int j=7;j<10;j++)
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
		        for(int j=10;j<13;j++)
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

	 protected void onResume() {  
		 	if(processState==0) 
		 	{
		        sensorManager.registerListener(this,  
		                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),  
		                SensorManager.SENSOR_DELAY_FASTEST);
		        sensorManager.registerListener(this,  
		                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),  
		                SensorManager.SENSOR_DELAY_FASTEST);
		        processState=1;
	        }
	 }  
	  
	 protected void onStop() { 
		 	if(processState==1) {
			    processState=0;
		        sensorManager.unregisterListener(this); 
		 	}
	}  
	 
	 //做惯性传感器采样
	 public void dotimer() {
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
	 //停止惯性传感器采样
	 public void stoptimer() {
		 timer.cancel();
	     timer=null;
	 }
	 
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int type = event.sensor.getType();
    	float[] values = event.values; 
    	if(processState==1) {
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
}
