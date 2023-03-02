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
    public mcpcpp(List<Task> list, int tasknum, String respath, Environment environmentin, double deadlinefactor, double[] percentage, double deadline, int instance) throws IOException {
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
        for (Task task:environment.list)
        {
            environment.taskvaTaskid.put(task.getCloudletId(),task);
        }
        execute(respath);
        FileWriter fw = new FileWriter(respath, true);
        fw.write(tasknum + " " + Arrays.toString(percentage) + " " + deadlinefactor +" "+instance+" "+afterfee+" "+ deadline+" "+environment.tail.getFinishtime());
        fw.write("\r\n");//换行
        fw.flush();
        fw.close();
        environment.clearvmhistory();
    }
    public void execute(String respath)
    {
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
        environment.createlocalvms();Task tail;RentOrNewMCP rentOrNewMCP=new RentOrNewMCP();
        while(null!=(tail=findtail(environment.tail)))
        {
            List<Task> cp=findcp(tail);
            for(int z=cp.size()-1;z>=0;z--)
            {
                String res=rentOrNewMCP.shcedule(cp.get(z),environment);
                String[] strings=res.split(" ");int vmid;double ft=0;
                if(strings[0].equals("1"))
                {
                   vmid=Integer.parseInt(strings[1]);
                    ft=environment.ComputeTaskFinishTime(i,vmid);
                }
                else{
                    if(strings.length==2)
                    {
                        vmid=Integer.parseInt(strings[1]);
                        ft=environment.ComputeTaskFinishTime(i,vmid);
                    }
                    else{
                        vmid=environment.createvm(Integer.parseInt(strings[1]),Integer.parseInt(strings[2]),ft,i.gettaskEarlyStartTime());
                        environment.allVmList.get(vmid).setDestoryTime((Math.ceil((ft-i.gettaskEarlyStartTime())/environment.BTU))*environment.BTU+i.gettaskEarlyStartTime());
                    }
                }
                updateTaskShcedulingInformation(cp.get(z),vmid,ft);
            }
        }
        afterfee=environment.calculateprices();
    }
    Task findtail(Task task)
    {
        for(Task j:task.getParentList())
        {
            if(j.getVmId()==-1) return  j;
        }
        for(Task j:task.getParentList())
        {
            return findtail(j);
        }
        return null;
    }
    List<Task> findcp(Task task)
    {
        Task target=null;double max=Double.MIN_VALUE;
        List<Task> res=new ArrayList<>();
        for(Task j:task.getParentList())
        {
            if(j.getVmId()==-1&&max<j.gettaskEralyFinTime())
            {
                max=j.gettaskEralyFinTime();target=j;
            }
        }
        if(target!=null)
        {
            res.add(target);
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
}
