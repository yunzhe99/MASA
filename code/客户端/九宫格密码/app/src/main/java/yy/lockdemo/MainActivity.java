package yy.lockdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import yy.lockdemo.widget.LockView;

import android.text.Editable;
import android.text.TextWatcher;
import android.content.Intent;
//socket相关的包
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private LockView lockView;
    public static TextView show_text;
    public static Button input_begin;//开始记录
    //public static Button input_clear;//按错了清空记录
    public static Button input_send;//没有按错，可以发送

    private static int begin_process=0;//能不能开始滑屏

    //socket相关
    private String server_address = FirstActivity.getserverip();//服务器地址
    private int server_port = Integer.valueOf(FirstActivity.getserverport());//服务器端口
    private Socket sk;//套接字
    private InputStream is;//服务器发过来的
    private OutputStream os;//发给服务器的
    private String Send_file_name = null;//发送的文件名
    //private String Send_file_path = null;//发送的文件完整路径
    Map<String, byte[]> map = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lockView = (LockView) findViewById(R.id.lock_view);
        show_text=(TextView)findViewById(R.id.show_text);


        input_begin=(Button) findViewById(R.id.input_begin);
       // input_clear=(Button) findViewById(R.id.input_clear);
        input_send=(Button) findViewById(R.id.input_send);

       // input_clear.setEnabled(false);
        input_send.setEnabled(false);
        input_begin.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                show_text.setText("可以开始了");
                lockView.resetKeyStatus();
                lockView.invalidate();
                begin_process=1;
                input_begin.setEnabled(false);
                //input_clear.setEnabled(true);
                input_send.setEnabled(false);
                lockView.clean();//防止恶意用户不按套路出牌
                lockView.onResume();
            }
        });
        /*
        input_clear.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                lockView.resetKeyStatus();
                lockView.invalidate();
                begin_process=0;
                lockView.clean();
                input_begin.setEnabled(true);
                input_clear.setEnabled(false);
                input_save.setEnabled(false);
                if(lockView.setpassword==0)//如果用户按错了
                    lockView.first_reset_password();
                reset_process_begin();

            }
        });
         */
        input_send.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                lockView.resetKeyStatus();
                lockView.invalidate();
                input_begin.setEnabled(true);
               // input_clear.setEnabled(false);
                input_send.setEnabled(false);
                if(lockView.setpassword==1)
                {
                    SendAllFile();
                }
            }
        });

        lockView.setKeyNumber(3);
        lockView.setMinPassSize(1);
        lockView.setKeyValues(
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9"
        );
        lockView.setOnConfirmPassListener(new LockView.ConfirmPassListener() {
            @Override
            public void onConfirm(String pass) {
                if (pass.equals("可以保存")) {
                    //showtext("当前用户保存了"+);//密码正确才能保存
                    if(lockView.setpassword==1)
                        lockView.writetoxls();//密码正确，可以保存
                    input_send.setEnabled(true);
                }
                else if (pass.equals("密码设置完毕"))
                {
                    showtext("密码设置完毕");
                    lockView.setpassword=1;//密码设置完毕
                    input_send.setEnabled(false);
                }
                else if(pass.equals("验证密码输入完毕")){
                    //showtext("准备发送验证");
                    lockView.writetoxls();//密码正确，可以保存
                    input_send.setEnabled(true);
                }
                else{
                    showtext("密码不一致");
                    input_send.setEnabled(false);
                    lockView.error();
                }
                input_begin.setEnabled(true);
            }
        });

        show_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                if(show_text.getText().toString().equals("训练完毕")) {
                    afterTrain();
                }
                else if(show_text.getText().toString().equals("你是真用户")) {
                    validYes();
                }
                else if(show_text.getText().toString().equals("你是假用户")) {
                    validNo();
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

    public void afterTrain() {
        Intent intent = new Intent(MainActivity.this, MainActivity_afterTrain.class);
        startActivity(intent);
    }
    public void validYes() {
        Intent intent = new Intent(MainActivity.this, MainActivity_validYes.class);
        startActivity(intent);
    }
    public void validNo() {
        Intent intent = new Intent(MainActivity.this, MainActivity_validNo.class);
        startActivity(intent);
    }

    public static void showtext(String text){
        show_text.setText(text);
    }

    public static int get_process_begin(){
        return begin_process;
    }

    public static void reset_process_begin()//抬手之后可以设置这个
    {
        begin_process = 0;
        input_begin.setEnabled(true);
    }

    //-------------------socket相关函数------------------------
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
            show_text.post(new Runnable() {
                @Override
                public void run() {
                    show_text.setText("连接成功");
                }
            });
            //Toast.makeText(context,"连接成功",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            show_text.post(new Runnable() {
                @Override
                public void run() {
                    show_text.setText("连接"+server_address+":"+Integer.toString(server_port)+"失败");
                }
            });
            //Toast.makeText(this,"连接"+server_address+":"+server_port+"失败",Toast.LENGTH_SHORT).show();
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
                    File file_path = new File(lockView.get_Send_file_path());
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
                                    os.write(String.valueOf(FirstActivity.get_keyboard_mode()).getBytes());
                                    os.flush();
                                    map = read_data_from_socket();
                                    server_ret = byteArratToInt_8(map.get("data"));

                                    //传输用户名
                                    os.write((byte)0xFC);
                                    os.write(FirstActivity.getusername().getBytes());
                                    os.flush();
                                    ifsend_username=1;
                                    map = read_data_from_socket();
                                    server_ret = byteArratToInt_8(map.get("data"));

                                    //传输密码
                                    os.write((byte)0xFD);
                                    os.write(lockView.get_password().getBytes());
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

                                    Send_file_name = null;
                                    fin.close();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    //show_text.setText("传送完毕");
                    deletedir(lockView.get_Send_file_path());//传送完毕再删除
                    os.write((byte)0xF4);
                    os.flush();
                    map = read_data_from_socket();
                    server_ret = byteArratToInt_8(map.get("data"));
                    if(server_ret==0xE4)
                    {
                        show_text.post(new Runnable() {
                            @Override
                            public void run() {
                                show_text.setText("训练完毕");
                            }

                        });
                    }
                    else if(server_ret==0xE5){
                        show_text.post(new Runnable() {
                            @Override
                            public void run() {
                                show_text.setText("你是真用户");
                            }
                        });
                    }
                    else if(server_ret==0xE6){
                        show_text.post(new Runnable() {
                            @Override
                            public void run() {
                                show_text.setText("你是假用户");
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

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir
                        (new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        if(dir.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public void deletedir(String filedir) {
        File dir = new File(filedir);
        if (dir.exists()) {
            deleteDir(dir);
        } else {
            dir.mkdirs();
        }
    }
}
