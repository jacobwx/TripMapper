import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;

public class OpenPolygon extends MapPolygonImpl {

    public OpenPolygon(List<? extends Coordinate> coordinates) {
        super(coordinates);
    }

    @Override
    public void paint(Graphics g, List<Point> points) {
        int[] xPoints = new int[points.size()];
        int[] yPoints = new int[points.size()];
        
        int i = 0;
        for (Point p : points) {
            xPoints[i] = (int) p.x;
            yPoints[i] = (int) p.y;
            i++;
        }
        g.setColor(getColor());
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        
        g2d.drawPolyline(xPoints, yPoints, points.size());
    }
}