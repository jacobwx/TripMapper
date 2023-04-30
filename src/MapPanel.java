import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class MapPanel extends JPanel {
    private Timer animationTimer;
    private JMapViewer mapViewer;
    private int currentFrame;
    private IconMarker marker;
    // List of visited points 
    List<Coordinate> visitedPoints = new ArrayList<>();
    // Create a new trail polygon with the updated list of points
    OpenPolygon trail = new OpenPolygon(visitedPoints);
    
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
    
    // Load the list of TripPoints into a new list of Coordinates
    private List<Coordinate> loadTrack(ArrayList<TripPoint> tripList) {
    	List<Coordinate> track = new ArrayList<>();
    	
    	for (int i=0; i < tripList.size(); i++) {
    		TripPoint point = tripList.get(i);
    		Coordinate coord = new Coordinate(point.getLat(),point.getLon());
    		track.add(i, coord);
    	}
    	
    	return track;
    }
    
    // Find the minimum value of point's latitude or longitude
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
    
    // Find the maximum value of point's latitude or longitude 
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

    // Play the track animation
    public void playAnimation(int animationTime, boolean includeStops, String filename) throws FileNotFoundException, IOException {
        
    	// Read file and apply second heuristic for stops 
        TripPoint.readFile(filename);
    	TripPoint.h2StopDetectionSimplified();
        
    	// Create track for animation
     	List<Coordinate> track;
    	if(includeStops) {
    		track = loadTrack(TripPoint.getTrip());  // use all points if stops are included
    	}
    	
    	else {
    		track = loadTrack(TripPoint.getMovingTrip()); // only use moving points if stops not included
    	}
    	
    	// Remove animation if one is already ongoing
        if (animationTimer != null) {
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
        
        // Create a timer to animate the marker
        int FPS = 60; // frames per second 
        int interval = 1000 / FPS;
        int numFrames = animationTime * FPS; // total number of frames
        currentFrame = 0;

        // Perform animation
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
                    
                    // Add the current marker position to the trail line
                    visitedPoints.add(new Coordinate(marker.getLat(), marker.getLon()));
                    
                    // Remove the old trail line from the map viewer
                    mapViewer.removeMapPolygon(trail);
                    
                    // Create a new trail line with updated points
                    trail = new OpenPolygon(visitedPoints);
                    trail.setColor(Color.RED);
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

}