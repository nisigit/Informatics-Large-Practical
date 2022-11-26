package uk.ac.ed.inf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class PathFinder {
    private final WorldState worldState;

    public PathFinder(WorldState worldState) {
        this.worldState = worldState;
    }

    public ArrayList<LngLat> findPath(LngLat start, LngLat end) throws IOException {
        ArrayList<LngLat> path = new ArrayList<>();
        Node startNode = new Node(start, null, start.distanceTo(end));
        PriorityQueue<Node> openList = new PriorityQueue<>();
        ArrayList<Node> closedList = new ArrayList<>();
        openList.add(startNode);

        boolean crossedCentralAreaBoundaries = false;

        while (openList.size() > 0) {
            Node cur_node = openList.poll();
            closedList.add(cur_node);

            for (CompassDirection compassDirection : CompassDirection.values()) {
                LngLat neighbourLngLat = cur_node.lngLat.nextPosition(compassDirection);
                if (pathCrossesNoFlyZone(cur_node.lngLat, neighbourLngLat)) {
                    continue;
                }
                if (pathCrossesCentralAreaBoundary(cur_node.lngLat, neighbourLngLat)) {
                    if (crossedCentralAreaBoundaries) {
                        continue;
                    } else {
                        crossedCentralAreaBoundaries = true;
                    }
                }

                Node neighbourNode = new Node(neighbourLngLat, cur_node, neighbourLngLat.distanceTo(end));


                if (neighbourNode.lngLat.closeTo(end)) {
                    Node cur = neighbourNode;
                    while (cur != null) {
                        path.add(0, cur.lngLat);
                        cur = cur.parent;
                    }
                    return path;
                }

                if (!closedList.contains(neighbourNode)) {
                    openList.add(neighbourNode);
                } else if (closedList.get(closedList.indexOf(neighbourNode)).distanceToTarget > neighbourNode.distanceToTarget) {
                    openList.add(neighbourNode);
                }
            }
        }
        return path;
    }


    private boolean pathCrossesNoFlyZone(LngLat curLngLat, LngLat neighbourLngLat) {
        NoFlyZone[] noFlyZones = this.worldState.getNoFlyZones();
        for (NoFlyZone noFlyZone : noFlyZones) {
            for (int i = 0; i < noFlyZone.getCoordinatesLngLat().length - 1; i++) {
                LngLat nfzVertex1 = noFlyZone.getCoordinatesLngLat()[i];
                LngLat nfzVertex2 = noFlyZone.getCoordinatesLngLat()[i + 1];
                if (segmentsIntercept(curLngLat, neighbourLngLat, nfzVertex1, nfzVertex2)) {
                    return true;
                }
            }
        }

        return false;
    }


    private boolean pathCrossesCentralAreaBoundary(LngLat curLngLat, LngLat neighbourLngLat) {
        LngLat[] centralAreaVertices = this.worldState.getCentralAreaVertices();
        for (int i = 0; i < centralAreaVertices.length - 1; i++) {
            LngLat caVertex1 = centralAreaVertices[i];
            LngLat caVertex2 = centralAreaVertices[i + 1];
            if (segmentsIntercept(curLngLat, neighbourLngLat, caVertex1, caVertex2)) {
                return true;
            }
        }
        return false;
    }

    private boolean segmentsIntercept(LngLat seg1Vertex1, LngLat seg1Vertex2, LngLat seg2Vertex1, LngLat seg2Vertex2) {
        double seg1Slope = (seg1Vertex2.lat() - seg1Vertex1.lat()) / (seg1Vertex2.lng() - seg1Vertex1.lng());
        double seg1Intercept = seg1Vertex1.lat() - seg1Slope * seg1Vertex1.lng();

        double seg2Slope = (seg2Vertex2.lat() - seg2Vertex1.lat()) / (seg2Vertex2.lng() - seg2Vertex1.lng());
        double seg2Intercept = seg2Vertex1.lat() - seg2Slope * seg2Vertex1.lng();

        if (seg1Slope == seg2Slope && seg1Intercept == seg2Intercept) { // collinear segments.
            return false;
        }

        double intersectionLng = (seg2Intercept - seg1Intercept) / (seg1Slope - seg2Slope);

        // If the intersection point of the two lines is within the bounds of the two segments, then the two segments intersect.
        return intersectionLng >= Math.min(seg1Vertex1.lng(), seg1Vertex2.lng()) && intersectionLng <= Math.max(seg1Vertex1.lng(), seg1Vertex2.lng()) &&
                intersectionLng >= Math.min(seg2Vertex1.lng(), seg2Vertex2.lng()) && intersectionLng <= Math.max(seg2Vertex1.lng(), seg2Vertex2.lng());
    }
}
