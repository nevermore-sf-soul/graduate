package org.workflowsim.algorithms;

import org.apache.commons.math3.util.Pair;
import org.workflowsim.*;
import org.workflowsim.utils.Parameters;

import java.util.Arrays;
import java.util.List;

public class HighPrivacyTaskScheduling {

//    HighPrivacyTaskScheduling(myalg m, Task i)
//    {
//        if(i.getParentList().size()==0)
//        {
//            i.setVmId(0);
//            i.setStarttime(0.0);
//            i.setFinishtime(0.0);
//            m.environment.allVmList.get(0).setEarlyidletime(0.0);
//            m.environment.vmrenthistory.get(0).add(new Pair<Double,Double>(0.0,0.0));
//        }
//        else if(i.getChildList().size()==0)
//        {
//
//        }
//        double min=Double.MAX_VALUE;
//        for(Vm j:m.environment.curVmList.get(0))
//        {
//            double st=Math.max(j.getEarlyidletime(),i.getEsttaskearlystartTime());
//
//            else if(i.getParentList().size()==1&&i.getParentList().get(0).getDepth()==0)
//            {
//
//            }
//            else{
//                double maxfilesize=0;
//                double[] tempin=new double[i.getParentList().size()];
//                /**
//                 * We set the bandwidth within a Datacenter is 80Mbps,which is the max bandwidth,
//                 * So,Wherever the task is scheduled,the estbandwidth is 80.
//                 */
//                for(FileItem j:i.getFileList())
//                {
//                    if(j.getType()==Parameters.FileType.INPUT)
//                    {
//                        for(int z=0;z<i.getParentList().size();z++)
//                        {
//                            if(i.getParentList().get(z).getFileList().stream().anyMatch(item -> item.getName().equals(j.getName())&&item.getType()==Parameters.FileType.OUTPUT))
//                            {
//                                tempin[z]+=j.getSize();
//                            }
//                        }
//                    }
//                }
//                maxfilesize= Arrays.stream(tempin).max().orElse(0);
//                double maxsize=i.getFileList().stream().filter(item -> Parameters.FileType.OUTPUT==item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).max().orElse(0);
//                i.setEstextTime((maxfilesize+maxsize)/10+i.getCloudletLength()/m.environment.maxspeed.get(i.getPrivacy_level()));
//            }
//        }
//    }
}
