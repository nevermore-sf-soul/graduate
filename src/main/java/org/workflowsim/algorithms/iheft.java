package org.workflowsim.algorithms;

import org.apache.commons.math3.stat.StatUtils;
import org.workflowsim.*;
import org.workflowsim.utils.Parameters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class iheft {
    Environment environment;
    Map<Task, Double> rankup = new HashMap<>();
    String respath;double deadlinefactor;double[] percentage;double deadline;
    public double prefee,afterfee;
    public iheft(List<Task>list,int tasknum,String respath,Environment environmentin,double deadlinefactor,double[] percentage,double deadline,int instance) throws IOException {
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
        environment.createlocalvms();
        execute(respath);
        FileWriter fw = new FileWriter(respath, true);
        fw.write(tasknum + " " + Arrays.toString(percentage) + " " + deadlinefactor +" "+instance+ " " +afterfee+" "+ deadline+" "+environment.tail.getFinishtime());
        fw.write("\r\n");//换行
        fw.flush();
        fw.close();
        environment.clearvmhistory();
    }
    public void execute(String respath)
    {
        calrankavg();
        environment.list.sort(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return Double.compare(rankup.get(o1),rankup.get(o2));
            }
        });
        for (Task task:environment.list)
        {
            environment.taskvaTaskid.put(task.getCloudletId(),task);
        }
        HEFT heft=new HEFT(environment);
        for(Vm vm:environment.allVmList)
        {
            if(environment.datacenterList.get(vm.getDatacenterid()).getPrivacylevel()!=1)
            {
                int[] cpucores=new int[]{1,2,4};
                for(int c=0;c<cpucores.length;c++)
                {
                    if(cpucores[c]<vm.getCpucore())
                    {
                        double curt=vm.getCreateTime();
                        boolean flag=true;
                        for(TripleValue tripleValue:environment.vmrenthistory.get(vm.getId()))
                        {
                            Task cur=environment.taskvaTaskid.get(tripleValue.getfirstval());
                            double st=Math.max(curt,cur.getStarttime());
                            double process=cur.getCloudletLength()/(cpucores[c]*environment.datacenterList.get(vm.getDatacenterid()).getMibps());
                            st+=process;
                            if(st>cur.getChildList().stream().mapToDouble(Task::getStarttime).min().orElse(0))
                            {
                                flag=false;
                                break;
                            }
                        }
                        if(flag) {
                            vm.setCpucore(cpucores[c]);
                            for(TripleValue tripleValue:environment.vmrenthistory.get(vm.getId()))
                            {
                                Task cur=environment.taskvaTaskid.get(tripleValue.getfirstval());
                                double st=Math.max(curt,cur.getStarttime());
                                cur.setStarttime(st);
                                double process=cur.getCloudletLength()/(cpucores[c]*environment.datacenterList.get(vm.getDatacenterid()).getMibps());
                                st+=process;
                                cur.setFinishtime(st);
                            }
                            break;
                        }
                    }
                }
            }
        }
        afterfee=environment.calculateprices2();
    }

    void calrankavg() {
        List<Task> list = environment.list;
        int num = list.size();
        double[] ranks1 = new double[list.size()];
        while (num > 0) {
            for (int i = list.size() - 1; i >= 0; i--) {
                if (rankup.get(list.get(i)) == null) {
                    if (list.get(i).getChildList().size() == 0) {
                        rankup.put(list.get(i), 0.0);
                        ranks1[i] = 0;num--;
                    } else {
                        double upmax = -1;
                        boolean flag = true;
                        for (Task j : list.get(i).getChildList()) {
                            if (rankup.get(j) != null) {
                                if (upmax < rankup.get(j)) {
                                    upmax = rankup.get(j);
                                }
                            } else {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            double t1 = upmax + list.get(i).getEstextTime();
                            rankup.put(list.get(i), t1);
                            ranks1[i] = t1;
                            num--;
                        }
                    }
                }
            }
        }

    }
}
