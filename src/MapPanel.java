import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle;

public class MapPanel extends JPanel {

    private boolean includeStops;
    private int animationTime;
    private Timer animationTimer;
    private JMapViewer mapViewer;
    private int currentFrame;
    private IconMarker marker;
    // List of visited points 
    List<Coordinate> visitedPoints = new ArrayList<>();
    
    // Create a new trail polygon with the updated list of points
    MapPolygonImpl trail = new MapPolygonImpl(visitedPoints);
    
    public MapPanel() {
    	 // Set the layout manager to BorderLayout
        setLayout(new BorderLayout());
    	
        // Create instance of MapViewer
    	mapViewer = new JMapViewer();
    	
    	// Set tile source
    	mapViewer.setTileSource(new OsmTileSource.TransportMap());
        
        // Set the default position and zoom level of the map
        mapViewer.setDisplayPosition(new Coordinate(35.211037, -97.438866), 12);
        
        // Add the JMapViewer to this JPanel
        add(mapViewer, BorderLayout.CENTER);
    }
    
    private List<Coordinate> loadTrack(ArrayList<TripPoint> tripList) {
    	List<Coordinate> track = new ArrayList<>();
    	
    	for (int i=0; i < tripList.size(); i++) {
    		TripPoint point = tripList.get(i);
    		Coordinate coord = new Coordinate(point.getLat(),point.getLon());
    		track.add(i, coord);
    	}
    	
    	return track;
    }
    
    // Find minimum value of point's latitude or longitude
    private static Coordinate min(List<Coordinate> track, boolean isLat) {
    	Coordinate min = track.get(0);
        
    	if(isLat) {
    		for (int i = 1; i < track.size(); i++) {
    			if (track.get(i).getLat() < min.getLat()) {
    				min = track.get(i);}}}
    	else {
    		for (int i = 1; i < track.size(); i++) {
    			if (track.get(i).getLon() < min.getLon()) {
    				min = track.get(i);}}}
    	return min;
    }
    
    // Find maximum value of point's latitude or longitude 
    private static Coordinate max(List<Coordinate> track, boolean isLat) {
    	Coordinate max = track.get(0);
        
    	if(isLat) {
    		for (int i = 1; i < track.size(); i++) {
    			if (track.get(i).getLat() > max.getLat()) {
    				max = track.get(i);}}}
    	else {
    		for (int i = 1; i < track.size(); i++) {
    			if (track.get(i).getLon() > max.getLon()) {
    				max = track.get(i);}}}
    	return max;
    }

    public void playAnimation(int animationTime, boolean includeStops, String filename) throws FileNotFoundException, IOException {
    	
        this.animationTime = animationTime;
        this.includeStops = includeStops;
        
        TripPoint.readFile(filename);
    	TripPoint.h2StopDetectionSimplified();
        
    	// Create track for animation
    	List<Coordinate> track = loadTrack(TripPoint.getMovingTrip());
    	
        // TODO: Add animation code here
        if (animationTimer != null || marker != null) {
        	// Stop animation if an animation is already ongoing
            animationTimer.stop();
            // Remove old marker from the map viewer
            mapViewer.removeMapMarker(marker);
            // Remove the old trail polygon from the map viewer
            mapViewer.removeMapPolygon(trail);
            visitedPoints.clear();
        }
        
        // Add icon marker to map
        marker = new IconMarker(track.get(0), new ImageIcon("raccoon.png").getImage());
        mapViewer.addMapMarker(marker);
        
        // Set the map view to center on the track
        double avgLat = (min(track, true).getLat()+max(track, true).getLat())/2;
        double avgLon = (min(track, false).getLon()+max(track, false).getLon())/2;
        mapViewer.setDisplayPosition(new Coordinate(avgLat, avgLon), 5);
        
        trail.setColor(Color.RED);
        trail.setBackColor(new Color(255, 0, 0, 100)); // Transparent red
       
        
        // Create a timer to animate the marker
        int FPS = 60; // frames per second 
        int interval = 1000 / FPS;
        int numFrames = animationTime * FPS; // total number of frames
        currentFrame = 0;

        animationTimer = new Timer(interval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
				// Update the position of the marker
                if (currentFrame  < numFrames) {
                    int index = (int) ((double) currentFrame / numFrames * track.size());
                    Coordinate coord = track.get(index);
                    marker.setLat(coord.getLat());
                    marker.setLon(coord.getLon());
                    marker.setImage(new ImageIcon("raccoon.png").getImage());
                    currentFrame++;
                    
                    // Add the current marker position to the trail polygon
                    visitedPoints.add(new Coordinate(marker.getLat(), marker.getLon()));
                   
                    // Remove the old trail polygon from the map viewer
                    mapViewer.removeMapPolygon(trail);
                    
                    List<Coordinate> trailPoints = visitedPoints.subList(1, visitedPoints.size());
                    trail = new MapPolygonImpl(trailPoints);
                    trail.setColor(Color.RED);
                    trail.setBackColor(new Color(255, 0, 0, 0)); // Transparent red
                    mapViewer.addMapPolygon(trail);


                } else {
                    // Animation is complete
                    animationTimer.stop();
                    mapViewer.removeMapMarker(marker);
                    mapViewer.removeMapPolygon(trail);
                    visitedPoints.clear();
                }

                // Redraw the map
                mapViewer.repaint();
            }
        });

        // Start the animation
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // TODO: Add map drawing code here
    }
}