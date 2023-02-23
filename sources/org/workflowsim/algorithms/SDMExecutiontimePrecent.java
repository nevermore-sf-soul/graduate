package org.workflowsim.algorithms;

import org.workflowsim.Task;

import java.util.List;

public class SDMExecutiontimePrecent implements baseSDM{
    @Override
    public void Settaskssubdeadline(List<Task> list, double deadline) {
        double EFT_tail=list.get(list.size()-1).getEsttaskeralyfinTime();
        double SlackTime=deadline-list.get(list.size()-1).getEsttasklatestfinTime();
        for(Task i:list)
        {
            i.setSubdeadline(i.getEsttaskeralyfinTime()+i.getEsttaskeralyfinTime()/EFT_tail*SlackTime);

        }
    }
}
