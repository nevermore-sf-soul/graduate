package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;

import java.util.Arrays;
import java.util.List;

public class SDMDepthPLSum implements baseSDM{
    @Override
    public void Settaskssubdeadline(Environment environment) {
        List<Task> list=environment.list;double deadline=environment.deadline;int[] plsum=environment.plsum;
        double SlackTime=deadline-list.get(list.size()-1).gettaskEralyFinTime();
        int totalpl= Arrays.stream(plsum).sum();
        for(Task i:list)
        {
            i.setSubdeadline(i.gettaskEralyFinTime()+SlackTime*(plsum[i.getDepth()]/(totalpl*1.0)));
        }
    }
}
