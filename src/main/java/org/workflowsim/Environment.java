package org.workflowsim;

import org.apache.commons.math3.util.Pair;
import org.workflowsim.utils.Parameters;

import java.util.*;

public class Environment {
        public List<Datacenter> datacenterList=new ArrayList<>();
        public List<Vm> allVmList=new ArrayList<>();
        public int edgenum,pedgenum;
        public List<List<Vm>> curVmList=new ArrayList<>();// current vms in system
        public double[][] bandwidth;
        public double[][] maxbandwidth;
        public double BTU=60;
        public int vmid=0;//vmid increasing automatic,when a new vm is created
        public Map<String,Double> vmprice=new HashMap<>();
        public Map<Integer,Double> maxspeed=new HashMap<>(); //不同隐私等级的任务能够获得的最大虚拟机速度
        public  Map<Integer,Integer> vmlocationvapl=new HashMap<>();
        public Map<Integer,List<TripleValue>> vmrenthistory=new HashMap<>(); //map中key为vmid，val为三值List，三值分别为任务id，任务开始时间，结束时间
        public List<Task>  list;
        public String path;
        public String SDM;
        public String TRM;
        public String LPLTSMLocal;
        public String LPLTSMUsingExistingVm;
        public String NPLTSMLocal;
        public String NPLTSMUsingExistingVm;
        public Map<Integer,Task> taskvaTaskid=new HashMap<>();

        public Map<Integer, Task> getTaskvaTaskid() {
                return taskvaTaskid;
        }

        public void setTaskvaTaskid(Map<Integer, Task> taskvaTaskid) {
                this.taskvaTaskid = taskvaTaskid;
        }

        public String getLPLTSMLocal() {
                return LPLTSMLocal;
        }

        public void setLPLTSMLocal(String LPLTSMLocal) {
                this.LPLTSMLocal = LPLTSMLocal;
        }



        public String getNPLTSMLocal() {
                return NPLTSMLocal;
        }

        public void setNPLTSMLocal(String NPLTSMLocal) {
                this.NPLTSMLocal = NPLTSMLocal;
        }

        public String getLPLTSMUsingExistingVm() {
                return LPLTSMUsingExistingVm;
        }

        public void setLPLTSMUsingExistingVm(String LPLTSMUsingExistingVm) {
                this.LPLTSMUsingExistingVm = LPLTSMUsingExistingVm;
        }

        public String getNPLTSMUsingExistingVm() {
                return NPLTSMUsingExistingVm;
        }

        public void setNPLTSMUsingExistingVm(String NPLTSMUsingExistingVm) {
                this.NPLTSMUsingExistingVm = NPLTSMUsingExistingVm;
        }

        public double dealinefactor;
        public double localvmfactor;
        public int tasknum;
        public double[] ptpercentage;
        public double deadline;
        public int[] plsum;
        public Task head;public Task tail;
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
        public Environment(){}

