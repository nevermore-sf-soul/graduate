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
        environment.edgenum=5;environment.pedgenum=2;
        environment.init();
        String[] SDM=new String[]{"SDMDepthPLSum","SDMPathPLSum","SDMExecutiontimePercent"};
        String[] TRM=new String[]{"TRMMaxRankavg" ,"TRMMinFloatTime","TRMTaskFeature"};
        String[] LPLTSMLocal=new String[]{"TSMLocalMinWaste","TSMLocalEarlyAvaiableTime" ,"TSMLocalEarlyFinishTime"};
        String[] LPLTSMUsingExistingVm=new String[]{"TSMUsingExistingVmFirstAdaptSTB","TSMUsingExistingVmLongestSTB","TSMUsingExistingVmShortestSTB"};
        int[] tasknums=new int[]{500,1000,2000,3000,4000};
        double[] deadlinefactors=new double[]{1.5,1.6,1.7,1.8,1.9};
        double[][] privacytaskpercent=new double[][]{{0.05,0.15,0.8},{0.1,0.2,0.7},{0.15,0.25,0.55},{0.2,0.3,0.5}};
        String[] workflowtype=new String[]{"CyberShake","Montage","Genome","Inspiral","Sipht"};
        myalg z=new myalg("F:/WorkflowSim-1.0-master/config/dax/Montage_300.xml",SDM[0],TRM[2],"TSMLocalMinWaste","TSMUsingExistingVmLongestSTB","TSMLocalEarlyFinishTime","TSMUsingExistingVmFirstAdaptSTB",1.5,300,new double[]{0.2,0.3,0.5},environment);
    }

    myalg(String path,String SDM,String TRM,String LPLTSMLocal,String LPLTSMUsingExistingVm,String NPLTSMLocal,String NPLTSMUsingExistingVm,double dealinefactor,int tasknum,double[] ptpercentage,Environment environmentin)
    {

        this.environment=environmentin;
        String s="s";
        environment.setPath(path);environment.setSDM(SDM);
        environment.setTRM(TRM);environment.setLPLTSMLocal(LPLTSMLocal);environment.setLPLTSMUsingExistingVm(LPLTSMUsingExistingVm);
        environment.setNPLTSMLocal(NPLTSMLocal);environment.setNPLTSMUsingExistingVm(NPLTSMUsingExistingVm);
        environment.setDealinefactor(dealinefactor);
        environment.setTasknum(tasknum);environment.setPtpercentage(ptpercentage);
        execute();
        environmentin.clearvmhistory();
    }
    void execute()
    {
        myparser workflowParser=new myparser(environment.getPath(),new myreplicalog());
        workflowParser.parse();
        List<Task> list=workflowParser.getTaskList();
        environment.list=list;
        HighPrivacyTaskScheduling highPrivacyTaskScheduling=new HighPrivacyTaskScheduling(environment);
        LNPrivacyTaskScheduling lnPrivacyTaskScheduling=new LNPrivacyTaskScheduling(environment);
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
        environment.head=headtask;environment.tail=tailtask;
        /**
         * 估计任务执行时间
         */
        esttaskexuteTime();
        /**
         * 确定工作流合理截止期，估计任务最早开始时间、最早结束时间
         */
        environment.deadline= environment.getDealinefactor()*caltaskestearlystarttime();
        /**
         * 确定本地虚拟机数目
         */

        environment.createlocalvms();
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
        if(environment.SDM.equals("SDMDepthPLSum"))
        {
            environment.plsum=calplsum();
        }
        /**
         * 进行子截止期划分
         */
        baseSDM baseSDM=null;
        switch (environment.SDM) {
            case "SDMPathPLSum" -> {
                baseSDM = new SDMPathPLSum();
            }
            case "SDMDepthPLSum" -> {
                baseSDM = new SDMDepthPLSum();
            }
            case "SDMExecutiontimePercent" -> {
                baseSDM = new SDMExecutiontimePrecent();
            }
        }
        if(baseSDM==null) throw new IllegalArgumentException("SDM method is not determined!");
        baseSDM.Settaskssubdeadline(environment);
        /**
         * 进行任务排序
         */
        baseTRM baseTRM=null;
        switch (environment.TRM) {
            case "TRMMaxRankavg" -> {
                baseTRM= new TRMMaxRankavg();
            }
            case "TRMMinFloatTime" -> {
                baseTRM = new TRMMinFloatTime();
            }
            case "TRMTaskFeature" -> {
                baseTRM = new TRMTaskFeature();
            }
        }
        if(baseTRM==null) throw new IllegalArgumentException("TRM method is not determined!");
        baseTRM.RankTasks(environment);
        /**
         * 进行任务调度
         */
        for(Task i:list)
        {
            environment.destoryVm(i.getDepth());
            if(i.getCloudletId()==environment.head.getCloudletId())
            {
                i.setVmId(0);
                i.setStarttime(0.0);
                i.setFinishtime(0.0);
                environment.allVmList.get(0).setEarlyidletime(0.0);
            }
            else if(i.getCloudletId()==environment.tail.getCloudletId())
            {
                i.setVmId(0);
                double MaxFT=-1;
                for(Task j:i.getParentList())
                {
                    MaxFT=Math.max(MaxFT,j.getFinishtime());
                }
                i.setStarttime(MaxFT);i.setFinishtime(MaxFT);
                environment.allVmList.get(0).setEarlyidletime(MaxFT);
            }
            else{
                if(i.getPrivacy_level()==1)
                {
                    highPrivacyTaskScheduling.execute(i);
                }
                else
                {
                    lnPrivacyTaskScheduling.execute(i);
                }
            }
        }
        /**
         * calculate the max local vm nums
         */
        System.out.println(environment.deadline);
            for(Task i:list)
            {
                if(i.getCloudletId()==headtask.getCloudletId()||i.getCloudletId()==tailtask.getCloudletId()) continue;
                System.out.println("Task"+i.getCloudletId()+" is scheduled to VM"+i.getVmId()+" from "+i.getStarttime()+" to "+i.getFinishtime());
            }
        System.out.println("totalfee "+environment.calculateprices());

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
                int pre=-1;
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
                for(int x=0;x<tempin.length;x++)
                {
                    if(tempin[x]>=maxfilesize)
                    {
                        maxfilesize=tempin[x];
                        pre=x;
                    }
                }
                if(maxfilesize!=0)
                i.setEstextTime((maxfilesize)/environment.maxbandwidth[i.getPrivacy_level()][i.getParentList().get(pre).getPrivacy_level()]+i.getCloudletLength()/environment.maxspeed.get(i.getPrivacy_level()));
                else i.setEstextTime(0);
            }
        }
    }
    double caltaskestearlystarttime()
    {
        List<Task> list=environment.list;
        int unhandledtasknum=list.size();
        double max=-1;
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
                                if(plsum<rankprivacy.get(j))
                                plsum=(rankprivacy.get(j));
                            }
                            else {
                                flag=false;
                                break;
                            }
                        }
                        if(flag)
                        {
                            double t1=upmax+list.get(i).getEstextTime();
                            double t2=plsum+1.0/list.get(i).getPrivacy_level();
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



