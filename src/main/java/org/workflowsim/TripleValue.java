package org.workflowsim;

public class TripleValue {
    int firstval;
    double StartTime;
    double FinishTime;
    public TripleValue(int a, double b, double c)
    {firstval=a;StartTime=b;FinishTime=c;}
    public int getfirstval() {
        return firstval;
    }

    public void setfirstval(int firstval) {
        this.firstval = firstval;
    }

    public double getStartTime() {
        return StartTime;
    }

    public void setStartTime(double startTime) {
        StartTime = startTime;
    }

    public double getFinishTime() {
        return FinishTime;
    }

    public void setFinishTime(double finishTime) {
        FinishTime = finishTime;
    }
}
