package org.workflowsim;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.File;
import org.workflowsim.algorithms.*;
import org.workflowsim.utils.Parameters;

import java.util.*;

public class myalg {
    public Environment environment=new Environment();
    public static void main(String[] args) {
        String s="s";
        myalg z=new myalg("F:/WorkflowSim-1.0-master/config/dax/Montage_50.xml",s,s,s,s,s,s,1.2,0.1,300,new double[]{0.2,0.3,0.5});
    }

    public int createvm(int vmcpucores,int datacenterid,double earlidletime)
    {
        Vm kvm=new Vm(vmcpucores,datacenterid,environment.vmid++,earlidletime,vmcpucores*environment.datacenterList.get(datacenterid).getMibps());
        environment.allVmList.add(kvm);
        environment.curVmList.get(datacenterid).add(kvm);
        environment.vmrenthistory.put(kvm.getId(),new ArrayList<>());
        return kvm.getId();
    }
    myalg(String path,String SDM,String TRM,String MPLTSM1,String MPLTSM2,String LPLTSM1,String LPLTSM2,double dealinefactor,double localvmfactor,int tasknum,double[] ptpercentage)
    {
        init(environment);
        String s="s";
        execute(path,SDM,TRM,MPLTSM1,MPLTSM2,LPLTSM1,LPLTSM2,dealinefactor,localvmfactor,tasknum,ptpercentage);
    }
    void execute(String path,String SDM,String TRM,String MPLTSM1,String MPLTSM2,String LPLTSM1,String LPLTSM2,double dealinefactor,double localvmfactor,int tasknum,double[] ptpercentage)
    {
        myparser workflowParser=new myparser(path,new myreplicalog());
        workflowParser.parse();
        List<Task> list=workflowParser.getTaskList();
        Task headtask=new Task(list.get(list.size()-1).getCloudletId()+1,0);
        Task tailtask=new Task(list.get(list.size()-1).getCloudletId()+2,0);
        headtask.setDepth(0);headtask.setPrivacy_level(3);tailtask.setPrivacy_level(3);
        list.sort(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o1.getDepth() - o2.getDepth();
            }
        });
        tailtask.setDepth(list.get(list.size()-1).getDepth()+1);
        for(Task i:list)
        {
            if(i.getParentList().size()==0) {
                headtask.addChild(i);i.addParent(headtask);
            }
            else if(i.getChildList().size()==0) {
                tailtask.addParent(i);i.addChild(tailtask);
            }
        }
        list.add(0,headtask);
        list.add(tailtask);
        esttaskexuteTime(list);
        double deadline=dealinefactor*caltaskestearlystarttime(list);
        caltaskestlatestfinTime(list,deadline);
        calrankavg(list);
        baseSDM baseSDM=new SDMDepthPLSum();
        baseSDM.Settaskssubdeadline(list,deadline);
        baseTRM baseTRM=new TRMMaxRankavg();
        baseTRM.RankTasks(list);
        for(Task i:list)
        {
            if(i.getPrivacy_level()==1)
            {

            }
        }
        /**
         * calculate the max local vm nums
         */

    }
    void init(Environment environment)
    {
        environment.vmprice.put("edge1",0.031);
        environment.vmprice.put("edge2",0.052);
        environment.vmprice.put("edge4",0.208);
        environment.vmprice.put("cloud1",0.0255);
        environment.vmprice.put("cloud2",0.0336);
        environment.vmprice.put("cloud4",0.1344);
        environment.vmprice.put("pedge1",0.031*1.5);
        environment.vmprice.put("pedge2",0.052*1.5);
        environment.vmprice.put("pedge4",0.208*1.5);
        environment.maxspeed.put(1,400.0);
        environment.maxspeed.put(2,12400.0);
        environment.maxspeed.put(3,20000.0);
        environment.vmlocationvapl.put(1,1);
        environment.vmlocationvapl.put(2,2);environment.vmlocationvapl.put(3,5);
        Datacenter datacenter_0=new Datacenter(0,200,0,"Datacenter_0",new ArrayList<>(),1);
        Datacenter datacenter_1=new Datacenter(12,3100,1,"Datacenter_1",new ArrayList<>(),2);
        Datacenter datacenter_2=new Datacenter(12,3100,2,"Datacenter_2",new ArrayList<>(),3);
        Datacenter datacenter_3=new Datacenter(12,3100,3,"Datacenter_3",new ArrayList<>(),3);
        Datacenter datacenter_4=new Datacenter(3000,5000,4,"Datacenter_4",new ArrayList<>(),3);
        environment.bandwidth=new long[5][5];
        for(int i=0;i<5;i++)
        {
            environment.bandwidth[i][i]=80;
            if(i==0){
                environment.bandwidth[i][1]=environment.bandwidth[1][i]=Misc.randomInt(50,60);environment.bandwidth[i][2]=environment.bandwidth[2][i]=Misc.randomInt(50,60);environment.bandwidth[i][3]=environment.bandwidth[3][i]=Misc.randomInt(50,60);environment.bandwidth[i][4]=environment.bandwidth[4][i]=30;
                }
            else if(i==1)
            {
                environment.bandwidth[i][2]=environment.bandwidth[2][i]=Misc.randomInt(60,65);environment.bandwidth[i][3]=environment.bandwidth[3][i]=Misc.randomInt(60,65);environment.bandwidth[i][4]=environment.bandwidth[4][i]=Misc.randomInt(40,50);
            }
            else if(i==2)
            {
                environment.bandwidth[i][3]=environment.bandwidth[3][i]=Misc.randomInt(60,65);environment.bandwidth[i][4]=environment.bandwidth[4][i]=Misc.randomInt(40,50);
            }
            else if(i==3){
                environment.bandwidth[i][4]=environment.bandwidth[4][i]=Misc.randomInt(40,50);
            }
        }

    }
    void esttaskexuteTime(List<Task> list)
    {
        for(Task i:list)
        {
            if(i.getParentList().size()==0)
            {
                i.setEstextTime(0);
            }
            else if(i.getParentList().size()==1&&i.getParentList().get(0).getDepth()==0)
            {
                double datasize=i.getFileList().stream().filter(item -> Parameters.FileType.INPUT==item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).max().orElse(0);
                i.setEstextTime((datasize)/10+i.getCloudletLength()/environment.maxspeed.get(i.getPrivacy_level()));
            }
            else{
                double maxfilesize=0;
                double[] tempin=new double[i.getParentList().size()];
                /**
                 * We set the bandwidth within a Datacenter is 80Mbps,which is the max bandwidth,
                 * So,Wherever the task is scheduled,the estbandwidth is 80.
                 */
                for(FileItem j:i.getFileList())
                {
                    if(j.getType()==Parameters.FileType.INPUT)
                    {
                        for(int z=0;z<i.getParentList().size();z++)
                        {
                            if(i.getParentList().get(z).getFileList().stream().anyMatch(item -> item.getName().equals(j.getName())&&item.getType()==Parameters.FileType.OUTPUT))
                            {
                                tempin[z]+=j.getSize();
                            }
                        }
                    }
                }
                maxfilesize= Arrays.stream(tempin).max().orElse(0);
                i.setEstextTime((maxfilesize)/10+i.getCloudletLength()/environment.maxspeed.get(i.getPrivacy_level()));
            }
        }
    }
    double caltaskestearlystarttime(List<Task> list)
    {
        int unhandledtasknum=list.size();double max=-1;
        while(unhandledtasknum>0)
        {
            for(Task i:list)
            {
                if(i.getParentList().size()==0)
                {
                    i.setEsttaskearlystartTime(0);
                    i.setEsttaskeralyfinTime(i.getEstextTime());
                    unhandledtasknum--;
                    max=Math.max(max,0);
                }
                else if(i.getParentList().size()!=0){
                    boolean flag=true;double early=-1;
                    for(Task j:i.getParentList())
                    {
                        if(j.getEsttaskearlystartTime()!=-1)
                        {
                            early=Math.max(early,j.getEsttaskearlystartTime()+j.getEstextTime());
                        }
                        else{
                            flag=false;
                            break;
                        }
                    }
                    if(flag)
                    {
                        i.setEsttaskearlystartTime(early);
                        i.setEsttaskeralyfinTime(early+i.getEstextTime());
                        unhandledtasknum--;
                        max=Math.max(max,early);
                    }
                }
            }
        }
        return max;
    }
    void caltaskestlatestfinTime(List<Task> list,double deadline)
    {
        int unhandledtasknum=list.size();
        while(unhandledtasknum>0)
        {
            for(int x=list.size()-1;x>=0;x--)
            {
                Task i=list.get(x);
                if(i.getChildList().size()==0)
                {
                    i.setEsttasklatestfinTime(deadline);
                    i.setEsttaskearlystartTime(i.getEsttasklatestfinTime()-i.getEstextTime());
                    unhandledtasknum--;
                }
                else if(i.getChildList().size()!=0){
                    boolean flag=true;double latest=Double.MAX_VALUE;
                    for(Task j:i.getChildList())
                    {
                        if(j.getEsttasklateststartTime()!=-1)
                        {
                            latest=Math.min(latest,j.getEsttasklatestfinTime()-j.getEstextTime());
                        }
                        else{
                            flag=false;
                            break;
                        }
                    }
                    if(flag)
                    {
                        i.setEsttasklatestfinTime(latest);
                        i.setEsttasklateststartTime(latest-i.getEstextTime());
                        unhandledtasknum--;
                    }
                }
            }
        }
    }
    void calrankavg(List<Task> list)
    {
        Map<Task, Double> rankprivacy=new HashMap<>();
        Map<Task, Double> rankup=new HashMap<>();
        int num=list.size();
        while (num>0)
        {
            for(int i=list.size()-1;i>=0;i--)
            {
                if(rankup.get(list.get(i))==null){
                    if(list.get(i).getChildList().size()==0)
                    {
                        rankprivacy.put(list.get(i),1.0/list.get(i).getPrivacy_level());
                        rankup.put(list.get(i),0.0);
                        num--;
                    }
                    else{
                        double plsum=0;double upmax=-1;boolean flag=true;
                        for(Task j:list.get(i).getChildList())
                        {
                            if(rankup.get(j)!=null&&rankprivacy.get(j)!=null)
                            {
                                if(upmax<rankup.get(j))
                                {
                                    upmax=rankup.get(j);
                                }
                                plsum+=(1.0/rankprivacy.get(j));}
                            else {
                                flag=false;
                                break;
                            }
                        }
                        if(flag)
                        {
                            double t1=upmax+list.get(i).getEstextTime();
                            double t2=plsum/list.get(i).getChildList().size()+1.0/list.get(i).getPrivacy_level();
                            rankup.put(list.get(i),t1);
                            rankprivacy.put(list.get(i),t2);
                            list.get(i).setRankavg((t1+t2)/2);
                            num--;
                        }
                    }
                }
            }
        }
    }
    void clearvmhistory()
    {
        environment.vmid=0;
        environment.vmrenthistory=new HashMap<>();
        for(Datacenter i: environment.datacenterList) i.setVms(new ArrayList<>());
        environment.curVmList=new ArrayList<>();
        environment.allVmList=new ArrayList<>();
    }
    void createlocalvms(int n)
    {
        int lvmn=(int) Math.floor((n/3.0)*2);
        int hvmn=n-lvmn;
        for(int i=0;i<lvmn;i++) createvm(1,0,0);
        for(int i=0;i<hvmn;i++) createvm(2,0,0);
    }
    double calculateprices()
    {
        return 0;
    }
    void updateTaskShcedulingInformation(Task t,Vm vm)
    {
        t.setVmId(vm.getId());

    }
    void updatetaskearliestlateststartTime(Task t)
    {
        for(Task j:t.getChildList())
        {
            if(t.getVmId()!=-1)
            {
                j.setEsttaskearlystartTime(Math.max(j.getEsttaskearlystartTime(),t.getFinishtime()));
            }
            else j.setEsttaskearlystartTime(Math.max(j.getEsttaskearlystartTime(),t.getEsttaskearlystartTime()));
            j.setEsttasklateststartTime(j.getEsttaskearlystartTime()+j.getEstextTime());
        }
    }
//    void subdeadlineupdate(Task t,String SDM)
//    {
//        if(SDM.equals("SDMExecutiontimePrecent"))
//        {
//            for(Task j:t.getChildList())
//            {
//                if(t.getVmId()!=-1)
//                {
//                    j.setEsttaskearlystartTime(Math.max(j.getEsttaskearlystartTime(),t.getFinishtime()));
//                    j.setSubdeadline();
//                }
//                else j.setEsttaskearlystartTime(Math.max(j.getEsttaskearlystartTime(),t.getEsttaskearlystartTime()));
//                j.setEsttasklateststartTime(j.getEsttaskearlystartTime()+j.getEstextTime());
//            }
//        }
//    }

}



