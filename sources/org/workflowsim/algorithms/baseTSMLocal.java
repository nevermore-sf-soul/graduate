package org.workflowsim.algorithms;

import org.apache.commons.math3.util.Pair;
import org.workflowsim.Environment;
import org.workflowsim.Task;
import org.workflowsim.Vm;

public interface baseTSMLocal {
        int ScheduleToLocalVms(Environment environment, Task i);
}
