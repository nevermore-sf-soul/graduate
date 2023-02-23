package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;

import java.util.Comparator;
import java.util.List;

public class TRMMaxRankavg implements baseTRM{
    @Override
    public void RankTasks(Environment environment) {
        List<Task> list=environment.list;
        list.sort(new Comparator<Task>() {
            @Override
            public int compare(Task task, Task t1) {
                if(task.getDepth()==t1.getDepth())
                return Double.compare(t1.getRankavg(),task.getRankavg());
                else return task.getDepth()-t1.getDepth();
            }
        });
    }
}
