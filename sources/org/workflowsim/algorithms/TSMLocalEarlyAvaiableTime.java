package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;
import org.workflowsim.Vm;

public class TSMLocalEarlyAvaiableTime implements baseTSMLocal {
    @Override
    public int ScheduleToLocalVms(Environment environment, Task i) {
        double MAT=Double.MAX_VALUE;Vm res=null;
        for(Vm vm:environment.curVmList.get(0))
        {
            if(MAT>vm.getEarlyidletime())
            {
                MAT=vm.getEarlyidletime();
                res=vm;
            }
        }
        return res.getId();
    }
}
