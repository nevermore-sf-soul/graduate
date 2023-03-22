package org.workflowsim.algorithms;

import org.apache.commons.math3.stat.StatUtils;
import org.workflowsim.*;
import org.workflowsim.utils.Parameters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class mcpcpp {
    Environment environment;
    String respath;double deadlinefactor;double[] percentage;double deadline;
    public double prefee,afterfee;
    public mcpcpp(List<Task> list, int tasknum, String respath, Environment environmentin, double deadlinefactor, double[] percentage, double deadline, int instance,double localscale,int localvms) throws IOException {
        environment = new Environment();
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
        environment.createlocalvms(localvms);
        for (Task task:environment.list)
        {
            environment.taskvaTaskid.put(task.getCloudletId(),task);
        }
        long st=System.nanoTime();
        execute();
        long et=System.nanoTime();
        FileWriter fw = new FileWriter(respath, true);
        fw.write(tasknum + " " + Arrays.toString(percentage) + " " + deadlinefactor +" "+instance+" "+localscale+" "+afterfee+" "+ deadline+" "+(et-st)+" "+environment.tail.getFinishtime());
        fw.write("\r\n");//换行
        fw.flush();
        fw.close();
        environment.clearvmhistory();
    }
    public void execute()
    {
        myalg.esttaskexuteTime(environment.list,environment);
        environment.deadline=myalg.caltaskestearlystarttime(environment.list);//随便取截止期
        caltaskestlatestfinTime();
        Task i=environment.head;
        i.setVmId(0);
        i.setStarttime(0.0);
        i.setFinishtime(0.0);
        environment.allVmList.get(0).setEarlyidletime(0.0);
        i= environment.tail;
        i.setVmId(0);
        PriorityQueue<Task> priorityQueue=new PriorityQueue<>(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return Double.compare(o1.getPrivacy_level(),o2.getPrivacy_level());
            }
        });
        for(Task task:environment.list) priorityQueue.offer(task);
        while(!priorityQueue.isEmpty())
        {
            Task head=priorityQueue.poll();
            if(head.getVmId()!=-1) continue;
            Pair<List<Task>,Integer> p=new Pair<>(new ArrayList<>(),-1);
            findcl(head,p);
            schedule(p);
        }
        i= environment.tail;
        double MaxFT = -1;
        for (Task j : i.getParentList()) {
            MaxFT = Math.max(MaxFT, j.getFinishtime());
        }
        i.setStarttime(MaxFT);
        i.setFinishtime(MaxFT);
        environment.allVmList.get(0).setEarlyidletime(MaxFT);
        afterfee=environment.calculateprices();
    }
    void findcl(Task task, Pair<List<Task>,Integer> res)
    {
        res.getKey().add(task);
        res.setValue(task.getPrivacy_level());
        for(Task i:task.getChildList())
        {
            if(i.getVmId()!=-1||i.getPrivacy_level()!=res.getValue()) continue;
            boolean flag=true; //有没有合格的任务
            for(Task j:i.getParentList())
            {
                if(j.getCloudletId()!=task.getCloudletId()&&j.getVmId()==-1){
                    flag=false;break;
                }
            }
            if(flag)
            {
                findcl(i,res);
                break;
            }
        }
    }
    Task findtail()
    {
       for(int x=environment.list.size()-1;x>=0;x--)
       {
           if(environment.list.get(x).getVmId()==-1) return environment.list.get(x);
       }
        return null;
    }
    List<Task> findcp(Task task)
    {
        Task target=null;double max=Double.MIN_VALUE;
        List<Task> res=new ArrayList<>();
        res.add(task);
        for(Task j:task.getParentList())
        {
            if(j.getVmId()==-1&&max<j.gettaskEralyFinTime())
            {
                max=j.gettaskEralyFinTime();target=j;
            }
        }
        if(target!=null)
        {
            res.addAll(findcp(target));
        }
        return res;
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
    public void updateTaskShcedulingInformation(Task t,int vmid)
    {
        t.setVmId(vmid);
        t.setStarttime(Math.max(t.gettaskEarlyStartTime(),environment.allVmList.get(vmid).getEarlyidletime()));
        double ft=environment.ComputeTaskFinishTime(t,vmid);
        t.setFinishtime(ft);
        environment.allVmList.get(vmid).setEarlyidletime(ft);
        if(environment.allVmList.get(vmid).getDestoryTime()<ft)
        {
            double exceedtime=ft-environment.allVmList.get(vmid).getDestoryTime();
            environment.allVmList.get(vmid).setDestoryTime(environment.allVmList.get(vmid).getDestoryTime()+Math.ceil(exceedtime/environment.BTU)*environment.BTU);
        }
        updatetaskearliestlateststartTime(t);
        List<TripleValue> temp=environment.vmrenthistory.getOrDefault(vmid,new ArrayList<>());
        temp.add(new TripleValue(t.getCloudletId(),t.getStarttime(),t.getFinishtime()));
        environment.vmrenthistory.put(vmid,temp);
        updatetaskearliestlatestFTime(t);updatetaskearliestlateststartTime(t);
    }
    public void updatetaskearliestlateststartTime(Task t)
    {
        for(Task j:t.getChildList())
        {
            if(j.getVmId()!=-1)
            {
                j.settaskEarlyStartTime(Math.max(j.gettaskEarlyStartTime(),t.getFinishtime()));
                j.settaskEralyFinTime(j.gettaskEarlyStartTime()+j.getEstextTime());
            }
        }
    }
    public void updatetaskearliestlatestFTime(Task t)
    {
        for(Task j:t.getParentList())
        {
            if(j.getVmId()!=-1)
            {
                j.settaskLatestFinTime(Math.min(j.gettaskLatestFinTime(),t.getStarttime()));
                j.settaskLatestStartTime(j.gettaskLatestFinTime()-j.getEstextTime());
            }
        }
    }
    class Pair<K,V>{
        public K key;public V value;

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    public void schedule(Pair<List<Task>,Integer> pair)
    {
        double est=pair.getKey().get(0).gettaskEarlyStartTime();
        double lft=pair.getKey().get(pair.getKey().size()-1).gettaskLatestFinTime();
        double mincost=Double.MAX_VALUE;int keyvmid=0;
        double mincostnew=Double.MAX_VALUE;
        double mincostRenew=Double.MAX_VALUE;
        StringBuilder sb=new StringBuilder();
        int ds=0;if(pair.getValue()==1) {
        } else if(pair.getValue()==2) ds=1;
        else ds= environment.pedgenum+1;
        int vmid1=-1;double ft=0;
        for(Vm vm:environment.allVmList)
        {
            if(vm.getDatacenterid()<=environment.vmlocationvapl.get(pair.getValue()))
            {
                if(vm.getDestoryTime()<est||vm.getDestoryTime()>est&&vm.getCreateTime()>lft) continue;

                double ct=est;
                for(Task i:pair.getKey())
                {
                    double starttime=Math.max(vm.getEarlyidletime(),ct);
                    double processtime=environment.computeprocesstime(i,vm.getId());
                    ct=starttime+processtime;
                }
                if(lft>ct)
                {
                    double newFee=Math.ceil((ct-est)/environment.BTU)*vm.getPrice();
                    if(newFee<mincostRenew)
                    {
                        mincostRenew=newFee;
                        keyvmid=vm.getId();
                        ft=ct;
                    }
                }
            }
        }
        int kdatacenterid=0;int kcpucore=0;
        for(int datacenterid=ds;datacenterid<=environment.vmlocationvapl.get(pair.getValue());datacenterid++)
        {
            int[] cpucores=new int[]{1,2,4};
            for(int cpucore:cpucores)
            {
                if(environment.datacenterList.get(datacenterid).getUseablecores()>=cpucore)
                {
                    double ct=est;
                    for(Task i:pair.getKey())
                    {
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
                                if(pre.getVmId()!=-1)
                                    processtime=Math.max(processtime,(tempfile)/environment.bandwidth[environment.allVmList.get(pre.getVmId()).getDatacenterid()][datacenterid]+(i.getCloudletLength()*1.0)/(cpucore*environment.datacenterList.get(datacenterid).getMibps()));
                            }
                        }
                        ct+=processtime;
                    }
                    if(ct<lft)
                    {
                        double newFee=Math.ceil((ct-est)/environment.BTU)*environment.vmprice.get(environment.datacenterList.get(datacenterid).getTag()+cpucore);
                        if(newFee<mincostnew)
                        {
                            mincostnew=newFee;
                            kdatacenterid=datacenterid;kcpucore=cpucore;ft=ct;
                        }
                    }
                }
            }
        }
        if(mincostnew==Double.MAX_VALUE&&mincostRenew==Double.MAX_VALUE) {
            double minft1 = Double.MAX_VALUE;
            for (int datacenterid = ds; datacenterid <= environment.vmlocationvapl.get(pair.getValue()); datacenterid++) {
                for (Vm vm : environment.curVmList.get(datacenterid)) {
                    if (vm.getDestoryTime() < est || vm.getDestoryTime() > est && vm.getCreateTime() > lft) continue;
                    double ct = est;
                    for (Task i : pair.getKey()) {
                        double starttime = Math.max(vm.getEarlyidletime(), ct);
                        double processtime = environment.computeprocesstime(i, vm.getId());
                        ct = starttime + processtime;
                    }
                    if (minft1 > ct) {
                        minft1 = ct;
                        keyvmid = vm.getId();
                    }
                }
            }
            for (int datacenterid = ds; datacenterid <= environment.vmlocationvapl.get(pair.getValue()); datacenterid++) {
                int[] cpucores = new int[]{4, 2, 1};
                for (int cpucore : cpucores) {
                    if (environment.datacenterList.get(datacenterid).getUseablecores() >= cpucore) {
                        double ct = est;
                        for (Task i : pair.getKey()) {
                            double processtime = 0;
                            if (i.getParentList().size() == 1 && i.getParentList().get(0).getCloudletId() == environment.head.getCloudletId()) {
                                double datasize = i.getFileList().stream().filter(item -> Parameters.FileType.INPUT == item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).sum();
                                processtime = (datasize) / 10 + (i.getCloudletLength() * 1.0) / (cpucore * environment.datacenterList.get(datacenterid).getMibps());
                            } else {
                                for (Task pre : i.getParentList()) {
                                    double tempfile = 0;
                                    for (FileItem f1 : i.getFileList()) {
                                        for (FileItem f2 : pre.getFileList()) {
                                            if (f1.getName().equals(f2.getName()) && f1.getType() == Parameters.FileType.INPUT && f2.getType() == Parameters.FileType.OUTPUT) {
                                                tempfile += f1.getSize();
                                            }
                                        }
                                    }
                                    if (pre.getVmId() != -1)
                                        processtime = Math.max(processtime, (tempfile) / environment.bandwidth[environment.allVmList.get(pre.getVmId()).getDatacenterid()][datacenterid] + (i.getCloudletLength() * 1.0) / (cpucore * environment.datacenterList.get(datacenterid).getMibps()));
                                }
                            }
                            ct += processtime;
                        }
                        if (ct < minft1) {
                            minft1 = ct;
                            keyvmid = -1;
                            kdatacenterid = datacenterid;
                            kcpucore = cpucore;
                            break;
                        }
                    }
                }
            }
            if(keyvmid!=-1)
            {
                for(Task i:pair.getKey())
                {
                    updateTaskShcedulingInformation(i,keyvmid);
                }
            }
            else{
                int id=environment.createvm(kcpucore,kdatacenterid,est,est);
                environment.allVmList.get(id).setDestoryTime((Math.ceil((minft1-est)/environment.BTU))*environment.BTU+est);
                for(Task i:pair.getKey())
                {
                    updateTaskShcedulingInformation(i,id);
                }
            }
        }
        else if(mincostnew!=Double.MAX_VALUE)
        {
            int id=environment.createvm(kcpucore,kdatacenterid,est,est);
            environment.allVmList.get(id).setDestoryTime((Math.ceil((ft-est)/environment.BTU))*environment.BTU+est);
            for(Task i:pair.getKey())
            {
                updateTaskShcedulingInformation(i,id);
            }
        }
        else{
            for(Task i:pair.getKey())
            {
                updateTaskShcedulingInformation(i,keyvmid);
            }
        }
    }
    public static void main(String[] args) {

        }
}
