package org.workflowsim.algorithms;

import org.workflowsim.Task;

import java.util.Comparator;
import java.util.List;

public class TRMMaxRankavg implements baseTRM{
    @Override
    public void RankTasks(List<Task> list) {
        list.sort(new Comparator<Task>() {
            @Override
            public int compare(Task task, Task t1) {
                return Double.compare(t1.getRankavg(),task.getRankavg());
            }
        });
    }
}
