package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;

import java.util.List;

public class SDMExecutiontimePrecent implements baseSDM{
    @Override
    public void Settaskssubdeadline(Environment environment) {
        List<Task> list=environment.list;double deadline=environment.deadline;int[] plsum=environment.plsum;
        double EFT_tail=list.get(list.size()-1).gettaskEralyFinTime();
        double SlackTime=deadline-list.get(list.size()-1).gettaskEralyFinTime();
        for(Task i:list)
        {
            i.setSubdeadline(i.gettaskEralyFinTime()+i.gettaskEralyFinTime()/EFT_tail*SlackTime);

        }
    }
}
