package datasolve;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import bluetoothSolve.BLEClient;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import android.os.Environment;
import android.util.Log;

public class ExcelSolve {

	int pagecount = 0;
	int pagetrans = 0;
	private WritableWorkbook wwb;
	private File exFile;
	private String exPath;
	int i,j;
	// ����excel��.
		public void createExcel(File file) {
			Log.i("PS", "������");
			WritableSheet ws = null;
			try {
				if (!file.exists()) {
					wwb = Workbook.createWorkbook(file);
					ws = wwb.createSheet("ϥ�ؽ�", 0);					
					ws = wwb.createSheet("���1", 1);
					ws = wwb.createSheet("���2", 2);
					ws = wwb.createSheet("�粿", 3);
					ws = wwb.createSheet("�ⲿ", 4);
					
					// ���ڴ���д���ļ���
					wwb.write();
					wwb.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void writeToExcel() {
				try {
					exPath = getExcelDir()+File.separator+"data.xls";
					exFile = new File(exPath);
					Workbook oldWwb = Workbook.getWorkbook(exFile);
					wwb = Workbook.createWorkbook(exFile,
							oldWwb);
					Log.i("Path=",exPath);
					WritableSheet ws = wwb.getSheet(0);
					pagecount +=100;
					for(int i=0;i<100;i++){
						
						if(pagecount==30000)
							pagetrans+=6;
						if(pagecount==60000)
							pagetrans+=6;
						if(pagecount==90000)
							pagetrans+=6;
						if(pagecount==120000)
							pagetrans+=6;
						if(pagecount==150000)
							pagetrans+=6;
						
					// ��ǰ����
					//int row = ws.getRows();
					Label l1 = new Label(0+pagetrans,BLEClient.countsolve*100+i,Double.toString(BLEClient.result[i][0]));
					Label l2 = new Label(1+pagetrans,BLEClient.countsolve*100+i,Double.toString(BLEClient.result[i][1]));
					Label l3 = new Label(2+pagetrans,BLEClient.countsolve*100+i,Double.toString(BLEClient.result[i][2]));
					Label l4 = new Label(3+pagetrans,BLEClient.countsolve*100+i,Double.toString(BLEClient.result[i][3]));
					Label l5 = new Label(4+pagetrans,BLEClient.countsolve*100+i,Double.toString(BLEClient.result[i][4]));

					ws.addCell(l1);
					ws.addCell(l2);
					ws.addCell(l3);
					ws.addCell(l4);
					ws.addCell(l5);
					}
					// ���ڴ���д���ļ���,ֻ��ˢһ��.
					wwb.write();
					wwb.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
	
	 
		// ��ȡExcel�ļ���
			public String getExcelDir() {
				// SD��ָ���ļ���
				String sdcardPath = Environment.getExternalStorageDirectory()
						.toString();
				File dir = new File(sdcardPath + File.separator + "Excel"
						+ File.separator + "Data");

				if (dir.exists()) {
					return dir.toString();

				} else {
					dir.mkdirs();
					Log.d("BAG", "����·��������,");
					return dir.toString();
				}
			}


	
}
