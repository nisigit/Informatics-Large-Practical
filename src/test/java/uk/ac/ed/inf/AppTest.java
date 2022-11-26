package uk.ac.ed.inf;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.PriorityQueue;

import static org.junit.Assert.*;

/**
 * Unit tests for the Drone Pizza Delivery Service App.
 */
public class AppTest 
{

    public static final double DISTANCE_TOLERANCE = 0.00015;

    public AppTest() {

    }

    @Before
    public void setUp() {
        ResponseFetcher.getInstance().setBaseUrl("https://ilp-rest.azurewebsites.net/");
    }

    /**
     * Test for the inCentralArea() method in the LngLat record.
     * @throws IOException If the REST server is not available or base url is invalid.
     */
    @Test
    public void inCentralAreaTest() throws IOException {
        LngLat withinCentralArea = new LngLat(-3.19000, 55.944617);
        LngLat outsideCentralArea = new LngLat(-4, 58);
        LngLat forrestHillPoint = new LngLat(-3.192473, 55.946233);
        LngLat onTheEdge = new LngLat(-3.192473, 55.943);

        assertTrue(withinCentralArea.inCentralArea());
        assertFalse(outsideCentralArea.inCentralArea());
        assertTrue(forrestHillPoint.inCentralArea()); // Vertice should be inside central area.
        assertTrue(onTheEdge.inCentralArea()); // An edge of the central area should be considered inside
    }

    @Test
    public void inNoFlyZoneTest() throws IOException {
        LngLat inGsGarden = new LngLat(-3.188393466950572, 55.943952704696954);
        LngLat inEiq = new LngLat(-3.1902565248433348, 55.945120512244245);
        LngLat inPotterrow = new LngLat(-3.1894540474406767, 55.945558262680095);
        LngLat bayes = new LngLat(-3.1876455472986436, 55.94518995314348);
        LngLat bayesEdgeCase = new LngLat(-3.187525376802995,55.94518429292029);
        LngLat outsidePotterrow = new LngLat(-3.1893999244256577, 55.94551504105377);

        assertTrue(inGsGarden.inNoFlyZone());
        assertTrue(inEiq.inNoFlyZone());
        assertTrue(inPotterrow.inNoFlyZone());
        assertTrue(bayes.inNoFlyZone());
        assertFalse(bayesEdgeCase.inNoFlyZone());
        assertFalse(outsidePotterrow.inNoFlyZone());
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
