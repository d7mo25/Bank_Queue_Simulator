import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * AnimationPanel.java
 *
 * A simple animated "playback" of an already-computed simulation:
 * counters are drawn as booths, and waiting customers are drawn as a
 * line of circles that shrinks/grows as time advances. A Swing Timer
 * drives a virtual clock forward at an adjustable speed so the user can
 * actually watch the queue form and drain, rather than only seeing
 * final numbers.
 */
public class AnimationPanel extends JPanel {

    private final Canvas canvas = new Canvas();
    private final JSlider speedSlider = new JSlider(1, 20, 5);
    private final JButton playPauseButton = new JButton("Play");
    private final JLabel clockLabel = new JLabel("t = 0.0 min");
    private final JButton restartButton = new JButton("Restart");

    private List<Customer> customers = new ArrayList<>();
    private int numCounters = 1;
    private double totalTime = 1;
    private double virtualClock = 0;
    private boolean playing = false;

    private final Timer timer;
    private static final int TICK_MS = 40;

    public AnimationPanel() {
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        controls.add(playPauseButton);
        controls.add(restartButton);
        controls.add(new JLabel("Speed:"));
        controls.add(speedSlider);
        controls.add(clockLabel);
        add(controls, BorderLayout.SOUTH);

        playPauseButton.addActionListener(e -> togglePlay());
        restartButton.addActionListener(e -> {
            virtualClock = 0;
            canvas.repaint();
            updateClockLabel();
        });

        timer = new Timer(TICK_MS, this::onTick);
    }

    public void loadSchedule(List<Customer> customers, int numCounters, double totalTime) {
        this.customers = customers;
        this.numCounters = Math.max(1, numCounters);
        this.totalTime = Math.max(1, totalTime);
        this.virtualClock = 0;
        stop();
        updateClockLabel();
        canvas.repaint();
    }

    private void togglePlay() {
        if (playing) {
            stop();
        } else {
            playing = true;
            playPauseButton.setText("Pause");
            timer.start();
        }
    }

    private void stop() {
        playing = false;
        playPauseButton.setText("Play");
        timer.stop();
    }

    private void onTick(ActionEvent e) {
        double minutesPerSecond = speedSlider.getValue(); // simulated minutes advanced per real second
        virtualClock += minutesPerSecond * (TICK_MS / 1000.0);
        if (virtualClock >= totalTime) {
            virtualClock = totalTime;
            stop();
        }
        updateClockLabel();
        canvas.repaint();
    }

    private void updateClockLabel() {
        clockLabel.setText(String.format("t = %.1f / %.1f min", virtualClock, totalTime));
    }

    /** Inner canvas that does the actual drawing. */
    private class Canvas extends JPanel {

        Canvas() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(600, 320));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // ---- Draw counters along the top ----
            int counterW = 90, counterH = 60;
            int gap = 20;
            int totalCountersWidth = numCounters * counterW + (numCounters - 1) * gap;
            int startX = Math.max(20, (width - totalCountersWidth) / 2);
            int counterY = 30;

            List<Customer> beingServed = new ArrayList<>();
            List<Customer> waiting = new ArrayList<>();
            for (Customer c : customers) {
                if (c.getServiceStartTime() < 0) continue;
                if (virtualClock >= c.getArrivalTime() && virtualClock < c.getServiceStartTime()) {
                    waiting.add(c);
                } else if (virtualClock >= c.getServiceStartTime() && virtualClock < c.getServiceEndTime()) {
                    beingServed.add(c);
                }
            }

            for (int i = 0; i < numCounters; i++) {
                int x = startX + i * (counterW + gap);
                Customer servedHere = null;
                for (Customer c : beingServed) {
                    if (c.getCounterAssigned() == i + 1) { servedHere = c; break; }
                }
                g2.setColor(servedHere != null ? new Color(46, 160, 90) : new Color(210, 214, 220));
                g2.fillRoundRect(x, counterY, counterW, counterH, 14, 14);
                g2.setColor(new Color(70, 70, 80));
                g2.drawRoundRect(x, counterY, counterW, counterH, 14, 14);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
                g2.drawString("Counter " + (i + 1), x + 8, counterY + 18);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
                if (servedHere != null) {
                    g2.drawString("Serving #" + servedHere.getId(), x + 8, counterY + 40);
                } else {
                    g2.drawString("Idle", x + 8, counterY + 40);
                }
            }

            // ---- Draw the waiting line beneath the counters ----
            int lineY = counterY + counterH + 60;
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Waiting line (" + waiting.size() + " customer" + (waiting.size() == 1 ? "" : "s") + "):",
                    20, lineY - 15);

            int circleD = 34;
            int cx = 20;
            int maxPerRow = Math.max(1, (width - 40) / (circleD + 8));
            for (int i = 0; i < waiting.size(); i++) {
                int row = i / maxPerRow;
                int col = i % maxPerRow;
                int x = cx + col * (circleD + 8);
                int y = lineY + row * (circleD + 8);
                g2.setColor(new Color(230, 150, 40));
                g2.fillOval(x, y, circleD, circleD);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
                String label = "#" + waiting.get(i).getId();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, x + (circleD - fm.stringWidth(label)) / 2, y + circleD / 2 + 4);
            }

            if (virtualClock >= totalTime && !customers.isEmpty()) {
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
                g2.setColor(new Color(40, 40, 45));
                g2.drawString("Simulation complete.", 20, height - 15);
            }
        }
    }
}
