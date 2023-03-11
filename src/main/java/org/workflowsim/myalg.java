package org.workflowsim;

import org.apache.commons.math3.stat.StatUtils;
import org.workflowsim.algorithms.*;
import org.workflowsim.utils.Parameters;
import simulation.generator.Generator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class myalg {
    public Environment environment;
    public static ThreadPoolExecutor threadPoolExecutor1=new ThreadPoolExecutor(5, 10,
            60, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            new ThreadPoolExecutor.CallerRunsPolicy());
    public static ThreadPoolExecutor threadPoolExecutor2=new ThreadPoolExecutor(15, 20,
            5, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(),
            new ThreadPoolExecutor.CallerRunsPolicy());
    public double prefee,afterfee;public double localscale;
    String[] SDM = new String[]{"SDMDepthPLSum", "SDMPathPLSum", "SDMExecutiontimePercent"};
    String[] TRM = new String[]{"TRMMaxRankavg", "TRMMinFloatTime", "TRMTaskFeature"};
    String[] LPLTSMLocal = new String[]{"TSMLocalMinWaste", "TSMLocalEarlyAvaiableTime", "TSMLocalEarlyFinishTime"};
    String[] LPLTSMUsingExistingVm = new String[]{"TSMUsingExistingVmFirstAdaptSTB", "TSMUsingExistingVmLongestSTB", "TSMUsingExistingVmShortestSTB"};

    public static void main(String[] args) throws Exception {
        Environment environment2 = new Environment();
        environment2.edgenum = 5;
        environment2.pedgenum = 2;
        environment2.init1();
        int[] tasknums = new int[]{150,200,250,300};
        double[] deadlinefactors = new double[]{1.5, 1.6, 1.7, 1.8, 1.9};
        double[][] privacytaskpercent = new double[][]{{0.05, 0.15, 0.8}, {0.1, 0.2, 0.7}, {0.15, 0.25, 0.55}, {0.2, 0.3, 0.5}};
        double[] localscal=new double[]{0.1,0.2,0.3,0.4};
        String[] workflowtype = new String[]{"CyberShake",  "Montage","Genome", "Inspiral", "Sipht"};
        String prefix = "F:/benchmark/data/";
//        CountDownLatch countDownLatch=new CountDownLatch(tasknums.length*privacytaskpercent.length*10);
//        for (int i = 0; i < tasknums.length; i++) {
//            for (int o = 0; o < privacytaskpercent.length; o++) {
//                for (int ins = 0; ins < 10; ins++) {
//                        String datapath = new String(prefix + workflowtype[1]+" " + tasknums[i] + " [" + privacytaskpercent[o][0] + "," + privacytaskpercent[o][1] + "," + privacytaskpercent[o][2] +  "]"+" "+ ins +".xml");
//                        generatethread generatethread=new generatethread(datapath, tasknums[i], privacytaskpercent[o], workflowtype[1],countDownLatch);
//                        threadPoolExecutor1.execute(generatethread);
//                }
//            }
//        }
//        countDownLatch.await();
//        threadPoolExecutor1.shutdownNow();
//        ReentrantLock reentrantLock=new ReentrantLock();
//        for (int i = 0; i < tasknums.length; i++) {
//            for (double[] doubles : privacytaskpercent) {
//                for (int ins = 0; ins < 10; ins++) {
//                    threadTest threadTest = new threadTest(tasknums[i], ins, doubles, workflowtype[1], environment2,reentrantLock);
//                    threadPoolExecutor2.execute(threadTest);
//                }
//            }
//        }

        CountDownLatch countDownLatch=new CountDownLatch(tasknums.length*privacytaskpercent.length*10*workflowtype.length);
        for (int i = 0; i < tasknums.length; i++) {
            for(int j=0;j<workflowtype.length;j++)
            {
            for (int o = 0; o < privacytaskpercent.length; o++) {
                for (int ins = 0; ins < 10; ins++) {
                    String datapath = new String(prefix + workflowtype[j]+" " + tasknums[i] + " [" + privacytaskpercent[o][0] + "," + privacytaskpercent[o][1] + "," + privacytaskpercent[o][2] +  "]"+" " + ins +".xml");
                    generatethread generatethread=new generatethread(datapath, tasknums[i], privacytaskpercent[o], workflowtype[j],countDownLatch);
                    threadPoolExecutor1.execute(generatethread);
                    }
                }
        }
            }
        countDownLatch.await();
        threadPoolExecutor1.shutdownNow();
        ReentrantLock reentrantLock=new ReentrantLock();
        for (String value : workflowtype) {
            ThreadTest2 threadTest = new ThreadTest2(value, environment2, reentrantLock);
            threadTest.execute();
        }


//        CountDownLatch countDownLatch=new CountDownLatch(tasknums.length*privacytaskpercent.length*10*workflowtype.length);
//        for (int i = 0; i < tasknums.length; i++) {
//            for(int j=0;j<workflowtype.length;j++)
//            {
//                for (int o = 0; o < privacytaskpercent.length; o++) {
//                    for (int ins = 0; ins < 10; ins++) {
//                        String datapath = new String(prefix + workflowtype[j]+" " + tasknums[i] + " [" + privacytaskpercent[o][0] + "," + privacytaskpercent[o][1] + "," + privacytaskpercent[o][2] +  "]"+" " + ins +".xml");
//                        generatethread generatethread=new generatethread(datapath, tasknums[i], privacytaskpercent[o], workflowtype[1],countDownLatch);
//                        threadPoolExecutor1.execute(generatethread);
//                    }
//                }
//            }
//        }
//        countDownLatch.await();
//        threadPoolExecutor1.shutdownNow();
//        ReentrantLock reentrantLock=new ReentrantLock();
//        for (String value : workflowtype) {
//            ThreadTest3 threadTest = new ThreadTest3(value, environment2, reentrantLock);
//            threadTest.execute();
//        }
//        threadPoolExecutor2.shutdownNow();

    }

    myalg(List<Task> list, String SDM, String TRM, String LPLTSMLocal, String LPLTSMUsingExistingVm, String NPLTSMLocal, String NPLTSMUsingExistingVm, double dealine, int tasknum, double[] ptpercentage, Environment environmentin, String ResPath, double deadlinefactor,int instance
    ,double localscale,int localvms) throws IOException {
        environment = new Environment();
        environment.pedgenum = environmentin.pedgenum;
        environment.edgenum = environmentin.edgenum;
        environment.maxbandwidth = environmentin.maxbandwidth;
        environment.maxspeed = environmentin.maxspeed;
        environment.vmprice = environmentin.vmprice;
        environment.vmlocationvapl = environmentin.vmlocationvapl;
        environment.init2();
        environment.bandwidth = environmentin.bandwidth;
        environment.list = new ArrayList<>();
        environment.createlocalvms(localvms);
        environment.list.addAll(list);
        environment.vmrenthistory=new HashMap<>();
        environment.setSDM(SDM);
        environment.setTRM(TRM);
        environment.setLPLTSMLocal(LPLTSMLocal);
        environment.setLPLTSMUsingExistingVm(LPLTSMUsingExistingVm);
        environment.setNPLTSMLocal(NPLTSMLocal);
        environment.setNPLTSMUsingExistingVm(NPLTSMUsingExistingVm);
        environment.deadline = dealine;
        environment.setTasknum(tasknum);
        environment.setPtpercentage(ptpercentage);
        environment.head = list.get(0);
        environment.tail = list.get(list.size() - 1);
        this.localscale=localscale;
        for (Task task:environment.list)
        {
            environment.taskvaTaskid.put(task.getCloudletId(),task);
        }
        execute(ResPath);
        FileWriter fw = new FileWriter(ResPath, true);
        fw.write(tasknum + " " + Arrays.toString(ptpercentage) + " " +SDM+" "+TRM+" "+LPLTSMLocal+" "+LPLTSMUsingExistingVm+" "+NPLTSMLocal+" "+NPLTSMUsingExistingVm+" "
                + deadlinefactor +" "+instance+" "+localscale+" "+afterfee+" "+ dealine+" "+environment.tail.getFinishtime());
        fw.write("\r\n");//换行
        fw.flush();
        fw.close();
        environment.clearvmhistory();
    }
    myalg(List<Task> list,double dealine, int tasknum, double[] ptpercentage, Environment environmentin, String ResPath, double deadlinefactor,int instance
    ,double localscale,int localvms) throws IOException {
        environment = new Environment();
        environment.pedgenum = environmentin.pedgenum;
        environment.edgenum = environmentin.edgenum;
        environment.maxbandwidth = environmentin.maxbandwidth;
        environment.maxspeed = environmentin.maxspeed;
        environment.vmprice = environmentin.vmprice;
        environment.vmlocationvapl = environmentin.vmlocationvapl;
        environment.bandwidth = environmentin.bandwidth;
        environment.init2();
        environment.createlocalvms(localvms);
        environment.list = new ArrayList<>();
        environment.list.addAll(list);
        environment.vmrenthistory=new HashMap<>();
        environment.setSDM(SDM[1]);
        environment.setTRM(TRM[0]);
        environment.setLPLTSMLocal(LPLTSMLocal[0]);
        environment.setLPLTSMUsingExistingVm(LPLTSMUsingExistingVm[1]);
        environment.setNPLTSMLocal(LPLTSMLocal[0]);
        environment.setNPLTSMUsingExistingVm(LPLTSMUsingExistingVm[2]);
        environment.deadline = dealine;
        environment.setTasknum(tasknum);
        environment.setPtpercentage(ptpercentage);
        environment.head = list.get(0);
        environment.tail = list.get(list.size() - 1);
        for (Task task:environment.list)
        {
            environment.taskvaTaskid.put(task.getCloudletId(),task);
        }
        execute(ResPath);
        FileWriter fw = new FileWriter(ResPath, true);
        fw.write(tasknum + " " + Arrays.toString(ptpercentage) +" "
                + deadlinefactor +" "+instance+" "+localscale+" "+afterfee+" "+ dealine+" "+environment.tail.getFinishtime());
        fw.write("\r\n");//换行
        fw.flush();
        fw.close();
        environment.clearvmhistory();
    }

    void execute(String ResPath) {
        HighPrivacyTaskScheduling highPrivacyTaskScheduling = new HighPrivacyTaskScheduling(environment);
        LNPrivacyTaskScheduling lnPrivacyTaskScheduling = new LNPrivacyTaskScheduling(environment);
        /**
         *计算任务最晚结束时间、最晚开始时间
         */
        caltaskestlatestfinTime();
        /**
         * 如果任务排序使用了任务的rank或者在子截止期划分中使用了rank，则计算rank
         */
        calrankavg();
        /**
         * 如果子截止期划分使用了隐私等级和，则计算
         */
        if (environment.SDM.equals("SDMDepthPLSum")) {
            environment.plsum = calplsum();
        }
        /**
         * 进行子截止期划分
         */
        baseSDM baseSDM = null;
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
        if (baseSDM == null) throw new IllegalArgumentException("SDM method is not determined!");
        baseSDM.Settaskssubdeadline(environment);
        /**
         * 进行任务排序
         */
        baseTRM baseTRM = null;
        switch (environment.TRM) {
            case "TRMMaxRankavg" -> {
                baseTRM = new TRMMaxRankavg();
            }
            case "TRMMinFloatTime" -> {
                baseTRM = new TRMMinFloatTime();
            }
            case "TRMTaskFeature" -> {
                baseTRM = new TRMTaskFeature();
            }
        }
        if (baseTRM == null) throw new IllegalArgumentException("TRM method is not determined!");
        /**
         * 进行任务调度
         */
        List<Task> temp=new ArrayList<>();
        temp.add(environment.head);
        while(!temp.isEmpty())
        {
            baseTRM.RankTasks(environment,temp);
            Task i=temp.get(0);
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
            } else {
                if (i.getPrivacy_level() == 1) {
                    highPrivacyTaskScheduling.execute(i);
                } else {
                    lnPrivacyTaskScheduling.execute(i);
                }
            }
            for(Task j:i.getChildList())
            {
                boolean flag=true;
                for(Task s:j.getParentList())
                {
                    if(s.getVmId()==-1) {
                        flag = false;break;
                    }
                }
                if(flag) temp.add(j);
            }
            temp.remove(0);
        }
//        prefee=environment.calculateprices();
//        environment.adjustSchedulingResult();
        afterfee=environment.calculateprices();
    }
    public static int estlocalvms(String datapath,Environment environmentin,int tasknums,ReentrantLock reentrantLock,double deadline)
    {
        Environment environment = new Environment();
        environment.pedgenum = environmentin.pedgenum;
        environment.edgenum = environmentin.edgenum;
        environment.maxbandwidth = environmentin.maxbandwidth;
        environment.maxspeed = environmentin.maxspeed;
        environment.vmprice = environmentin.vmprice;
        environment.vmlocationvapl = environmentin.vmlocationvapl;
        environment.bandwidth = environmentin.bandwidth;
        environment.vmrenthistory=new HashMap<>();
        environment.init2();
        List<Task> list=gettasks(datapath,reentrantLock);
        environment.list=list;
        int l=1,r=tasknums*10;int ans=0;
        while(l<r)
        {
            int mid=(l+r)/2;
            if(check(list,environment,mid,deadline))
            {
                ans=mid;r=mid-1;
            }else l=mid+1;
        }
        return ans;
    }
    public static boolean check(List<Task> list,Environment environment,int mid,double deadline)
    {
        environment.createlocalvms(mid);
        myalg.caltaskestearlystarttime(list);
        environment.head = list.get(0);
        environment.tail = list.get(list.size() - 1);
        int num=list.size();
        while(num>0)
        {
            for(Task i: list)
            {
                if(i.getVmId()!=-1) continue;
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
                        for(Vm vm:environment.curVmList.get(0))
                            {
                                double t=environment.ComputeTaskFinishTime(i,vm.getId());
                                if(min>t)
                                {
                                    min=t;keyvmid=vm.getId();
                                }
                            }
                            environment.updateTaskShcedulingInformation(i,keyvmid,min);
                        num--;
                    }
                }
            }
        }
        boolean flag= environment.tail.getFinishtime() <= deadline;
        environment.clearvmhistory();
        return flag;
    }
    public static void esttaskexuteTime(List<Task> list, Environment environment) {
        for (Task i : list) {
            if (i.getParentList().size() == 0) {
                i.setEstextTime(0);
            } else if (i.getParentList().size() == 1 && i.getParentList().get(0).getDepth() == 0) {
                double datasize = i.getFileList().stream().filter(item -> Parameters.FileType.INPUT == item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).sum();
                i.setEstextTime((datasize) / 10 + i.getCloudletLength() / environment.maxspeed.get(i.getPrivacy_level()));
            } else {
                double maxfilesize = 0;
                double[] tempin = new double[i.getParentList().size()];
                int pre = -1;
                /**
                 * We set the bandwidth within a Datacenter is 80Mbps,which is the max bandwidth,
                 * So,Wherever the task is scheduled,the estbandwidth is 80.
                 */
                for (FileItem j : i.getFileList()) {
                    if (j.getType() == Parameters.FileType.INPUT) {
                        for (int z = 0; z < i.getParentList().size(); z++) {
                            if (i.getParentList().get(z).getFileList().stream().anyMatch(item -> item.getName().equals(j.getName()) && item.getType() == Parameters.FileType.OUTPUT)) {
                                tempin[z] += j.getSize();
                            }
                        }
                    }
                }
                for (int x = 0; x < tempin.length; x++) {
                    if (tempin[x] >= maxfilesize) {
                        maxfilesize = tempin[x];
                        pre = x;
                    }
                }
                if (maxfilesize != 0)
                    i.setEstextTime((maxfilesize) / environment.maxbandwidth[i.getPrivacy_level()][i.getParentList().get(pre).getPrivacy_level()] + i.getCloudletLength() / environment.maxspeed.get(i.getPrivacy_level()));
                else i.setEstextTime(0);
            }
        }
    }

    public static double caltaskestearlystarttime(List<Task> list) {
        int unhandledtasknum = list.size();
        double max = -1;
        while (unhandledtasknum > 0) {
            for (Task i : list) {
                if (i.getParentList().size() == 0) {
                    i.settaskEarlyStartTime(0);
                    i.settaskEralyFinTime(0);
                    unhandledtasknum--;
                    max = Math.max(max, 0);
                } else if (i.getParentList().size() != 0) {
                    boolean flag = true;
                    double early = -1;
                    for (Task j : i.getParentList()) {
                        if (j.gettaskEarlyStartTime() != -1) {
                            early = Math.max(early, j.gettaskEarlyStartTime() + j.getEstextTime());
                        } else {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        i.settaskEarlyStartTime(early);
                        i.settaskEralyFinTime(early + i.getEstextTime());
                        unhandledtasknum--;
                        max = Math.max(max, i.gettaskEralyFinTime());
                    }
                }
            }
        }
        return max;
    }

    void caltaskestlatestfinTime() {
        List<Task> list = environment.list;
        double deadline = environment.deadline;
        int unhandledtasknum = list.size();
        while (unhandledtasknum > 0) {
            for (int x = list.size() - 1; x >= 0; x--) {
                Task i = list.get(x);
                if (i.getChildList().size() == 0) {
                    i.settaskLatestFinTime(deadline);
                    i.settaskLatestStartTime(i.gettaskLatestFinTime() - i.getEstextTime());
                    unhandledtasknum--;
                } else if (i.getChildList().size() != 0) {
                    boolean flag = true;
                    double latest = Double.MAX_VALUE;
                    for (Task j : i.getChildList()) {
                        if (j.gettaskLatestStartTime() != -1) {
                            latest = Math.min(latest, j.gettaskLatestFinTime() - j.getEstextTime());
                        } else {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        i.settaskLatestFinTime(latest);
                        i.settaskLatestStartTime(latest - i.getEstextTime());
                        unhandledtasknum--;
                    }
                }
            }
        }

    }

    void calrankavg() {
        List<Task> list = environment.list;
        Map<Task, Double> rankprivacy = new HashMap<>();
        Map<Task, Double> rankup = new HashMap<>();
        int num = list.size();
        double[] ranks1 = new double[list.size()];
        double[] ranks2 = new double[list.size()];
        while (num > 0) {
            for (int i = list.size() - 1; i >= 0; i--) {
                if (rankup.get(list.get(i)) == null) {
                    if (list.get(i).getChildList().size() == 0) {
                        rankprivacy.put(list.get(i), 0.0);
                        rankup.put(list.get(i), 0.0);
                        ranks1[i] = 0;
                        ranks2[i] = 0;
                        num--;
                    } else {
                        double plsum = 0;
                        double upmax = -1;
                        boolean flag = true;
                        for (Task j : list.get(i).getChildList()) {
                            if (rankup.get(j) != null && rankprivacy.get(j) != null) {
                                if (upmax < rankup.get(j)) {
                                    upmax = rankup.get(j);
                                }
                                if (plsum < rankprivacy.get(j))
                                    plsum = (rankprivacy.get(j));
                            } else {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            double t1 = upmax + list.get(i).getEstextTime();
                            double t2 = plsum + 1.0 / list.get(i).getPrivacy_level();
                            rankup.put(list.get(i), t1);
                            rankprivacy.put(list.get(i), t2);
                            ranks1[i] = t1;
                            ranks2[i] = t2;
                            num--;
                        }
                    }
                }
            }
        }
        double mean1 = StatUtils.mean(ranks1);
        double mean2 = StatUtils.mean(ranks2);
        for (int i = 0; i < ranks1.length; i++) {
            ranks1[i] = ranks1[i] / mean1;
            ranks2[i] = ranks2[i] / mean2;
            list.get(i).setRankavg((ranks1[i] + ranks2[i]) / 2);
        }
    }

    int[] calplsum() {
        List<Task> list = environment.list;
        int[] plsum = new int[list.get(list.size() - 1).getDepth() + 1];
        for (Task i : list) {
            plsum[i.getDepth()] += 1.0 / i.getPrivacy_level();
        }
        double totalpl = Arrays.stream(plsum).sum();
        double pre = 0;
        for (int i = 0; i < plsum.length; i++) {
            plsum[i] += pre;
            pre = plsum[i];
        }
        return plsum;
    }

    public static List<Task> gettasks(String datapath,ReentrantLock reentrantLock)
    {
        myparser workflowParser = new myparser(datapath, new myreplicalog());
        reentrantLock.lock();
        workflowParser.parse();
        reentrantLock.unlock();
        List<Task> list = workflowParser.getTaskList();
        Task headtask = new Task(list.get(list.size() - 1).getCloudletId() + 1, 0);
        Task tailtask = new Task(list.get(list.size() - 1).getCloudletId() + 2, 0);
        headtask.setDepth(0);
        headtask.setPrivacy_level(3);
        tailtask.setPrivacy_level(3);
        list.sort(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o1.getDepth() - o2.getDepth();
            }
        });
        tailtask.setDepth(list.get(list.size() - 1).getDepth() + 1);
        for (Task task : list) {
            if (task.getParentList().size() == 0) {
                headtask.addChild(task);
                task.addParent(headtask);
            } else if (task.getChildList().size() == 0) {
                tailtask.addParent(task);
                task.addChild(tailtask);
            }
        }
        list.add(0, headtask);
        list.add(tailtask);
        return list;
    }
}



