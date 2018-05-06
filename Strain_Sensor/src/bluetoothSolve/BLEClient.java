package bluetoothSolve;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;











import com.ps.strain_sensor.MainActivity;

import datasolve.ExcelSolve;
import datasolve.SearchinExcel;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;



public class BLEClient extends Activity{
	
	
	public static double lencheck1,lencheck2,lencheck3,lencheck4,lencheck5;
	public static double[] checklength = new double[5];
	public static ExcelSolve solve = new ExcelSolve();
	public static int countsolve = -1; 
	
	
	DecimalFormat df1 = new DecimalFormat(".00");
	
	public static boolean hear_data = false;
	private boolean runalways=false;
	private boolean availableflag = false;
	// constants
	private static final int REQUEST_ENABLE_BT = 2;
	private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SerialPortServiceClass_UUID
	private static final int numsInOneFrame = 5;// for PH sensor,final data in one frame,when  cmd==0x3A || 0x3B
	private static final int bytesInOneFrame = 11;// raw data in one frame 
	private static final int maxFrames = 400;
	
	// member variables
    private Activity uiActivity = null;
    private Handler uiHandler = null;
    private static  BluetoothAdapter mBluetoothAdapter; 
    private static  BluetoothDevice mBluetoothDevice;
    private List<BluetoothDevice> BLDeviceList = new ArrayList<BluetoothDevice>();
    private AlertDialog BLDialog=null;
    private static BluetoothSocket btSocket;
	public static boolean RunningFlag=false; //for the thread of communication1&2
	private static InputStream mmInStream;
	private static OutputStream mmOutStream;
	
    //final result for the object of the class
	public static int sizeOfResult=-1; // frame numbers in "result"
    public static double[][] result = new double[maxFrames][numsInOneFrame]; // transfer
    public static boolean DataReady = false; // the final data is ready or not	
	
	public static boolean startRec1 = false;
	
