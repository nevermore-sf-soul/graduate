package org.workflowsim.algorithms;

import org.workflowsim.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SDMPathPLSum implements baseSDM{

    @Override
    public void Settaskssubdeadline(List<Task> list, double deadline) {
        Task head=null;
        for(Task i:list)
        {
            if(i.getParentList().size()==0)
                head=i;
        }
        for(Task i:list)
        {
            i.setSubdeadline((head.getRankavg()-i.getRankavg())/head.getRankavg()*deadline);
        }
    }
}
