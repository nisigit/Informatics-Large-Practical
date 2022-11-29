package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;


public class JsonMaker {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public JsonMaker() {

    }


    public static void createDeliveriesJson(WorldState worldState) throws IOException {
        ArrayNode deliveries = OBJECT_MAPPER.createArrayNode();
        Order[] orders = worldState.getOrders();
        for (Order order : orders) {
            if (order.getOrderOutcome() == OrderOutcome.Delivered) {
                ObjectNode orderNode = OBJECT_MAPPER.createObjectNode();
                orderNode.put("orderNo", order.getOrderNo());
                orderNode.put("outcome", order.getOrderOutcome().toString());
                orderNode.put("costInPence", order.getPriceTotalInPence());
                deliveries.add(orderNode);
            }
        }
        ObjectWriter writer = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
        String writePath = "deliveries-" + worldState.getDate().toString() + ".json";
        writer.writeValue(Paths.get(writePath).toFile(), deliveries);
    }


    public static void createFlightPathJson(Drone drone, WorldState worldState) throws IOException {
        ArrayNode flightPath = OBJECT_MAPPER.createArrayNode();
        ArrayList<PathStep> fullDronePath = drone.getFullDronePath();
        for (PathStep pathStep : fullDronePath) {
            ObjectNode stepNode = OBJECT_MAPPER.createObjectNode();
            PathStep prevStep = pathStep.getPrevStep();
            stepNode.put("orderNo", pathStep.getOrderNo());
            stepNode.put("fromLongitude", prevStep.getToLngLat().lng());
            stepNode.put("fromLatitude", prevStep.getToLngLat().lat());
            stepNode.put("angle", pathStep.getStepDirection().getAngle());
            stepNode.put("toLongitude", pathStep.getToLngLat().lng());
            stepNode.put("toLatitude", pathStep.getToLngLat().lat());
            stepNode.put("ticksSinceStartOfCalculation", 0);
            flightPath.add(stepNode);
        }
        ObjectWriter writer = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
        String writePath = "flightPath-" + worldState.getDate().toString() + ".json";
        writer.writeValue(Paths.get(writePath).toFile(), flightPath);
    }
}
