package org.workflowsim;

import simulation.generator.Generator;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class threadTest implements Runnable{
    double[] privacytaskpercent;
    int tasknum;
    int ins;
    String workflowtype;
    Environment environment2;
    String[] SDM = new String[]{"SDMDepthPLSum", "SDMPathPLSum", "SDMExecutiontimePercent"};
    String[] TRM = new String[]{"TRMMaxRankavg", "TRMMinFloatTime", "TRMTaskFeature"};
    String[] LPLTSMLocal = new String[]{"TSMLocalMinWaste", "TSMLocalEarlyAvaiableTime", "TSMLocalEarlyFinishTime"};
    String[] LPLTSMUsingExistingVm = new String[]{"TSMUsingExistingVmFirstAdaptSTB", "TSMUsingExistingVmLongestSTB", "TSMUsingExistingVmShortestSTB"};
    double[] deadlinefactors = new double[]{1.5, 1.6, 1.7, 1.8, 1.9};
    threadTest(int a,int b,double[] c,String workflowtype,Environment environment) {
        tasknum=a;ins=b;privacytaskpercent=c;this.workflowtype=workflowtype;environment2=environment;
    }

    @Override
    public void run() {
        String prefix = "F:/benchmark/data/";
        String datapath = new String(prefix + "CyberShake_" + tasknum + " [" + privacytaskpercent[0] + "," + privacytaskpercent[1] + "," + privacytaskpercent[2] + "_" + ins + "].xml");
        Generator generator = new Generator();
        try {
            generator.execute(datapath, tasknum, privacytaskpercent, workflowtype);
        } catch (Exception e) {
            e.printStackTrace();
        }
        myparser workflowParser = new myparser(datapath, new myreplicalog());
        workflowParser.parse();
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
        /**
         * 估计任务执行时间
         */
        myalg.esttaskexuteTime(list, environment2);
        /**
         * 确定工作流合理截止期，估计任务最早开始时间、最早结束时间
         */
        String respath = "F:/benchmark/result/" + tasknum + " [" + privacytaskpercent[0] + "," + privacytaskpercent[1] + "," + privacytaskpercent[2] + "_" + ins + "].txt";
        double totaldeadline = myalg.caltaskestearlystarttime(list);

        for (int k = 0; k < SDM.length; k++) {
            for (int l = 0; l < TRM.length; l++) {
                for (int m = 0; m < LPLTSMLocal.length; m++) {
                    for (int n = 0; n < LPLTSMUsingExistingVm.length; n++) {
                        for (int x = 0; x < LPLTSMLocal.length; x++) {
                            for (int y = 0; y < LPLTSMUsingExistingVm.length; y++) {
                                for (int p = 0; p < deadlinefactors.length; p++) {
                                    try {
                                        myalg z = new myalg(list, SDM[k], TRM[l], LPLTSMLocal[m], LPLTSMUsingExistingVm[n], LPLTSMLocal[x], LPLTSMUsingExistingVm[y],
                                                totaldeadline * deadlinefactors[p], tasknum, privacytaskpercent, environment2, respath, deadlinefactors[p]);
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

