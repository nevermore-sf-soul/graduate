package org.workflowsim.algorithms;

import org.workflowsim.Task;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TRMRandom implements baseTRM{
    @Override
    public void RankTasks(List<Task> list) {
        Collections.shuffle(list,new Random());
    }
}