	// construct function
	public BLEClient (Activity newAct,Handler newHandler){
		uiActivity=newAct;	
		uiHandler=newHandler;
		
		initBLE();//get BluetoothAdapter and open Bluetooth.

		// bluetooth broacastReceiver
		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		uiActivity.registerReceiver(mBLReceiver, intent);
		
	}
	
	
	// initial BLE
	public void initBLE(){		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {  // Device does not support Bluetooth			
			new AlertDialog.Builder(uiActivity).setTitle("No BluetoothAdapter").show();
		}else{                            // Device supports Bluetooth					
			if (!mBluetoothAdapter.isEnabled()) {    // Bluetooth not opened				
				new AlertDialog.Builder(uiActivity)
						.setTitle("Open bluetooth first")
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,int which) { // go to open Bluetooth
										Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
										uiActivity.startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
									}
								}).show();				
		    } // if Bluetooth has opened, shows nothing!
	   }
	 
	}
	
	public void searchDevice(){
		if (mBluetoothAdapter.isEnabled()) {
			Message msg=Message.obtain();
			 msg.what=Constants.SEARCHING_DEVICE;
			 uiHandler.sendMessage(msg);
            
			if (mBluetoothAdapter.isDiscovering()) {  //start searching device
				mBluetoothAdapter.cancelDiscovery();
			}
			BLDeviceList.clear();// clear device list
			boolean flag=mBluetoothAdapter.startDiscovery();
			Log.i("PS", "�Ƿ�ʼ������"+String.valueOf(flag));
			
	    }else{                                 // Bluetooth not opened
	       new AlertDialog.Builder(uiActivity)
	                 .setTitle("Bluetooth is still closed!")
	                 .setMessage("Please open Bluetooth first and then press the button again.")
					 .setPositiveButton("Ok",
								new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,int which) { // go to open Bluetooth
										Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
										uiActivity.startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
									}
								}).show();
	    }
	}
	
	    BroadcastReceiver mBLReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			Log.i("PS", "�յ��㲥"+action);
			
			// device found
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.i("PS", device.getName()+"  "+device.getAddress() );
				if(device.getBondState() == BluetoothDevice.BOND_NONE){
					Log.i("PS", "BOND_NONE" );						
				}
				if(device.getBondState() == BluetoothDevice.BOND_BONDING){
					Log.i("PS", "BOND_BONDING" );	
				}
				if(device.getBondState() == BluetoothDevice.BOND_BONDED){
					Log.i("PS", "BOND_BONDED" );	
				}
				// add to device list
				BLDeviceList.add(device);

				// add to listView
				String[] stringList = getStringListFromDevice();

				
				// dialog build
				if (BLDialog != null)
					BLDialog.dismiss();

				BLDialog = new AlertDialog.Builder(uiActivity)
						.setTitle("��ѡ���豸��")					
						.setItems(stringList,
								new DialogInterface.OnClickListener() {							
									@Override
									public void onClick(DialogInterface dialog,int which) {
										Toast.makeText(uiActivity,
												BLDeviceList.get(which).getName(),
												Toast.LENGTH_SHORT).show();
										handleChosenDevice(which);
									}
									private void handleChosenDevice(int which) {
										BluetoothDevice mBLTdevice = BLDeviceList.get(which);
										mBluetoothDevice= mBLTdevice;
										try {
											
											if (mBLTdevice.getBondState() == BluetoothDevice.BOND_NONE) {
												Boolean returnValue = false;
												Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
												returnValue = (Boolean) createBondMethod.invoke(mBLTdevice);
												Log.i("PS", mBLTdevice.getName()+"  "+mBLTdevice.getAddress() );
											    Message msg=Message.obtain();
												msg.what=Constants.BONDING_DEVICE;
									           uiHandler.sendMessage(msg);
											   } else if (mBLTdevice.getBondState() == BluetoothDevice.BOND_BONDED) {
												   
												bluetoothConnect(mBLTdevice);
												
											   } else if (mBLTdevice.getBondState() == BluetoothDevice.BOND_BONDING) {
												 Message msg=Message.obtain();
												 msg.what=Constants.BONDING_DEVICE;
										         uiHandler.sendMessage(msg);
											 } 
										 } catch (Exception e) {
											e.printStackTrace();
										 }
									 }

								}).create();
                 BLDialog.show();

			}
			else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				 Message msg=Message.obtain();
				 msg.what=Constants.DISCOVERY_FINISHED;
		         uiHandler.sendMessage(msg);
			}                                     
			else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				Log.i("PS", "���״̬�ı�");
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(device== mBluetoothDevice){
					if (device.getBondState() == BluetoothDevice.BOND_NONE) {
					    Message msg=Message.obtain();
					    msg.what=Constants.CONNECT_FAIL;
			            uiHandler.sendMessage(msg);
					   } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
						   Log.i("PS", "��δ��Ա�����Գɹ���");   
						bluetoothConnect(device);
						
					   } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
						 Message msg=Message.obtain();
					 msg.what=Constants.BONDING_DEVICE;
				        uiHandler.sendMessage(msg);
					 } 			
				}
			 }			
			
		}
		
		private String[] getStringListFromDevice() {
			int len = BLDeviceList.size();
			String str[] = new String[len];

			for (int idx = 0; idx < len; idx++) {
				BluetoothDevice device = BLDeviceList.get(idx);

				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					str[idx] = "δ����豸��" + device.getName() + "\n"
							+ device.getAddress() + "\n";
				} else {
					str[idx] = "������豸��" + device.getName() + "\n"
							+ device.getAddress() + "\n";
				}

			}
			return str;
		}
		
	};
	
	// get btSocket
	private void bluetoothConnect(BluetoothDevice device) {
		
		UUID uuid = UUID.fromString(SPP_UUID);
		try {
			btSocket = device.createRfcommSocketToServiceRecord(uuid);
			
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("PS", "get socket failed!");
		}
		
		if(btSocket==null){
		    Message msg=Message.obtain();
			msg.what=Constants.CONNECT_FAIL;
	        uiHandler.sendMessage(msg);
		}else{
			Log.i("PS", device.getName()+" get socket successd!");
		}
		
		(new openSocketThread()).start();
		try{
			mmInStream = btSocket.getInputStream();
		    mmOutStream = btSocket.getOutputStream();
		    }catch (IOException connectException) {}
		
		runalways = true;
		hear_data = true;
		
	}
		
	
	private  class openSocketThread extends Thread {
		
		@Override
		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();
		    if(!btSocket.isConnected()){ 
			       try {   // Connect the device through the socket. This will block until it succeeds or throws an exception
			    	   btSocket.connect();
			    	   Log.i("PS", "has executed connectSocket");
			    	   
			       } catch (IOException connectException) {
			    	   Log.i("PS", "connectSocket IOException");  
					    Message msg=Message.obtain();
						msg.what=Constants.CONNECT_FAIL;
				        //ps uiHandler.sendMessage(msg); // Unable to connect; close the socket and get out
			           try {
			        	   btSocket.close();
			        	   Log.i("PS", "close Socket"); 
			          } catch (IOException closeException) { }
			           return;
			       }
		    }
	       
		     Message msg=Message.obtain();
			 
		    //ps  uiHandler.sendMessage(msg);
			 (new CommunicationThreadReady	()).start();
				Log.i("PS", "����һֱ�������ݵ��߳�");
				msg.what=Constants.SHOWCHART;
				uiHandler.sendMessage(msg);
				 Log.i("PS", "���ͳɹ�");    		    
		}
	
	}
	private  class CommunicationThreadReady extends Thread {
		//transfer
		private static final int framesToRead = 100;// can be adjusted!
		final static int RDSTATE_START = 0;
		final static int RDSTATE_DATA = 7;
		final static int RDSTATE_FIN = 8;		   
		int readState = RDSTATE_START;
		int framesHasRead = 0; //the number of frames we have read
		byte[] oneFrame=new byte[bytesInOneFrame] ; // header(2A) + PGA(01) + 7 remaining data(7*2bytes)
		int frameIndex = 0; //index in one frame
		   
		   private int toInt(byte b) {
				return (0xff & b);
			}	   
		   
		@Override
		public void run() {
			Log.i("PS", "��Ľ���run������");
			read_data();
		}
	
		//�����Ƕ�ȡ���ݵĲ���
		//1��ȡtransfer
		private void read_data(){
			DataReady = false;
			byte[] buffer=new byte[11]; 
			while (hear_data) {
				// buffer store for the stream
				int bytes=0; // bytes number returned from read()
				 // wait for data available, at most wait 3 seconds.
			 	   Log.i("PS","�ڶ�����");

				    		try {
					    	    for(int t=0;t<11;t++){
					    	    bytes=mmInStream.read(buffer, t, 1);
					    	   }}catch (IOException e) {
				            	Log.i("PS", "IOException when read Inputstream");
				            	Log.i("PS", e.toString());
				            	hear_data=false;
							    Message msg1=Message.obtain();
								msg1.what=Constants.READ_FAIL;
							    uiHandler.sendMessage(msg1);
				            }
					     // break;//�������������˲�����������inputstream   ������������
				
			      
				  // read every b in buff handle in frame
				  for (int idx = 0; idx < 11; idx++) {
						byte b = buffer[idx];
						// state machine						
						switch (readState) {
						case RDSTATE_START:
							if (b == (byte)0x2A) {
								readState=RDSTATE_DATA;
								frameIndex = 0;
								oneFrame[frameIndex]= b;
								frameIndex++;
							}
							break;
							
						case RDSTATE_DATA:
							oneFrame[frameIndex]= b;
							frameIndex++;
							if (frameIndex == bytesInOneFrame) { // "data" has already been read all.
								//readState=RDSTATE_FIN;
								handle_data();
								readState = RDSTATE_START;
								} 
							break;

						case RDSTATE_FIN:
                            //readState = RDSTATE_START;
                            Log.i("PS", "dierci");
							break;

						}// end of state machine

						if(!hear_data) break;
					}// end of read every b in buff
			}
		}
		
	private void handle_data(){
	    	if(frameIndex!=bytesInOneFrame){
	    		Log.i("PS","Error!");
	    		return;
	    	}else{
	    	   Log.i("PS","��"+String.valueOf(framesHasRead)+"�飺"+Converter.getHexString(oneFrame, 9));
	    	   int num1,num2;
	    	   double num;
	    	   result[framesHasRead][0]=toInt(oneFrame[0]);
	           num1 = toInt(oneFrame[1]);
	           num2 = toInt(oneFrame[2]);
	           num = num1 + num2 * 256;
	           Log.i("pss", String.valueOf(num));
	           num = (1000*(4200-num)/num-100)/100;
	           Log.i("pss", String.valueOf(num));
	           checklength[0] = num;
	           num1 = toInt(oneFrame[3]);
	           num2 = toInt(oneFrame[4]);
	           num = num1 + num2 * 256;
	           num = (4*num)/65536;
	           checklength[1] = num;
	           num1 = toInt(oneFrame[5]);
	           num2 = toInt(oneFrame[6]);
	           num = num1 + num2 * 256;
	           num = (4*num)/65536;
	           checklength[2] = num;
	           num1 = toInt(oneFrame[7]);
	           num2 = toInt(oneFrame[8]);
	           num = num1 + num2 * 256;
	           num = (1000*(4200-num)/num-100)/100;
	           checklength[3] = num;
	           num1 = toInt(oneFrame[9]);
	           num2 = toInt(oneFrame[10]);
	           num = num1 + num2 * 256;
	           num = (1000*(4200-num)/num-100)/100;
	           checklength[4] = num;
	           
	           
	           result[framesHasRead][0]=checklength[0];
	           result[framesHasRead][1]=checklength[1];
	           result[framesHasRead][2]=checklength[2];
	           result[framesHasRead][3]=checklength[3];
	           result[framesHasRead][4]=checklength[4];
	           Message msg=Message.obtain();
	           msg.what=Constants.SHOWLENGTH;
				 uiHandler.sendMessage(msg);
	           }		        	   
	           framesHasRead++;
	           if(framesHasRead==framesToRead)
	           {
	        	   countsolve++;
	        	   solve.writeToExcel();
	        	   framesHasRead = 0;
	        	   byte buffer = 0;
	        	   try {
	       			mmOutStream.write(buffer);
	       			mmOutStream.flush();
	       				} catch (IOException e) {
	       				e.printStackTrace();
	       			}
	           }
	    }
	}
}
