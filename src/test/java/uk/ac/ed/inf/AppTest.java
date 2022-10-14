package uk.ac.ed.inf;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the Drone Pizza Delivery Service App.
 */
public class AppTest 
{

    public static final double DISTANCE_TOLERANCE = 0.00015;

    /**
     * Test for the inCentralArea() method in the LngLat record.
     * @throws IOException
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


    @Test
    public void getDeliveryCostTest() throws IOException, InvalidPizzaCombinationException {
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net"));
        Order order = new Order();

        ArrayList<String> orderItems1 = new ArrayList<>(List.of("Margarita", "Calzone"));
        int price1 = order.getDeliveryCost(restaurants, orderItems1);
        assertEquals(price1, 2500, 0.0);


        ArrayList<String> orderItems2 = new ArrayList<>(List.of("Proper Pizza", "Pineapple & Ham & Cheese"));
        int price2 = order.getDeliveryCost(restaurants, orderItems2);
        assertEquals(price2, 2400, 0.0);
    }

    @Test(expected = InvalidPizzaCombinationException.class)
    public void invalidPizzaDeliveryCostTest() throws InvalidPizzaCombinationException, IOException {
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net"));
        Order order = new Order();

        ArrayList<String> orderItems = new ArrayList<>(List.of("Margarita", "Vegan Delight"));
        int price = order.getDeliveryCost(restaurants, orderItems);
    }

}
