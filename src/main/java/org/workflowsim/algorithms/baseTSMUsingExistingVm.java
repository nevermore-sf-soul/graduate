package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;
import org.workflowsim.TripleValue;

import java.util.List;

public interface baseTSMUsingExistingVm {
    int ScheduleExistingVms(Environment environment, List<TripleValue> t);
}
