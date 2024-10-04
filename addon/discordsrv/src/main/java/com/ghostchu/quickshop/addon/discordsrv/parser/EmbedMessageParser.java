package com.ghostchu.quickshop.addon.discordsrv.parser;

import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.annotations.SerializedName;
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

  @NotNull
  public MessageEmbed parse(@NotNull final String json) {
    // test json
    if(!CommonUtil.isJson(json)) {
      throw new IllegalArgumentException("Invalid json: " + json);
    }
    // map check
    final Map<String, Object> map = JsonUtil.getGson().fromJson(json, new TypeToken<Map<String, Object>>() {
    }.getType());
    if(!map.containsKey("embed") && map.containsKey("embeds")) {
      throw new IllegalArgumentException("json argument are multiple embeds! only single embed message is supported!");
    }
    final PackageDTO packageDto = JsonUtil.getGson().fromJson(json, PackageDTO.class);
    final PackageDTO.EmbedDTO dto = packageDto.getEmbed();
    final EmbedBuilder builder = new EmbedBuilder();
    if(dto.getTitle() != null) {
      builder.setTitle(dto.getTitle());
    }
    if(dto.getDescription() != null) {
      builder.setDescription(dto.getDescription());
    }
    if(dto.getColor() != null) {
      builder.setColor(dto.getColor());
    }
    if(dto.getFooter() != null) {
      builder.setFooter(dto.getFooter().getText(), emptyDefault(dto.getFooter().getIconUrl()));
    }
    if(dto.getThumbnail() != null) {
      builder.setThumbnail(emptyDefault(dto.getThumbnail().getUrl()));
    }
    if(dto.getImage() != null && StringUtils.isNotBlank(dto.getImage().getUrl())) {
      builder.setImage(emptyDefault(dto.getImage().getUrl()));
    }
    if(dto.getAuthor() != null) {
      builder.setAuthor(dto.getAuthor().getName(), emptyDefault(dto.getAuthor().getUrl()), emptyDefault(dto.getAuthor().getIconUrl()));
    }
    builder.setTimestamp(Instant.now());
    if(dto.getFields() != null) {
      for(final PackageDTO.EmbedDTO.FieldsDTO field : dto.getFields()) {
        if(field != null && field.getName() != null && field.getValue() != null) {
          String fieldName = field.getName();
          String fieldValue = field.getValue();
          if(StringUtils.isEmpty(fieldName)) {
            fieldName = ZERO_WIDTH_SPACE;
          }
          if(StringUtils.isEmpty(fieldValue)) {
            fieldValue = ZERO_WIDTH_SPACE;
          }
          builder.addField(fieldName, fieldValue, field.inline);
        }
      }
    }
    return builder.build();
  }

  @Nullable
  private String emptyDefault(@Nullable final String v) {

    if(v == null || StringUtils.isBlank(v) || !v.startsWith("http")) {
      return null;
    }
    return v;
  }

  @NoArgsConstructor
  @Data
  public static class PackageDTO {

    @SerializedName("embed")
    private EmbedDTO embed;

    @NoArgsConstructor
    @Data
    public static class EmbedDTO {

      @SerializedName("title")
      private String title;
      @SerializedName("description")
      private String description;
      @SerializedName("color")
      private Integer color;
      @SerializedName("url")
      private String url;
      @SerializedName("author")
      private AuthorDTO author;
      @SerializedName("thumbnail")
      private ThumbnailDTO thumbnail;
      @SerializedName("image")
      private ImageDTO image;
      @SerializedName("footer")
      private FooterDTO footer;
      @SerializedName("fields")
      private List<FieldsDTO> fields;

      @NoArgsConstructor
      @Data
      public static class AuthorDTO {

        @SerializedName("name")
        private String name;
        @SerializedName("url")
        private String url;
        @SerializedName("icon_url")
        private String iconUrl;
      }

      @NoArgsConstructor
      @Data
      public static class ThumbnailDTO {

        @SerializedName("url")
        private String url;
      }

      @NoArgsConstructor
      @Data
      public static class ImageDTO {

        @SerializedName("url")
        private String url;
      }

      @NoArgsConstructor
      @Data
      public static class FooterDTO {

        @SerializedName("text")
        private String text;
        @SerializedName("icon_url")
        private String iconUrl;
      }

      @NoArgsConstructor
      @Data
      public static class FieldsDTO {

        @SerializedName("name")
        private String name;
        @SerializedName("value")
        private String value;
        @SerializedName("inline")
        private Boolean inline;
      }
    }
  }

}
