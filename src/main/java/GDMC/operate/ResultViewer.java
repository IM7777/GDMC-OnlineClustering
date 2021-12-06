package GDMC.operate;

import GDMC.model.Cluster;
import GDMC.model.Grid;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jxm on 2021/7/22.
 */
public class ResultViewer {

    public void showChart(Map<Integer, Cluster> clusters) {
        DefaultXYDataset xyDataset = new DefaultXYDataset();

        for (Map.Entry<Integer, Cluster> entry : clusters.entrySet()) {
            int label = entry.getKey();
            Cluster cluster = entry.getValue();
            int size = entry.getValue().getGrids().size();
            double[][] data = new double[2][size];
            for (int i = 0; i < size; i++) {
                Grid grid = cluster.getGrids().get(i);
                data[0][i] = grid.getCentroid().getAttr()[0];
                data[1][i] = grid.getCentroid().getAttr()[1];
            }
            xyDataset.addSeries(label, data);
        }
        JFreeChart chart = ChartFactory.createScatterPlot("result", "x", "y",
                xyDataset, PlotOrientation.VERTICAL, true, false, false);
        ChartFrame frame = new ChartFrame("pic", chart, true);
        chart.setBackgroundPaint(Color.white);
        chart.setBorderPaint(Color.GREEN);
        chart.setBorderStroke(new BasicStroke(1.5f));
        XYPlot xyplot = (XYPlot) chart.getPlot();

        xyplot.setBackgroundPaint(new Color(255, 253, 246));
        ValueAxis vaaxis = xyplot.getDomainAxis();
        vaaxis.setRange(0,10);
        vaaxis.setAxisLineStroke(new BasicStroke(1.5f));

        ValueAxis yAxis = xyplot.getRangeAxis();
        yAxis.setRange(0,10);

        ValueAxis va = xyplot.getDomainAxis(0);
        va.setAxisLineStroke(new BasicStroke(1.5f));

        va.setAxisLineStroke(new BasicStroke(1.5f)); // 坐标轴粗细
        va.setAxisLinePaint(new Color(215, 215, 215)); // 坐标轴颜色
        xyplot.setOutlineStroke(new BasicStroke(1.5f)); // 边框粗细
        va.setLabelPaint(new Color(10, 10, 10)); // 坐标轴标题颜色
        va.setTickLabelPaint(new Color(102, 102, 102)); // 坐标轴标尺值颜色
        ValueAxis axis = xyplot.getRangeAxis();
        axis.setAxisLineStroke(new BasicStroke(1.5f));

        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot
                .getRenderer();
        xylineandshaperenderer.setSeriesOutlinePaint(0, Color.WHITE);
        xylineandshaperenderer.setUseOutlinePaint(true);
        NumberAxis numberaxis = (NumberAxis) xyplot.getDomainAxis();
        numberaxis.setAutoRangeIncludesZero(false);
        numberaxis.setTickMarkInsideLength(2.0F);
        numberaxis.setTickMarkOutsideLength(0.0F);
        numberaxis.setAxisLineStroke(new BasicStroke(1.5f));

        frame.pack();
        frame.setVisible(true);

    }

}
