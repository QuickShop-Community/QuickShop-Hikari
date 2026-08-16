package com.ghostchu.quickshop.addon.discordsrv.parser;

import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.annotations.SerializedName;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EmbedMessageParser {

  private static final String ZERO_WIDTH_SPACE = "‎";

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
    if(dto.getImage() != null && !CommonUtil.isBlank(dto.getImage().getUrl())) {
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
          if(CommonUtil.isEmptyString(fieldName)) {
            fieldName = ZERO_WIDTH_SPACE;
          }
          if(CommonUtil.isEmptyString(fieldValue)) {
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

    if(v == null || CommonUtil.isBlank(v) || !v.startsWith("http")) {
      return null;
    }
    return v;
  }

  public static class PackageDTO {

    @SerializedName("embed")
    private EmbedDTO embed;

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

      public static class AuthorDTO {

        @SerializedName("name")
        private String name;
        @SerializedName("url")
        private String url;
        @SerializedName("icon_url")
        private String iconUrl;

        public AuthorDTO() {

      }

        public String getName() {

          return this.name;
        }

        public String getUrl() {

          return this.url;
        }

        public String getIconUrl() {

          return this.iconUrl;
        }

        public void setName(final String name) {

          this.name = name;
        }

        public void setUrl(final String url) {

          this.url = url;
        }

        public void setIconUrl(final String iconUrl) {

          this.iconUrl = iconUrl;
        }

        @Override
        public boolean equals(final Object o) {

          if(o == this) return true;
          if(!(o instanceof EmbedMessageParser.PackageDTO.EmbedDTO.AuthorDTO)) return false;
          final EmbedMessageParser.PackageDTO.EmbedDTO.AuthorDTO other = (EmbedMessageParser.PackageDTO.EmbedDTO.AuthorDTO)o;
          return Objects.equals(this.getName(), other.getName())
                 && Objects.equals(this.getUrl(), other.getUrl())
                 && Objects.equals(this.getIconUrl(), other.getIconUrl());
        }

        @Override
        public int hashCode() {

          return Objects.hash(this.getName(), this.getUrl(), this.getIconUrl());
        }

        @Override
        public String toString() {

          return "EmbedMessageParser.PackageDTO.EmbedDTO.AuthorDTO(name=" + this.getName() + ", url=" + this.getUrl() + ", iconUrl=" + this.getIconUrl() + ")";
        }
      }

      public static class ThumbnailDTO {

        @SerializedName("url")
        private String url;

        public ThumbnailDTO() {

      }

        public String getUrl() {

          return this.url;
        }

        public void setUrl(final String url) {

          this.url = url;
        }

        @Override
        public boolean equals(final Object o) {

          if(o == this) return true;
          if(!(o instanceof EmbedMessageParser.PackageDTO.EmbedDTO.ThumbnailDTO)) return false;
          final EmbedMessageParser.PackageDTO.EmbedDTO.ThumbnailDTO other = (EmbedMessageParser.PackageDTO.EmbedDTO.ThumbnailDTO)o;
          return Objects.equals(this.getUrl(), other.getUrl());
        }

        @Override
        public int hashCode() {

          return Objects.hash(this.getUrl());
        }

        @Override
        public String toString() {

          return "EmbedMessageParser.PackageDTO.EmbedDTO.ThumbnailDTO(url=" + this.getUrl() + ")";
        }
      }

      public static class ImageDTO {

        @SerializedName("url")
        private String url;

        public ImageDTO() {

      }

        public String getUrl() {

          return this.url;
        }

        public void setUrl(final String url) {

          this.url = url;
        }

        @Override
        public boolean equals(final Object o) {

          if(o == this) return true;
          if(!(o instanceof EmbedMessageParser.PackageDTO.EmbedDTO.ImageDTO)) return false;
          final EmbedMessageParser.PackageDTO.EmbedDTO.ImageDTO other = (EmbedMessageParser.PackageDTO.EmbedDTO.ImageDTO)o;
          return Objects.equals(this.getUrl(), other.getUrl());
        }

        @Override
        public int hashCode() {

          return Objects.hash(this.getUrl());
        }

        @Override
        public String toString() {

          return "EmbedMessageParser.PackageDTO.EmbedDTO.ImageDTO(url=" + this.getUrl() + ")";
        }
      }

      public static class FooterDTO {

        @SerializedName("text")
        private String text;
        @SerializedName("icon_url")
        private String iconUrl;

        public FooterDTO() {

      }

        public String getText() {

          return this.text;
        }

        public String getIconUrl() {

          return this.iconUrl;
        }

        public void setText(final String text) {

          this.text = text;
        }

        public void setIconUrl(final String iconUrl) {

          this.iconUrl = iconUrl;
        }

        @Override
        public boolean equals(final Object o) {

          if(o == this) return true;
          if(!(o instanceof EmbedMessageParser.PackageDTO.EmbedDTO.FooterDTO)) return false;
          final EmbedMessageParser.PackageDTO.EmbedDTO.FooterDTO other = (EmbedMessageParser.PackageDTO.EmbedDTO.FooterDTO)o;
          return Objects.equals(this.getText(), other.getText())
                 && Objects.equals(this.getIconUrl(), other.getIconUrl());
        }

        @Override
        public int hashCode() {

          return Objects.hash(this.getText(), this.getIconUrl());
        }

        @Override
        public String toString() {

          return "EmbedMessageParser.PackageDTO.EmbedDTO.FooterDTO(text=" + this.getText() + ", iconUrl=" + this.getIconUrl() + ")";
        }
      }

      public static class FieldsDTO {

        @SerializedName("name")
        private String name;
        @SerializedName("value")
        private String value;
        @SerializedName("inline")
        private Boolean inline;

        public FieldsDTO() {

      }

        public String getName() {

          return this.name;
        }

        public String getValue() {

          return this.value;
        }

        public Boolean getInline() {

          return this.inline;
        }

        public void setName(final String name) {

          this.name = name;
        }

        public void setValue(final String value) {

          this.value = value;
        }

        public void setInline(final Boolean inline) {

          this.inline = inline;
        }

        @Override
        public boolean equals(final Object o) {

          if(o == this) return true;
          if(!(o instanceof EmbedMessageParser.PackageDTO.EmbedDTO.FieldsDTO)) return false;
          final EmbedMessageParser.PackageDTO.EmbedDTO.FieldsDTO other = (EmbedMessageParser.PackageDTO.EmbedDTO.FieldsDTO)o;
          return Objects.equals(this.getInline(), other.getInline())
                 && Objects.equals(this.getName(), other.getName())
                 && Objects.equals(this.getValue(), other.getValue());
        }

        @Override
        public int hashCode() {

          return Objects.hash(this.getInline(), this.getName(), this.getValue());
        }

        @Override
        public String toString() {

          return "EmbedMessageParser.PackageDTO.EmbedDTO.FieldsDTO(name=" + this.getName() + ", value=" + this.getValue() + ", inline=" + this.getInline() + ")";
        }
      }

      public EmbedDTO() {

      }

      public String getTitle() {

        return this.title;
      }

      public String getDescription() {

        return this.description;
      }

      public Integer getColor() {

        return this.color;
      }

      public String getUrl() {

        return this.url;
      }

      public AuthorDTO getAuthor() {

        return this.author;
      }

      public ThumbnailDTO getThumbnail() {

        return this.thumbnail;
      }

      public ImageDTO getImage() {

        return this.image;
      }

      public FooterDTO getFooter() {

        return this.footer;
      }

      public List<FieldsDTO> getFields() {

        return this.fields;
      }

      public void setTitle(final String title) {

        this.title = title;
      }

      public void setDescription(final String description) {

        this.description = description;
      }

      public void setColor(final Integer color) {

        this.color = color;
      }

      public void setUrl(final String url) {

        this.url = url;
      }

      public void setAuthor(final AuthorDTO author) {

        this.author = author;
      }

      public void setThumbnail(final ThumbnailDTO thumbnail) {

        this.thumbnail = thumbnail;
      }

      public void setImage(final ImageDTO image) {

        this.image = image;
      }

      public void setFooter(final FooterDTO footer) {

        this.footer = footer;
      }

      public void setFields(final List<FieldsDTO> fields) {

        this.fields = fields;
      }

      @Override
      public boolean equals(final Object o) {

        if(o == this) return true;
        if(!(o instanceof EmbedMessageParser.PackageDTO.EmbedDTO)) return false;
        final EmbedMessageParser.PackageDTO.EmbedDTO other = (EmbedMessageParser.PackageDTO.EmbedDTO)o;
        return Objects.equals(this.getColor(), other.getColor())
               && Objects.equals(this.getTitle(), other.getTitle())
               && Objects.equals(this.getDescription(), other.getDescription())
               && Objects.equals(this.getUrl(), other.getUrl())
               && Objects.equals(this.getAuthor(), other.getAuthor())
               && Objects.equals(this.getThumbnail(), other.getThumbnail())
               && Objects.equals(this.getImage(), other.getImage())
               && Objects.equals(this.getFooter(), other.getFooter())
               && Objects.equals(this.getFields(), other.getFields());
      }

      @Override
      public int hashCode() {

        return Objects.hash(this.getColor(), this.getTitle(), this.getDescription(), this.getUrl(), this.getAuthor(), this.getThumbnail(), this.getImage(), this.getFooter(), this.getFields());
      }

      @Override
      public String toString() {

        return "EmbedMessageParser.PackageDTO.EmbedDTO(title=" + this.getTitle() + ", description=" + this.getDescription() + ", color=" + this.getColor() + ", url=" + this.getUrl() + ", author=" + this.getAuthor() + ", thumbnail=" + this.getThumbnail() + ", image=" + this.getImage() + ", footer=" + this.getFooter() + ", fields=" + this.getFields() + ")";
      }
    }

    public PackageDTO() {

    }

    public EmbedDTO getEmbed() {

      return this.embed;
    }

    public void setEmbed(final EmbedDTO embed) {

      this.embed = embed;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof EmbedMessageParser.PackageDTO)) return false;
      final EmbedMessageParser.PackageDTO other = (EmbedMessageParser.PackageDTO)o;
      return Objects.equals(this.getEmbed(), other.getEmbed());
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getEmbed());
    }

    @Override
    public String toString() {

      return "EmbedMessageParser.PackageDTO(embed=" + this.getEmbed() + ")";
    }
  }

}
