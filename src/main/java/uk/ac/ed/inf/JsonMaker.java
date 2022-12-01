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
     * deliveries made by the drone
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
        String writePath = "deliveries-" + dataFetcher.getDate() + ".json";
        writeToFile(writePath, deliveries);
    }



    public static void createFlightPathJson(ArrayList<PathStep> fullDronePath) throws IOException {
        ArrayNode flightPath = OBJECT_MAPPER.createArrayNode();
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
        String filePath = "flightpath-" + DataFetcher.getInstance().getDate() + ".json";
        writeToFile(filePath, flightPath);
    }

    public static void createDroneGeoJson(ArrayList<PathStep> fullDronePath) throws IOException {
        ObjectNode featureCollection = OBJECT_MAPPER.createObjectNode();
        ArrayNode features = OBJECT_MAPPER.createArrayNode();
        ObjectNode feature = OBJECT_MAPPER.createObjectNode();
        features.add(feature);
        feature.put("type", "Feature");
        featureCollection.put("type", "FeatureCollection");
        featureCollection.set("features", features);
        ObjectNode properties = OBJECT_MAPPER.createObjectNode();
        ObjectNode geometry = OBJECT_MAPPER.createObjectNode();
        feature.set("properties", properties);
        feature.set("geometry", geometry);
        geometry.put("type", "LineString");
        ArrayNode coordinates = OBJECT_MAPPER.createArrayNode();
        geometry.set("coordinates", coordinates);
        for (PathStep pathStep : fullDronePath) {
            ArrayNode coordinate = OBJECT_MAPPER.createArrayNode();
            coordinate.add(pathStep.getToLngLat().lng());
            coordinate.add(pathStep.getToLngLat().lat());
            coordinates.add(coordinate);
        }
        String fileName = "drone-" + DataFetcher.getInstance().getDate() + ".geojson";
        writeToFile(fileName, featureCollection);
    }

}
