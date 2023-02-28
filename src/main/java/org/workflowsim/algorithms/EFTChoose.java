package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;
import org.workflowsim.Vm;

public class EFTChoose {
    public EFTChoose() {
    }

    public int execute(Environment environment, Task i) {
        double min = Double.MAX_VALUE;
        int keyvmid = -1;
        for (int datacenterId = 1; datacenterId <= environment.vmlocationvapl.get(i.getPrivacy_level()); datacenterId++) {
            for (Vm j : environment.curVmList.get(datacenterId)) {
                double ft = environment.ComputeTaskFinishTime(i, j.getId());
                if (min > ft) {
                    min = ft;
                    keyvmid = j.getId();
                }
            }
//            }
        }
        return keyvmid;
    }
}
