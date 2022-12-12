package com.ghostchu.quickshop.addon.discordsrv.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ghostchu.quickshop.util.MsgUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class EmbedMessageParser {
    private final static String ZERO_WIDTH_SPACE = "\u200E";
    private static final Gson gson = new Gson();

    @NotNull
    public MessageEmbed parse(@NotNull String json) {
        // test json
        if (!MsgUtil.isJson(json))
            throw new IllegalArgumentException("Invalid json: " + json);
        // map check
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
        if (!map.containsKey("embed") && map.containsKey("embeds"))
            throw new IllegalArgumentException("json argument are multiple embeds! only single embed message is supported!");
        PackageDTO packageDto = gson.fromJson(json, PackageDTO.class);
        PackageDTO.EmbedDTO dto = packageDto.getEmbed();
        EmbedBuilder builder = new EmbedBuilder();
        if (dto.getTitle() != null)
            builder.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            builder.setDescription(dto.getDescription());
        if (dto.getColor() != null)
            builder.setColor(dto.getColor());
        if (dto.getFooter() != null)
            builder.setFooter(dto.getFooter().getText(), dto.getFooter().getIconUrl());
        if (dto.getThumbnail() != null)
            builder.setThumbnail(emptyDefault(dto.getThumbnail().getUrl()));
        if (dto.getImage() != null && StringUtils.isNotBlank(dto.getImage().getUrl()))
            builder.setImage(dto.getImage().getUrl());
        if (dto.getAuthor() != null)
            builder.setAuthor(dto.getAuthor().getName(), dto.getAuthor().getUrl(), dto.getAuthor().getIconUrl());
        builder.setTimestamp(Instant.now());
        if (dto.getFields() != null) {
            for (PackageDTO.EmbedDTO.FieldsDTO field : dto.getFields()) {
                if (field != null && field.getName() != null && field.getValue() != null) {
                    String fieldName = field.getName();
                    String fieldValue = field.getValue();
                    if (StringUtils.isEmpty(fieldName))
                        fieldName = ZERO_WIDTH_SPACE;
                    if (StringUtils.isEmpty(fieldValue))
                        fieldValue = ZERO_WIDTH_SPACE;
                    builder.addField(fieldName, fieldValue, field.inline);
                }
            }
        }
        return builder.build();
    }

    @Nullable
    private String emptyDefault(@Nullable String v) {
        if (v == null || !v.startsWith("http"))
            return null;
        return v;
    }

    @NoArgsConstructor
    @Data
    public static class PackageDTO {
        @JsonProperty("embed")
        private EmbedDTO embed;

        @NoArgsConstructor
        @Data
        public static class EmbedDTO {
            @JsonProperty("title")
            private String title;
            @JsonProperty("description")
            private String description;
            @JsonProperty("color")
            private Integer color;
            @JsonProperty("url")
            private String url;
            @JsonProperty("author")
            private AuthorDTO author;
            @JsonProperty("thumbnail")
            private ThumbnailDTO thumbnail;
            @JsonProperty("image")
            private ImageDTO image;
            @JsonProperty("footer")
            private FooterDTO footer;
            @JsonProperty("fields")
            private List<FieldsDTO> fields;

            @NoArgsConstructor
            @Data
            public static class AuthorDTO {
                @JsonProperty("name")
                private String name;
                @JsonProperty("url")
                private String url;
                @JsonProperty("icon_url")
                private String iconUrl;
            }

            @NoArgsConstructor
            @Data
            public static class ThumbnailDTO {
                @JsonProperty("url")
                private String url;
            }

            @NoArgsConstructor
            @Data
            public static class ImageDTO {
                @JsonProperty("url")
                private String url;
            }

            @NoArgsConstructor
            @Data
            public static class FooterDTO {
                @JsonProperty("text")
                private String text;
                @JsonProperty("icon_url")
                private String iconUrl;
            }

            @NoArgsConstructor
            @Data
            public static class FieldsDTO {
                @JsonProperty("name")
                private String name;
                @JsonProperty("value")
                private String value;
                @JsonProperty("inline")
                private Boolean inline;
            }
        }
    }

}
