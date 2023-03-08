package org.workflowsim.algorithms;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.poi.ss.formula.functions.T;
import org.cloudbus.cloudsim.Cloudlet;
import org.workflowsim.*;
import org.workflowsim.utils.Parameters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class clustering {
    Environment environment;
    Map<Task, Double> rankup = new HashMap<>();
    String respath;double deadlinefactor;
    clustering(List<Task> list, int tasknum, String respath, Environment environmentin, double deadlinefactor, double[] percentage, double deadline, int instance) throws IOException {
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
        environment.createlocalvms();
        FileWriter fw = new FileWriter(respath, true);
        fw.write(tasknum + " " + Arrays.toString(percentage) + " " + deadlinefactor +" "+instance+ " " +deadline+" "+environment.tail.getFinishtime());
        fw.write("\r\n");//换行
        fw.flush();
        fw.close();
        environment.clearvmhistory();
    }
    public void execute()
    {
        int num=environment.list.size();
        myalg.caltaskestearlystarttime(environment.list);
        caltaskestlatestfinTime();
        Task i=environment.head;
        i.setVmId(0);
        i.setStarttime(0.0);
        i.setFinishtime(0.0);
        environment.allVmList.get(0).setEarlyidletime(0.0);
        i= environment.tail;
        i.setVmId(0);
        double MaxFT = -1;
        for (Task j : i.getParentList()) {
            MaxFT = Math.max(MaxFT, j.getFinishtime());
        }
        i.setStarttime(MaxFT);
        i.setFinishtime(MaxFT);
        environment.allVmList.get(0).setEarlyidletime(MaxFT);
        environment.createlocalvms();
        calrankavg();
        PriorityQueue<Task> priorityQueue=new PriorityQueue<>(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return Double.compare(o2.getRankavg(),o1.getRankavg());
            }
        });
        for(Task task:environment.list) priorityQueue.offer(task);
        while(!priorityQueue.isEmpty())
        {
            Task head=priorityQueue.poll();
            if(head.getVmId()!=-1) continue;
            Pair<List<Task>,Integer> p=new Pair<>(new ArrayList<>(),-1);
            findcl(head,p);

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
     void findcl(Task task, Pair<List<Task>,Integer> res)
    {
        res.getKey().add(task);res.setValue(Math.max(res.getValue(),task.getPrivacy_level()));
        Task sucess=null;double rankavg=Double.MIN_VALUE;
        List<Task> candinate=new ArrayList<>();
        for(Task i:task.getChildList())
        {
            if(i.getPrivacy_level()<res.getValue()) continue;
            boolean flag=true; //有没有合格的任务
            for(Task j:i.getParentList())
            {
                if(j.getCloudletId()!=task.getCloudletId()&&j.getVmId()==-1){
                    flag=false;break;
                }
            }
            if(flag)
            {
                candinate.add(i);
            }
        }
        if(candinate.isEmpty())
        {
            return;
        }
        boolean flag=false; //有没有隐私等级不同的任务
        for(Task i:candinate)
        if(i.getPrivacy_level()!=res.getValue())
        {
            flag=true;break;
        }
        if(flag)
        {
            double maxsavetime=Double.MIN_VALUE;
            for(Task i:candinate)
            {
                double savet;
                double maxband=Double.MIN_VALUE;
                for(int x=0;x<=environment.vmlocationvapl.get(res.getValue());x++)
                {
                    for(int y=(i.getPrivacy_level()==1)?0:environment.vmlocationvapl.get(i.getPrivacy_level()-1)+1;y<=environment.vmlocationvapl.get(i.getPrivacy_level());y++)
                    {
                        maxband=Math.max(maxband,environment.bandwidth[x][y]);
                    }
                }
                double instructionsum=i.getCloudletLength();
                savet=instructionsum/environment.maxspeed.get(i.getPrivacy_level())-instructionsum/environment.maxspeed.get(res.getValue());
                double tempfile=0;
                for(FileItem f1:i.getFileList())
                {
                    for(FileItem f2:task.getFileList())
                    {
                        if(f1.getName().equals(f2.getName())&&f1.getType()==Parameters.FileType.INPUT&&f2.getType()==Parameters.FileType.OUTPUT)
                        {
                            tempfile+=f1.getSize();
                        }
                    }
                }
                savet+=tempfile/maxband;
                if(savet>maxsavetime)
                {
                    maxsavetime=savet;
                    sucess=i;
                }
                }
        }else{
            double maxrank=Double.MIN_VALUE;
            for(Task i:candinate)
            {
                if(maxrank<i.getRankavg())
                {
                    maxrank=i.getRankavg();
                    sucess=i;
                }
            }
        }
        findcl(sucess,res);
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
    public void updateTaskShcedulingInformation(Task t,int vmid,double EarlyAvaiableTime)
    {
        t.setVmId(vmid);
        t.setStarttime(Math.max(t.gettaskEarlyStartTime(),environment.allVmList.get(vmid).getEarlyidletime()));
        t.setFinishtime(EarlyAvaiableTime);
        if(environment.allVmList.get(vmid).getDestoryTime()<EarlyAvaiableTime)
        {
            double exceedtime=EarlyAvaiableTime-environment.allVmList.get(vmid).getDestoryTime();
            environment.allVmList.get(vmid).setDestoryTime(environment.allVmList.get(vmid).getDestoryTime()+Math.ceil(exceedtime/environment.BTU)*environment.BTU);
        }
        environment.allVmList.get(vmid).setEarlyidletime(EarlyAvaiableTime);
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
    public void schedule(Pair<List<Task>,Integer> pair)
    {

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
}
