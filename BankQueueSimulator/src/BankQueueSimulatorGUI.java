import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * BankQueueSimulatorGUI.java
 *
 * Entry point for the Bank Queue Simulator. Lets the user configure:
 *   - number of service counters
 *   - number of customers
 *   - average service time per customer
 *   - average time between customer arrivals (arrival rate)
 *
 * ... then runs the simulation (see SimulationEngine) and displays:
 *   - a results summary (average waiting time, average/max queue length, utilization)
 *   - a full per-customer log table
 *   - a queue-length-over-time chart
 *   - a simple playback animation of the queue forming and draining
 *
 * Course: Data Structures and Algorithms
 * Institution: Universiti Antarabangsa Albukhary (AIU)
 */
public class BankQueueSimulatorGUI extends JFrame {

    private final JSpinner counterSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
    private final JSpinner customerSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 2000, 1));
    private final JSpinner serviceTimeSpinner = new JSpinner(new SpinnerNumberModel(4.0, 0.1, 120.0, 0.5));
    private final JSpinner arrivalRateSpinner = new JSpinner(new SpinnerNumberModel(2.0, 0.1, 120.0, 0.5));
    private final JCheckBox reproducibleCheck = new JCheckBox("Use fixed random seed (reproducible run)");

    private final JLabel avgWaitLabel = valueLabel("--");
    private final JLabel maxWaitLabel = valueLabel("--");
    private final JLabel avgQueueLabel = valueLabel("--");
    private final JLabel maxQueueLabel = valueLabel("--");
    private final JLabel utilLabel = valueLabel("--");
    private final JLabel totalTimeLabel = valueLabel("--");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Arrival (min)", "Counter", "Service Start", "Service End", "Wait Time"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable customerTable = new JTable(tableModel);

    private final QueueChartPanel chartPanel = new QueueChartPanel();
    private final AnimationPanel animationPanel = new AnimationPanel();

    private static final DecimalFormat DF = new DecimalFormat("#0.00");

    public BankQueueSimulatorGUI() {
        super("Bank Queue Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(880, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildInputPanel(), BorderLayout.WEST);
        add(buildCenterTabs(), BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------- UI setup

    private JComponent buildHeader() {
        JLabel title = new JLabel("Bank Queue Simulator");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel subtitle = new JLabel("Data Structures & Algorithms Project  \u2014  Universiti Antarabangsa Albukhary");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(Color.GRAY);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(title);
        panel.add(subtitle);
        panel.setBorder(new EmptyBorder(0, 4, 10, 0));
        return panel;
    }

    private JComponent buildInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 215)), "Simulation Parameters",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                form.getFont().deriveFont(Font.BOLD, 13f)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addField(form, gc, row++, "Number of counters:", counterSpinner);
        addField(form, gc, row++, "Number of customers:", customerSpinner);
        addField(form, gc, row++, "Avg. service time (min):", serviceTimeSpinner);
        addField(form, gc, row++, "Avg. time between arrivals (min):", arrivalRateSpinner);

        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        form.add(reproducibleCheck, gc);

        JButton runButton = new JButton("Run Simulation");
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD, 14f));
        runButton.setBackground(new Color(30, 110, 210));
        runButton.setForeground(Color.WHITE);
        runButton.setFocusPainted(false);
        runButton.addActionListener(e -> runSimulation());

        gc.gridy = row++;
        gc.insets = new Insets(14, 4, 4, 4);
        form.add(runButton, gc);

        JLabel hint = new JLabel("<html><i>Tip: a smaller \"time between arrivals\" than "
                + "\"service time \u00f7 counters\" will make the queue grow over time.</i></html>");
        hint.setFont(hint.getFont().deriveFont(11f));
        hint.setForeground(Color.GRAY);
        gc.gridy = row++;
        gc.insets = new Insets(4, 4, 4, 4);
        form.add(hint, gc);

        JPanel results = buildResultsPanel();

        panel.add(form);
        panel.add(Box.createVerticalStrut(12));
        panel.add(results);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 215)), "Results Summary",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                panel.getFont().deriveFont(Font.BOLD, 13f)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        row = addResultRow(panel, gc, row, "Average waiting time:", avgWaitLabel);
        row = addResultRow(panel, gc, row, "Maximum waiting time:", maxWaitLabel);
        row = addResultRow(panel, gc, row, "Average queue length:", avgQueueLabel);
        row = addResultRow(panel, gc, row, "Maximum queue length:", maxQueueLabel);
        row = addResultRow(panel, gc, row, "Avg. counter utilization:", utilLabel);
        addResultRow(panel, gc, row, "Total simulation time:", totalTimeLabel);

        return panel;
    }

    private JComponent buildCenterTabs() {
        JTabbedPane tabs = new JTabbedPane();

        JScrollPane tableScroll = new JScrollPane(customerTable);
        customerTable.setFillsViewportHeight(true);
        customerTable.setRowHeight(22);

        tabs.addTab("Queue Length Chart", chartPanel);
        tabs.addTab("Customer Log", tableScroll);
        tabs.addTab("Live Animation", animationPanel);
        return tabs;
    }

    private static void addField(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 1;
        gc.weightx = 0.6;
        form.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 0.4;
        form.add(field, gc);
    }

    private static int addResultRow(JPanel panel, GridBagConstraints gc, int row, String label, JLabel valueLabel) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0.6;
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(12f));
        panel.add(l, gc);
        gc.gridx = 1;
        gc.weightx = 0.4;
        panel.add(valueLabel, gc);
        return row + 1;
    }

    private static JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setForeground(new Color(30, 110, 210));
        return label;
    }

    // ---------------------------------------------------------------- simulation logic

    private void runSimulation() {
        int numCounters = (Integer) counterSpinner.getValue();
        int numCustomers = (Integer) customerSpinner.getValue();
        double serviceTime = (Double) serviceTimeSpinner.getValue();
        double interArrivalTime = (Double) arrivalRateSpinner.getValue();
        long seed = reproducibleCheck.isSelected() ? 42L : -1L;

        SimulationResult result = SimulationEngine.run(numCounters, numCustomers, serviceTime, interArrivalTime, seed);

        avgWaitLabel.setText(DF.format(result.averageWaitTime) + " min");
        maxWaitLabel.setText(DF.format(result.maxWaitTime) + " min");
        avgQueueLabel.setText(DF.format(result.averageQueueLength) + " customers");
        maxQueueLabel.setText(result.maxQueueLength + " customers");
        utilLabel.setText(DF.format(result.averageUtilization) + " %");
        totalTimeLabel.setText(DF.format(result.totalSimulationTime) + " min");

        tableModel.setRowCount(0);
        for (Customer c : result.customers) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    DF.format(c.getArrivalTime()),
                    "Counter " + c.getCounterAssigned(),
                    DF.format(c.getServiceStartTime()),
                    DF.format(c.getServiceEndTime()),
                    DF.format(c.getWaitTime())
            });
        }

        chartPanel.setSeries(result.queueLengthSeries);
        animationPanel.loadSchedule(result.customers, numCounters, result.totalSimulationTime);
    }

    // ---------------------------------------------------------------- main

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }
        SwingUtilities.invokeLater(() -> {
            BankQueueSimulatorGUI gui = new BankQueueSimulatorGUI();
            gui.setVisible(true);
            gui.runSimulation(); // populate with a default run on startup
        });
    }
}
