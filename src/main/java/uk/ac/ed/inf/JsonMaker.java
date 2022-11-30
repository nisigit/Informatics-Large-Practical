package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 * Class with static methods to create JSON files with details about the deliveries
 * made and paths taken by the drone on a given day.
 */
public class JsonMaker {

    /**
     * ObjectMapper object to create JSON objects.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Class constructor.
     */
    public JsonMaker() {

    }

    /**
     * Method to create/overwrite and populate it with a JSON nodes.
     * @param filePath String representing the path to the file where the JSON/GeoJSON nodes
     *                 will be written to.
     * @param jsonNode JsonNode object containing the JSON/GeoJSON nodes to be written to the file.
     * @throws IOException if the file cannot be created/overwritten.
     */
    private static void writeToFile(String filePath, JsonNode jsonNode) throws IOException {
        ObjectWriter objectWriter = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(Paths.get(filePath).toFile(), jsonNode);
    }

    /**
     * Method to create/overwrite a JSON file and populating it with details about the
     * deliveries made by the drone
     *
     * @param worldState WorldState object containing information about orders, flying zones,
     *                   restaurants, etc. for in which the deliveries were made.
     * @throws IOException if the file cannot be created/overwritten.
     */
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
        String writePath = "deliveries-" + worldState.getDate().toString() + ".json";
        writeToFile(writePath, deliveries);
    }


    /**
     * Method to create/overwrite a JSON file and populating it with details about every
     * move made by the drone on a given day, and the order no. for collecting/delivering it.
     * @param drone Drone object representing the drone which made the moves.
     * @param worldState WorldState object containing information about orders, flying zones,
     *                   restaurants, etc. in which the moves were made.
     * @throws IOException if the file cannot be created/overwritten.
     */
    public static void createFlightPathJson(Drone drone, WorldState worldState) throws IOException {
        ArrayNode flightPath = OBJECT_MAPPER.createArrayNode();
        ArrayList<PathStep> fullDronePath = drone.getFullDronePath();
        for (PathStep pathStep : fullDronePath) {
            ObjectNode stepNode = OBJECT_MAPPER.createObjectNode();
            PathStep prevStep = pathStep.getPrevStep();
            stepNode.put("orderNo", pathStep.getOrderNo());
            stepNode.put("fromLongitude", prevStep.getToLngLat().lng());
            stepNode.put("fromLatitude", prevStep.getToLngLat().lat());
            stepNode.put("angle", pathStep.getStepDirectionAngle());
            stepNode.put("toLongitude", pathStep.getToLngLat().lng());
            stepNode.put("toLatitude", pathStep.getToLngLat().lat());
            stepNode.put("ticksSinceStartOfCalculation", pathStep.getTicksSinceStartOfCalculation());
            flightPath.add(stepNode);
        }
        String filePath = "flightPath-" + worldState.getDate().toString() + ".json";
        writeToFile(filePath, flightPath);
    }

    /**
     * Method to create/overwrite a GeoJSON file and populating it with coordinates of the
     * all moves made by the drone on a given day
     * @param drone Drone object representing the drone which made the moves.
     * @param worldState WorldState object containing information about orders, flying zones,
     * @throws IOException if the file cannot be created/overwritten.
     */
    public static void createDroneGeoJson(Drone drone, WorldState worldState) throws IOException {
        ObjectNode featureCollection = OBJECT_MAPPER.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        ArrayNode features = OBJECT_MAPPER.createArrayNode();
        featureCollection.set("features", features);
        ObjectNode feature = OBJECT_MAPPER.createObjectNode();
        features.add(feature);
        feature.put("type", "Feature");
        ObjectNode properties = OBJECT_MAPPER.createObjectNode();
        feature.set("properties", properties);
        ObjectNode geometry = OBJECT_MAPPER.createObjectNode();
        feature.set("geometry", geometry);
        geometry.put("type", "LineString");
        ArrayNode coordinates = OBJECT_MAPPER.createArrayNode();
        geometry.set("coordinates", coordinates);
        ArrayList<PathStep> fullDronePath = drone.getFullDronePath();
        for (PathStep pathStep : fullDronePath) {
            ArrayNode coordinate = OBJECT_MAPPER.createArrayNode();
            coordinate.add(pathStep.getToLngLat().lng());
            coordinate.add(pathStep.getToLngLat().lat());
            coordinates.add(coordinate);
        }
        String fileName = "drone-" + worldState.getDate().toString() + ".geojson";
        writeToFile(fileName, featureCollection);
    }

}
