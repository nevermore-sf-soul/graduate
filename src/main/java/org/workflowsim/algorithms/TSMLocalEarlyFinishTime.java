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
        double ratio=i.getRankavg()/environment.head.getRankavg();
        for(int j=0;j<environment.curVmList.get(0).size()*ratio;j++)
        {
                Vm vm=environment.curVmList.get(0).get(j);
                double ft=environment.ComputeTaskFinishTime(i, vm.getId());
                if(min>ft)
                {
                    min=ft;
                    keyvmid=vm.getId();
                }
            }
//            }
        return keyvmid;
    }
}
