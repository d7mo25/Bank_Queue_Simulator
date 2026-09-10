import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * QueueChartPanel.java
 *
 * A lightweight, dependency-free line/step chart that plots queue length
 * (number of customers waiting) against simulation time. Drawn directly
 * with Graphics2D so the project has no external library dependencies.
 */
public class QueueChartPanel extends JPanel {

    private List<double[]> series; // list of [time, queueLength]
    private double maxTime = 1;
    private double maxQueue = 1;

    private static final Color AXIS_COLOR = new Color(90, 90, 100);
    private static final Color LINE_COLOR = new Color(30, 110, 210);
    private static final Color FILL_COLOR = new Color(30, 110, 210, 40);
    private static final Color GRID_COLOR = new Color(225, 228, 232);

    public QueueChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 320));
    }

    public void setSeries(List<double[]> series) {
        this.series = series;
        maxTime = 1;
        maxQueue = 1;
        if (series != null) {
            for (double[] p : series) {
                maxTime = Math.max(maxTime, p[0]);
                maxQueue = Math.max(maxQueue, p[1]);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int marginLeft = 55;
        int marginBottom = 40;
        int marginTop = 25;
        int marginRight = 20;

        int plotWidth = width - marginLeft - marginRight;
        int plotHeight = height - marginTop - marginBottom;

        g2.setColor(new Color(40, 40, 45));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        g2.drawString("Queue Length Over Time", marginLeft, 18);

        if (series == null || series.isEmpty()) {
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 13f));
            g2.setColor(Color.GRAY);
            g2.drawString("Run a simulation to see the queue-length chart here.", marginLeft, height / 2);
            return;
        }

        // Grid + axes
        g2.setColor(GRID_COLOR);
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = marginTop + plotHeight - (int) ((double) i / gridLines * plotHeight);
            g2.drawLine(marginLeft, y, marginLeft + plotWidth, y);
        }

        g2.setColor(AXIS_COLOR);
        g2.drawLine(marginLeft, marginTop, marginLeft, marginTop + plotHeight);
        g2.drawLine(marginLeft, marginTop + plotHeight, marginLeft + plotWidth, marginTop + plotHeight);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
        for (int i = 0; i <= gridLines; i++) {
            double value = maxQueue * i / gridLines;
            int y = marginTop + plotHeight - (int) ((double) i / gridLines * plotHeight);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.format("%.0f", value), marginLeft - 30, y + 4);
        }
        g2.drawString("Time (min)", marginLeft + plotWidth / 2 - 25, height - 8);

        // Rotated Y-axis label
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.rotate(-Math.PI / 2);
        g2r.drawString("Customers Waiting", -(marginTop + plotHeight / 2 + 45), 15);
        g2r.dispose();

        // Build polygon points (step chart)
        int n = series.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            double[] p = series.get(i);
            xs[i] = marginLeft + (int) (p[0] / maxTime * plotWidth);
            ys[i] = marginTop + plotHeight - (int) (p[1] / maxQueue * plotHeight);
        }

        // Filled area under the curve
        int[] fillX = new int[n + 2];
        int[] fillY = new int[n + 2];
        System.arraycopy(xs, 0, fillX, 0, n);
        System.arraycopy(ys, 0, fillY, 0, n);
        fillX[n] = xs[n - 1];
        fillY[n] = marginTop + plotHeight;
        fillX[n + 1] = xs[0];
        fillY[n + 1] = marginTop + plotHeight;
        g2.setColor(FILL_COLOR);
        g2.fillPolygon(fillX, fillY, n + 2);

        // Line
        g2.setColor(LINE_COLOR);
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < n - 1; i++) {
            g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }
    }
}
