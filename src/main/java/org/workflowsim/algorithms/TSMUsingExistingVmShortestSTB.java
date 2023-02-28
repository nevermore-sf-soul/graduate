package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.TripleValue;

import java.util.List;

public class TSMUsingExistingVmShortestSTB implements baseTSMUsingExistingVm {
    @Override
    public int ScheduleExistingVms(Environment environment, List<TripleValue> t) {
        double min=Double.MAX_VALUE;int res=-1;
        for(TripleValue tripleValue:t)
        {
            double temp=tripleValue.getFinishTime()-tripleValue.getStartTime();
            if(temp<min)
            {
                min=temp;
                res=tripleValue.getfirstval();
            }
        }
        return res;
    }
}
