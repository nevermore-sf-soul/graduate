package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;
import org.workflowsim.Vm;

public class TSMLocalEarlyFinishTime implements baseTSMLocal {
    @Override
    public int ScheduleToLocalVms(Environment environment, Task i) {
            double min=Double.MAX_VALUE;
            int keyvmid=-1;
//            for(int datacenterId=0;datacenterId<=environment.vmlocationvapl.get(i.getPrivacy_level());datacenterId++)
//            {
            for(Vm j:environment.curVmList.get(0))
            {
                double ft=environment.ComputeTaskFinishTime(i, j.getId());
                if(min>ft)
                {
                    min=ft;
                    keyvmid=j.getId();
                }
            }
//            }
        return keyvmid;
    }
}
