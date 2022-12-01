package uk.ac.ed.inf;

import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;

import static org.junit.Assert.*;

/**
 * Unit tests for the Drone Pizza Delivery Service App.
 */
public class AppTest 
{

    public AppTest() {

    }

    @Before
    public void setUp() throws MalformedURLException {
        DataFetcher.getInstance().setBaseUrl("https://ilp-rest.azurewebsites.net/");
    }

    /**
     * Test for the distanceTo() method in the LngLat record.
     */
    @Test
    public void distanceToTest() {
        LngLat point1 = new LngLat(0, 0);
        LngLat point2 = new LngLat(1, 0);
        LngLat point3 = new LngLat(1, 1);

        assertEquals(1, point1.distanceTo(point2), 0.0);
        assertEquals(Math.sqrt(2), point1.distanceTo(point3), 0.0);
        assertEquals(0, point1.distanceTo(point1), 0.0);
    }

    /**
     * Test for the closeTo() method in the LngLat record.
     */
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

    /**
     * Test for the nextPosition() method in the LngLat record.
     */
    @Test
    public void nextPositionTest() {
        LngLat initPoint = new LngLat(0, 0);
        LngLat travellingPoint = new LngLat(0, 0);

        CompassDirection[] directionSequence1 = new CompassDirection[] {
                CompassDirection.N,
                CompassDirection.E,
                CompassDirection.W,
                CompassDirection.S
        };

        for (CompassDirection direction : directionSequence1) {
            travellingPoint = travellingPoint.nextPosition(direction);
        }
        assertTrue(travellingPoint.closeTo(initPoint));
    }

}
