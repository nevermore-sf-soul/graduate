package org.workflowsim;

public class Vm {
        int cpucore;
        int datacenterid;
        int id;
        double earlyidletime;
        int totalcalability;
        double createTime;

    public double getCreateTime() {
        return createTime;
    }

    public void setCreateTime(double createTime) {
        this.createTime = createTime;
    }

    public double getDestoryTime() {
        return destoryTime;
    }

    public void setDestoryTime(double destoryTime) {
        this.destoryTime = destoryTime;
    }

    double destoryTime;


        public int getTotalcalability() {
            return totalcalability;
        }

        public void setTotalcalability(int totalcalability) {
            this.totalcalability = totalcalability;
        }

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
        double rankorder;

    public double getRankorder() {
        return rankorder;
    }

    public void setRankorder(double rankorder) {
        this.rankorder = rankorder;
    }

    double price;
        double totalprice;
        public Vm(int cpucore, int datacenterid, int id, double earlyidletime,int totalcalability) {
            this.cpucore = cpucore;
            this.datacenterid = datacenterid;
            this.id = id;
            this.earlyidletime = earlyidletime;
            this.totalcalability=totalcalability;
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
