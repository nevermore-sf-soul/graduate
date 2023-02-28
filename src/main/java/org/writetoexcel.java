package org;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.write.biff.RowsExceededException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.workflowsim.threadTest;


public class writetoexcel {
    static Map<Integer,Integer> taskn=new HashMap<>();
    static Map<Double,Integer> deadline=new HashMap<>();
    static double[][][][] min=new double[4][4][5][10];//tasknum,privacypercent,deadlinefactor,instance
    static double[][] privacytaskpercent = new double[][]{{0.05, 0.15, 0.8}, {0.1, 0.2, 0.7}, {0.15, 0.25, 0.55}, {0.2, 0.3, 0.5}};
    static Map<String,Integer> sdm=new HashMap<>();
    static Map<String,Integer> trm=new HashMap<>();static Map<String,Integer> ltsm1=new HashMap<>();static Map<String,Integer> ltsm2=new HashMap<>();
    static Map<String,Integer> ntsm1=new HashMap<>();static Map<String,Integer> ntsm2=new HashMap<>();
    public static void main(String argv[]) {
        System.setProperty("log4j.configurationFile","./path_to_the_log4j2_config_file/log4j2.xml");
        Logger log = LogManager.getLogger(writetoexcel.class.getName());
        String[] SDM = new String[]{"SDMDepthPLSum", "SDMPathPLSum", "SDMExecutiontimePercent"};
        sdm.put("SDMDepthPLSum",0);sdm.put("SDMPathPLSum",1);sdm.put("SDMExecutiontimePercent",2);
        String[] TRM = new String[]{"TRMMaxRankavg", "TRMMinFloatTime", "TRMTaskFeature"};
        trm.put("TRMMaxRankavg",0);trm.put("TRMMinFloatTime",1);trm.put("TRMTaskFeature",2);
        String[] LPLTSMLocal = new String[]{"TSMLocalMinWaste", "TSMLocalEarlyAvaiableTime", "TSMLocalEarlyFinishTime"};
        ltsm1.put("TSMLocalMinWaste",0);ltsm1.put("TSMLocalEarlyAvaiableTime",1);ltsm1.put("TSMLocalEarlyFinishTime",2);
        ntsm1.put("TSMLocalMinWaste",0);ntsm1.put("TSMLocalEarlyAvaiableTime",1);ntsm1.put("TSMLocalEarlyFinishTime",2);
        String[] LPLTSMUsingExistingVm = new String[]{"TSMUsingExistingVmFirstAdaptSTB", "TSMUsingExistingVmLongestSTB", "TSMUsingExistingVmShortestSTB"};
        ltsm2.put("TSMUsingExistingVmFirstAdaptSTB",0);ltsm2.put("TSMUsingExistingVmLongestSTB",1);ltsm2.put("TSMUsingExistingVmShortestSTB",2);
        ntsm2.put("TSMUsingExistingVmFirstAdaptSTB",0);ntsm2.put("TSMUsingExistingVmLongestSTB",1);ntsm2.put("TSMUsingExistingVmShortestSTB",2);
//        int[] tasknums = new int[]{150,200,250,300};
        int[] tasknums = new int[]{150,200,250,300};
        double[] deadlinefactors = new double[]{1.5, 1.6, 1.7, 1.8, 1.9};
        String excelFilePath = "F:/res.xls";
        String encoding = "GBK";
        String[] workflowtype = new String[]{"CyberShake", "Montage", "Genome", "Inspiral", "Sipht"};
        List<String > respath=new ArrayList<>();
        taskn.put(150,0);taskn.put(200,1);taskn.put(250,2);taskn.put(300,3);
        deadline.put(1.5,0);deadline.put(1.6,1);deadline.put(1.7,2);deadline.put(1.8,3);deadline.put(1.9,4);
        for (int i = 0; i < tasknums.length; i++) {
            for(int w=0;w<workflowtype.length;w++)
            {
            for (int j=0;j< privacytaskpercent.length;j++) {
                for (int ins = 0; ins < 10; ins++) {
                    String t=new String("F:/benchmark/result/" + workflowtype[w]+"_"+tasknums[i] + " [" + privacytaskpercent[j][0] + "," + privacytaskpercent[j][1] + "," + privacytaskpercent[j][2]+ "]_"+ins+".txt");
                    respath.add(t);
                    double[] deadmin=new double[]{Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};

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
                            deadmin[deadline.get(deadlinefactor)]=Math.min(deadmin[deadline.get(deadlinefactor)],Double.parseDouble(list[11]));
                        }
                        for(int x=0;x<5;x++)
                        {
                            min[i][j][x][ins]=deadmin[x];
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        }
        exportonefile(respath, excelFilePath, encoding);

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
            String[] infos={"tasknum","percentage","deadlinefactor","instance","SDM","TRM","LTSMLocal","LTSMUsing","NTSMLocal","NTSMUsing","Fee","deadline","makespan"};
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
                    body=sheet.createRow(i);
                    String[] list =  lineTxt.split(" ");
                    int n=0;
                    double deadlinefactor=Double.parseDouble(list[4]);
                    int tasknum=Integer.parseInt(list[0]);
                    int ins=Integer.parseInt(String.valueOf(path.charAt(path.length()-6)));
                    double t1=Double.parseDouble(list[1].substring(1,list[1].length()-1));double t2=Double.parseDouble(list[2].substring(0,list[2].length()-1));double t3=Double.parseDouble(list[3].substring(0,list[3].length()-1));
                    int per=0;
                    for(int x=0;x< privacytaskpercent.length;x++)
                    {
                        if(privacytaskpercent[x][0]==t1&&privacytaskpercent[x][1]==t2&&privacytaskpercent[x][2]==t3)
                        {
                            per=x;break;
                        }
                    }
                    double minz=min[taskn.get(tasknum)][per][deadline.get(deadlinefactor)][ins];
                    cell=body.createCell(n++);
                    cell.setCellValue(tasknum);
                    cell=body.createCell(n++);
                    cell.setCellValue(per);
                    cell=body.createCell(n++);
                    cell.setCellValue(deadlinefactor);
                    cell=body.createCell(n++);
                    cell.setCellValue(ins);
                    cell=body.createCell(n++);
                    cell.setCellValue(sdm.get(list[5]));
                    cell=body.createCell(n++);
                    cell.setCellValue(trm.get(list[6]));
                    cell=body.createCell(n++);
                    cell.setCellValue(ltsm1.get(list[7]));
                    cell=body.createCell(n++);
                    cell.setCellValue(ltsm2.get(list[8]));
                    cell=body.createCell(n++);
                    cell.setCellValue(ntsm1.get(list[9]));
                    cell=body.createCell(n++);
                    cell.setCellValue(ntsm2.get(list[10]));
                    cell=body.createCell(n++);
                    double temp=Double.parseDouble(list[11]);
                    double res=(temp-minz)/minz*10;
                    cell.setCellValue(res);
                    cell=body.createCell(n++);
                    cell.setCellValue(Double.parseDouble(list[12]));
                    cell=body.createCell(n++);
                    cell.setCellValue(Double.parseDouble(list[13]));
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



