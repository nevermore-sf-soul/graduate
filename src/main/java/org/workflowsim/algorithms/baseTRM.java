package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;

import java.util.List;

public interface baseTRM {
    public void RankTasks(Environment environment,List<Task> list);
}
