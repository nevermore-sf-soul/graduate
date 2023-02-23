package org.workflowsim;

import org.apache.commons.math3.util.Pair;

import java.util.*;

public class Environment {
        public List<Datacenter> datacenterList=new ArrayList<>();
        public List<Vm> allVmList=new ArrayList<>();
        public List<List<Vm>> curVmList=new ArrayList<>();// current vms in system
        public double[][] bandwidth;
        public int vmid=0;//vmid increasing automatic,when a new vm is created
        public Map<String,Double> vmprice=new HashMap<>();
        public Map<Integer,Double> maxspeed=new HashMap<>(); //不同隐私等级的任务能够获得的最大虚拟机速度
        public  Map<Integer,Integer> vmlocationvapl=new HashMap<>();
        public Map<Integer,List<Pair<Double,Double>>> vmrenthistory=new HashMap<>(); //the vm execute history,which according the unique vmId;
        public List<Task>  list;
        public String path;
        public String SDM;
        public String TRM;
        public String MPLTSM1;
        public String MPLTSM2;
        public String LPLTSM1;
        public String LPLTSM2;
        public double dealinefactor;
        public double localvmfactor;
        public int tasknum;
        public double[] ptpercentage;
        public double deadline;
        public int[] plsum;
        public List<Task> getList() {
                return list;
        }

        public void setList(List<Task> list) {
                this.list = list;
        }

        public String getPath() {
                return path;
        }

        public void setPath(String path) {
                this.path = path;
        }

        public String getSDM() {
                return SDM;
        }

        public void setSDM(String SDM) {
                this.SDM = SDM;
        }

        public String getTRM() {
                return TRM;
        }

        public void setTRM(String TRM) {
                this.TRM = TRM;
        }

        public String getMPLTSM1() {
                return MPLTSM1;
        }

        public void setMPLTSM1(String MPLTSM1) {
                this.MPLTSM1 = MPLTSM1;
        }

        public String getMPLTSM2() {
                return MPLTSM2;
        }

        public void setMPLTSM2(String MPLTSM2) {
                this.MPLTSM2 = MPLTSM2;
        }

        public String getLPLTSM1() {
                return LPLTSM1;
        }

        public void setLPLTSM1(String LPLTSM1) {
                this.LPLTSM1 = LPLTSM1;
        }

        public String getLPLTSM2() {
                return LPLTSM2;
        }

        public void setLPLTSM2(String LPLTSM2) {
                this.LPLTSM2 = LPLTSM2;
        }

        public double getDealinefactor() {
                return dealinefactor;
        }

        public void setDealinefactor(double dealinefactor) {
                this.dealinefactor = dealinefactor;
        }

        public double getLocalvmfactor() {
                return localvmfactor;
        }

        public void setLocalvmfactor(double localvmfactor) {
                this.localvmfactor = localvmfactor;
        }

        public int getTasknum() {
                return tasknum;
        }

        public void setTasknum(int tasknum) {
                this.tasknum = tasknum;
        }

        public double[] getPtpercentage() {
                return ptpercentage;
        }

        public void setPtpercentage(double[] ptpercentage) {
                this.ptpercentage = ptpercentage;
        }
        Environment(){}

