package com.test.BTClient;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import com.test.BTClient.DeviceListActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
//import android.view.Menu;            //如使用菜单加入此三包
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Robot extends Activity implements Runnable { 
	
	private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
	
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
	
	private InputStream is;    //输入流，用来接收蓝牙数据
	//private TextView text0;    //提示栏解句柄
//    private EditText edit0;    //发送数据输入句柄
//    private TextView dis;       //接收数据显示句柄
//    private ScrollView sv;      //翻页句柄
    private String smsg = "";    //显示用数据缓存
    private String fmsg = "";    //保存用数据缓存
    boolean sym=false;
    
   
    

    public String filename=""; //用来保存存储的文件名
    BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
    boolean _discoveryFinished = false;    
    boolean bRun = true;
    boolean bThread = false;
	
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备
	
    private TextView tv_msg = null;
    private EditText ed_msg = null;
    private Button btn_send = null;
    private Button connect_net_btn = null;
    
    // private Button btn_login = null;
    private static final String HOST = "172.31.176.158";
    private static final int PORT = 30000;
    private Socket socket = null;
    
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String content = "";
    //接收线程发送过来信息，并用TextView显示
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CharSequence cs = content;
            tv_msg.setText(cs);
            //content=content.substring(0,1);
            sendMessagebyBT(content);
            //out.println("已将命令发送给小车");
        }
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);   //设置画面为主画面 main.xml
        
        //text0 = (TextView)findViewById(R.id.Text0);  //得到提示栏句柄
//        edit0 = (EditText)findViewById(R.id.Edit0);   //得到输入框句柄
//        sv = (ScrollView)findViewById(R.id.ScrollView01);  //得到翻页句柄
//        dis = (TextView) findViewById(R.id.in);      //得到数据显示句柄

       //如果打开本地蓝牙设备不成功，提示信息，结束程序
        tv_msg = (TextView) findViewById(R.id.receive);
        ed_msg = (EditText) findViewById(R.id.sendtext);
        btn_send = (Button) findViewById(R.id.sendbutton);
        connect_net_btn = (Button)findViewById(R.id.connect_net_btn);

        
        btn_send.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String msg = ed_msg.getText().toString();
                TextView num_tv = (TextView)findViewById(R.id.num);
                if(!num_tv.getText().toString().isEmpty())
                	{
	                	String num_s = num_tv.getText().toString();
	                	int num = Integer.parseInt(num_s);
	                	for(int i = 1; i <= num; i++)
	                		sendMessagebyBT(msg);
                	}
                sendMessagebyBT(msg);
                
                
                
            }
        });
        connect_net_btn.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try {
					TextView ip_tv = (TextView)findViewById(R.id.ip);
					TextView port_tv = (TextView)findViewById(R.id.port);
					String host = ip_tv.getText().toString();
					String port = port_tv.getText().toString();
					int port_int = 0;
					if(!host.equals("") && !port.equals(""))
						port_int = Integer.parseInt(port);
					else
					{
						Toast.makeText(getApplicationContext(), "请输入正确的IP和端口地址", Toast.LENGTH_SHORT).show();
						return;
					}

		            socket = new Socket(host, port_int);
		            in = new BufferedReader(new InputStreamReader(socket
		                    .getInputStream()));
		            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		        } catch (IOException ex) {
		            ex.printStackTrace();
		            ShowDialog("login exception" + ex.getMessage());
		        }
			}
        	
        });
        //启动线程，接收服务器发送过来的数据
        new Thread(Robot.this).start();
        Button upbutton=(Button)findViewById(R.id.upbutton);
        Button leftbutton=(Button)findViewById(R.id.leftbutton);
        Button rightbutton=(Button)findViewById(R.id.rightbutton);
        Button backbutton=(Button)findViewById(R.id.backbutton);
        SeekBar speed=(SeekBar)findViewById(R.id.seekBar1);
       
        OnSeekBarChangeListener osc=new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				
				// TODO Auto-generated method stub
				  TextView tv=(TextView)findViewById(R.id.textView1);
				  tv.setText((CharSequence)Integer.toString(arg0.getProgress()));
				  if(arg0.getProgress()!=102&&arg0.getProgress()!=108&&arg0.getProgress()!=114&&arg0.getProgress()!=98)
				  
				  sendMessagebyBT(String.valueOf((char)arg0.getProgress()));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				  TextView tv=(TextView)findViewById(R.id.textView1);
				  tv.setText((CharSequence)Integer.toString(arg0.getProgress()));
				  if(arg0.getProgress()!=102&&arg0.getProgress()!=108&&arg0.getProgress()!=114&&arg0.getProgress()!=98)
				  sendMessagebyBT(String.valueOf((char)arg0.getProgress()));
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				  TextView tv=(TextView)findViewById(R.id.textView1);
				  tv.setText((CharSequence)Integer.toString(arg0.getProgress()));
				  if(arg0.getProgress()!=102&&arg0.getProgress()!=108&&arg0.getProgress()!=114&&arg0.getProgress()!=98)
				  sendMessagebyBT(String.valueOf((char)arg0.getProgress()));
			}
		};
		 speed.setOnSeekBarChangeListener(osc);
		 OnTouchListener otl=new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				if(arg1.getAction()==MotionEvent.ACTION_DOWN){
					
					String s=null;
					if(arg0.getId()==R.id.upbutton)
						s="f";
				
					if(arg0.getId()==R.id.backbutton)
						s="b";
					if(arg0.getId()==R.id.leftbutton)
						s="l";
					if(arg0.getId()==R.id.rightbutton)
						s="r";
					sendMessagebyBT(s);
					
				}
				if(arg1.getAction()==MotionEvent.ACTION_UP){
					sendMessagebyBT("s");
				}
			
				return false;
			}
		};
    
        upbutton.setOnTouchListener(otl);
        leftbutton.setOnTouchListener(otl);
        backbutton.setOnTouchListener(otl);
        rightbutton.setOnTouchListener(otl);
        
        if (_bluetooth == null){
        	Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // 设置设备可以被搜索  
       new Thread(){
    	   public void run(){
    		   if(_bluetooth.isEnabled()==false){
        		_bluetooth.enable();
    		   }
    	   }   	   
       }.start();      
    }

    //发送按键响应
