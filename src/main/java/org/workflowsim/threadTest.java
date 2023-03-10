package org.workflowsim;

import simulation.generator.Generator;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class threadTest implements Runnable{
    double[] privacytaskpercent;
    int tasknum;
    int ins;
    String workflowtype;
    Environment environment2;
    ReentrantLock reentrantLock;
    static String[] SDM = new String[]{"SDMDepthPLSum", "SDMPathPLSum", "SDMExecutiontimePercent"};
    static String[] TRM = new String[]{"TRMMaxRankavg", "TRMMinFloatTime", "TRMTaskFeature"};
    static String[] LPLTSMLocal = new String[]{"TSMLocalMinWaste", "TSMLocalEarlyAvaiableTime", "TSMLocalEarlyFinishTime"};
    static String[] LPLTSMUsingExistingVm = new String[]{"TSMUsingExistingVmFirstAdaptSTB", "TSMUsingExistingVmLongestSTB", "TSMUsingExistingVmShortestSTB"};
    double[] deadlinefactors = new double[]{1.5, 1.6, 1.7, 1.8, 1.9};
    double[] localscal=new double[]{0.1,0.2,0.3,0.4};
    threadTest(int a,int b,double[] c,String workflowtype,Environment environment,ReentrantLock reentrantLock) {
        tasknum=a;ins=b;privacytaskpercent=c;this.workflowtype=workflowtype;environment2=environment;this.reentrantLock=reentrantLock;
    }

    @Override
    public void run() {
        String prefix = "F:/benchmark/data/";
        String datapath = new String(prefix + workflowtype+" " + tasknum + " [" + privacytaskpercent[0] + "," + privacytaskpercent[1] + "," + privacytaskpercent[2] +"]"+" "+ ins +".xml");
        myparser workflowParser = new myparser(datapath, new myreplicalog());
        reentrantLock.lock();
        workflowParser.parse();
        List<Task> list = workflowParser.getTaskList();
        reentrantLock.unlock();
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
        /**
         * 估计任务执行时间
         */
        myalg.esttaskexuteTime(list, environment2);

        /**
         * 确定工作流合理截止期，估计任务最早开始时间、最早结束时间
         */
        String respath = "F:/benchmark/result/" + workflowtype+"_"+tasknum + " [" + privacytaskpercent[0] + "," + privacytaskpercent[1] + "," + privacytaskpercent[2]+ "]_"+" "+ins+".txt";
        double totaldeadline = myalg.caltaskestearlystarttime(list);
        for (int p = 0; p < deadlinefactors.length; p++) {
            int lvms=myalg.estlocalvms(datapath,environment2,list.size()-2,reentrantLock,totaldeadline*deadlinefactors[p]);
            for (int k = 0; k < SDM.length; k++) {
            for (int l = 0; l < TRM.length; l++) {
                for (int m = 0; m < LPLTSMLocal.length; m++) {
                    for (int n = 0; n < LPLTSMUsingExistingVm.length; n++) {
                        for (int x = 0; x < LPLTSMLocal.length; x++) {
                            for (int y = 0; y < LPLTSMUsingExistingVm.length; y++) {
                                for(int lo=0;lo<localscal.length;lo++)
                                    {
                                        try {
                                            myalg.caltaskestearlystarttime(list);
                                            myalg z = new myalg(list, SDM[k], TRM[l], LPLTSMLocal[m], LPLTSMUsingExistingVm[n], LPLTSMLocal[x], LPLTSMUsingExistingVm[y],
                                                    totaldeadline * deadlinefactors[p], tasknum, privacytaskpercent, environment2, respath, deadlinefactors[p],ins,localscal[lo], (int) (localscal[lo]*lvms));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

//    public static void main(String[] args) {
//        String s = "s";
//        Environment environment2 = new Environment();
//        environment2.edgenum = 5;
//        environment2.pedgenum = 2;
//        environment2.init1();
//        int[] tasknums = new int[]{150,200,250,300};
//        double[] deadlinefactors = new double[]{1.5, 1.6, 1.7, 1.8, 1.9};
//        double[][] privacytaskpercent = new double[][]{{0.05, 0.15, 0.8}, {0.1, 0.2, 0.7}, {0.15, 0.25, 0.55}, {0.2, 0.3, 0.5}};
//
//        String[] workflowtype = new String[]{"CyberShake",  "Montage","Genome", "Inspiral", "Sipht"};
//        String prefix = "F:/benchmark/data/";
//        String datapath = new String(prefix + workflowtype[1]+" " + 300 + " [" + 0.2 + "," + 0.3 + "," + 0.5 +"]"+" " + 9 +".xml");
//        myparser workflowParser = new myparser(datapath, new myreplicalog());
//        workflowParser.parse();
//        List<Task> list = workflowParser.getTaskList();
//        Task headtask = new Task(list.get(list.size() - 1).getCloudletId() + 1, 0);
//        Task tailtask = new Task(list.get(list.size() - 1).getCloudletId() + 2, 0);
//        headtask.setDepth(0);
//        headtask.setPrivacy_level(3);
//        tailtask.setPrivacy_level(3);
//        list.sort(new Comparator<Task>() {
//            @Override
//            public int compare(Task o1, Task o2) {
//                return o1.getDepth() - o2.getDepth();
//            }
//        });
//        tailtask.setDepth(list.get(list.size() - 1).getDepth() + 1);
//        for (Task task : list) {
//            if (task.getParentList().size() == 0) {
//                headtask.addChild(task);
//                task.addParent(headtask);
//            } else if (task.getChildList().size() == 0) {
//                tailtask.addParent(task);
//                task.addChild(tailtask);
//            }
//        }
//        list.add(0, headtask);
//        list.add(tailtask);
//        /**
//         * 估计任务执行时间
//         */
//        myalg.esttaskexuteTime(list, environment2);
//        /**
//         * 确定工作流合理截止期，估计任务最早开始时间、最早结束时间
//         */
//        String respath = "F:/benchmark/result/" + workflowtype[1]+"_"+300 + " [" + 0.2 + "," + 0.3 + "," + 0.5+ "]_"+9+".txt";
//        double totaldeadline = myalg.caltaskestearlystarttime(list);
//        myalg.caltaskestearlystarttime(list);
//        for (int k = 0; k < SDM.length; k++) {
//            for (int l = 0; l < TRM.length; l++) {
//                for (int m = 0; m < LPLTSMLocal.length; m++) {
//                    for (int n = 0; n < LPLTSMUsingExistingVm.length; n++) {
//                        for (int x = 0; x < LPLTSMLocal.length; x++) {
//                            for (int y = 0; y < LPLTSMUsingExistingVm.length; y++) {
//                                for (int p = 0; p < deadlinefactors.length; p++) {
//                                    try {
//                                        myalg.caltaskestearlystarttime(list);
//                                        myalg z = new myalg(list, SDM[k], TRM[l], LPLTSMLocal[m], LPLTSMUsingExistingVm[n], LPLTSMLocal[x], LPLTSMUsingExistingVm[y],
//                                                totaldeadline * deadlinefactors[p], 300, privacytaskpercent[3], environment2, respath, deadlinefactors[p],8);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

}

