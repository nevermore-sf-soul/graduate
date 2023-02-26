package org.workflowsim;

import simulation.generator.Generator;

import java.util.concurrent.CountDownLatch;

public class generatethread implements Runnable{
    String path; int tasknum;double[] percent;String workflowtype;
    CountDownLatch countDownLatch;
    generatethread(String path, int tasknum, double[] percent, String workflowtype,CountDownLatch countDownLatch)
    {
        this.percent=percent;this.path=path;
        this.tasknum=tasknum;
        this.workflowtype=workflowtype;
        this.countDownLatch=countDownLatch;
    }
    @Override
    public void run() {
        Generator generator = new Generator();
        try {
            generator.execute(path, tasknum, percent, workflowtype);
        } catch (Exception e) {
            e.printStackTrace();
        }
        countDownLatch.countDown();
    }
}