        public void init1(){
                vmprice.put("local1",0.0);
                vmprice.put("local2",0.0);
                vmprice.put("edge1",0.031/60);
                vmprice.put("edge2",0.052/60);
                vmprice.put("edge4",0.208/60);
                vmprice.put("cloud1",0.0255/60);
                vmprice.put("cloud2",0.0336/60);
                vmprice.put("cloud4",0.1344/60);
                vmprice.put("pedge1",0.031*1.5/60);
                vmprice.put("pedge2",0.052*1.5/60);
                vmprice.put("pedge4",0.208*1.5/60);
                maxspeed.put(1,400.0);
                maxspeed.put(2,12400.0);
                maxspeed.put(3,20000.0);
                vmlocationvapl.put(1,0);
                vmlocationvapl.put(2,pedgenum);
                vmlocationvapl.put(3,2+pedgenum+edgenum-1);
                bandwidth=new double[2+pedgenum+edgenum][2+pedgenum+edgenum];
                double internaledge=0;
                for(int i=0;i<2+pedgenum+edgenum;i++)
                {
                        for(int j=i;j<2+pedgenum+edgenum;j++)
                        {
                                if(i==j) bandwidth[i][i]=10;
                                else if(i==0)
                                {
                                        if(j<=(pedgenum+edgenum))
                                        {
                                                bandwidth[j][i]=bandwidth[i][j]=Misc.randomDouble(50/8.0,60/8.0);
                                        }
                                        else{
                                                bandwidth[j][i]=bandwidth[i][j]=30/8.0;
                                        }
                                }
                                else if(i<=(pedgenum+edgenum))
                                {
                                        if(j<=(pedgenum+edgenum))
                                        {
                                                bandwidth[i][j]=bandwidth[j][i]=Misc.randomDouble(60/8.0,65/8.0);
                                                internaledge=Math.max(internaledge,bandwidth[i][j]);
                                        }
                                        else{
                                                bandwidth[i][j]=bandwidth[j][i]=Misc.randomDouble(40/8.0,50/8.0);
                                        }
                                }
                        }
                }
                maxbandwidth=new double[4][4];
                double max1=-1;
                for(int i=1;i<=(pedgenum+edgenum);i++)
                {
                        max1=Math.max(max1,bandwidth[0][i]);
                }
                maxbandwidth[1][2]=maxbandwidth[2][1]=max1;
                max1=-1;
                for(int i=1;i<=(pedgenum+edgenum);i++)
                {
                        max1=Math.max(max1,bandwidth[i][1+pedgenum+edgenum]);
                }
                maxbandwidth[2][3]=maxbandwidth[3][2]=max1;
                maxbandwidth[1][3]=maxbandwidth[3][1]=30/8.0;
                maxbandwidth[1][1]=10;maxbandwidth[2][2]=internaledge;maxbandwidth[3][3]=10;
        }
        public void init2()
        {
                int localdcnum=0;
                Datacenter datacenter_0=new Datacenter(0,200,localdcnum++,"Datacenter_0",new ArrayList<>(),1,"local");
                datacenterList.add(datacenter_0);
                for(int i=0;i<pedgenum;i++)
                {
                        Datacenter d=new Datacenter(12,3100,localdcnum++,"Datacenter_"+(localdcnum-1),new ArrayList<>(),2,"pedge");
                        datacenterList.add(d);
                }
                for(int i=0;i<edgenum;i++)
                {
                        Datacenter d=new Datacenter(12,3100,localdcnum++,"Datacenter_"+(localdcnum-1),new ArrayList<>(),3,"edge");
                        datacenterList.add(d);
                }
                Datacenter datacenter_4=new Datacenter(3000,5000,localdcnum,"Datacenter_"+localdcnum,new ArrayList<>(),3,"cloud");
                datacenterList.add(datacenter_4);
                for(int i=0;i<2+pedgenum+edgenum;i++) curVmList.add(new ArrayList<>());
        }
        public void updateTaskShcedulingInformation(Task t,int vmid,double EarlyAvaiableTime)
        {
                t.setVmId(vmid);
                t.setStarttime(Math.max(t.gettaskEarlyStartTime(),allVmList.get(vmid).getEarlyidletime()));
                t.setFinishtime(EarlyAvaiableTime);
                if(allVmList.get(vmid).getDestoryTime()<EarlyAvaiableTime)
                {
                        double exceedtime=EarlyAvaiableTime-allVmList.get(vmid).getDestoryTime();
                        allVmList.get(vmid).setDestoryTime(allVmList.get(vmid).getDestoryTime()+Math.ceil(exceedtime/BTU)*BTU);
                }
                allVmList.get(vmid).setEarlyidletime(EarlyAvaiableTime);
                updatetaskearliestlateststartTime(t);
                List<TripleValue> temp=vmrenthistory.getOrDefault(vmid,new ArrayList<>());
                temp.add(new TripleValue(t.getCloudletId(),t.getStarttime(),t.getFinishtime()));
                vmrenthistory.put(vmid,temp);
                if(t.getSubdeadline()!=0&&t.getFinishTime()>t.getSubdeadline())
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
                double res=0;
                for(Vm vm:allVmList)
                {
                     if(datacenterList.get(vm.getDatacenterid()).getPrivacylevel()!=1)
                     {
                             res+=Math.ceil(vm.getDestoryTime()-vm.getCreateTime())/BTU*vm.getPrice();
                     }
                }
                return res;
        }
        public double calculateprices2()
        {
                double res=0;
                for(Vm vm:allVmList)
                {
                        if(datacenterList.get(vm.getDatacenterid()).getPrivacylevel()!=1)
                        {
                             List<TripleValue> tripleValues=vmrenthistory.get(vm.getId());
                             tripleValues.sort(new Comparator<TripleValue>() {
                                     @Override
                                     public int compare(TripleValue tripleValue, TripleValue t1) {
                                             return Double.compare(tripleValue.getStartTime(), t1.getStartTime());
                                     }
                             });
                             double st=0;double et=0;double curfee=0;
                             for(TripleValue tripleValue:tripleValues){
                                if(tripleValue.getStartTime()>et)
                                {
                                        curfee+=(Math.ceil(et-st)/BTU*vm.getPrice());
                                        st=tripleValue.getStartTime();
                                        et=tripleValue.getStartTime()+Math.ceil((tripleValue.getFinishTime()-tripleValue.getStartTime())/BTU)*BTU;
                                }
                                else{
                                        if(tripleValue.getFinishTime()>et)
                                        {
                                                et=et+Math.ceil((tripleValue.getFinishTime()-et)/BTU)*BTU;
                                        }
                                }
                             }
                             curfee+=(Math.ceil(et-st)/BTU*vm.getPrice());
                             res+=curfee;
                        }
                }
                return res;
        }

