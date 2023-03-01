package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.FileItem;
import org.workflowsim.Task;
import org.workflowsim.Vm;
import org.workflowsim.utils.Parameters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class HEFT {
    Environment environment;
    Map<Task, Double> rankup = new HashMap<>();
    String respath;double deadlinefactor;
    HEFT(List<Task> list, int tasknum, String respath, Environment environmentin, double deadlinefactor, double[] percentage, double deadline, int instance) throws IOException {
        this.environment = new Environment();
        environment.pedgenum = environmentin.pedgenum;
        environment.edgenum = environmentin.edgenum;
        environment.maxbandwidth = environmentin.maxbandwidth;
        environment.maxspeed = environmentin.maxspeed;
        environment.vmprice = environmentin.vmprice;
        environment.vmlocationvapl = environmentin.vmlocationvapl;
        environment.bandwidth = environmentin.bandwidth;
        environment.list = new ArrayList<>();
        environment.list.addAll(list);
        environment.vmrenthistory=new HashMap<>();
        environment.init2();
        environment.deadline = deadline;
        environment.setTasknum(tasknum);
        environment.setPtpercentage(percentage);
        environment.head = list.get(0);
        environment.tail = list.get(list.size() - 1);
        this.deadlinefactor=deadlinefactor;this.respath=respath;
        execute();
        FileWriter fw = new FileWriter(respath, true);
        fw.write(tasknum + " " + Arrays.toString(percentage) + " " + deadlinefactor +" "+instance+ " " +deadline+" "+environment.tail.getFinishtime());
        fw.write("\r\n");//换行
        fw.flush();
        fw.close();
        environment.clearvmhistory();
    }
    HEFT(Environment environmentin)
    {
        this.environment = environmentin;
        this.deadlinefactor=environmentin.deadline;
        execute();
    }
    public void execute()
    {
        for(Task i: environment.list)
        {
            int ds=i.getPrivacy_level()==2?1:2;
            environment.destoryVm(i.getDepth());
            if (i.getCloudletId() == environment.head.getCloudletId()) {
                i.setVmId(0);
                i.setStarttime(0.0);
                i.setFinishtime(0.0);
                environment.allVmList.get(0).setEarlyidletime(0.0);
            } else if (i.getCloudletId() == environment.tail.getCloudletId()) {
                i.setVmId(0);
                double MaxFT = -1;
                for (Task j : i.getParentList()) {
                    MaxFT = Math.max(MaxFT, j.getFinishtime());
                }
                i.setStarttime(MaxFT);
                i.setFinishtime(MaxFT);
                environment.allVmList.get(0).setEarlyidletime(MaxFT);
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
                                    processtime=(tempfile)/environment.bandwidth[environment.allVmList.get(pre.getVmId()).getDatacenterid()][datacenterid]+(i.getCloudletLength()*1.0)/(cpucore*environment.datacenterList.get(datacenterid).getMibps());
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
            }
        }
    }
}
