package org.cloudbus.cloudsim.examples.newmethodexamples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.workflowsim.*;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

public class example1 {
    static Map<String,Integer> datacentermips=new HashMap<>();
    protected static List<CondorVM> createlocalVM(int userId, int vms, int vmIdBase) {

        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<CondorVM> list = new LinkedList<>();
        int lownum=(int) Math.floor((vms/3.0)*2);
        int highnum=vms-lownum;
        //VM Parameters
        long size = 0; //image size (MB)
        int ram = 0; //vm memory (MB)
        int mips = 200;
        long bw = 0;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        CondorVM[] vm = new CondorVM[vms];
        for (int i = 0; i < lownum; i++) {
            double ratio = 1.0;
            vm[i] = new CondorVM(vmIdBase + i, userId, mips * ratio, 1, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        for (int i = lownum; i < vms; i++) {
            double ratio = 1.0;
            vm[i] = new CondorVM(vmIdBase + i, userId, mips * ratio, 2, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }
    protected  static WorkflowDatacenter createDatacenter(int totalcores,String name,String datacentertype,int privacy_level){
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<Host> hostList = new ArrayList<>();

        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.
        //
        // Here is the trick to use multiple data centers in one broker. Broker will first
        // allocate all vms to the first datacenter and if there is no enough resource then it will allocate
        // the failed vms to the next available datacenter. The trick is make sure your datacenter is not
        // very big so that the broker will distribute them.
        // In a future work, vm scheduling algorithms should be done
        //

            List<Pe> peList1 = new ArrayList<>();
            int mips =datacentermips.get(datacentertype);
            // 3. Create PEs and add these into the list.
            //for a quad-core machine, a list of 4 PEs is required:
            for(int i=0;i<totalcores;i++)
            {
                peList1.add(new Pe(i, new PeProvisionerSimple(mips)));
            }
            int hostId = 0;
            int ram = 0; //host memory (MB)
            long storage = 0; //host storage
            int bw = 0;
            hostList.add(
                    new Host(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList1,
                            new VmSchedulerTimeShared(peList1))); // This is our first machine
            hostId++;


        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 0;         // time zone this resource located
        double cost = 0;              // the cost of using processing in this resource
        double costPerMem = 0;		// the cost of using memory in this resource
        double costPerStorage = 0;	// the cost of using storage in this resource
        double costPerBw = 0;			// the cost of using bw in this resource
        WorkflowDatacenter datacenter = null;
        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        characteristics.setPrivacy_level(privacy_level);

        // 6. Finally, we need to create a cluster storage object.
        /**
         * The bandwidth between data centers.
         */
        try {
            datacenter = new WorkflowDatacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return datacenter;
    }




    public static void main(String[] args) {
        List<CondorVM> x=new ArrayList<>();
        datacentermips.put("local", 200);
        datacentermips.put("edge", 2000);
        datacentermips.put("cloud",5000);
        try {
            // First step: Initialize the WorkflowSim package.

            /**
             * However, the exact number of vms may not necessarily be vmNum If
             * the data center or the host doesn't have sufficient resources the
             * exact vmNum would be smaller than that. Take care.
             */
            int vmNum = 1000;//number of vms;
            /**
             * Should change this based on real physical path
             */
            String daxPath = "F:/WorkflowSim-1.0-master/config/dax/Montage_100.xml";
            if(args.length!=0) daxPath=args[0];
            java.io.File daxFile = new File(daxPath);
            if (!daxFile.exists()) {
                Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }

            /**
             * Since we are using MINMIN scheduling algorithm, the planning
             * algorithm should be INVALID such that the planner would not
             * override the result of the scheduler
             */
            Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.STATIC;
            Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.HEFT;
            ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;

            /**
             * No overheads
             */
            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);

            /**
             * No Clustering
             */
            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

            /**
             * Initialize static parameters
             */
            Parameters.init(vmNum, daxPath, null,
                    null, op, cp, sch_method, pln_method,
                    null, 0);
            ReplicaCatalog.init(file_system);

            // before creating any entities.
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            WorkflowDatacenter localdatacenter = createDatacenter(4,"localDatacenter","local",1);
            WorkflowDatacenter edgedatacenter1 = createDatacenter(12,"edgeDatacenter"+"_1","edge",2);
            WorkflowDatacenter edgedatacenter2 = createDatacenter(12,"edgeDatacenter"+"_2","edge",3);
            WorkflowDatacenter edgedatacenter3 = createDatacenter(12,"edgeDatacenter"+"_3","edge",3);
            WorkflowDatacenter clouddatacenter = createDatacenter(5000,"edgeDatacenter","cloud",3);



            /**
             * Create a WorkflowPlanner with one scheduler.
             */
            WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
            /**
             * Create a WorkflowEngine. Attach it to the workflow planner
             */
            WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
            /**
             * Create two list of VMs. The trick is that make sure all vmId is
             * unique so we need to index vm from a base (in this case
             * Parameters.getVmNum/2 for the second vmlist1).
             */
            List<CondorVM> vmlist0 = createlocalVM(wfEngine.getSchedulerId(0), 3, 0);

            /**
             * Submits these lists of vms to this WorkflowEngine.
             */
            wfEngine.submitVmList(vmlist0, 0);

            /**
             * Binds the data centers with the scheduler id. This scheduler
             * controls two data centers. Make sure your data center is not very
             * big otherwise all the vms will be allocated to the first
             * available data center In the future, the vm allocation algorithm
             * should be improved.
             */

            wfEngine.bindSchedulerDatacenter(localdatacenter.getId(), 0);
            wfEngine.bindSchedulerDatacenter(edgedatacenter1.getId(), 0);
            wfEngine.bindSchedulerDatacenter(edgedatacenter2.getId(), 0);
            wfEngine.bindSchedulerDatacenter(edgedatacenter3.getId(), 0);
            wfEngine.bindSchedulerDatacenter(clouddatacenter.getId(), 0);
            CloudSim.startSimulation();
            List<Job> outputList0 = wfEngine.getJobsReceivedList();
            CloudSim.stopSimulation();
            printJobList(outputList0);
        } catch (Exception e) {
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }
    protected static void printJobList(List<Job> list) {
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Job ID" + indent + "Task ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + indent
                + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Depth"+indent + "PL");
        DecimalFormat dft = new DecimalFormat("###.##");
        for (Job job : list) {
            Log.print(indent + job.getCloudletId() + indent + indent);
            if (job.getClassType() == Parameters.ClassType.STAGE_IN.value) {
                Log.print("Stage-in");
            }
            for (Task task : job.getTaskList()) {
                Log.print(task.getCloudletId() + ",");
            }
            Log.print(indent);

            if (job.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + indent + job.getResourceId() + indent + indent + indent + job.getVmId()
                        + indent + indent + indent + dft.format(job.getActualCPUTime())
                        + indent + indent + dft.format(job.getExecStartTime()) + indent + indent + indent
                        + dft.format(job.getFinishTime()) + indent + indent + indent + job.getDepth()+ indent+(job.getClassType() == Parameters.ClassType.STAGE_IN.value?0:job.getTaskList().get(0).getPrivacy_level()));
            } else if (job.getCloudletStatus() == Cloudlet.FAILED) {
                Log.print("FAILED");
                Log.printLine(indent + indent + job.getResourceId() + indent + indent + indent + job.getVmId()
                        + indent + indent + indent + dft.format(job.getActualCPUTime())
                        + indent + indent + dft.format(job.getExecStartTime()) + indent + indent + indent
                        + dft.format(job.getFinishTime()) + indent + indent + indent + job.getDepth()+ indent+(job.getClassType() == Parameters.ClassType.STAGE_IN.value?0:job.getTaskList().get(0).getPrivacy_level()));
            }
        }
    }
}
