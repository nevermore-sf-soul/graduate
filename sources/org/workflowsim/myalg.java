package org.workflowsim;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.File;
import org.workflowsim.algorithms.*;
import org.workflowsim.utils.Parameters;

import java.util.*;

public class myalg {
    public Environment environment;
    public static void main(String[] args) {
        String s="s";
        Environment environment=new Environment();
        environment.init();
        myalg z=new myalg("F:/WorkflowSim-1.0-master/config/dax/Montage_50.xml",s,s,s,s,s,s,1.2,0.1,300,new double[]{0.2,0.3,0.5},environment);
    }


    myalg(String path,String SDM,String TRM,String MPLTSM1,String MPLTSM2,String LPLTSM1,String LPLTSM2,double dealinefactor,double localvmfactor,int tasknum,double[] ptpercentage,Environment environmentin)
    {
        this.environment=environmentin;
        String s="s";
        environment.setPath(path);environment.setSDM(SDM);
        environment.setTRM(TRM);environment.setMPLTSM1(MPLTSM1);environment.setMPLTSM2(MPLTSM2);
        environment.setLPLTSM1(LPLTSM1);environment.setLPLTSM1(LPLTSM2);
        environment.setDealinefactor(dealinefactor);
        environment.setLocalvmfactor(localvmfactor);
        environment.setTasknum(tasknum);environment.setPtpercentage(ptpercentage);
        execute();
        environmentin.clearvmhistory();
    }
    void execute()
    {
        myparser workflowParser=new myparser(environment.getPath(),new myreplicalog());
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

        /**
         * 确定本地虚拟机数目
         */
        int x=getlocalvmnums();
        int lvmn=(int) Math.floor((x/3.0)*2);
        int hvmn=x-lvmn;
        environment.datacenterList.get(0).setCpucores(lvmn+hvmn*2);
        environment.createlocalvms(x);
        /**
         * 估计任务执行时间
         */
        esttaskexuteTime();
        /**
         * 确定工作流合理截止期，估计任务最早开始时间、最早结束时间
         */
        environment.deadline= environment.getDealinefactor()*caltaskestearlystarttime();
        /**
         *计算任务最晚结束时间、最晚开始时间
         */
        caltaskestlatestfinTime();
        /**
         * 如果任务排序使用了任务的rank或者在子截止期划分中使用了rank，则计算rank
         */
        if(environment.SDM.equals("SDMPathPLSum")||environment.TRM.equals("TRMMaxRankavg"))
        calrankavg();
        /**
         * 如果子截止期划分使用了隐私等级和，则计算
         */
        if(environment.SDM.equals("SDMPathPLSum"))
        {
            environment.plsum=calplsum();
        }
        /**
         * 进行子截止期划分
         */
        baseSDM baseSDM=new SDMPathPLSum();
        baseSDM.Settaskssubdeadline(environment);
        /**
         * 进行任务排序
         */
        baseTRM baseTRM=new TRMMaxRankavg();
        baseTRM.RankTasks(environment);
        /**
         * 进行任务调度
         */
        for(Task i:list)
        {
            if(i.getPrivacy_level()==1)
            {
                HighPrivacyTaskScheduling highPrivacyTaskScheduling=new HighPrivacyTaskScheduling(environment,i);
            }
            else if(i.getPrivacy_level()==2)
            {

            }
        }
        /**
         * calculate the max local vm nums
         */

    }
    int getlocalvmnums()
    {


        return 0;
    }
    void esttaskexuteTime()
    {
        List<Task> list=environment.list;
        for(Task i:list)
        {
            if(i.getParentList().size()==0)
            {
                i.setEstextTime(0);
            }
            else if(i.getParentList().size()==1&&i.getParentList().get(0).getDepth()==0)
            {
                double datasize=i.getFileList().stream().filter(item -> Parameters.FileType.INPUT==item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).sum();
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
    double caltaskestearlystarttime()
    {
        List<Task> list=environment.list;
        int unhandledtasknum=list.size();double max=-1;
        while(unhandledtasknum>0)
        {
            for(Task i:list)
            {
                if(i.getParentList().size()==0)
                {
                    i.settaskEarlyStartTime(0);
                    i.settaskEralyFinTime(0);
                    unhandledtasknum--;
                    max=Math.max(max,0);
                }
                else if(i.getParentList().size()!=0){
                    boolean flag=true;double early=-1;
                    for(Task j:i.getParentList())
                    {
                        if(j.gettaskEarlyStartTime()!=-1)
                        {
                            early=Math.max(early,j.gettaskEarlyStartTime()+j.getEstextTime());
                        }
                        else{
                            flag=false;
                            break;
                        }
                    }
                    if(flag)
                    {
                        i.settaskEarlyStartTime(early);
                        i.settaskEralyFinTime(early+i.getEstextTime());
                        unhandledtasknum--;
                        max=Math.max(max,i.gettaskEralyFinTime());
                    }
                }
            }
        }
        return max;
    }
    void caltaskestlatestfinTime()
    {
        List<Task> list=environment.list;double deadline=environment.deadline;
        int unhandledtasknum=list.size();
        while(unhandledtasknum>0)
        {
            for(int x=list.size()-1;x>=0;x--)
            {
                Task i=list.get(x);
                if(i.getChildList().size()==0)
                {
                    i.settaskLatestFinTime(deadline);
                    i.settaskLatestStartTime(i.gettaskLatestFinTime()-i.getEstextTime());
                    unhandledtasknum--;
                }
                else if(i.getChildList().size()!=0){
                    boolean flag=true;double latest=Double.MAX_VALUE;
                    for(Task j:i.getChildList())
                    {
                        if(j.gettaskLatestStartTime()!=-1)
                        {
                            latest=Math.min(latest,j.gettaskLatestFinTime()-j.getEstextTime());
                        }
                        else{
                            flag=false;
                            break;
                        }
                    }
                    if(flag)
                    {
                        i.settaskLatestFinTime(latest);
                        i.settaskLatestStartTime(latest-i.getEstextTime());
                        unhandledtasknum--;
                    }
                }
            }
        }

    }
    void calrankavg()
    {
        List<Task> list=environment.list;
        Map<Task, Double> rankprivacy=new HashMap<>();
        Map<Task, Double> rankup=new HashMap<>();
        int num=list.size();
        double[] ranks1=new double[list.size()];
        double[] ranks2=new double[list.size()];
        while (num>0)
        {
            for(int i=list.size()-1;i>=0;i--)
            {
                if(rankup.get(list.get(i))==null){
                    if(list.get(i).getChildList().size()==0)
                    {
                        rankprivacy.put(list.get(i),0.0);
                        rankup.put(list.get(i),0.0);
                        ranks1[i]=0;ranks2[i]=0;
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
                                plsum+=(1.0/j.getPrivacy_level());}
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
                            ranks1[i]=t1;ranks2[i]=t2;
                            num--;
                        }
                    }
                }
            }
        }
        double mean1 = StatUtils.mean(ranks1);
        double mean2 = StatUtils.mean(ranks2);
        for ( int i=0;i<ranks1.length;i++ ) {
            ranks1[i]=ranks1[i]/mean1;
            ranks2[i]=ranks2[i]/mean2;
            list.get(i).setRankavg((ranks1[i]+ranks2[i])/2);
        }
    }
    int[] calplsum()
    {
        List<Task> list=environment.list;
        int[] plsum=new int[list.get(list.size()-1).getDepth()+1];
        for(Task i:list)
        {
            plsum[i.getDepth()]+=1.0/i.getPrivacy_level();
        }
        double totalpl= Arrays.stream(plsum).sum();
        double pre=0;
        for(int i=0;i<plsum.length;i++)
        {
            plsum[i]+=pre;
            pre=plsum[i];
        }
        return plsum;
    }
}



