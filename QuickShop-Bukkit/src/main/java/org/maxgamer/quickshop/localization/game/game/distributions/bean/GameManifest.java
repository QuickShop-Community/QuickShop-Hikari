package org.maxgamer.quickshop.localization.game.game.distributions.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GameManifest {

    @JsonProperty("arguments")
    private ArgumentsDTO arguments;
    @JsonProperty("assetIndex")
    private AssetIndexDTO assetIndex;
    @JsonProperty("assets")
    private String assets;
    @JsonProperty("complianceLevel")
    private Integer complianceLevel;
    @JsonProperty("downloads")
    private DownloadsDTO downloads;
    @JsonProperty("id")
    private String id;
    @JsonProperty("javaVersion")
    private JavaVersionDTO javaVersion;
    @JsonProperty("libraries")
    private List<LibrariesDTO> libraries;
    @JsonProperty("logging")
    private LoggingDTO logging;
    @JsonProperty("mainClass")
    private String mainClass;
    @JsonProperty("minimumLauncherVersion")
    private Integer minimumLauncherVersion;
    @JsonProperty("releaseTime")
    private String releaseTime;
    @JsonProperty("time")
    private String time;
    @JsonProperty("type")
    private String type;

    @NoArgsConstructor
    @Data
    public static class ArgumentsDTO {
        @JsonProperty("game")
        private List<String> game;
        @JsonProperty("jvm")
        private List<JvmDTO> jvm;

        @NoArgsConstructor
        @Data
        public static class JvmDTO {
            @JsonProperty("rules")
            private List<RulesDTO> rules;
            @JsonProperty("value")
            private List<String> value;

            @NoArgsConstructor
            @Data
            public static class RulesDTO {
                @JsonProperty("action")
                private String action;
                @JsonProperty("os")
                private OsDTO os;

                @NoArgsConstructor
                @Data
                public static class OsDTO {
                    @JsonProperty("name")
                    private String name;
                }
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class AssetIndexDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("sha1")
        private String sha1;
        @JsonProperty("size")
        private Integer size;
        @JsonProperty("totalSize")
        private Integer totalSize;
        @JsonProperty("url")
        private String url;
    }

    @NoArgsConstructor
    @Data
    public static class DownloadsDTO {
        @JsonProperty("client")
        private ClientDTO client;
        @JsonProperty("client_mappings")
        private ClientMappingsDTO clientMappings;
        @JsonProperty("server")
        private ServerDTO server;
        @JsonProperty("server_mappings")
        private ServerMappingsDTO serverMappings;

        @NoArgsConstructor
        @Data
        public static class ClientDTO {
            @JsonProperty("sha1")
            private String sha1;
            @JsonProperty("size")
            private Integer size;
            @JsonProperty("url")
            private String url;
        }

        @NoArgsConstructor
        @Data
        public static class ClientMappingsDTO {
            @JsonProperty("sha1")
            private String sha1;
            @JsonProperty("size")
            private Integer size;
            @JsonProperty("url")
            private String url;
        }

        @NoArgsConstructor
        @Data
        public static class ServerDTO {
            @JsonProperty("sha1")
            private String sha1;
            @JsonProperty("size")
            private Integer size;
            @JsonProperty("url")
            private String url;
        }

        @NoArgsConstructor
        @Data
        public static class ServerMappingsDTO {
            @JsonProperty("sha1")
            private String sha1;
            @JsonProperty("size")
            private Integer size;
            @JsonProperty("url")
            private String url;
        }
    }

    @NoArgsConstructor
    @Data
    public static class JavaVersionDTO {
        @JsonProperty("component")
        private String component;
        @JsonProperty("majorVersion")
        private Integer majorVersion;
    }

    @NoArgsConstructor
    @Data
    public static class LoggingDTO {
        @JsonProperty("client")
        private ClientDTO client;

        @NoArgsConstructor
        @Data
        public static class ClientDTO {
            @JsonProperty("argument")
            private String argument;
            @JsonProperty("file")
            private FileDTO file;
            @JsonProperty("type")
            private String type;

            @NoArgsConstructor
            @Data
            public static class FileDTO {
                @JsonProperty("id")
                private String id;
                @JsonProperty("sha1")
                private String sha1;
                @JsonProperty("size")
                private Integer size;
                @JsonProperty("url")
                private String url;
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class LibrariesDTO {
        @JsonProperty("downloads")
        private DownloadsDTO downloads;
        @JsonProperty("name")
        private String name;
        @JsonProperty("rules")
        private List<RulesDTO> rules;
        @JsonProperty("natives")
        private NativesDTO natives;
        @JsonProperty("extract")
        private ExtractDTO extract;

        @NoArgsConstructor
        @Data
        public static class DownloadsDTO {
            @JsonProperty("artifact")
            private ArtifactDTO artifact;

            @NoArgsConstructor
            @Data
            public static class ArtifactDTO {
                @JsonProperty("path")
                private String path;
                @JsonProperty("sha1")
                private String sha1;
                @JsonProperty("size")
                private Integer size;
                @JsonProperty("url")
                private String url;
            }
        }

        @NoArgsConstructor
        @Data
        public static class NativesDTO {
            @JsonProperty("osx")
            private String osx;
        }

        @NoArgsConstructor
        @Data
        public static class ExtractDTO {
            @JsonProperty("exclude")
            private List<String> exclude;
        }

        @NoArgsConstructor
        @Data
        public static class RulesDTO {
            @JsonProperty("action")
            private String action;
            @JsonProperty("os")
            private OsDTO os;

            @NoArgsConstructor
            @Data
            public static class OsDTO {
                @JsonProperty("name")
                private String name;
            }
        }
    }
}
