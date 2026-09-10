/**
 * Counter.java
 *
 * Represents a single bank service counter (teller). Implements
 * Comparable so a collection of Counters can be managed inside a
 * java.util.PriorityQueue, always giving fast (O(log n)) access to
 * whichever counter becomes free soonest.
 */
public class Counter implements Comparable<Counter> {

    private final int id;
    private double nextAvailableTime;
    private double totalBusyTime;
    private int customersServed;

    public Counter(int id) {
        this.id = id;
        this.nextAvailableTime = 0.0;
        this.totalBusyTime = 0.0;
        this.customersServed = 0;
    }

    public int getId() { return id; }

    public double getNextAvailableTime() { return nextAvailableTime; }

    /**
     * Assigns a customer to this counter starting at startTime and running
     * for durationMinutes. Updates the counter's internal bookkeeping.
     */
    public void serve(double startTime, double durationMinutes) {
        this.nextAvailableTime = startTime + durationMinutes;
        this.totalBusyTime += durationMinutes;
        this.customersServed++;
    }

    public double getTotalBusyTime() { return totalBusyTime; }

    public int getCustomersServed() { return customersServed; }

    /** Percentage of the simulation this counter spent actively serving someone. */
    public double getUtilization(double totalSimulationTime) {
        if (totalSimulationTime <= 0) return 0.0;
        return (totalBusyTime / totalSimulationTime) * 100.0;
    }

    @Override
    public int compareTo(Counter other) {
        return Double.compare(this.nextAvailableTime, other.nextAvailableTime);
    }
}