        public void init(){
                vmprice.put("edge1",0.031);
                vmprice.put("edge2",0.052);
                vmprice.put("edge4",0.208);
                vmprice.put("cloud1",0.0255);
                vmprice.put("cloud2",0.0336);
                vmprice.put("cloud4",0.1344);
                vmprice.put("pedge1",0.031*1.5);
                vmprice.put("pedge2",0.052*1.5);
                vmprice.put("pedge4",0.208*1.5);
                maxspeed.put(1,400.0);
                maxspeed.put(2,12400.0);
                maxspeed.put(3,20000.0);
                vmlocationvapl.put(1,1);
                vmlocationvapl.put(2,2);vmlocationvapl.put(3,5);
                Datacenter datacenter_0=new Datacenter(0,200,0,"Datacenter_0",new ArrayList<>(),1);
                Datacenter datacenter_1=new Datacenter(12,3100,1,"Datacenter_1",new ArrayList<>(),2);
                Datacenter datacenter_2=new Datacenter(12,3100,2,"Datacenter_2",new ArrayList<>(),3);
                Datacenter datacenter_3=new Datacenter(12,3100,3,"Datacenter_3",new ArrayList<>(),3);
                Datacenter datacenter_4=new Datacenter(3000,5000,4,"Datacenter_4",new ArrayList<>(),3);
                bandwidth=new double[5][5];
                for(int i=0;i<5;i++)
                {
                        bandwidth[i][i]=80/8;
                        if(i==0){
                                bandwidth[i][1]=bandwidth[1][i]=Misc.randomDouble(50/8.0,60/8.0);bandwidth[i][2]=bandwidth[2][i]=Misc.randomDouble(50/8.0,60/8.0);bandwidth[i][3]=bandwidth[3][i]=Misc.randomDouble(50/8.0,60/8.0);bandwidth[i][4]=bandwidth[4][i]=30/8.0;
                        }
                        else if(i==1)
                        {
                                bandwidth[i][2]=bandwidth[2][i]=Misc.randomDouble(60/8.0,65/8.0);bandwidth[i][3]=bandwidth[3][i]=Misc.randomDouble(60/8.0,65/8.0);bandwidth[i][4]=bandwidth[4][i]=Misc.randomDouble(40/8.0,50/8.0);
                        }
                        else if(i==2)
                        {
                                bandwidth[i][3]=bandwidth[3][i]=Misc.randomDouble(60/8.0,65/8.0);bandwidth[i][4]=bandwidth[4][i]=Misc.randomDouble(40/8.0,50/8.0);
                        }
                        else if(i==3){
                                bandwidth[i][4]=bandwidth[4][i]=Misc.randomDouble(40/8.0,50/8.0);
                        }
                }
        };
        public void updateTaskShcedulingInformation(Task t,int vmid,double EarlyAvaiableTime)
        {
                t.setVmId(vmid);
                t.setStarttime(Math.max(t.gettaskEarlyStartTime(),allVmList.get(vmid).getEarlyidletime()));
                t.setFinishtime(EarlyAvaiableTime);
                allVmList.get(vmid).setEarlyidletime(EarlyAvaiableTime);
                updatetaskearliestlateststartTime(t);
                if(t.getFinishTime()>t.getSubdeadline())
                {
                        subdeadlineupdate(t);
                }
        }
        public void updatetaskearliestlateststartTime(Task t)
        {
                for(Task j:t.getChildList())
                {
                        if(t.getVmId()!=-1)
                        {
                                j.settaskEarlyStartTime(Math.max(j.gettaskEarlyStartTime(),t.getFinishtime()));
                        }
                        else j.settaskEarlyStartTime(Math.max(j.gettaskEarlyStartTime(),t.gettaskEralyFinTime()));
                        j.settaskEralyFinTime(j.gettaskEarlyStartTime()+j.getEstextTime());
                        updatetaskearliestlateststartTime(j);
                }
        }
        public void subdeadlineupdate(Task t)
        {
                double EFT_tail=list.get(list.size()-1).gettaskEralyFinTime();
                double SlackTime=deadline-EFT_tail;
                if(SDM.equals("SDMExecutiontimePrecent"))
                {
                        for(Task j:t.getChildList())
                        {
                                j.setSubdeadline(j.gettaskEralyFinTime()+j.gettaskEralyFinTime()/EFT_tail*SlackTime);
                                subdeadlineupdate(j);
                        }
                }
                else if(SDM.equals("SDMPathPLSum"))
                {
                        int totalpl= Arrays.stream(plsum).sum();
                        for(Task j:t.getChildList())
                        {
                                j.setSubdeadline(j.gettaskEralyFinTime()+SlackTime*(plsum[j.getDepth()]/(totalpl*1.0)));
                                subdeadlineupdate(j);
                        }
                }
        }
        public double calculateprices()
        {
                return 0;
        }
        public int createvm(int vmcpucores,int datacenterid,double earlyidletime)
        {
                Vm kvm=new Vm(vmcpucores,datacenterid,vmid++,earlyidletime,vmcpucores*datacenterList.get(datacenterid).getMibps());
                allVmList.add(kvm);
                curVmList.get(datacenterid).add(kvm);
                vmrenthistory.put(kvm.getId(),new ArrayList<>());
                return kvm.getId();
        }
        public void createlocalvms(int n)
        {
                int lvmn=(int) Math.floor((n/3.0)*2);
                int hvmn=n-lvmn;
                for(int i=0;i<lvmn;i++) createvm(1,0,0);
                for(int i=0;i<hvmn;i++) createvm(2,0,0);
        }
        public void clearvmhistory()
        {
                vmid=0;
                vmrenthistory=new HashMap<>();
                for(Datacenter i: datacenterList) i.setVms(new ArrayList<>());
                curVmList=new ArrayList<>();
                allVmList=new ArrayList<>();
        }
}
