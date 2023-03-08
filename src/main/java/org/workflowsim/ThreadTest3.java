package org.workflowsim;

import org.workflowsim.algorithms.*;
import simulation.generator.Generator;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadTest3 {
    String workflowtype;
    Environment environment2;
    ReentrantLock reentrantLock;
    int[] tasknums = new int[]{150,200,250,300};
    double[][] privacytaskpercent = new double[][]{{0.05, 0.15, 0.8}, {0.1, 0.2, 0.7}, {0.15, 0.25, 0.55}, {0.2, 0.3, 0.5}};
    double[] bandscal=new double[]{0.1,0.2,0.3,0.4};
    ThreadTest3(String workflowtype,Environment environment,ReentrantLock reentrantLock) {
        this.workflowtype=workflowtype;environment2=environment;this.reentrantLock=reentrantLock;
    }
    public void execute() {
        String prefix = "F:/benchmark/data/";
        for (int i = 0; i < tasknums.length; i++) {
            for (int j=0;j< privacytaskpercent.length;j++) {
                for (int ins = 0; ins < 10; ins++) {
                    String datapath = new String(prefix + workflowtype+" " + tasknums[i] + " [" + privacytaskpercent[j][0] + "," + privacytaskpercent[j][1] + "," + privacytaskpercent[j][2] +"]"+" " + ins +".xml");
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
                    String respath = "F:/benchmark/result/" +workflowtype;
                    for (int p = 0; p < bandscal.length; p++) {
                        try {
//                                myalg.caltaskestearlystarttime(list);
//                                myalg z = new myalg(list,
//                                        totaldeadline * deadlinefactors[p], tasknums[i], privacytaskpercent[j], environment2, respath+" myalg.txt", deadlinefactors[p],ins);
//                                myalg.caltaskestearlystarttime(list);
//                                iheft IHEFT=new iheft(list,tasknums[i], respath+" iheft.txt",environment2,deadlinefactors[p],privacytaskpercent[j],totaldeadline * deadlinefactors[p],ins);
//                                myalg.caltaskestearlystarttime(list);
//                                mcpcpp nocloud=new mcpcpp(list,tasknums[i],respath+" mcpcpp.txt",environment2,deadlinefactors[p],privacytaskpercent[j],totaldeadline * deadlinefactors[p],ins);
                            clustering clustering=new clustering(list,tasknums[i], respath+" cluster.txt",environment2,privacytaskpercent[j],ins,bandscal[p]);
                            HEFT heft=new HEFT(list,tasknums[i], respath+" heft.txt",environment2,privacytaskpercent[j],ins,bandscal[p]);
                            nocloud nocloud=new nocloud(list,tasknums[i], respath+" nocloud.txt",environment2,privacytaskpercent[j],ins,bandscal[p]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


}

