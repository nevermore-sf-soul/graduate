package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.TripleValue;

import java.util.List;

public class TSMUsingExistingVmFirstAdaptSTB implements baseTSMUsingExistingVm {
    @Override
    public int ScheduleExistingVms(Environment environment, List<TripleValue> t) {
        if(t.size()==0) return -1;
        return t.get(0).getfirstval();
    }
}
