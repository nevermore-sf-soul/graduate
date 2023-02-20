package org.workflowsim.algorithms;

import org.workflowsim.Task;

import java.util.Arrays;
import java.util.List;

public class SDMDepthPLSum implements baseSDM{
    @Override
    public void Settaskssubdeadline(List<Task> list,double deadline) {
        double SlackTime=deadline-list.get(list.size()-1).getEsttasklatestfinTime();
        int[] plsum=new int[list.get(list.size()-1).getDepth()+1];
        for(Task i:list)
        {
            plsum[i.getDepth()]+=i.getPrivacy_level();
        }
        double totalpl= Arrays.stream(plsum).sum();
        for(Task i:list)
        {
            i.setSubdeadline(i.getEsttaskeralyfinTime()+SlackTime*(plsum[i.getDepth()]/totalpl));
        }

    }
}
