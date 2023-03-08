package org.workflowsim.algorithms;

import org.workflowsim.Environment;
import org.workflowsim.FileItem;
import org.workflowsim.Task;
import org.workflowsim.Vm;
import org.workflowsim.utils.Parameters;

import java.util.ArrayList;
import java.util.List;

public class RentOrNewMCP {
    public RentOrNewMCP(){}
    public String shcedule(Task i, Environment environment)
    {
        double mincostnew=Double.MAX_VALUE;
        double mincostRenew=Double.MAX_VALUE;
        StringBuilder sb=new StringBuilder();
        int ds=0;if(i.getPrivacy_level()==1) {
    } else if(i.getPrivacy_level()==2) ds=1;
        else ds= environment.pedgenum+1;
        int vmid1=-1;double ft1,ft2=0;
            for(Vm vm:environment.allVmList)
            {
                if(vm.getDatacenterid()<=environment.vmlocationvapl.get(i.getPrivacy_level()))
                {
                    if(vm.getDestoryTime()<i.gettaskEarlyStartTime()||vm.getDestoryTime()>i.gettaskEarlyStartTime()&&vm.getCreateTime()>i.gettaskLatestFinTime()) continue;
                    double RFT=vm.getDestoryTime();
                    double EAT=vm.getEarlyidletime();
                    ft1=environment.ComputeTaskFinishTime(i,vm.getId());
                    if(ft1<=i.gettaskLatestFinTime())
                    {
                        double renewFee=Math.ceil((ft1-RFT)/environment.BTU)*vm.getPrice();
                        if(mincostRenew>renewFee) {
                            mincostRenew=renewFee;vmid1=vm.getId();
                        }
                    }
                }
        }
        if(vmid1!=-1) {
            sb.append(1+" ");
            sb.append(vmid1);
            return sb.toString();
        }
        int kdatacenterid=0;int kcpucore=0;
        for(int datacenterid=ds;datacenterid<=environment.vmlocationvapl.get(i.getPrivacy_level());datacenterid++)
        {
            int[] cpucores=new int[]{1,2,4};
            for(int cpucore:cpucores)
            {
                int allcores=0;
                for(Vm t:environment.allVmList)
                {
                    if(t.getDatacenterid()==datacenterid&&t.getCreateTime()<=i.gettaskEarlyStartTime()&&t.getDestoryTime()>=i.gettaskEralyFinTime())
                    {
                        allcores+=t.getCpucore();
                    }
                }
                if(environment.datacenterList.get(datacenterid).getCpucores()-allcores>=cpucore)
                {
                    double starttime=i.gettaskEarlyStartTime();
                    double processtime=0;
                    if(i.getParentList().size()==1&&i.getParentList().get(0).getCloudletId()==environment.head.getCloudletId())
                    {
                        double datasize=i.getFileList().stream().filter(item -> Parameters.FileType.INPUT==item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).sum();
                        processtime=(datasize)/10+(i.getCloudletLength()*1.0)/(cpucore*environment.datacenterList.get(datacenterid).getMibps());
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
                            if(pre.getVmId()!=-1)
                            processtime=Math.max(processtime,(tempfile)/environment.bandwidth[environment.allVmList.get(pre.getVmId()).getDatacenterid()][datacenterid]+(i.getCloudletLength()*1.0)/(cpucore*environment.datacenterList.get(datacenterid).getMibps()));
                        }
                    }
                    ft2=starttime+processtime;
                    if(ft2<=i.gettaskLatestFinTime())
                    {
                        double newFee=Math.ceil(processtime/environment.BTU)*environment.vmprice.get(environment.datacenterList.get(datacenterid).getTag()+cpucore);
                        if(mincostnew>newFee)
                        {
                            mincostnew=newFee;
                            kdatacenterid=datacenterid;kcpucore=cpucore;
                        }
                        break;
                    }
                }
            }
        }
        if(mincostnew==Double.MAX_VALUE&&mincostRenew==Double.MAX_VALUE)
        {
            double minft1=Double.MAX_VALUE;
            double minft2=Double.MAX_VALUE;
            for(int datacenterid=ds;datacenterid<=environment.vmlocationvapl.get(i.getPrivacy_level());datacenterid++)
            {
                for(Vm vm:environment.curVmList.get(datacenterid))
                {
                    double RFT=vm.getDestoryTime();
                    double EAT=vm.getEarlyidletime();
                    double FT=environment.ComputeTaskFinishTime(i,vm.getId());
                    if(FT<=minft1)
                    {
                        minft1=FT;
                        vmid1=vm.getId();
                    }
                }
            }
            for(int datacenterid=ds;datacenterid<=environment.vmlocationvapl.get(i.getPrivacy_level());datacenterid++)
            {
                int cpucore=4;
                int allcores=0;
                for(Vm t:environment.allVmList)
                {
                    if(t.getDatacenterid()==datacenterid&&t.getCreateTime()<=i.gettaskEarlyStartTime()&&t.getDestoryTime()>=i.gettaskEralyFinTime())
                    {
                        allcores+=t.getCpucore();
                    }
                }
                if(environment.datacenterList.get(datacenterid).getCpucores()-allcores>=cpucore)
                {
                    double starttime=i.gettaskEarlyStartTime();
                    double processtime=0;
                    if(i.getParentList().size()==1&&i.getParentList().get(0).getCloudletId()==environment.head.getCloudletId())
                    {
                        double datasize=i.getFileList().stream().filter(item -> Parameters.FileType.INPUT==item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).sum();
                        processtime=(datasize)/10+(i.getCloudletLength()*1.0)/(cpucore*environment.datacenterList.get(datacenterid).getMibps());
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
                            if(pre.getVmId()!=-1)
                            processtime=Math.max(processtime,(tempfile)/environment.bandwidth[environment.allVmList.get(pre.getVmId()).getDatacenterid()][datacenterid]+(i.getCloudletLength()*1.0)/(cpucore*environment.datacenterList.get(datacenterid).getMibps()));
                        }
                    }
                    double ft=starttime+processtime;
                    if(ft<minft2)
                    {
                        minft2=ft;
                        kdatacenterid=datacenterid;kcpucore=cpucore;
                    }
                }
            }
            sb.append(0+" ");
            if(minft1<minft2)
            {
                sb.append(vmid1);
            }
            else{
                sb.append(kcpucore+" "+kdatacenterid+" "+minft2);
            }
        }
        else{
            sb.append(1+" ");
            int id=environment.createvm(kcpucore,kdatacenterid,ft2,i.gettaskEarlyStartTime());
            environment.allVmList.get(id).setDestoryTime((Math.ceil((ft2-i.gettaskEarlyStartTime())/environment.BTU))*environment.BTU+i.gettaskEarlyStartTime());
            sb.append(id);
        }
        return sb.toString();
    }
}
