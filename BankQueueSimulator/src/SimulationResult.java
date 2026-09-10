import java.util.List;

/**
 * SimulationResult.java
 *
 * Simple data holder bundling everything the GUI needs after a
 * simulation run: per-customer records, per-counter statistics,
 * summary numbers, and a time series of queue length for charting.
 */
public class SimulationResult {

    public List<Customer> customers;
    public List<Counter> counters;

    public double averageWaitTime;
    public double maxWaitTime;
    public double averageQueueLength;
    public int maxQueueLength;
    public double totalSimulationTime;
    public double averageUtilization;

    /** (time, queueLength) sample points, in chronological order, for the chart. */
    public List<double[]> queueLengthSeries;
}
