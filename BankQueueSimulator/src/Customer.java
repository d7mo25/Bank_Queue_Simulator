/**
 * Customer.java
 *
 * Represents a single customer moving through the bank queue simulation.
 * All time values are in minutes, measured from the start of the simulation
 * (time 0).
 */
public class Customer {

    private final int id;
    private final double arrivalTime;
    private final double serviceDuration;

    private int counterAssigned = -1;
    private double serviceStartTime = -1;
    private double serviceEndTime = -1;

    public Customer(int id, double arrivalTime, double serviceDuration) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceDuration = serviceDuration;
    }

    public int getId() { return id; }

    public double getArrivalTime() { return arrivalTime; }

    public double getServiceDuration() { return serviceDuration; }

    public int getCounterAssigned() { return counterAssigned; }

    public void setCounterAssigned(int counterAssigned) { this.counterAssigned = counterAssigned; }

    public double getServiceStartTime() { return serviceStartTime; }

    public void setServiceStartTime(double serviceStartTime) { this.serviceStartTime = serviceStartTime; }

    public double getServiceEndTime() { return serviceEndTime; }

    public void setServiceEndTime(double serviceEndTime) { this.serviceEndTime = serviceEndTime; }

    /** Time spent waiting in the queue before being served. */
    public double getWaitTime() {
        return serviceStartTime - arrivalTime;
    }
}
