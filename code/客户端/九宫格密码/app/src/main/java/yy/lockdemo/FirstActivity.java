package yy.lockdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

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



public class FirstActivity extends Activity {
	private Button record_user_button;
	private Button check_user_button;
    public static EditText serverip;//服务器ip
    public static TextView serverid;//服务器ip
    public static EditText serverport;//服务器端口
    public static TextView serverpt;//服务器端口
	public static EditText username;//用户名
	public static TextView userid;//userid
    public static TextView show_thing;//显示的

    public static String right_password;
	private static int key_board_mode=0;//1表示用户录入模式，2表示用户验证模式


    //socket相关
    private String server_address;//服务器地址
    private int server_port;//服务器端口
    private Socket sk;//套接字
    private InputStream is;//服务器发过来的
    private OutputStream os;//发给服务器的
    Map<String, byte[]> map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstpage);
        serverid = (TextView) findViewById(R.id.serverid);
        serverip = (EditText) findViewById(R.id.serverip);
        serverpt = (TextView) findViewById(R.id.serverpt);
        serverport = (EditText) findViewById(R.id.serverport);
        userid = (TextView) findViewById(R.id.userid);
        username = (EditText) findViewById(R.id.username);
        show_thing=(TextView)findViewById(R.id.show_thing);
        key_board_mode=1;//默认是录入模式
        
        record_user_button = (Button) findViewById(R.id.record_user_button);
        check_user_button = (Button) findViewById(R.id.check_user_button);
        
        
        //如果是录入信息的话
        record_user_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示方式声明Intent，直接启动SecondActivity
            	if(username.getText().toString().equals(""))
            	{
            		Toast.makeText(FirstActivity.this, "用户id不能为空！",Toast.LENGTH_SHORT).show();
            	}
            	else {
            		key_board_mode=1;//录入模式
            		jump_to_keyboard();
            	}
            }
        });
        
        check_user_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示方式声明Intent，直接启动SecondActivity
            	if(username.getText().toString().equals(""))
            	{
            		Toast.makeText(FirstActivity.this, "用户id不能为空！",Toast.LENGTH_SHORT).show();
            	}
            	else {
            		key_board_mode=2;//验证模式
                    get_rightpassword();//从服务器端获取正确的密码
            	}
            }
        });

        show_thing.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0){
                if(show_thing.getText().toString().equals("用户登录")) {
                    //show_thing.setText("正确密码为"+right_password);
                   jump_to_keyboard();
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




    //获取服务器ip
    public static String getserverip() {
        return serverip.getText().toString();
    }

    //获取服务器端口
    public static String getserverport() {
        return serverport.getText().toString();
    }
    //获取用户名
    public static String getusername() {
    	return username.getText().toString();
    }
    
    public static int get_keyboard_mode() {
    	return key_board_mode;
    }

    private void jump_to_keyboard() {
    	Intent intent = new Intent(FirstActivity.this, MainActivity.class);
        startActivity(intent);
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
        server_address=serverip.getText().toString();
        server_port=Integer.valueOf(serverport.getText().toString());
        show_thing.post(new Runnable() {
            @Override
            public void run() {
                show_thing.setText("正在连接");
            }
        });
        try {
            sk = new Socket(server_address, server_port);
            is = sk.getInputStream();
            os = sk.getOutputStream();
            show_thing.post(new Runnable() {
                @Override
                public void run() {
                    show_thing.setText("连接成功");
                }
            });
        }
        catch (Exception e) {
            show_thing.post(new Runnable() {
                @Override
                public void run() {
                    show_thing.setText("连接失败");
                }
            });
            e.printStackTrace();
            sk.close();
            is.close();
            os.close();
        }
    }

    public void get_rightpassword()
    {
        new Thread() {
            public void run() {
                try {
                    network_init();
                    int server_ret = 0;

                    os = sk.getOutputStream();
                    //训练还是预测？
                    os.write((byte)0xFE);
                    os.write(String.valueOf(key_board_mode).getBytes());//验证模式，发送2
                    os.flush();
                    map = read_data_from_socket();
                    server_ret = byteArratToInt_8(map.get("data"));

                    //发送用户名
                    os.write((byte)0xFC);
                    os.write(getusername().getBytes());//用户名
                    os.flush();
                    map = read_data_from_socket();
                    server_ret = byteArratToInt_8(map.get("data"));
                    if(server_ret==0xE9)//用户不存在
                    {
                        show_thing.post(new Runnable() {
                            @Override
                            public void run() {
                                show_thing.setText("用户名不存在");
                            }
                        });
                       sk.close();
                        is.close();
                        os.close();
                    }
                    else{
                        //请求密码
                        os.write((byte)0xF5);
                        os.write(String.valueOf(username).getBytes());//验证模式，发送2
                        os.flush();
                        BufferedReader in = new BufferedReader(new InputStreamReader(is));
                        right_password=in.readLine();
                        os.write((byte)0xFF);
                        os.flush();
                        sk.close();
                        is.close();
                        os.close();
                        in.close();
                        show_thing.post(new Runnable() {
                            @Override
                            public void run() {
                                show_thing.setText("用户登录");
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static String get_right_password()
    {
        return right_password;
    }

}
