package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.FileItem;
import org.workflowsim.Task;
import org.workflowsim.Vm;
import org.workflowsim.utils.Parameters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class nocloud {
    Environment environment;
    Map<Task, Double> rankup = new HashMap<>();
    String respath;
    public nocloud(List<Task> list, int tasknum, String respath, Environment environmentin, double[] percentage, int instance,double bandscal,double localscale,int localvms) throws IOException {
        this.environment = new Environment();
        environment.pedgenum = environmentin.pedgenum;
        environment.edgenum = environmentin.edgenum;
        environment.bandwidth = new double[environmentin.bandwidth.length][environmentin.bandwidth[0].length];
        for(int x=0;x<environmentin.bandwidth.length;x++)
        {
            for(int y=0;y<environmentin.bandwidth[0].length;y++)
            {
                environment.bandwidth[x][y]=environmentin.bandwidth[x][y]*bandscal;
            }
        }
        environment.maxspeed = environmentin.maxspeed;
        environment.maxbandwidth=new double[environmentin.maxbandwidth.length][environmentin.maxbandwidth[0].length];
        for(int x=0;x<environmentin.maxbandwidth.length;x++)
        {
            for(int y=0;y<environmentin.maxbandwidth[0].length;y++)
            {
                environment.maxbandwidth[x][y]=environmentin.maxbandwidth[x][y]*bandscal;
            }
        }
        environment.vmprice = environmentin.vmprice;
        environment.vmlocationvapl = new HashMap<>();
        environment.vmlocationvapl.put(1,0);
        environment.vmlocationvapl.put(2,environmentin.pedgenum);
        environment.vmlocationvapl.put(3,environmentin.pedgenum+environmentin.edgenum);
        environment.list = new ArrayList<>();
        environment.list.addAll(list);
        environment.vmrenthistory=new HashMap<>();
        environment.init2();
        environment.setTasknum(tasknum);
        environment.setPtpercentage(percentage);
        environment.head = list.get(0);
        environment.tail = list.get(list.size() - 1);
        this.respath=respath;
        environment.createlocalvms(localvms);
        long st=System.nanoTime();
        execute();
        long et=System.nanoTime();
        FileWriter fw = new FileWriter(respath, true);
        fw.write(tasknum + " " + Arrays.toString(percentage) + " "+instance+" "+bandscal+" "+localscale+" "+(et-st)+" "+environment.tail.getFinishtime());
        fw.write("\r\n");//换行
        fw.flush();
        fw.close();
        environment.clearvmhistory();
    }
    public void execute()
    {

        int num=environment.list.size();
        while(num>0)
        {
            for(Task i: environment.list)
            {
                if(i.getVmId()!=-1) continue;
                int ds=i.getPrivacy_level()==2?1:2;
                environment.destoryVm(i.getDepth());
                if (i.getCloudletId() == environment.head.getCloudletId()) {
                    i.setVmId(0);
                    i.setStarttime(0.0);
                    i.setFinishtime(0.0);
                    environment.allVmList.get(0).setEarlyidletime(0.0);num--;
                } else {
                    boolean flag=true;
                    for(Task task:i.getParentList())
                    {
                        if(task.getVmId()==-1)
                        {
                            flag=false;break;
                        }
                    }
                    if(!flag) continue;
                    if (i.getCloudletId() == environment.tail.getCloudletId()) {
                        i.setVmId(0);
                        double MaxFT = -1;
                        for (Task j : i.getParentList()) {
                            MaxFT = Math.max(MaxFT, j.getFinishtime());
                        }
                        i.setStarttime(MaxFT);
                        i.setFinishtime(MaxFT);
                        environment.allVmList.get(0).setEarlyidletime(MaxFT);num--;
                    }else{
                        double min=Double.MAX_VALUE;int keyvmid=0;
                        for(int datacenterid=0;datacenterid<=environment.vmlocationvapl.get(i.getPrivacy_level());datacenterid++)
                        {
                            for(Vm vm:environment.curVmList.get(datacenterid))
                            {
                                double t=environment.ComputeTaskFinishTime(i,vm.getId());
                                if(min>t)
                                {
                                    min=t;keyvmid=vm.getId();
                                }
                            }
                        }
                        int kdatacenterid=0;int kcpucore=0;
                        for(int datacenterid=ds;datacenterid<=environment.vmlocationvapl.get(i.getPrivacy_level());datacenterid++)
                        {
                            int[] cpucores=new int[]{4,2,1};
                            for(int cpucore:cpucores)
                            {
                                if(environment.datacenterList.get(datacenterid).getUseablecores()>=cpucore)
                                {
                                    double starttime=i.gettaskEarlyStartTime();
                                    double processtime=0;
                                    if(i.getParentList().size()==1&&i.getParentList().get(0).getCloudletId()==environment.head.getCloudletId())
                                    {
                                        double datasize=i.getFileList().stream().filter(item -> Parameters.FileType.INPUT==item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).sum();
                                        processtime=(datasize)/10+(i.getCloudletLength()*1.0)/(cpucore*environment.datacenterList.get(datacenterid).getMibps());
                                    }
                                    else{
                                        for(Task pre:i.getParentList())
                                        {
                                            double tempfile=0;
                                            for(FileItem f1:i.getFileList())
                                            {
                                                for(FileItem f2:pre.getFileList())
                                                {
                                                    if(f1.getName().equals(f2.getName())&&f1.getType()==Parameters.FileType.INPUT&&f2.getType()==Parameters.FileType.OUTPUT)
                                                    {
                                                        tempfile+=f1.getSize();
                                                    }
                                                }
                                            }
                                            processtime=Math.max(processtime,(tempfile)/environment.bandwidth[environment.allVmList.get(pre.getVmId()).getDatacenterid()][datacenterid]+(i.getCloudletLength()*1.0)/(cpucore*environment.datacenterList.get(datacenterid).getMibps()));
                                        }
                                    }
                                    double ft2=starttime+processtime;
                                    if(ft2<min)
                                    {
                                        min=ft2;
                                        keyvmid=-1;
                                        kdatacenterid=datacenterid;kcpucore=cpucore;break;
                                    }
                                }
                            }
                        }
                        if(keyvmid!=-1)
                        {
                            environment.updateTaskShcedulingInformation(i,keyvmid,min);
                        }
                        else{
                            int id=environment.createvm(kcpucore,kdatacenterid,min,i.gettaskEarlyStartTime());
                            environment.allVmList.get(id).setDestoryTime((Math.ceil((min-i.gettaskEarlyStartTime())/environment.BTU))*environment.BTU+i.gettaskEarlyStartTime());
                            environment.updateTaskShcedulingInformation(i,id,min);
                        }
                        num--;
                    }
                }
            }
        }

    }
}
