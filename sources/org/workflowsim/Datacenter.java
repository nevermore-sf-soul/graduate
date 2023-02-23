package org.workflowsim;

import java.util.List;

public class Datacenter {
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
