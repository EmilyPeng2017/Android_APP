package com.ps.strain_sensor;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import jxl.Sheet;
import jxl.Workbook;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import datasolve.ExcelSolve;
import datasolve.SearchinExcel;
import bluetoothSolve.BLEClient;
import bluetoothSolve.Constants;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity{
	//先把表格的数据存入数组   然后再跟数组里的数比较大小
	public static double[][] shuzu = new double[10][300];
	private File exFile;
	private String exPath;
	private ExcelSolve excelsolve = new ExcelSolve();    
	
	DecimalFormat df = new DecimalFormat(".00");
	
	public int isvisible = -1;
	ImageView sensor1,sensor2,sensor3,sensor4,sensor5;
	TextView text1,text2,text3,text4,text5,text11,text22,text33,text44,text55,cm1,cm2,cm3,cm4,cm5;
	//要新建表格transfer
  	
  	private String excelPath;
	private File excelFile;
	private ExcelSolve solve;
	
	//Bluetooth
    public BLEClient mBLEClient=null;
    private TextView BLEState;
    private Button search;
    
    
  //作图需要的    
    private Timer timer = new Timer();
    private GraphicalView chart;
    private TimerTask task;
    private double addY1 = -1;
    private double addY2= -1;
    private double addY3 = -1;
    private double addY4 = -1;
    private double addY5 = -1;
    private int addX = -1;
   // private long addX;
	/**曲线数量*/
    private final int SERIES_NR=5;
    //private TimeSeries series1;//多么痛的领悟
    private XYSeries series1;
    private XYMultipleSeriesDataset dataset1;
    private XYSeries series2;
    private XYSeries series3;
    private XYSeries series4;
    private XYSeries series5;
    private Handler charthandler;
    
    /**时间数据*/
   // Date[] xcache = new Date[2000];
    int[] xcache1 = new int[2000];
	/**数据*/
    double[] ycache1 = new double[2000]; 
    int[] xcache2 = new int[2000];
	/**数据*/
    double[] ycache2 = new double[2000];
    int[] xcache3 = new int[2000];
	/**数据*/
    double[] ycache3 = new double[2000];
    int[] xcache4 = new int[2000];
	/**数据*/
    double[] ycache4 = new double[2000];
    int[] xcache5 = new int[2000];
	/**数据*/
    double[] ycache5 = new double[2000];
    private int waitin;
    private int waitin1;
    
    
    
  //Handler for main thread
		private class MyHandler extends Handler {
	        private final WeakReference<MainActivity> mActivity;
	        
	        public MyHandler(MainActivity activity) {
	            mActivity = new WeakReference<MainActivity>(activity);
	        }
	        
	        @Override
	        public void handleMessage(Message msg) {
	        	MainActivity activity = mActivity.get();
	            if (activity != null) {
	                 switch(msg.what){
	                   case Constants.SEARCHING_DEVICE :
	                	   BLEState.setText("搜索设备中。。");
	                	   break;
	                   case Constants.BONDING_DEVICE :
	                	   BLEState.setText("与设备配对中。。");
	                	   break;
	                   case Constants.CONNECT_FAIL :
	                	   BLEState.setText("尚未成功连接");
	                	   break;
	                   case Constants.CONNECT_SUCCESS :
	                	   msg.what=Constants.SHOWCHART;
	                	   break;
	                   case Constants.DISCOVERY_FINISHED :
	                	   BLEState.setText("搜索设备结束");
	                	   break;
	                   case Constants.SEND_CMD :
	                	   BLEState.setText("发送命令成功，读取数据中");
	                	   break;
	                   case Constants.SHOWCHART :
	                	   drawChart();	  	                	
	                	   break;
	                   case Constants.SHOWLENGTH :
	                	   text11.setText(String.valueOf(df.format(BLEClient.checklength[0])));
	                	   text22.setText(String.valueOf(df.format(BLEClient.checklength[1])));
	                	   text33.setText(String.valueOf(df.format(BLEClient.checklength[2])));
	                	   text44.setText(String.valueOf(df.format(BLEClient.checklength[3])));
	                	   text55.setText(String.valueOf(df.format(BLEClient.checklength[4])));
	                	   break;
	                   }
	            }
	        }
	    }
	    private final MyHandler handler = new MyHandler(this);
	
    
    
	/** Called when the activity is first created. */    
    private List<String> list = new ArrayList<String>();    
    private TextView myTextView;    
    private Spinner mySpinner;    
    private ArrayAdapter<String> adapter;    
    @Override    
    public void onCreate(Bundle savedInstanceState) { 
    	super.onCreate(savedInstanceState);    
        setContentView(R.layout.activity_main);  
         
        try { 
		 	   Log.i("PS","执行到读取数据里了");
		 	   exPath = excelsolve.getExcelDir()+File.separator+"sensor.xls";
				exFile = new File(exPath);
				Workbook book = Workbook.getWorkbook(exFile);
		        Sheet sheet = book.getSheet(0);  
		        for(int i= 0;i<134-1;i++){
		        	shuzu[0][i]=Double.parseDouble(sheet.getCell(1,i).getContents());
		        	shuzu[1][i]=Double.parseDouble(sheet.getCell(0,i).getContents());}
		        sheet = book.getSheet(1);  
		        for(int i= 0;i<134-1;i++){
		            shuzu[2][i]=Double.parseDouble(sheet.getCell(1,i).getContents());
		            shuzu[3][i]=Double.parseDouble(sheet.getCell(0,i).getContents());}
		        sheet = book.getSheet(2);  
		        for(int i= 0;i<134-1;i++){
		            shuzu[4][i]=Double.parseDouble(sheet.getCell(1,i).getContents());
		            shuzu[5][i]=Double.parseDouble(sheet.getCell(0,i).getContents());}
		        sheet = book.getSheet(3);  
		        for(int i= 0;i<134-1;i++){
		        	shuzu[6][i]=Double.parseDouble(sheet.getCell(1,i).getContents());
		        	shuzu[7][i]=Double.parseDouble(sheet.getCell(0,i).getContents());}
		        sheet = book.getSheet(4);  
		        for(int i= 0;i<134-1;i++){
		        	shuzu[8][i]=Double.parseDouble(sheet.getCell(1,i).getContents());
		        	shuzu[9][i]=Double.parseDouble(sheet.getCell(0,i).getContents());}
		        book.close();}
		        
		      catch (Exception e) {  
		          System.out.println(e);  
		         }
        
        sensor1 = (ImageView) findViewById(R.id.imageView1);
        sensor2 = (ImageView) findViewById(R.id.imageView2);
        sensor3 = (ImageView) findViewById(R.id.imageView3);
        sensor4 = (ImageView) findViewById(R.id.imageView4);
        sensor5 = (ImageView) findViewById(R.id.imageView5);
        text1 = (TextView) findViewById(R.id.ssensor1);
        text2 = (TextView) findViewById(R.id.ssensor2);
        text3 = (TextView) findViewById(R.id.ssensor3);
        text4 = (TextView) findViewById(R.id.ssensor4);
        text5 = (TextView) findViewById(R.id.ssensor5);
        text11 = (TextView) findViewById(R.id.sensort1);
        text22 = (TextView) findViewById(R.id.sensort2);
        text33 = (TextView) findViewById(R.id.sensort3);
        text44 = (TextView) findViewById(R.id.sensort4);
        text55 = (TextView) findViewById(R.id.sensort5);
        cm1 = (TextView) findViewById(R.id.cm1);
        cm2 = (TextView) findViewById(R.id.cm2);
        cm3 = (TextView) findViewById(R.id.cm3);
        cm4 = (TextView) findViewById(R.id.cm4);
        cm5 = (TextView) findViewById(R.id.cm5);
        
        solve = new ExcelSolve();        
        excelPath = solve.getExcelDir()+File.separator+"data.xls";
		excelFile = new File(excelPath);
		solve.createExcel(excelFile);
        Log.i("PS", excelPath+excelFile);
        
        waitin = 0;
        LinearLayout layout = (LinearLayout)findViewById(R.id.chart);
        //生成图表
		//chart = ChartFactory.getTimeChartView(this, getDateDemoDataset(), getDemoRenderer(), "volt");
		chart = ChartFactory.getLineChartView(this, getDateDemoDataset(), getDemoRenderer());
		layout.addView(chart, new LayoutParams(LayoutParams.WRAP_CONTENT,700));
          
    	BLEState = (TextView) findViewById(R.id.textView1);
    	mBLEClient = new BLEClient(MainActivity.this,handler);
	    search = (Button) findViewById(R.id.search_bt);
	    search.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				updateblue();
			}
		});
          
        //第一步：添加一个下拉列表项的list，这里添加的项就是下拉列表的菜单项    
        list.add("1个"); 
        list.add("2个");    
        list.add("3个");    
        list.add("4个");
        list.add("5个"); 
        myTextView = (TextView)findViewById(R.id.TextView_num);
        myTextView.setTextSize(20.0f);
        mySpinner = (Spinner)findViewById(R.id.Spinner_num);
        //第二步：为下拉列表定义一个适配器，这里就用到里前面定义的list。    
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list);    
        //第三步：为适配器设置下拉列表下拉时的菜单样式。    
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
        //第四步：将适配器添加到下拉列表上    
        mySpinner.setAdapter(adapter);    
        //第五步：为下拉列表设置各种事件的响应，这个事响应菜单被选中    
        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {    
                // TODO Auto-generated method stub    
                /* 将所选mySpinner 的值带入myTextView 中*/    
                myTextView.setText("传感器数量："+ adapter.getItem(arg2));
                if(arg2==0)
                {
                	isvisible = 1;
                	sensor1.setVisibility(View.VISIBLE);
                	sensor2.setVisibility(View.INVISIBLE);
                	sensor3.setVisibility(View.INVISIBLE);
                	sensor4.setVisibility(View.INVISIBLE);
                	sensor5.setVisibility(View.INVISIBLE);
                	text1.setVisibility(View.VISIBLE);
                	text2.setVisibility(View.INVISIBLE);
                	text3.setVisibility(View.INVISIBLE);
                	text4.setVisibility(View.INVISIBLE);
                	text5.setVisibility(View.INVISIBLE);
                	text11.setVisibility(View.VISIBLE);
                	text22.setVisibility(View.INVISIBLE);
                	text33.setVisibility(View.INVISIBLE);
                	text44.setVisibility(View.INVISIBLE);
                	text55.setVisibility(View.INVISIBLE);
                	cm1.setVisibility(View.VISIBLE);
                	cm2.setVisibility(View.INVISIBLE);
                	cm3.setVisibility(View.INVISIBLE);
                	cm4.setVisibility(View.INVISIBLE);
                	cm5.setVisibility(View.INVISIBLE);
                }
                else if(arg2==1)
                {
                	isvisible = 2;
                	sensor1.setVisibility(View.VISIBLE);
                	sensor2.setVisibility(View.VISIBLE);
                	sensor3.setVisibility(View.INVISIBLE);
                	sensor4.setVisibility(View.INVISIBLE);
                	sensor5.setVisibility(View.INVISIBLE);
                	text1.setVisibility(View.VISIBLE);
                	text2.setVisibility(View.VISIBLE);
                	text3.setVisibility(View.INVISIBLE);
                	text4.setVisibility(View.INVISIBLE);
                	text5.setVisibility(View.INVISIBLE);
                	text11.setVisibility(View.VISIBLE);
                	text22.setVisibility(View.VISIBLE);
                	text33.setVisibility(View.INVISIBLE);
                	text44.setVisibility(View.INVISIBLE);
                	text55.setVisibility(View.INVISIBLE);
                	cm1.setVisibility(View.VISIBLE);
                	cm2.setVisibility(View.VISIBLE);
                	cm3.setVisibility(View.INVISIBLE);
                	cm4.setVisibility(View.INVISIBLE);
                	cm5.setVisibility(View.INVISIBLE);
                }
                else if(arg2==2)
                {
                	isvisible = 3;
                	sensor1.setVisibility(View.VISIBLE);
                	sensor2.setVisibility(View.VISIBLE);
                	sensor3.setVisibility(View.VISIBLE);
                	sensor4.setVisibility(View.INVISIBLE);
                	sensor5.setVisibility(View.INVISIBLE);
                	text1.setVisibility(View.VISIBLE);
                	text2.setVisibility(View.VISIBLE);
                	text3.setVisibility(View.VISIBLE);
                	text4.setVisibility(View.INVISIBLE);
                	text5.setVisibility(View.INVISIBLE);
                	text11.setVisibility(View.VISIBLE);
                	text22.setVisibility(View.VISIBLE);
                	text33.setVisibility(View.VISIBLE);
                	text44.setVisibility(View.INVISIBLE);
                	text55.setVisibility(View.INVISIBLE);
                	cm1.setVisibility(View.VISIBLE);
                	cm2.setVisibility(View.VISIBLE);
                	cm3.setVisibility(View.VISIBLE);
                	cm4.setVisibility(View.INVISIBLE);
                	cm5.setVisibility(View.INVISIBLE);
                }
                else if(arg2==3)
                {
                	isvisible = 4;
                	sensor1.setVisibility(View.VISIBLE);
                	sensor2.setVisibility(View.VISIBLE);
                	sensor3.setVisibility(View.VISIBLE);
                	sensor4.setVisibility(View.VISIBLE);
                	sensor5.setVisibility(View.INVISIBLE);
                	text1.setVisibility(View.VISIBLE);
                	text2.setVisibility(View.VISIBLE);
                	text3.setVisibility(View.VISIBLE);
                	text4.setVisibility(View.VISIBLE);
                	text5.setVisibility(View.INVISIBLE);
                	text11.setVisibility(View.VISIBLE);
                	text22.setVisibility(View.VISIBLE);
                	text33.setVisibility(View.VISIBLE);
                	text44.setVisibility(View.VISIBLE);
                	text55.setVisibility(View.INVISIBLE);
                	cm1.setVisibility(View.VISIBLE);
                	cm2.setVisibility(View.VISIBLE);
                	cm3.setVisibility(View.VISIBLE);
                	cm4.setVisibility(View.VISIBLE);
                	cm5.setVisibility(View.INVISIBLE);
                }
                else if(arg2==4)
                {
                	isvisible = 5;
                	sensor1.setVisibility(View.VISIBLE);
                	sensor2.setVisibility(View.VISIBLE);
                	sensor3.setVisibility(View.VISIBLE);
                	sensor4.setVisibility(View.VISIBLE);
                	sensor5.setVisibility(View.VISIBLE);
                	text1.setVisibility(View.VISIBLE);
                	text2.setVisibility(View.VISIBLE);
                	text3.setVisibility(View.VISIBLE);
                	text4.setVisibility(View.VISIBLE);
                	text5.setVisibility(View.VISIBLE);
                	text11.setVisibility(View.VISIBLE);
                	text22.setVisibility(View.VISIBLE);
                	text33.setVisibility(View.VISIBLE);
                	text44.setVisibility(View.VISIBLE);
                	text55.setVisibility(View.VISIBLE);
                	cm1.setVisibility(View.VISIBLE);
                	cm2.setVisibility(View.VISIBLE);
                	cm3.setVisibility(View.VISIBLE);
                	cm4.setVisibility(View.VISIBLE);
                	cm5.setVisibility(View.VISIBLE);
                }
                /* 将mySpinner 显示*/    
                arg0.setVisibility(View.VISIBLE);    
            }    
            public void onNothingSelected(AdapterView<?> arg0) {    
                // TODO Auto-generated method stub    
                myTextView.setText("NONE");    
                arg0.setVisibility(View.VISIBLE);    
            }    
        });    
        /*下拉菜单弹出的内容选项触屏事件处理*/    
        mySpinner.setOnTouchListener(new Spinner.OnTouchListener(){    
            public boolean onTouch(View v, MotionEvent event) {    
                // TODO Auto-generated method stub    
                /** 
                 *  
                 */  
                return false;    
            }  
        });    
        /*下拉菜单弹出的内容选项焦点改变事件处理*/    
        mySpinner.setOnFocusChangeListener(new Spinner.OnFocusChangeListener(){    
        public void onFocusChange(View v, boolean hasFocus) {    
            // TODO Auto-generated method stub    
  
        }    
        });    
    }
	 
    
   	
    private void updateblue(){  
        UpdateBlue updateBlue = new UpdateBlue(this);  
        updateBlue.execute();  
    }  
  	    
    class UpdateBlue extends AsyncTask<Void,Integer,Integer>{  
        private Context context;  
        UpdateBlue(Context context) {  
            this.context = context;  
        }  
  
        /** 
         * 运行在UI线程中，在调用doInBackground()之前执行 
         */  
        @Override  
        protected void onPreExecute() {  
            Toast.makeText(context,"开始执行",Toast.LENGTH_SHORT).show();  
        }  
        /** 
         * 后台运行的方法，可以运行非UI线程，可以执行耗时的方法 
         */  
        @Override  
        protected Integer doInBackground(Void... params) {  
        	mBLEClient.searchDevice();            
            return null;  
        }  
  
        /** 
         * 运行在ui线程中，在doInBackground()执行完毕后执行 
         */  
        @Override  
        protected void onPostExecute(Integer integer) {  
            Toast.makeText(context,"执行完毕",Toast.LENGTH_SHORT).show();  
        }  
  
        /** 
         * 在publishProgress()被调用以后执行，publishProgress()用于更新进度 
         */  
        @Override  
        protected void onProgressUpdate(Integer... values) {  
        	BLEState.setText(""+values[0]); 
        }  
    }  

    
    /**
	    * 数据对象
	    * @return
	    */
	   private XYMultipleSeriesDataset getDateDemoDataset() {
		    dataset1 = new XYMultipleSeriesDataset();
		    final int nr = 10;
		   Random r = new Random();
		   series1 = new XYSeries("Series1" );
		   series2 = new XYSeries("Series2" );
		   series3 = new XYSeries("Series3" );
		   series4 = new XYSeries("Series4" );
		   series5 = new XYSeries("Series5" );
		  for (int k = 0; k < nr;k++) {
		  int x = k; // x：0-10之间的随机整数
		  double y = 50+r.nextInt()%50; // y:50-100之间的随机整数
		  series1.add(x,y);// 往系列中加入一个随机分布的点
		  series2.add(x,y);
		  series3.add(x,y);
		  series4.add(x,y);
		  series5.add(x,y);
		  //ps
		  }
		  // 把添加了点的折线放入dataset
		 dataset1.addSeries(series1);
		 dataset1.addSeries(series2);
		 dataset1.addSeries(series3);
		 dataset1.addSeries(series4);
		 dataset1.addSeries(series5);
		  
		  return dataset1;
		  } 
    
   
	   
	   /**
		 * 设定如表样式
		 * @return
		 */
		   private XYMultipleSeriesRenderer getDemoRenderer() {
			    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
			    renderer.setChartTitle("传感器长度实时曲线");//标题
			    renderer.setChartTitleTextSize(20);
			    renderer.setXTitle("时间");    //x轴说明
			    renderer.setAxisTitleTextSize(16);
			    renderer.setAxesColor(Color.BLACK);
			    renderer.setLabelsTextSize(15);    //数轴刻度字体大小
			    renderer.setLabelsColor(Color.BLACK);
			    renderer.setLegendTextSize(15);    //曲线说明
			    renderer.setXLabelsColor(Color.BLACK);
			    renderer.setYLabelsColor(0,Color.BLACK);
			    renderer.setShowLegend(false);
			    renderer.setMargins(new int[] {90, 30, 0, 0});
			    XYSeriesRenderer r = new XYSeriesRenderer();//渲染器  单个曲线  所以只需要一个渲染器
			    r.setColor(Color.BLUE);//第一条曲线
			    r.setChartValuesTextSize(15);
			    r.setChartValuesSpacing(5);
			    r.setPointStyle(PointStyle.POINT);
			    r.setFillBelowLine(true);
			    r.setFillBelowLineColor(Color.WHITE);
			    r.setFillPoints(true);
			    r.setLineWidth(2f);
			    renderer.addSeriesRenderer(r);
			    renderer.setMarginsColor(Color.WHITE);
			    renderer.setPanEnabled(false,false);
			    renderer.setShowGrid(true);
			    renderer.setYAxisMax(20);//设置轴的范围大小
			    renderer.setYAxisMin(-5);
			    renderer.setInScroll(true);  //调整大小
			    r = new XYSeriesRenderer();
			    r.setColor(Color.GREEN);//第二条曲线
			    r.setChartValuesTextSize(15);
			    r.setChartValuesSpacing(5);
			    r.setPointStyle(PointStyle.POINT);
			    r.setFillBelowLine(true);
			    r.setFillBelowLineColor(Color.WHITE);
			    r.setFillPoints(true);
			    r.setLineWidth(2f);
			    renderer.addSeriesRenderer(r);
			    renderer.setMarginsColor(Color.WHITE);
			    renderer.setPanEnabled(false,false);
			    renderer.setShowGrid(true);
			    renderer.setYAxisMax(50);//设置轴的范围大小
			    renderer.setYAxisMin(-5);
			    renderer.setInScroll(true);  //调整大小
			    r = new XYSeriesRenderer();
			    r.setColor(Color.YELLOW);//第三条曲线
			    r.setChartValuesTextSize(15);
			    r.setChartValuesSpacing(5);
			    r.setPointStyle(PointStyle.POINT);
			    r.setFillBelowLine(true);
			    r.setFillBelowLineColor(Color.WHITE);
			    r.setFillPoints(true);
			    r.setLineWidth(2f);
			    renderer.addSeriesRenderer(r);
			    renderer.setMarginsColor(Color.WHITE);
			    renderer.setPanEnabled(false,false);
			    renderer.setShowGrid(true);
			    renderer.setYAxisMax(50);//设置轴的范围大小
			    renderer.setYAxisMin(-5);
			    renderer.setInScroll(true);  //调整大小
			    r = new XYSeriesRenderer();
			    r.setColor(Color.RED);//第四条曲线
			    r.setChartValuesTextSize(15);
			    r.setChartValuesSpacing(5);
			    r.setPointStyle(PointStyle.POINT);
			    r.setFillBelowLine(true);
			    r.setFillBelowLineColor(Color.WHITE);
			    r.setFillPoints(true);
			    r.setLineWidth(2f);
			    renderer.addSeriesRenderer(r);
			    renderer.setMarginsColor(Color.WHITE);
			    renderer.setPanEnabled(false,false);
			    renderer.setShowGrid(true);
			    renderer.setYAxisMax(50);//设置轴的范围大小
			    renderer.setYAxisMin(-5);
			    renderer.setInScroll(true);  //调整大小
			    r = new XYSeriesRenderer();
			    r.setColor(Color.CYAN);//第五条曲线
			    r.setChartValuesTextSize(15);
			    r.setChartValuesSpacing(5);
			    r.setPointStyle(PointStyle.POINT);
			    r.setFillBelowLine(true);
			    r.setFillBelowLineColor(Color.WHITE);
			    r.setFillPoints(true);
			    r.setLineWidth(2f);
			    renderer.addSeriesRenderer(r);
			    renderer.setMarginsColor(Color.WHITE);
			    renderer.setPanEnabled(false,false);
			    renderer.setShowGrid(true);
			    renderer.setYAxisMax(50);//设置轴的范围大小
			    renderer.setYAxisMin(-5);
			    renderer.setInScroll(true);  //调整大小
			    return renderer;
			  }   
	   
	   
		   private void updateChart() {
			   //设定长度为100
			    //int length = series1.getItemCount();
			   int length = series1.getItemCount();
			   if(length>=100) length = 100;
			    //addY=random.nextInt()%10;//随机产生数
			    addY1=BLEClient.checklength[0];
			    addY2=BLEClient.checklength[1]*10;
			    addY3=BLEClient.checklength[2]*10;
			    addY4=BLEClient.checklength[3];
			    addY5=BLEClient.checklength[4];
			    if(isvisible==1)
			    {
			    	addY2=-10;
				    addY3=-10;
				    addY4=-10;
				    addY5=-10;
			    }
			    if(isvisible==2)
			    {
				    addY3=-10;
				    addY4=-10;
				    addY5=-10;
			    }
			    if(isvisible==3)
			    {
				    addY4=-10;
				    addY5=-10;
			    }
			    if(isvisible==4)
			    {
				    addY5=-10;
			    }
			    Log.i("PS","addY1="+String.valueOf(addY1));
			    
			    addX = waitin1; 
			    waitin = waitin+1;
			    waitin1 = waitin1+1;
			    if(waitin>=99)
			    {
			    	waitin=0;
			    }
			    if(waitin1>=999)
			    {
			    	waitin1=0;
			    }
			    
			    //addX=new Date().getTime();
			    
			    //将前面的点放入缓存
				for (int i = 0; i < length; i++) {
					//xcache[i] =  new Date((long)series1.getX(i));
					xcache1[i] = (int) series1.getX(i);
					ycache1[i] = (double) series1.getY(i);
					xcache2[i] = (int) series2.getX(i);
					ycache2[i] = (double) series2.getY(i);
					xcache3[i] = (int) series3.getX(i);
					ycache3[i] = (double) series3.getY(i);
					xcache4[i] = (int) series4.getX(i);
					ycache4[i] = (double) series4.getY(i);
					xcache5[i] = (int) series5.getX(i);
					ycache5[i] = (double) series5.getY(i);
				}
			    
				series1.clear();
				series2.clear();
				series3.clear();
				series4.clear();
				series5.clear();
				//将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
				//这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
				series1.add(addX, addY1);
				series2.add(addX, addY2);
				series3.add(addX, addY3);
				series4.add(addX, addY4);
				series5.add(addX, addY5);
				for (int k = 0; k < length; k++) {
		    		series1.add(xcache1[k], ycache1[k]);
		    		series2.add(xcache2[k], ycache2[k]);
		    		series3.add(xcache3[k], ycache3[k]);
		    		series4.add(xcache4[k], ycache4[k]);
		    		series5.add(xcache5[k], ycache5[k]);
		    	}
				//在数据集中添加新的点集
				dataset1.removeSeries(series1);
				dataset1.addSeries(series1);
				dataset1.removeSeries(series2);
				dataset1.addSeries(series2);
				dataset1.removeSeries(series3);
				dataset1.addSeries(series3);
				dataset1.removeSeries(series4);
				dataset1.addSeries(series4);
				dataset1.removeSeries(series5);
				dataset1.addSeries(series5);
				//曲线更新
				//chart.repaint();
				chart.invalidate();
		    }	   
		   

		    
		   	private void drawChart(){ 	
		   			Log.i("PS", "进入作图了"); 
		   			charthandler = new Handler() {
		   			@Override
		   			public void handleMessage(Message msg) {
		   			//刷新图表
		   			updateChart();
		   			super.handleMessage(msg);
		   		}
		   	};
		   		task = new TimerTask() {
		   		@Override
		   		public void run() {
		   		Message message = new Message();
		   		message.what = 200;
		   		charthandler.sendMessage(message);
		   		}
		   		};
		   		timer.schedule(task,50,100);
		   	}

  
}
