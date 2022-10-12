package uk.ac.ed.inf;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    public static final double DISTANCE_TOLERANCE = 0.00015;

    @Test
    public void inCentralAreaTest() throws IOException {
        LngLat withinCentralArea = new LngLat(-3.19000, 55.944617);
        LngLat outsideCentralArea = new LngLat(-4, 58);

        assertTrue(withinCentralArea.inCentralArea());
        assertFalse(outsideCentralArea.inCentralArea());
    }
    @Test
    public void distanceToTest() {
        LngLat point1 = new LngLat(0, 0);
        LngLat point2 = new LngLat(1, 0);
        LngLat point3 = new LngLat(1, 1);

        assertEquals(1, point1.distanceTo(point2), 0.0);
        assertEquals(Math.sqrt(2), point1.distanceTo(point3), 0.0);
        assertEquals(0, point1.distanceTo(point1), 0.0);
    }

    @Test
    public void closeToTest() {
        LngLat point1 = new LngLat(0, 0);
        LngLat point2 = new LngLat(0.00013, 0);
        LngLat point3 = new LngLat(0.00015, 0);
        LngLat point4 = new LngLat(0.00003, 0.00009);
        LngLat point5 = new LngLat(0.00010, 0.00012);

        assertTrue(point1.closeTo(point2));
        assertFalse(point1.closeTo(point3));
        assertTrue(point1.closeTo(point4));
        assertFalse(point1.closeTo(point5));
    }

    @Test
    public void nextPositionTest() {
        LngLat initPoint = new LngLat(0, 0);
        LngLat travellingPoint = new LngLat(0, 0);

        CompassDirection[] directionSequence1 = new CompassDirection[] {
                CompassDirection.N,
                CompassDirection.E,
                CompassDirection.S,
                CompassDirection.W
        };

        for (CompassDirection direction : directionSequence1) {
            travellingPoint = travellingPoint.nextPosition(direction);
        }

        assertTrue(travellingPoint.closeTo(initPoint));

    }

}