        public int createvm(int vmcpucores,int datacenterid,double earlyidletime,double createtime)
        {
                Vm kvm=new Vm(vmcpucores,datacenterid,vmid++,earlyidletime,vmcpucores*datacenterList.get(datacenterid).getMibps());
                allVmList.add(kvm);
                curVmList.get(datacenterid).add(kvm);
                kvm.setCreateTime(createtime);
                vmrenthistory.put(kvm.getId(),new ArrayList<>());
                datacenterList.get(datacenterid).setUseablecores(datacenterList.get(datacenterid).getUseablecores()-vmcpucores);
                kvm.setPrice(vmprice.get(datacenterList.get(datacenterid).getTag()+vmcpucores));
                return kvm.getId();
        }
        public void createlocalvms()
        {
                for(int i=0;i<2;i++) {int id=createvm(1,0,0,0);allVmList.get(id).destoryTime=Double.MAX_VALUE;allVmList.get(id).createTime=0;}
                for(int i=0;i<3;i++) {int id=createvm(2,0,0,0);allVmList.get(id).destoryTime=Double.MAX_VALUE;allVmList.get(id).createTime=0;}
        }
        public void createlocalvms(int n)
        {
                int a= (int) Math.ceil(n/3.0),b=n-a;
                for(int i=0;i<b;i++) {int id=createvm(1,0,0,0);allVmList.get(id).destoryTime=Double.MAX_VALUE;allVmList.get(id).createTime=0;}
                for(int i=0;i<a;i++) {int id=createvm(2,0,0,0);allVmList.get(id).destoryTime=Double.MAX_VALUE;allVmList.get(id).createTime=0;}
        }
        public void clearvmhistory()
        {
                vmid=0;
                vmrenthistory=new HashMap<>();
                for(Datacenter i: datacenterList) {i.setVms(new ArrayList<>());i.setUseablecores(i.getCpucores());}
                curVmList=new ArrayList<>();
                for(int i=0;i<2+pedgenum+edgenum;i++) curVmList.add(new ArrayList<>());
                for(Task i:list)
                {
                        i.setSubdeadline(-1);
                        i.setStarttime(-1);
                        i.setFinishtime(-1);
                        i.setVmId(-1);
                        i.settaskLatestStartTime(-1);
                        i.settaskLatestFinTime(-1);
                }
                allVmList=new ArrayList<>();
        }
        public double ComputeTaskFinishTime(Task i,int vmid)
        {
                Vm j=allVmList.get(vmid);
                double starttime=Math.max(j.getEarlyidletime(),i.gettaskEarlyStartTime());
                double processtime=computeprocesstime(i,vmid);
                return starttime+processtime;
        }
        public double computeprocesstime(Task i,int vmid)
        {
                Vm j=allVmList.get(vmid);
                double processtime=0;
                if(i.getParentList().size()==1&&i.getParentList().get(0).getCloudletId()==head.getCloudletId())
                {
                        double datasize=i.getFileList().stream().filter(item -> Parameters.FileType.INPUT==item.getType()).map(FileItem::getSize).mapToDouble(Double::doubleValue).sum();
                        processtime=(datasize)/10+(i.getCloudletLength()*1.0)/(j.getCpucore()*datacenterList.get(j.getDatacenterid()).getMibps());
                }
                else{
                        for(Task pre:i.getParentList())
                        {
                                double tempfile=0;
                                if(pre.getVmId()==vmid)
                                {
                                        tempfile=0;
                                }
                                else if(pre.getVmId()!=-1){
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
                                }
                                if(pre.getVmId()!=-1)
                                processtime=Math.max(processtime,(tempfile)/bandwidth[allVmList.get(pre.getVmId()).getDatacenterid()][j.getDatacenterid()]+(i.getCloudletLength()*1.0)/(j.getCpucore()*datacenterList.get(j.getDatacenterid()).getMibps()));
                        }
                }
                return processtime;
        }
        public void destoryVm(int depeth)
        {
                double MinEST=list.stream().filter(task -> task.getDepth()==depeth&&task.getVmId()==-1).mapToDouble(Task::gettaskEarlyStartTime).min().orElse(-1);
                for(int d=1;d<=(1+pedgenum+edgenum);d++)
                {
                    Iterator<Vm> iterator=curVmList.get(d).iterator();
                    while(iterator.hasNext())
                    {
                       Vm vm=iterator.next();
                            if(vm.destoryTime<=MinEST)
                            {
                                releaseVm(vm.getId());
                                iterator.remove();
                            }
                    }
                }
        }

