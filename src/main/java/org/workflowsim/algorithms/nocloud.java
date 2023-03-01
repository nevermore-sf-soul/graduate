package org.workflowsim.algorithms;

import org.apache.commons.math3.stat.StatUtils;
import org.workflowsim.*;
import org.workflowsim.utils.Parameters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class nocloud {
    Environment environment;
    Map<Task, Double> rankup = new HashMap<>();
    String respath;double deadlinefactor;double[] percentage;double deadline;
    public double prefee,afterfee;
    public nocloud(List<Task> list, int tasknum, String respath, Environment environmentin, double deadlinefactor, double[] percentage, double deadline, int instance) throws IOException {
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
        environment.setSDM("SDMPathPLSum");
        environment.setTRM("TRMTaskFeature");
        environment.setLPLTSMLocal("TSMLocalMinWaste");
        environment.setLPLTSMUsingExistingVm("TSMUsingExistingVmShortestSTB");
        environment.setNPLTSMLocal("TSMLocalMinWaste");
        environment.setNPLTSMUsingExistingVm("TSMUsingExistingVmShortestSTB");
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
        environment.vmlocationvapl.put(3,8);
        environment.clearvmhistory();
    }
    public void execute(String respath)
    {

        HighPrivacyTaskScheduling highPrivacyTaskScheduling = new HighPrivacyTaskScheduling(environment);
        LNPrivacyTaskScheduling lnPrivacyTaskScheduling = new LNPrivacyTaskScheduling(environment);
        environment.vmlocationvapl.put(3,7);
        environment.createlocalvms();
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
        /**
         * 进行子截止期划分
         */
        baseSDM baseSDM = new SDMPathPLSum();
        baseSDM.Settaskssubdeadline(environment);
        /**
         * 进行任务排序
         */
        baseTRM baseTRM = new TRMTaskFeature();
       baseTRM.RankTasks(environment);
        /**
         * 进行任务调度
         */
        for (Task i : environment.list) {
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
            } else {
                if (i.getPrivacy_level() == 1) {
                    highPrivacyTaskScheduling.execute(i);
                } else {
                    lnPrivacyTaskScheduling.execute(i);
                }
            }
        }
//        prefee=environment.calculateprices();
//        environment.adjustSchedulingResult();
        afterfee=environment.calculateprices();
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
}
