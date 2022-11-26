package uk.ac.ed.inf;


import java.io.IOException;
import java.util.Objects;

public class Node implements Comparable<Node> {

    public LngLat lngLat;

    public double distanceToTarget;

    public Node parent;

    public Node(LngLat lngLat, Node parent, double distanceToTarget) throws IOException {
        this.lngLat = lngLat;
        this.parent = parent;
        this.distanceToTarget = distanceToTarget;
    }

    @Override
    public int compareTo(Node o) {
        return Double.compare(this.distanceToTarget, o.distanceToTarget);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return this.lngLat.equals(node.lngLat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lngLat);
    }
}