        public void releaseVm(int Vmid) {
                Datacenter datacenter=datacenterList.get(allVmList.get(Vmid).getDatacenterid());
                datacenter.setUseablecores(datacenter.getUseablecores()+allVmList.get(Vmid).cpucore);
        }
        public void adjustSchedulingResult()
        {
                Map<Integer,TreeMap<Double,Double>> AllSTB=computeAllSTB();
                computeallvmorder();
                allVmList.sort(new Comparator<Vm>() {
                        @Override
                        public int compare(Vm vm, Vm t1) {
                                int d1=datacenterList.get(vm.getDatacenterid()).getPrivacylevel();
                                int d2=datacenterList.get(t1.getDatacenterid()).getPrivacylevel();
                                if(d1==d2)
                                {
                                        return Double.compare(vm.rankorder,t1.rankorder);
                                }
                                else return d1-d2;
                        }
                });
                for(int i=0;i<allVmList.size();i++)
                {
                        if(allVmList.get(i).getDatacenterid()==0)
                        {
                                continue;
                        }
                        Vm vm=allVmList.get(i);
                                //<taskid,<vmid,[STBstarttime,STBendtime,startadj,endtimeadj]>>
                                boolean exsed=false;
                                double duringst=0,duringet=0;
                         List<Map<Integer,Map<Integer,List<Double>>>>  list=new ArrayList<>();
                        for(TripleValue tripleValue:vmrenthistory.get(vm.getId()))
                                {
                                        Map<Integer,Map<Integer,List<Double>>> taskidleSTB=new HashMap<>();
                                        if(!exsed)
                                        {
                                                exsed=true;
                                                duringst=tripleValue.getStartTime();
                                                duringet=tripleValue.getFinishTime();
                                        }
                                        boolean flag=false;Task curtask=taskvaTaskid.get(tripleValue.firstval);
                                        for(int j=i+1;j<allVmList.size();j++)
                                        {
                                                Vm vm1=allVmList.get(j);
                                                if(vm1.getId()!=vm.getId()&&datacenterList.get(vm1.getDatacenterid()).getPrivacylevel()<=curtask.getPrivacy_level())
                                                {
                                                        for(Map.Entry<Double,Double> entry:AllSTB.get(vm1.getId()).entrySet())
                                                        {
                                                                double stadj=Math.max(entry.getKey(),curtask.getParentList().stream().mapToDouble(Task::getFinishtime).max().orElse(0));
                                                                double etadj=stadj+computeprocesstime(curtask,vm1.getId());
                                                                if(stadj>=curtask.getParentList().stream().mapToDouble(Task::getFinishtime).max().orElse(0)&&
                                                                etadj<=curtask.getChildList().stream().mapToDouble(Task::getStarttime).min().orElse(0)&&
                                                                etadj<=entry.getValue())
                                                                {
                                                                        duringet=curtask.getFinishtime();
                                                                        flag=true;
                                                                        List<Double> time=new ArrayList<>();
                                                                        time.add(stadj);time.add(etadj);
                                                                        Map<Integer,List<Double>> map=new HashMap<>();
                                                                        map.put(vm1.getId(),time);
                                                                        taskidleSTB.put(curtask.getCloudletId(),map);
                                                                        break;
                                                                }
                                                        }
                                                        if(!flag)
                                                        {
                                                                exsed=false;
                                                                if((duringet-duringst)>=BTU)
                                                                {
                                                                        list.add(taskidleSTB);
                                                                }
                                                        }
                                                }

                                        }
                                }
                        changetask(list);
                }
        }
        public void changetask(List<Map<Integer,Map<Integer,List<Double>>>> taskidleSTBlist)
        {
                for(Map<Integer,Map<Integer,List<Double>>> taskidleSTB:taskidleSTBlist )
                {
                        for(Map.Entry<Integer,Map<Integer,List<Double>>> map1:taskidleSTB.entrySet())
                        {
                                int taskid=map1.getKey();
                                Task curtask=taskvaTaskid.get(taskid);
                                for(Map.Entry<Integer,List<Double>> map2:map1.getValue().entrySet())
                                {
                                        int vmid=map2.getKey();
                                        List<TripleValue> tripleValues=vmrenthistory.get(curtask.getVmId());
                                        Iterator<TripleValue> iterator=tripleValues.iterator();
                                        while(iterator.hasNext())
                                        {
                                                TripleValue temp=iterator.next();
                                                if(temp.getfirstval()==taskid)
                                                {
                                                        iterator.remove();break;
                                                }
                                        }
                                        curtask.setVmId(vmid);
                                        List<TripleValue> temp=vmrenthistory.getOrDefault(vmid,new ArrayList<>());
                                        temp.add(new TripleValue(taskid,map2.getValue().get(0),map2.getValue().get(1)));
                                        vmrenthistory.put(vmid,temp);
                                }
                        }
                }
        }
        public Map<Integer, TreeMap<Double,Double>> computeAllSTB()
        {
                Map<Integer, TreeMap<Double,Double>> res=new HashMap<>();
                for(Vm vm:allVmList)
                {
                        TreeMap<Double,Double> curSTB=new TreeMap<>();
                        List<TripleValue> temp=vmrenthistory.get(vm.getId());
                        temp.sort(new Comparator<TripleValue>() {
                                @Override
                                public int compare(TripleValue tripleValue, TripleValue t1) {
                                        return Double.compare(tripleValue.getStartTime(),t1.getStartTime());
                                }
                        });
                        double STBST=vm.getCreateTime();
                        for(TripleValue tripleValue:temp)
                        {
                                if(tripleValue.getStartTime()==STBST)
                                {
                                        STBST=tripleValue.getFinishTime();
                                }
                                else{
                                        curSTB.put(STBST,tripleValue.getStartTime());
                                        STBST=tripleValue.getFinishTime();
                                }
                        }
                        if(temp.size()!=0&&temp.get(temp.size()-1).getFinishTime()<vm.getDestoryTime())
                        {
                                curSTB.put(temp.get(temp.size()-1).getFinishTime(),vm.getDestoryTime());
                        }
                        res.put(vm.getId(),curSTB);
                }
                return res;
        }
        public void computeallvmorder()
        {
                for(Vm vm:allVmList)
                {
                        vm.rankorder=vmrenthistory.get(vm.getId()).stream().mapToDouble(tripleValue -> taskvaTaskid.get(tripleValue.getfirstval()).getCloudletLength()).sum()/vmrenthistory.get(vm.getId()).size();
                }
        }

}
