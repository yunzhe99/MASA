package com.zcw.payview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FirstActivity extends Activity {
	private Button record_user_button;
	private Button check_user_button;
	public TextView serverid;
	public static EditText serverip;
	public TextView serverpt;
	public static EditText serverport;
	public static EditText username;
	public TextView userid;
	private static int key_board_mode=0;//1表示用户录入模式，2表示用户验证模式
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
        key_board_mode=0;//默认是录入模式
        
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
            	else if(serverip.getText().toString().equals(""))
            	{
            		Toast.makeText(FirstActivity.this, "服务器ip不能为空！",Toast.LENGTH_SHORT).show();
            	}
            	else if(serverport.getText().toString().equals(""))
            	{
            		Toast.makeText(FirstActivity.this, "服务器端口不能为空！",Toast.LENGTH_SHORT).show();
            	}
            	else {
            		key_board_mode=1;//录入数据模式，将会训练
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
            	else if(serverip.getText().toString().equals(""))
            	{
            		Toast.makeText(FirstActivity.this, "服务器ip不能为空！",Toast.LENGTH_SHORT).show();
            	}
            	else if(serverport.getText().toString().equals(""))
            	{
            		Toast.makeText(FirstActivity.this, "服务器端口不能为空！",Toast.LENGTH_SHORT).show();
            	}
            	else {
            		key_board_mode=2;//验证模式，将会验证
            		jump_to_keyboard();               
            	}
            }
        });   
    }
    //获取服务器ip地址
    public static String getserverip() {
    	return serverip.getText().toString();
    }
    //获取服务器端口号
    public static String getserverport() {
    	return serverport.getText().toString();
    }
    //获取用户名
    public static String getusername() {
    	return username.getText().toString();
    }
    //获取当前模式
    public static int get_keyboard_mode() {
    	return key_board_mode;
    }
    
    private void jump_to_keyboard() {
    	Intent intent = new Intent(FirstActivity.this, MainActivity.class);
        startActivity(intent);
    }
}