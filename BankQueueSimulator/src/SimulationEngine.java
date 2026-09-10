import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * SimulationEngine.java
 *
 * Contains the actual bank-queue simulation algorithm.
 *
 * Design notes / Data Structures used:
 *  - CustomQueue<Customer>   : holds customers in strict First-In-First-Out
 *                              arrival order (the "queue" the assignment is about).
 *  - PriorityQueue<Counter>  : a binary-heap based priority queue that always
 *                              gives the counter which will become free the
 *                              soonest, in O(log n) time. This is what lets
 *                              a customer be routed to the best available
 *                              counter instead of scanning every counter.
 *  - Sweep-line algorithm    : after everyone has been served, we sweep
 *                              through all the "started waiting" / "stopped
 *                              waiting" time points (sorted) to reconstruct
 *                              how many people were in line at every instant,
 *                              which gives us the average & max queue length.
 *
 * Arrival times and service durations are generated from an exponential
 * distribution (the standard model for random, independent arrivals /
 * service times in queueing theory), based on the average rate/time the
 * user supplies.
 */
public class SimulationEngine {

    /**
     * Runs one full simulation.
     *
     * @param numCounters          number of open service counters (servers)
     * @param numCustomers         total number of customers to simulate
     * @param avgServiceTime       average time (minutes) a teller spends per customer
     * @param avgInterArrivalTime  average time (minutes) between customer arrivals
     * @param seed                 random seed (for reproducible runs); pass -1 for a random seed
     */
    public static SimulationResult run(int numCounters, int numCustomers,
                                        double avgServiceTime, double avgInterArrivalTime,
                                        long seed) {

        Random rng = (seed == -1) ? new Random() : new Random(seed);

        // ---- 1. Generate customers (random arrival times & service durations) ----
        CustomQueue<Customer> arrivalQueue = new CustomQueue<>();
        List<Customer> allCustomers = new ArrayList<>(numCustomers);

        double clock = 0.0;
        for (int i = 1; i <= numCustomers; i++) {
            clock += exponentialRandom(rng, avgInterArrivalTime);
            double serviceDuration = exponentialRandom(rng, avgServiceTime);
            serviceDuration = Math.max(serviceDuration, 0.1); // avoid zero-length service
            Customer c = new Customer(i, clock, serviceDuration);
            arrivalQueue.enqueue(c);
            allCustomers.add(c);
        }

        // ---- 2. Set up counters in a min-priority-queue keyed by "next free time" ----
        PriorityQueue<Counter> counterHeap = new PriorityQueue<>();
        List<Counter> allCounters = new ArrayList<>(numCounters);
        for (int i = 1; i <= numCounters; i++) {
            Counter c = new Counter(i);
            counterHeap.add(c);
            allCounters.add(c);
        }

        // ---- 3. Process customers strictly in FIFO arrival order ----
        double totalWait = 0.0;
        double maxWait = 0.0;
        List<double[]> waitIntervals = new ArrayList<>(); // [arrivalTime, serviceStartTime]

        while (!arrivalQueue.isEmpty()) {
            Customer customer = arrivalQueue.dequeue();

            Counter bestCounter = counterHeap.poll(); // counter that frees up soonest
            double startTime = Math.max(customer.getArrivalTime(), bestCounter.getNextAvailableTime());

            customer.setCounterAssigned(bestCounter.getId());
            customer.setServiceStartTime(startTime);
            bestCounter.serve(startTime, customer.getServiceDuration());
            customer.setServiceEndTime(bestCounter.getNextAvailableTime());
            counterHeap.add(bestCounter); // push back with updated availability

            double wait = customer.getWaitTime();
            totalWait += wait;
            if (wait > maxWait) maxWait = wait;

            if (wait > 1e-9) {
                waitIntervals.add(new double[]{customer.getArrivalTime(), startTime});
            }
        }

        // ---- 4. Sweep-line pass to compute queue-length statistics over time ----
        List<double[]> events = new ArrayList<>(); // [time, delta]  delta = +1 start waiting, -1 stop waiting
        for (double[] interval : waitIntervals) {
            events.add(new double[]{interval[0], 1});
            events.add(new double[]{interval[1], -1});
        }
        events.sort((a, b) -> {
            int cmp = Double.compare(a[0], b[0]);
            if (cmp != 0) return cmp;
            return Double.compare(a[1], b[1]); // process "-1" before "+1" at same instant
        });

        List<double[]> queueLengthSeries = new ArrayList<>();
        int currentLength = 0;
        int maxLength = 0;
        double areaUnderCurve = 0.0;
        double lastTime = 0.0;

        queueLengthSeries.add(new double[]{0.0, 0.0});
        for (double[] event : events) {
            double time = event[0];
            areaUnderCurve += currentLength * (time - lastTime);
            currentLength += (int) event[1];
            maxLength = Math.max(maxLength, currentLength);
            queueLengthSeries.add(new double[]{time, currentLength});
            lastTime = time;
        }

        double totalSimTime = allCustomers.isEmpty() ? 0.0 :
                Math.max(lastTime, Collections.max(allCustomers, (a, b) ->
                        Double.compare(a.getServiceEndTime(), b.getServiceEndTime())).getServiceEndTime());

        if (totalSimTime > lastTime) {
            areaUnderCurve += currentLength * (totalSimTime - lastTime);
            queueLengthSeries.add(new double[]{totalSimTime, currentLength});
        }

        double averageQueueLength = (totalSimTime > 0) ? areaUnderCurve / totalSimTime : 0.0;

        // ---- 5. Assemble the result ----
        SimulationResult result = new SimulationResult();
        result.customers = allCustomers;
        result.counters = allCounters;
        result.averageWaitTime = numCustomers == 0 ? 0.0 : totalWait / numCustomers;
        result.maxWaitTime = maxWait;
        result.averageQueueLength = averageQueueLength;
        result.maxQueueLength = maxLength;
        result.totalSimulationTime = totalSimTime;
        result.queueLengthSeries = queueLengthSeries;

        double utilSum = 0.0;
        for (Counter c : allCounters) {
            utilSum += c.getUtilization(totalSimTime);
        }
        result.averageUtilization = allCounters.isEmpty() ? 0.0 : utilSum / allCounters.size();

        return result;
    }

    /** Draws a random value from an exponential distribution with the given mean. */
    private static double exponentialRandom(Random rng, double mean) {
        double u = rng.nextDouble();
        // avoid log(0)
        while (u <= 0.0) u = rng.nextDouble();
        return -mean * Math.log(u);
    }
}
