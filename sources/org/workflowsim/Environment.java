package org.workflowsim;

import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
        public List<Datacenter> datacenterList=new ArrayList<>();
        public List<Vm> allVmList=new ArrayList<>();
        public List<List<Vm>> curVmList=new ArrayList<>();// current vms in system
        public long[][] bandwidth;
        public int vmid=0;//vmid increasing automatic,when a new vm is created
        public Map<String,Double> vmprice=new HashMap<>();
        public Map<Integer,Double> maxspeed=new HashMap<>(); //不同隐私等级的任务能够获得的最大虚拟机速度
        public  Map<Integer,Integer> vmlocationvapl=new HashMap<>();
        public Map<Integer,List<Pair<Double,Double>>> vmrenthistory=new HashMap<>(); //the vm execute history,which according the unique vmId;
        Environment(){}
}
