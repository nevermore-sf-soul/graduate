package org.workflowsim.algorithms;

import org.apache.commons.math3.analysis.function.Max;
import org.apache.commons.math3.util.Pair;
import org.workflowsim.*;
import org.workflowsim.utils.Parameters;

import java.util.Arrays;
import java.util.List;

public class HighPrivacyTaskScheduling {
    public HighPrivacyTaskScheduling(Environment environment, Task i)
    {
        if(i.getParentList().size()==0)
        {
            i.setVmId(0);
            i.setStarttime(0.0);
            i.setFinishtime(0.0);
            environment.allVmList.get(0).setEarlyidletime(0.0);
            environment.vmrenthistory.get(0).add(new Pair<Double,Double>(0.0,0.0));
        }
        else if(i.getChildList().size()==0)
        {
            i.setVmId(0);
            double MaxFT=-1;
            for(Task j:i.getParentList())
            {
                MaxFT=Math.max(MaxFT,j.getFinishtime());
            }
            i.setStarttime(MaxFT);i.setFinishtime(MaxFT);
            environment.allVmList.get(0).setEarlyidletime(MaxFT);
            environment.vmrenthistory.get(0).add(new Pair<Double,Double>(MaxFT,MaxFT));
        }
        else {
            double min=Double.MAX_VALUE;
            int keyvmid=-1;
//            for(int datacenterId=0;datacenterId<=environment.vmlocationvapl.get(i.getPrivacy_level());datacenterId++)
//            {
                for(Vm j:environment.curVmList.get(0))
                {
                    double starttime=Math.max(j.getEarlyidletime(),i.gettaskEarlyStartTime());
                    double processtime=0;
                    if(i.getParentList().size()==1&&i.getParentList().get(0).getDepth()==0)
                        {
                        double datasize=i.getFileList().stream().filter(item -> Parameters.FileType.INPUT==item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).sum();
                        processtime=(datasize)/10+(i.getCloudletLength()*1.0)/(j.getCpucore()*environment.datacenterList.get(j.getDatacenterid()).getMibps());
                        }
                    else{
                        for(Task pre:i.getParentList())
                        {
                        double tempfile=0;
                            for(FileItem f1:i.getFileList())
                            {
                                for(FileItem f2:pre.getFileList())
                                {
                                    if(f1.getName().equals(f2.getName())&&f1.getType()==Parameters.FileType.INPUT&&f2.getType()==Parameters.FileType.OUTPUT)
                                    {
                                        tempfile+=f1.getSize();
                                    }
                                }
                            }
                        processtime=(tempfile)/environment.bandwidth[environment.allVmList.get(pre.getVmId()).getDatacenterid()][j.getDatacenterid()]+(i.getCloudletLength()*1.0)/(j.getCpucore()*environment.datacenterList.get(j.getDatacenterid()).getMibps());
                        }
                    }
                    if(min>starttime+processtime)
                    {
                        min=starttime+processtime;
                        keyvmid=j.getId();
                    }
                }
//            }
            environment.updateTaskShcedulingInformation(i,keyvmid,min);

        }

    }
}
