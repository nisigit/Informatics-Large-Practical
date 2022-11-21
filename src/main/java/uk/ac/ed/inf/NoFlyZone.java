package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NoFlyZone(@JsonProperty("name") String name, @JsonProperty("coordinates") LngLat[] coordinates) {

}