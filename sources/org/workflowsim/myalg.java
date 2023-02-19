package org.workflowsim;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;

import java.util.ArrayList;
import java.util.List;

public class myalg {
   static  List<Datacenter> datacenterList=new ArrayList<>();
    static  List<Vm> VmList=new ArrayList<>();
    long[][] bandwidth;
    public static void main(String[] args) {
        WorkflowParser workflowParser=new WorkflowParser("F:/WorkflowSim-1.0-master/config/dax/Montage_100.xml");
        workflowParser.parse();
        List<Task> list=workflowParser.getTaskList();
        List<Task> orderdtasks=calculatetaskorder(list);

    }

    private static List<Task> calculatetaskorder(List<Task> taskList,) {
    }

    class Datacenter {
        private int cpucores;
        private int mibps;
        private int id;
        private String name;
        private List<Vm> vms;
        private int privacylevel;

        public Datacenter(int cpucores, int mibps, int id, String name, List<Vm> vms, int privacylevel) {
            this.cpucores = cpucores;
            this.mibps = mibps;
            this.id = id;
            this.name = name;
            this.vms = vms;
            this.privacylevel = privacylevel;
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
    public void createvm(int vmcpucores,int datacenterid)
    {

    }
}
