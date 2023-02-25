package org.workflowsim.algorithms;

import org.workflowsim.*;

import java.util.*;

public class LNPrivacyTaskScheduling {
    Environment environment;
    public LNPrivacyTaskScheduling(Environment environment){this.environment=environment;}
    public void execute(Task i)
    {
        int keyvmid=-1;double ft=0;
        if(i.getPrivacy_level()==2)
        {
            baseTSMLocal baseTSMLocal =null;
            switch (environment.LPLTSMLocal) {
                case "TSMLocalEarlyAvaiableTime" -> {
                    baseTSMLocal = new TSMLocalEarlyAvaiableTime();
                }
                case "TSMLocalEarlyFinishTime" -> {
                    baseTSMLocal = new TSMLocalEarlyFinishTime();
                }
                case "TSMLocalMinWaste" -> {
                    baseTSMLocal = new TSMLocalMinWaste();
                }
            }
            if(baseTSMLocal==null) throw new IllegalArgumentException("TSMLocal method is not determined!");
            int vmid1= baseTSMLocal.ScheduleToLocalVms(environment,i);
            double ft1=environment.ComputeTaskFinishTime(i,vmid1);
            if(ft1>i.getSubdeadline())
            {
                List<TripleValue> t=computeSTB(i,environment.LPLTSMUsingExistingVm.equals("TSMUsingExistingVmFirstAdaptSTB"));
                baseTSMUsingExistingVm baseTSMUsingExistingVm =null;
                switch (environment.LPLTSMUsingExistingVm) {
                    case "TSMUsingExistingVmFirstAdaptSTB" -> {
                        baseTSMUsingExistingVm = new TSMUsingExistingVmFirstAdaptSTB();
                    }
                    case "TSMUsingExistingVmLongestSTB" -> {
                        baseTSMUsingExistingVm = new TSMUsingExistingVmLongestSTB();
                    }
                    case "TSMUsingExistingVmShortestSTB" -> {
                        baseTSMUsingExistingVm = new TSMUsingExistingVmShortestSTB();
                    }
                }
                if(baseTSMUsingExistingVm==null) throw new IllegalArgumentException("baseTSMUsingExistingVm method is not determined!");
                int vmid2= baseTSMUsingExistingVm.ScheduleExistingVms(environment,t);
                if(vmid2==-1)
                {
                    RentOrRenewVm rentOrRenewVm=new RentOrRenewVm();
                    String res=rentOrRenewVm.shcedule(i,environment);
                    String[] strings=res.split(" ");
                    int vmid3=-1;double ft3;
                    if(strings[0].equals("0"))
                    {
                        EFTChoose eftChoose=new EFTChoose();
                        vmid2=eftChoose.execute(environment,i);
                        double ft2;
                        if(vmid2==-1)
                        {
                            ft2=Double.MAX_VALUE;
                        }
                        else{
                            ft2=environment.ComputeTaskFinishTime(i,vmid2);
                        }
                        if(Integer.parseInt(strings[1])< environment.vmid)
                        {
                            vmid3=Integer.parseInt(strings[1]);
                            ft3=environment.ComputeTaskFinishTime(i,vmid3);
                        }
                        else{
                            ft3=Double.parseDouble(strings[3]);
                        }
                        int x=threecomparemin(ft1,ft2,ft3);
                        if(x==1)
                        {
                            ft=ft1;keyvmid=vmid1;
                        }
                        else if(x==2)
                        {
                            ft=ft2;keyvmid=vmid2;
                        }
                        else {
                            ft=ft3;
                            if(Integer.parseInt(strings[1])< environment.vmid)
                            {
                                keyvmid=vmid3;
                            }
                            else{
                                keyvmid=environment.createvm(Integer.parseInt(strings[1]),Integer.parseInt(strings[2]),ft,i.gettaskEarlyStartTime());
                                environment.allVmList.get(keyvmid).setDestoryTime((Math.ceil((ft-i.gettaskEarlyStartTime())/environment.BTU))*environment.BTU+i.gettaskEarlyStartTime());
                            }
                        }
                    }
                    else{
                        ft=environment.ComputeTaskFinishTime(i,Integer.parseInt(strings[1]));
                        keyvmid=Integer.parseInt(strings[1]);
                    }
                }
                else{
                    //找到了一个空闲时间块，那么肯定不会超出子截止期
                    ft= environment.ComputeTaskFinishTime(i,vmid2);keyvmid=vmid2;
                }
            }else{
                ft=ft1;keyvmid=vmid1;
            }
        }
        else{
                baseTSMLocal baseTSMLocal =null;
                switch (environment.NPLTSMLocal) {
                    case "TSMLocalEarlyAvaiableTime" -> {
                        baseTSMLocal = new TSMLocalEarlyAvaiableTime();
                    }
                    case "TSMLocalEarlyFinishTime" -> {
                        baseTSMLocal = new TSMLocalEarlyFinishTime();
                    }
                    case "TSMLocalMinWaste" -> {
                        baseTSMLocal = new TSMLocalMinWaste();
                    }
                }
                if(baseTSMLocal==null) throw new IllegalArgumentException("TSMLocal method is not determined!");
                int vmid1= baseTSMLocal.ScheduleToLocalVms(environment,i);
                double ft1=environment.ComputeTaskFinishTime(i,vmid1);
                if(ft1>i.getSubdeadline())
                {
                    List<TripleValue> t=computeSTB(i,environment.NPLTSMUsingExistingVm.equals("TSMUsingExistingVmFirstAdaptSTB"));
                    baseTSMUsingExistingVm baseTSMUsingExistingVm =null;
                    switch (environment.NPLTSMUsingExistingVm) {
                        case "TSMUsingExistingVmFirstAdaptSTB" -> {
                            baseTSMUsingExistingVm = new TSMUsingExistingVmFirstAdaptSTB();
                        }
                        case "TSMUsingExistingVmLongestSTB" -> {
                            baseTSMUsingExistingVm = new TSMUsingExistingVmLongestSTB();
                        }
                        case "TSMUsingExistingVmShortestSTB" -> {
                            baseTSMUsingExistingVm = new TSMUsingExistingVmShortestSTB();
                        }
                    }
                    if(baseTSMUsingExistingVm==null) throw new IllegalArgumentException("baseTSMUsingExistingVm method is not determined!");
                    int vmid2= baseTSMUsingExistingVm.ScheduleExistingVms(environment,t);
                    if(vmid2==-1)
                    {
                        RentOrRenewVm rentOrRenewVm=new RentOrRenewVm();
                        String res=rentOrRenewVm.shcedule(i,environment);
                        String[] strings=res.split(" ");
                        int vmid3=-1;double ft3;
                        if(strings[0].equals("0"))
                        {
                            EFTChoose eftChoose=new EFTChoose();
                            vmid2=eftChoose.execute(environment,i);
                            double ft2;
                            if(vmid2==-1)
                            {
                                ft2=Double.MAX_VALUE;
                            }
                            else{
                                ft2=environment.ComputeTaskFinishTime(i,vmid2);
                            }
                            if(Integer.parseInt(strings[1])< environment.vmid)
                            {
                                vmid3=Integer.parseInt(strings[1]);
                                ft3=environment.ComputeTaskFinishTime(i,vmid3);
                            }
                            else{
                                ft3=Double.parseDouble(strings[3]);
                            }
                            int x=threecomparemin(ft1,ft2,ft3);
                            if(x==1)
                            {
                                ft=ft1;keyvmid=vmid1;
                            }
                            else if(x==2)
                            {
                                ft=ft2;keyvmid=vmid2;
                            }
                            else {
                                ft=ft3;
                                if(Integer.parseInt(strings[1])< environment.vmid)
                                {
                                    keyvmid=vmid3;
                                }
                                else{
                                    keyvmid=environment.createvm(Integer.parseInt(strings[1]),Integer.parseInt(strings[2]),ft,i.gettaskEarlyStartTime());
                                    environment.allVmList.get(keyvmid).setDestoryTime((Math.ceil((ft-i.gettaskEarlyStartTime())/environment.BTU))*environment.BTU+i.gettaskEarlyStartTime());
                                }
                            }
                        }
                        else{
                            ft=environment.ComputeTaskFinishTime(i,Integer.parseInt(strings[1]));
                            keyvmid=Integer.parseInt(strings[1]);
                        }
                    }
                    else{
                        //找到了一个空闲时间块，那么肯定不会超出子截止期
                        ft= environment.ComputeTaskFinishTime(i,vmid2);keyvmid=vmid2;
                    }
                }else{
                    ft=ft1;keyvmid=vmid1;
                }
        }
        environment.updateTaskShcedulingInformation(i,keyvmid,ft);
    }
    public List<TripleValue> computeSTB(Task i,boolean flag)
    {
        List<TripleValue> res=new ArrayList<>();//三值分别为虚拟机id，最后一个空闲时间块开始时间，结束时间
        for(int datacenterId=1;datacenterId<=environment.vmlocationvapl.get(i.getPrivacy_level());datacenterId++)
        {
            for(Vm vm:environment.curVmList.get(datacenterId))
            {
                double STBstartTime=vm.getEarlyidletime();
                double STBendTime=vm.getDestoryTime();
                double ft=environment.ComputeTaskFinishTime(i,vm.getId());
                if(STBstartTime<=i.gettaskLatestStartTime()&&ft<=i.getSubdeadline()&&ft<=STBendTime)
                {
                    res.add(new TripleValue(vm.getId(),STBstartTime,STBendTime));
                    if(flag) return res;
                }
            }
        }
        return res;
    }


    public int threecomparemin(double t1,double t2,double t3)
    {
        if(t1<t2)
        {
            if(t1<t3)
            {
                return 1;
            }
            else {
                return 3;
            }
        }
        else{
            if(t3>t1) return 2;
            else
            {
                if(t3>t2) return 2;
                else return 3;
            }
        }
    }

    public static void main(String[] args) {

    }
}
