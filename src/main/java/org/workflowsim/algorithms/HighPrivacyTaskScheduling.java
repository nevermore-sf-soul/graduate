package org.workflowsim.algorithms;

import org.apache.commons.math3.analysis.function.Max;
import org.apache.commons.math3.util.Pair;
import org.workflowsim.*;
import org.workflowsim.utils.Parameters;

import java.util.Arrays;
import java.util.List;

public class HighPrivacyTaskScheduling {
    Environment environment;
    public HighPrivacyTaskScheduling(Environment environment){this.environment=environment;}
    public void execute(Task i)
    {
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
            environment.updateTaskShcedulingInformation(i,keyvmid,min);

    }
}
