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
     * Method to create/overwrite and populate it with a JSON node.
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
     * all the orders delivered by the drone on a given day.
     * @throws IOException if the file cannot be created/overwritten.
     */
    public static void createDeliveriesJson() throws IOException {
        DataFetcher dataFetcher = DataFetcher.getInstance();
        ArrayNode deliveries = OBJECT_MAPPER.createArrayNode();
        Order[] orders = dataFetcher.getOrders();
        for (Order order : orders) {
            if (order.getOrderOutcome() == OrderOutcome.Delivered) {
                ObjectNode orderNode = OBJECT_MAPPER.createObjectNode();
                orderNode.put("orderNo", order.getOrderNo());
                orderNode.put("outcome", order.getOrderOutcome().toString());
                orderNode.put("costInPence", order.getPriceTotalInPence());
                deliveries.add(orderNode);
            }
        }
        String filePath = "resultfiles/deliveries-" + dataFetcher.getDate() + ".json";
        writeToFile(filePath, deliveries);
    }

    /**
     * Method to create/overwrite a JSON file and populating it with details about all the
     * moves made by the drone on a given day.
     * @param allDroneMoves ArrayList of DroneMove objects representing all the moves made by the drone.
     * @throws IOException if the file cannot be created/overwritten.
     */
    public static void createFlightPathJson(ArrayList<DroneMove> allDroneMoves) throws IOException {
        ArrayNode flightPath = OBJECT_MAPPER.createArrayNode();
        for (DroneMove droneMove : allDroneMoves) {
            ObjectNode moveNode = OBJECT_MAPPER.createObjectNode();
            moveNode.put("orderNo", droneMove.orderNo());
            moveNode.put("fromLongitude", droneMove.fromLngLat().lng());
            moveNode.put("fromLatitude", droneMove.fromLngLat().lat());
            moveNode.put("angle", droneMove.stepDirectionAngle());
            moveNode.put("toLongitude", droneMove.toLngLat().lng());
            moveNode.put("toLatitude", droneMove.toLngLat().lat());
            moveNode.put("ticksSinceStartOfCalculation", droneMove.ticksSinceStartOfCalculation());
            flightPath.add(moveNode);
        }
        String filePath = "resultfiles/flightpath-" + DataFetcher.getInstance().getDate() + ".json";
        writeToFile(filePath, flightPath);
    }

    /**
     * Method to create/overwrite a GeoJSON file and populating it with a FeatureCollection
     * containing the drone's flight path as a LineString feature
     * @param allDroneMoves ArrayList of DroneMove objects representing all the moves made by the drone
     *                      in a given day.
     * @throws IOException If the output file cannot be created/overwritten.
     */
    public static void createDroneGeoJson(ArrayList<DroneMove> allDroneMoves) throws IOException {
        ArrayNode coordinates = OBJECT_MAPPER.createArrayNode();
        for (DroneMove droneMove : allDroneMoves) {
            ArrayNode coordinate = OBJECT_MAPPER.createArrayNode();
            coordinate.add(droneMove.toLngLat().lng());
            coordinate.add(droneMove.toLngLat().lat());
            coordinates.add(coordinate);
        }
        ObjectNode geometry = OBJECT_MAPPER.createObjectNode();
        geometry.put("type", "LineString");
        geometry.set("coordinates", coordinates);

        ObjectNode properties = OBJECT_MAPPER.createObjectNode();

        ObjectNode feature = OBJECT_MAPPER.createObjectNode();
        feature.put("type", "Feature");
        feature.set("properties", properties);
        feature.set("geometry", geometry);

        ArrayNode features = OBJECT_MAPPER.createArrayNode();
        features.add(feature);

        ObjectNode featureCollection = OBJECT_MAPPER.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.set("features", features);

        String filePath = "resultfiles/drone-" + DataFetcher.getInstance().getDate() + ".geojson";
        writeToFile(filePath, featureCollection);
    }

}
