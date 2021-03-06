/*
 * Trip.java - Copyright(c) 2013, 2014 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Nov 27, 2013
 */
package org.noroomattheinn.visibletesla.data;

import java.util.ArrayList;
import java.util.List;
import org.noroomattheinn.utils.GeoUtils;

/**
 * Object that represents a single trip.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class Trip {
    private final List<WayPoint> waypoints;
    private double energyEstimate = Double.NaN;

    public Trip() {
        waypoints = new ArrayList<>();
    }

    public void addWayPoint(WayPoint wp) { 
        waypoints.add(wp);
    }

    public List<WayPoint> getWayPoints() { return waypoints; }

    public void addElevationData() {
        if (!Double.isNaN(waypoints.get(0).getElevation())) return;   // Already added
        List<GeoUtils.ElevationData> edl = GeoUtils.getElevations(waypoints);
        if (edl == null) return;
        int nElevations = edl.size();
        for (int i = 0; i < waypoints.size(); i++) {
            waypoints.get(i).setElevation((i < nElevations) ? edl.get(i).elevation : 0.0);
        }
    }
    
    public double distance() {
        if (waypoints.isEmpty()) return 0.0;
        double startOdo = firstWayPoint().getOdo();
        double endOdo = lastWayPoint().getOdo();
        return (endOdo - startOdo);
    }
    
    public double estimateEnergy() {
        if (!Double.isNaN(energyEstimate)) return energyEstimate;
        double cumulative = 0.0;

        Double lastPower = null;
        long lastTime = 0;
        for (WayPoint wp: waypoints) {
            double power = wp.getPower();
            if (lastPower == null) {
                lastPower = power;
                lastTime = wp.getTime();
            } else {
                double thisEnergy;
                long dT = wp.getTime() - lastTime;
                double dP = Math.abs(power-lastPower);
                double minP = Math.min(power, lastPower);
                thisEnergy = minP * dT + (dP*dT)/2.0;
                cumulative += thisEnergy;
                lastTime = wp.getTime();
                lastPower = power;
            }
        }
        energyEstimate = cumulative/(1000*60*60);
        return energyEstimate;
    }


    public boolean isEmpty() { return waypoints.isEmpty(); }
    public WayPoint firstWayPoint() { return waypoints.get(0); }
    public WayPoint lastWayPoint() { return waypoints.get(waypoints.size()-1); }

    public String asJSON() { return asJSON(true); }

    public String asJSON(boolean useMiles) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("[\n");
        for (WayPoint wp : waypoints) {
            if (first) first = false;
            else sb.append(",\n");
            sb.append(wp.asJSON(useMiles));
        }
        sb.append("]\n");
        return sb.toString();

    }

    @Override public String toString() { return asJSON(); }
    
}