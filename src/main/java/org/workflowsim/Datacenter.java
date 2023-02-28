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
        private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getUseablecores() {
        return useablecores;
    }

    public void setUseablecores(int useablecores) {
        this.useablecores = useablecores;
    }

    public Datacenter(int cpucores, int mibps, int id, String name, List<Vm> vms, int privacylevel, String tag) {
            this.cpucores = cpucores;
            this.mibps = mibps;
            this.id = id;
            this.name = name;
            this.vms = vms;
            this.privacylevel = privacylevel;
            useablecores=cpucores;
            this.tag=tag;
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
