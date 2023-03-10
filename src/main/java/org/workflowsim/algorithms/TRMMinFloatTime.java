package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;

import java.util.Comparator;
import java.util.List;

public class TRMMinFloatTime implements baseTRM{
    @Override
    public void RankTasks(Environment environment,List<Task> list) {
        list.sort(new Comparator<Task>() {
            @Override
            public int compare(Task task, Task t1) {
                if(task.getDepth()==t1.getDepth())
                return Double.compare(task.gettaskLatestStartTime()-task.gettaskEarlyStartTime(),t1.gettaskLatestStartTime()-t1.gettaskEarlyStartTime());
                else return task.getDepth()-t1.getDepth();
            }
        });
    }
}
