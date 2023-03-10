package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.Task;
import org.workflowsim.Vm;

public class TSMLocalMinWaste implements baseTSMLocal {
    @Override
    public int ScheduleToLocalVms(Environment environment, Task i) {
        double min=Double.MAX_VALUE;int vmid=-1;
        double ratio=i.getRankavg()/environment.head.getRankavg();
        for(int j=0;j<environment.curVmList.get(0).size()*ratio;j++)
        {
            Vm vm=environment.curVmList.get(0).get(j);
            double WCT=Math.max(vm.getEarlyidletime(),i.gettaskEarlyStartTime())-vm.getEarlyidletime();
            double WCN=WCT*vm.getCpucore()*environment.datacenterList.get(vm.getDatacenterid()).getMibps();
            if(min>WCN)
            {
                min=WCN;
                vmid=vm.getId();
            }
        }
        return vmid;
    }
}
