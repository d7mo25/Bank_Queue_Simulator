# Bank Queue Simulator (GUI)

A Java Swing desktop application that simulates a bank queueing system with
multiple service counters, built for a **Data Structures and Algorithms**
course project.

You set the number of counters, number of customers, average service time,
and average time between arrivals — the simulator generates a random but
realistic customer stream, routes each customer through the counters, and
reports the average waiting time and queue length. You can also watch it
play out with the built-in animation tab.

## Features

- Configurable **number of counters**, **number of customers**, **average
  service time**, and **average time between arrivals**
- Results summary: average / maximum waiting time, average / maximum queue
  length, average counter utilization, total simulation time
- Full **per-customer log table** (arrival time, counter assigned, service
  start/end, wait time)
- A **queue-length-over-time chart**, drawn natively with `Graphics2D`
  (no external charting library)
- A **live animation tab** that plays the simulation back so you can watch
  customers form a line and get served, with play/pause and a speed slider

## Data structures & algorithms used

| Component                    | What it's for |
|-------------------------------|----------------|
| `CustomQueue<T>` (linked list) | A hand-built generic FIFO queue used to hold customers in strict arrival order — this is the actual "queue" the assignment is about. |
| `PriorityQueue<Counter>` (binary heap) | Always gives the counter that becomes free soonest in O(log n), so each customer is routed to the best available teller without scanning every counter. |
| Sweep-line algorithm | After the run, all "started waiting" / "stopped waiting" timestamps are sorted and swept through once to reconstruct exactly how many people were in line at every instant — used to compute the average and maximum queue length. |
| Exponential random sampling | Arrival gaps and service durations are drawn from an exponential distribution (the standard queueing-theory model for independent random arrivals/service), based on the averages you enter. |

## Project structure

```
src/
  Customer.java              - one customer's record (arrival, service, wait times)
  Counter.java                - one teller counter, tracks availability & utilization
  CustomQueue.java             - hand-built generic FIFO queue (linked list)
  SimulationEngine.java        - the core simulation algorithm
  SimulationResult.java        - bundles all output stats/data for the GUI
  QueueChartPanel.java          - draws the queue-length-over-time chart
  AnimationPanel.java           - live playback animation of the queue
  BankQueueSimulatorGUI.java    - main window / entry point (has `main`)
```

## How to build and run

Requires a JDK (version 11+; developed and tested on JDK 21).

```bash
cd src
javac *.java
java BankQueueSimulatorGUI
```

Or use the pre-built runnable jar:

```bash
java -jar BankQueueSimulator.jar
```

## How to use it

1. Enter the number of counters (tellers), number of customers to simulate,
   average service time per customer, and average time between customer
   arrivals — all in minutes.
2. Click **Run Simulation**.
3. Check the **Results Summary** panel on the left for the headline numbers.
4. Switch between the three tabs to see the queue-length chart, the full
   per-customer log, or watch the **Live Animation** play out (press Play).

Tip: if the average time between arrivals is smaller than the average
service time divided by the number of counters, customers arrive faster
than they can be served and the queue will keep growing — a good scenario
to demonstrate the effect of adding more counters.

## Notes for your report

- Optionally tick **"Use fixed random seed"** before running so you get the
  exact same customer stream every time — useful for comparing, e.g., 2
  counters vs. 4 counters on identical customer data for your report.
- All simulation math lives in `SimulationEngine.java`, fully commented, if
  you need to explain or extend the algorithm for submission.
