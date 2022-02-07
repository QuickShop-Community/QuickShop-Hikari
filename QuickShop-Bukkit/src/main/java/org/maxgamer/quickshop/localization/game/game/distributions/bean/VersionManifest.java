package org.maxgamer.quickshop.localization.game.game.distributions.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class VersionManifest {

    @JsonProperty("latest")
    private LatestDTO latest;
    @JsonProperty("versions")
    private List<VersionsDTO> versions;

    @NoArgsConstructor
    @Data
    public static class LatestDTO {
        @JsonProperty("release")
        private String release;
        @JsonProperty("snapshot")
        private String snapshot;
    }

    @NoArgsConstructor
    @Data
    public static class VersionsDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("type")
        private String type;
        @JsonProperty("url")
        private String url;
        @JsonProperty("time")
        private String time;
        @JsonProperty("releaseTime")
        private String releaseTime;
    }
}
