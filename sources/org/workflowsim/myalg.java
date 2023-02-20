package org.workflowsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class myalg {
   static  List<Datacenter> datacenterList=new ArrayList<>();
    static  List<Vm> VmList=new ArrayList<>();
    long[][] bandwidth;
    static int vmid;
    static Map<String,Double> vmprice=new HashMap<>();
    public static void main(String[] args) {
        myparser workflowParser=new myparser("F:/WorkflowSim-1.0-master/config/dax/Montage_100.xml",new myreplicalog());
        workflowParser.parse();
        vmprice.put("edge1",0.031);
        vmprice.put("edge2",0.052);
        vmprice.put("edge4",0.208);
        vmprice.put("cloud1",0.0255);
        vmprice.put("cloud2",0.0336);
        vmprice.put("cloud4",0.1344);

        List<Task> list=workflowParser.getTaskList();
        List<Task> orderdtasks=calculatetaskorder(list);

    }

    private static List<Task> calculatetaskorder(List<Task> taskList) {
        return null;
    }

    class Datacenter {
        private int cpucores;
        private int mibps;
        private int id;
        private String name;
        private List<Vm> vms;
        private int privacylevel;
        private int useablecores;
        public Datacenter(int cpucores, int mibps, int id, String name, List<Vm> vms, int privacylevel) {
            this.cpucores = cpucores;
            this.mibps = mibps;
            this.id = id;
            this.name = name;
            this.vms = vms;
            this.privacylevel = privacylevel;
            useablecores=cpucores;
        }

        public List<Vm> getVms() {
            return vms;
        }

        public void setVms(List<Vm> vms) {
            this.vms = vms;
        }

        public int getPrivacylevel() {
            return privacylevel;
        }

        public void setPrivacylevel(int privacylevel) {
            this.privacylevel = privacylevel;
        }

        public int getCpucores() {
            return cpucores;
        }

        public void setCpucores(int cpucores) {
            this.cpucores = cpucores;
        }

        public int getMibps() {
            return mibps;
        }

        public void setMibps(int mibps) {
            this.mibps = mibps;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    class Vm{
        int cpucore;
        int datacenterid;
        int id;
        double earlyidletime;

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getTotalprice() {
            return totalprice;
        }

        public void setTotalprice(double totalprice) {
            this.totalprice = totalprice;
        }

        double price;
        double totalprice;
        public Vm(int cpucore, int datacenterid, int id, double earlyidletime) {
            this.cpucore = cpucore;
            this.datacenterid = datacenterid;
            this.id = id;
            this.earlyidletime = earlyidletime;
        }

        public int getCpucore() {
            return cpucore;
        }

        public void setCpucore(int cpucore) {
            this.cpucore = cpucore;
        }

        public int getDatacenterid() {
            return datacenterid;
        }

        public void setDatacenterid(int datacenterid) {
            this.datacenterid = datacenterid;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public double getEarlyidletime() {
            return earlyidletime;
        }

        public void setEarlyidletime(double earlyidletime) {
            this.earlyidletime = earlyidletime;
        }
    }
    public void createvm(int vmcpucores,int datacenterid,double earlidletime)
    {
        Vm kvm=new Vm(vmcpucores,datacenterid,vmid++,earlidletime);

        VmList.add(kvm);
    }
}
