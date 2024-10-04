package com.ghostchu.quickshop.util.paste;

import com.ghostchu.quickshop.common.util.JsonUtil;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Paste the paste through <a href="https://bytebin.lucko.me/post">https://bytebin.lucko.me/post</a>
 * Website Author: Lucko (<a href="https://github.com/lucko">https://github.com/lucko</a>)
 *
 * @author Ghost_chu
 */
public class LuckoPastebinPaster implements PasteInterface {

  @Override
  @NotNull
  public String pasteTheText(@NotNull final String text) throws IOException {

    final HttpResponse<String> response = Unirest.post("https://bytebin.lucko.me/post")
            .body(text)
            .asString();
    if(response.isSuccess()) {
      final String json = response.getBody();
      final Response req = JsonUtil.getGson().fromJson(json, Response.class);
      return req.getKey();
    } else {
      throw new IOException(response.getStatus() + " " + response.getStatusText() + ": " + response.getBody());
    }

  }

  @Override
  public String pasteTheTextJson(@NotNull final String text) throws Exception {

    final HttpResponse<String> response = Unirest.post("https://bytebin.lucko.me/post")
            .body(JsonUtil.getGson().toJson(new JsonPadding(text)))
            .asString();
    if(response.isSuccess()) {
      final String json = response.getBody();
      final Response req = JsonUtil.getGson().fromJson(json, Response.class);
      return "https://bytebin.lucko.me/" + req.getKey();
    } else {
      throw new IOException(response.getStatus() + " " + response.getStatusText() + ": " + response.getBody());
    }
  }

  @NoArgsConstructor
  @Data
  static class Response {

    private String key;
  }

  @Data
  static class JsonPadding {

    private static final String _paster = "QuickShop";
    private String data;

    public JsonPadding(final String data) {

      this.data = data;
    }
  }
}

