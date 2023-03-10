package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;
import org.workflowsim.Vm;

public class TSMLocalEarlyAvaiableTime implements baseTSMLocal {
    @Override
    public int ScheduleToLocalVms(Environment environment, Task i) {
        double MAT=Double.MAX_VALUE;Vm res=null;
        double ratio=i.getRankavg()/environment.head.getRankavg();
        for(int j=0;j<environment.curVmList.get(0).size()*ratio;j++)
        {
            Vm vm=environment.curVmList.get(0).get(j);
            if(MAT>vm.getEarlyidletime())
            {
                MAT=vm.getEarlyidletime();
                res=vm;
            }
        }
        return res.getId();
    }
}