//    public void onSendButtonClicked(View v){
//    	int i=0;
//    	int n=0;
//    	try{
//    		OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
//    		byte[] bos = edit0.getText().toString().getBytes();
//    		for(i=0;i<bos.length;i++){
//    			if(bos[i]==0x0a)n++;
//    		}
//    		byte[] bos_new = new byte[bos.length+n];
//    		n=0;
//    		for(i=0;i<bos.length;i++){ //手机中换行为0a,将其改为0d 0a后再发送
//    			if(bos[i]==0x0a){
//    				bos_new[n]=0x0d;
//    				n++;
//    				bos_new[n]=0x0a;
//    			}else{
//    				bos_new[n]=bos[i];
//    			}
//    			n++;
//    		}
//    		
//    		os.write(bos_new);	
//    	}catch(IOException e){  		
//    	}  	
//    }
    
    //接收活动结果，响应startActivityForResult()
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode){
    	case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
    		// 响应返回结果
            if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                // MAC地址，由DeviceListActivity设置返回
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // 得到蓝牙设备句柄      
                _device = _bluetooth.getRemoteDevice(address);
 
                // 用服务号得到socket
                try{
                	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                }catch(IOException e){
                	Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                }
                //连接socket
            	Button btn = (Button) findViewById(R.id.Button03);
                try{
                	_socket.connect();
                	Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                	btn.setText("断开");
                }catch(IOException e){
                	try{
                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                		_socket.close();
                		_socket = null;
                	}catch(IOException ee){
                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                	}
                	
                	return;
                }
                
                //打开接收线程
                try{
            		is = _socket.getInputStream();   //得到蓝牙数据输入流
            		}catch(IOException e){
            			Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
            			return;
            		}
            		if(bThread==false){
            			ReadThread.start();
            			bThread=true;
            		}else{
            			bRun = true;
            		}
            }
    		break;
    	default:break;
    	}
    }
    
    //接收数据线程
    Thread ReadThread=new Thread(){
    	
    	public void run(){
    		int num = 0;
    		byte[] buffer = new byte[1024];
    		byte[] buffer_new = new byte[1024];
    		int i = 0;
    		int n = 0;
    		bRun = true;
    		//接收线程
    		while(true){
    			try{
    				while(is.available()==0){
    					while(bRun == false){}
    				}
    				while(true){
    					num = is.read(buffer);         //读入数据
    					n=0;
    					
    					String s0 = new String(buffer,0,num);
    					fmsg+=s0;    //保存收到数据
    					for(i=0;i<num;i++){
    						if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
    							buffer_new[n] = 0x0a;
    							i++;
    						}else{
    							buffer_new[n] = buffer[i];
    						}
    						n++;
    					}
    					String s = new String(buffer_new,0,n);
    					smsg=s;   //写入接收缓存
    					if(is.available()==0)break;  //短时间没有数据才跳出进行显示
    				}
    				//发送显示消息，进行显示刷新
    					handler.sendMessage(handler.obtainMessage());       	    		
    	    		}catch(IOException e){
    	    		}
    		}
    	}
    };
    
    //消息处理队列
    Handler handler= new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		TextView in=(TextView)findViewById(R.id.in);
    		in.setText(smsg);
    		//dis.setText(smsg);   //显示数据 
    		//sv.scrollTo(0,dis.getMeasuredHeight()); //跳至数据最后一页
    	}
    };
    
    //关闭程序掉用处理部分
    public void onDestroy(){
    	super.onDestroy();
    	if(_socket!=null)  //关闭连接socket
    	try{
    		_socket.close();
    	}catch(IOException e){}
    //	_bluetooth.disable();  //关闭蓝牙服务
    }
    
    //菜单处理部分
  /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {//建立菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }*/

  /*  @Override
    public boolean onOptionsItemSelected(MenuItem item) { //菜单响应函数
        switch (item.getItemId()) {
        case R.id.scan:
        	if(_bluetooth.isEnabled()==false){
        		Toast.makeText(this, "Open BT......", Toast.LENGTH_LONG).show();
        		return true;
        	}
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.quit:
            finish();
            return true;
        case R.id.clear:
        	smsg="";
        	ls.setText(smsg);
        	return true;
        case R.id.save:
        	Save();
        	return true;
        }
        return false;
    }*/
    
    //连接按键响应函数
    public void onConnectButtonClicked(View v){ 
    	if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
    		Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	
        //如未连接设备则打开DeviceListActivity进行设备搜索
    	Button btn = (Button) findViewById(R.id.Button03);
    	if(_socket==null){
    		Intent serverIntent = new Intent(this, DeviceListActivity.class); //跳转程序设置
    		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
    	}
    	else{
    		 //关闭连接socket
    	    try{
    	    	
    	    	is.close();
    	    	_socket.close();
    	    	_socket = null;
    	    	bRun = false;
    	    	btn.setText("连接");
    	    }catch(IOException e){}   
    	}
    	return;
    }
    
    //保存按键响应函数
//    public void onSaveButtonClicked(View v){
//    	Save();
//    }
//    
//    //清除按键响应函数
//    public void onClearButtonClicked(View v){
//    	smsg="";
//    	fmsg="";
//    	dis.setText(smsg);
//    	return;
//    }
//    
    //退出按键响应函数
    public void onQuitButtonClicked(View v){
    	finish();
    }
    
    //保存功能实现
//	private void Save() {
//		//显示对话框输入文件名
//		LayoutInflater factory = LayoutInflater.from(BTClient.this);  //图层模板生成器句柄
//		final View DialogView =  factory.inflate(R.layout.sname, null);  //用sname.xml模板生成视图模板
//		new AlertDialog.Builder(BTClient.this)
//								.setTitle("文件名")
//								.setView(DialogView)   //设置视图模板
//								.setPositiveButton("确定",
//								new DialogInterface.OnClickListener() //确定按键响应函数
//								{
//									public void onClick(DialogInterface dialog, int whichButton){
//										EditText text1 = (EditText)DialogView.findViewById(R.id.sname);  //得到文件名输入框句柄
//										filename = text1.getText().toString();  //得到文件名
//										
//										try{
//											if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //如果SD卡已准备好
//												
//												filename =filename+".txt";   //在文件名末尾加上.txt										
//												File sdCardDir = Environment.getExternalStorageDirectory();  //得到SD卡根目录
//												File BuildDir = new File(sdCardDir, "/data");   //打开data目录，如不存在则生成
//												if(BuildDir.exists()==false)BuildDir.mkdirs();
//												File saveFile =new File(BuildDir, filename);  //新建文件句柄，如已存在仍新建文档
//												FileOutputStream stream = new FileOutputStream(saveFile);  //打开文件输入流
//												stream.write(fmsg.getBytes());
//												stream.close();
//												Toast.makeText(BTClient.this, "存储成功！", Toast.LENGTH_SHORT).show();
//											}else{
//												Toast.makeText(BTClient.this, "没有存储卡！", Toast.LENGTH_LONG).show();
//											}
//										
//										}catch(IOException e){
//											return;
//										}
//										
//										
//										
//									}
//								})
//								.setNegativeButton("取消",   //取消按键响应函数,直接退出对话框不做任何处理 
//								new DialogInterface.OnClickListener() {
//									public void onClick(DialogInterface dialog, int which) { 
//									}
//								}).show();  //显示对话框
//	} 
//
 public void sendMessagebyBT(String s){
	 if(_socket != null){
		 if (_socket.isConnected()) {
			 int i=0;
		    	int n=0;
		    	try{
		    		OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
		    		byte[] bos = s.getBytes();
		    		for(i=0;i<bos.length;i++){
		    			if(bos[i]==0x0a)n++;
		    		}
		    		byte[] bos_new = new byte[bos.length+n];
		    		n=0;
		    		for(i=0;i<bos.length;i++){ //手机中换行为0a,将其改为0d 0a后再发送
		    			if(bos[i]==0x0a){
		    				bos_new[n]=0x0d;
		    				n++;
		    				bos_new[n]=0x0a;
		    			}else{
		    				bos_new[n]=bos[i];
		    			}
		    			n++;
		    		}
		    		
		    		os.write(bos_new);	
		    	}catch(IOException e){  		
		    	}  	
	     }
		 else
			 Toast.makeText(getApplicationContext(), "蓝牙未连接", Toast.LENGTH_SHORT).show();
	
	 }
	 else
		 Toast.makeText(getApplicationContext(), "蓝牙未连接", Toast.LENGTH_SHORT).show();
    
		
    	
	
    	
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		  try {
	            while (true) {
	            	if(socket != null){
		                if (!socket.isClosed()) {
		                    if (socket.isConnected()) {
		                        if (!socket.isInputShutdown()) {
		                            if ((content = in.readLine()) != null) {
		                               
		                                mHandler.sendMessage(mHandler.obtainMessage());
		                            } else {
	
		                            }
		                        }
		                    }
		                }
	            	}
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
    

    
public void ShowDialog(String msg) {
    new AlertDialog.Builder(this).setTitle("notification").setMessage(msg)
            .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();
}
}