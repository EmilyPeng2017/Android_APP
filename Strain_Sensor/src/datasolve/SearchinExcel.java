package datasolve;

import java.io.File;

import android.util.Log;
import bluetoothSolve.BLEClient;
import bluetoothSolve.Converter;
import jxl.Sheet;
import jxl.Workbook;

public class SearchinExcel {

	private double[][] shuzu = new double[4][400];
	public static double lencheck1,lencheck2,lencheck3,lencheck4;
	private File exFile;
	private String exPath;
	private ExcelSolve excelsolve = new ExcelSolve();
	//首先读表    电流值在两者中间的   取对应的同一行的电压值
	 public void checkTable(){
    try { 
 	   Log.i("PS","执行到check里了");

        exPath = excelsolve.getExcelDir()+File.separator+"sensor.xls";
		exFile = new File(exPath);
		Workbook book = Workbook.getWorkbook(exFile);
        // 获得第一个工作表对象  
        Sheet sheet = book.getSheet(0);  
        for(int i= 0;i<134-1;i++){
        	shuzu[0][i]=Double.parseDouble(sheet.getCell(1,i).getContents());
        	//System.out.println("查询的表格中的数据为"+shuzu[0][i]);
        	shuzu[0][i+1]=Double.parseDouble(sheet.getCell(1,i+1).getContents());
        	if((BLEClient.checklength[0]<=shuzu[0][i+1])&&(BLEClient.checklength[0]>=shuzu[0][i]))
        		{
        		lencheck1 = Double.parseDouble(sheet.getCell(0,i+1).getContents());
        		break;
        		}
        	else if(BLEClient.checklength[0]<=shuzu[0][0])
        		{
        		lencheck1 = Double.parseDouble(sheet.getCell(0,0).getContents());
        		break;
        		}
        	else if((i == 134-1)&&BLEClient.checklength[0]>=shuzu[0][134-1])
        		{
        		lencheck1 = Double.parseDouble(sheet.getCell(0,i).getContents());
        		break;
        		}}

        	sheet = book.getSheet(1);  
            for(int i= 0;i<134-1;i++){
            	shuzu[0][i]=Double.parseDouble(sheet.getCell(1,i).getContents());
            	//System.out.println("查询的表格中的数据为"+shuzu[0][i]);
            	shuzu[1][i+1]=Double.parseDouble(sheet.getCell(1,i+1).getContents());
            	if((BLEClient.checklength[1]<=shuzu[1][i+1])&&(BLEClient.checklength[1]>=shuzu[1][i]))
            		{
            		lencheck2 = Double.parseDouble(sheet.getCell(0,i+1).getContents());
            		break;
            		}
            	else if(BLEClient.checklength[1]<=shuzu[1][0])
            		{
            		lencheck2 = Double.parseDouble(sheet.getCell(0,0).getContents());
            		break;
            		}
            	else if((i == 134-1)&&BLEClient.checklength[1]>=shuzu[1][134-1])
            		{
            		lencheck2 = Double.parseDouble(sheet.getCell(0,i).getContents());
            		break;
            		}}
            	
            	sheet = book.getSheet(2);  
                for(int i= 0;i<134-1;i++){
                	shuzu[2][i]=Double.parseDouble(sheet.getCell(1,i).getContents());
                	//System.out.println("查询的表格中的数据为"+shuzu[0][i]);
                	shuzu[0][i+1]=Double.parseDouble(sheet.getCell(1,i+1).getContents());
                	if((BLEClient.checklength[2]<=shuzu[2][i+1])&&(BLEClient.checklength[2]>=shuzu[2][i]))
                		{
                		lencheck3 = Double.parseDouble(sheet.getCell(0,i+1).getContents());
                		break;
                		}
                	else if(BLEClient.checklength[2]<=shuzu[2][0])
                		{
                		lencheck3 = Double.parseDouble(sheet.getCell(0,0).getContents());
                		break;
                		}
                	else if((i == 134-1)&&BLEClient.checklength[2]>=shuzu[2][134-1])
                		{
                		lencheck3 = Double.parseDouble(sheet.getCell(0,i).getContents());
                		break;
                		}}
                	
                	sheet = book.getSheet(3);  
                    for(int i= 0;i<134-1;i++){
                    	shuzu[3][i]=Double.parseDouble(sheet.getCell(1,i).getContents());
                    	//System.out.println("查询的表格中的数据为"+shuzu[0][i]);
                    	shuzu[3][i+1]=Double.parseDouble(sheet.getCell(1,i+1).getContents());
                    	if((BLEClient.checklength[3]<=shuzu[3][i+1])&&(BLEClient.checklength[3]>=shuzu[3][i]))
                    		{
                    		lencheck4 = Double.parseDouble(sheet.getCell(0,i+1).getContents());
                    		break;
                    		}
                    	else if(BLEClient.checklength[3]<=shuzu[3][0])
                    		{
                    		lencheck4 = Double.parseDouble(sheet.getCell(0,0).getContents());
                    		break;
                    		}
                    	else if((i == 134-1)&&BLEClient.checklength[3]>=shuzu[3][134-1])
                    		{
                    		lencheck4 = Double.parseDouble(sheet.getCell(0,i).getContents());
                    		break;
                    		}}
                    
        
        book.close();} 
            
      catch (Exception e) {  
          System.out.println(e);  
         }
    System.out.println(lencheck1 +lencheck2 +lencheck3 +lencheck4);
}
	
}
