package org.workflowsim.algorithms;

import org.workflowsim.FileItem;
import org.workflowsim.Task;
import org.workflowsim.utils.Parameters;

import java.util.Comparator;
import java.util.List;

public class TRMTaskFeature implements baseTRM{
    @Override
    public void RankTasks(List<Task> list) {
        list.sort(new Comparator<Task>() {
            @Override
            public int compare(Task task, Task t1) {
                if(task.getPrivacy_level()==t1.getPrivacy_level())
                {
                    if(task.getCloudletLength()==t1.getCloudletLength())
                    {
                        double temp1=task.getFileList().stream().mapToDouble(FileItem::getSize).sum();
                        double temp2=t1.getFileList().stream().mapToDouble(FileItem::getSize).sum();
                        return Double.compare(temp2, temp1);
                    }
                    else return Double.compare(t1.getCloudletLength(),task.getCloudletLength());
                }
                else return task.getPrivacy_level()-t1.getPrivacy_level();
            }
        });
    }
}
