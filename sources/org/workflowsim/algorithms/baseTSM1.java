package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;
import org.workflowsim.Vm;

public interface baseTSM1 {
        Vm ScheduleToLocalVms(Environment environment, Task i);
}
