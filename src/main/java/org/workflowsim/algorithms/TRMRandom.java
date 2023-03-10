package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Misc;
import org.workflowsim.Task;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class TRMRandom implements baseTRM{
    @Override
    public void RankTasks(Environment environment,List<Task> list) {

        list.sort(new Comparator<Task>() {
            @Override
            public int compare(Task task, Task t1) {
                if(task.getDepth()==t1.getDepth())
                    return Misc.randomInt(-2,2);
                else return task.getDepth()-t1.getDepth();
            }
        });
    }
}
