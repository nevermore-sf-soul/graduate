package org;

import java.io.*;
import java.util.*;

import jxl.write.biff.RowsExceededException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.workflowsim.threadTest;


public class writetoexcel2 {
    static Map<Integer,Integer> taskn=new HashMap<>();
    static Map<Double,Integer> deadline=new HashMap<>();
    static double[][][][][][] min=new double[4][4][5][4][10][5];//tasknum,privacypercent,deadlinefactor,localscale,instance,workflowtype
    static Map<String,Integer> work=new HashMap<>();
    static double[][] privacytaskpercent = new double[][]{{0.05, 0.15, 0.8}, {0.1, 0.2, 0.7}, {0.15, 0.25, 0.55}, {0.2, 0.3, 0.5}};
    static Map<Double,Integer> localscale=new HashMap<>();
    static Map<String,Integer> alg=new HashMap<>();
    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile","./path_to_the_log4j2_config_file/log4j2.xml");
        Logger log = LogManager.getLogger(writetoexcel.class.getName());
        int[] tasknums = new int[]{150,200,250,300};
        double[] deadlinefactors = new double[]{1.5, 1.6, 1.7, 1.8, 1.9};
        String excelFilePath = "F:/";
        String encoding = "GBK";
        List<String > respath=new ArrayList<>();
        taskn.put(150,0);taskn.put(200,1);taskn.put(250,2);taskn.put(300,3);
        deadline.put(1.5,0);deadline.put(1.6,1);deadline.put(1.7,2);deadline.put(1.8,3);deadline.put(1.9,4);
        String[] algtype=new String[]{"iheft","myalg","mcpcpp"};
        String[] workflowtype = new String[]{"Genome", "Sipht"};
        work.put("Genome",0);work.put("Sipht",1);
        localscale.put(0.1,0);localscale.put(0.2,1);localscale.put(0.3,2);localscale.put(0.4,3);
        alg.put("iheft",0);alg.put("myalg",1);alg.put("mcpcpp",2);
        for(int a=0;a<4;a++)
        {
            for(int j=0;j<4;j++)
            {
                for(int k=0;k<5;k++)
                {
                    for(int z=0;z<4;z++)
                    {
                        for(int w=0;w<10;w++)
                        {
                            for(int l=0;l<5;l++)
                            min[a][j][k][z][w][l]=Double.MAX_VALUE;
                        }
                    }
                }
            }
        }
        for(int w=0;w<workflowtype.length;w++)
        {
            respath.clear();
        for (int i = 0; i < algtype.length; i++) {

                    String t=new String("F:/benchmark/result/compare1/" + workflowtype[w]+" "+algtype[i]+".txt");
                    respath.add(t);
                    try {
                        File file=new File(t);
                        InputStreamReader read = null;
                        read = new InputStreamReader(new FileInputStream(file), encoding);
                        BufferedReader bufferedReader = new BufferedReader(read);
                        String lineTxt = null;
                        while ((lineTxt = bufferedReader.readLine()) != null){
                            String[] list =  lineTxt.split(" ");
                            int n=0;
                            double deadlinefactor=Double.parseDouble(list[4]);
                            int tasknum=Integer.parseInt(list[0]);
                            int ins=Integer.parseInt(list[5]);
                            double local=Double.parseDouble(list[6]);
                            double t1=Double.parseDouble(list[1].substring(1,list[1].length()-1));double t2=Double.parseDouble(list[2].substring(0,list[2].length()-1));double t3=Double.parseDouble(list[3].substring(0,list[3].length()-1));
                            int per=0;
                            for(int x=0;x< privacytaskpercent.length;x++)
                            {
                                if(privacytaskpercent[x][0]==t1&&privacytaskpercent[x][1]==t2&&privacytaskpercent[x][2]==t3)
                                {
                                    per=x;break;
                                }
                            }
                            min[taskn.get(tasknum)][per][deadline.get(deadlinefactor)][localscale.get(local)][ins][w]=Math.min(min[taskn.get(tasknum)][per][deadline.get(deadlinefactor)][localscale.get(local)][ins][w],Double.parseDouble(list[7]));
                        }

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            exportonefile(respath, excelFilePath+workflowtype[w]+".xls", encoding);
        }
}

    public static void exportonefile( List<String> filePath, String excelFilePath, String encoding) {
        //创建工作薄
        HSSFWorkbook workbook=new HSSFWorkbook();
        //创建sheet
        HSSFSheet sheet=workbook.createSheet();
        //创建第一行row
        HSSFRow header=sheet.createRow(0);
        //创建单元格并插入表头
        HSSFCell cell=null;
        String[] infos={"tasknum","percentage","deadlinefactor","localscale","instance","Fee","deadline","makespan","algtype","workflowtype","timecost"};
        for(int i=0;i<infos.length;i++){
            cell=header.createCell(i);
            cell.setCellValue(infos[i]);
        }

        //
        //一些临时变量，用于写到excel中
        try {
            int i = 1;
            for(String path:filePath)
            {
                File file=new File(path);
                InputStreamReader read = null;
                read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                HSSFRow body=null;
                while ((lineTxt = bufferedReader.readLine()) != null){
                    if(i==65536)
                    {
                        sheet=workbook.createSheet();
                        i=0;
                    }
                    String[] list =  lineTxt.split(" ");
                    int n=0;
                    double deadlinefactor=Double.parseDouble(list[4]);
                    int tasknum=Integer.parseInt(list[0]);
                    int ins=Integer.parseInt(list[5]);
                    double local=Double.parseDouble(list[6]);
                    double t1=Double.parseDouble(list[1].substring(1,list[1].length()-1));double t2=Double.parseDouble(list[2].substring(0,list[2].length()-1));double t3=Double.parseDouble(list[3].substring(0,list[3].length()-1));
                    int per=0;
                    int time=Integer.parseInt(list[9]);
                    String[] p=path.split("/");
                    String[] spli=p[p.length-1].split(" ");
                    int w=work.get(spli[0]);
                    for(int x=0;x< privacytaskpercent.length;x++)
                    {
                        if(privacytaskpercent[x][0]==t1&&privacytaskpercent[x][1]==t2&&privacytaskpercent[x][2]==t3)
                        {
                            per=x;break;
                        }
                    }
                    double minz=min[taskn.get(tasknum)][per][deadline.get(deadlinefactor)][localscale.get(local)][ins][w];
                    String al=spli[1].substring(0,spli[1].length()-4);
                    body=sheet.createRow(i);
                    cell=body.createCell(n++);
                    cell.setCellValue(tasknum);
                    cell=body.createCell(n++);
                    cell.setCellValue(per);
                    cell=body.createCell(n++);
                    cell.setCellValue(deadlinefactor);
                    cell=body.createCell(n++);
                    cell.setCellValue(local);
                    cell=body.createCell(n++);
                    cell.setCellValue(ins);
                    cell=body.createCell(n++);
                    double temp=Double.parseDouble(list[7]);
                    double res=(temp-minz)/minz*10;
                    cell.setCellValue(res);
                    cell=body.createCell(n++);
                    cell.setCellValue(Double.parseDouble(list[8]));
                    cell=body.createCell(n++);
                    cell.setCellValue(Double.parseDouble(list[10]));
                    cell=body.createCell(n++);
                    cell.setCellValue(al);
                    cell=body.createCell(n++);
                    cell.setCellValue(spli[0]);
                    cell=body.createCell(n++);
                    cell.setCellValue(time);
                    i++;
                }
            }
            File file=new File(excelFilePath);
            file.createNewFile();
            FileOutputStream fileOutputStream=new FileOutputStream(excelFilePath);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



