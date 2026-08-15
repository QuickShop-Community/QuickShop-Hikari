package com.ghostchu.quickshop.util.paste;

import com.ghostchu.quickshop.common.util.JsonUtil;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

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

  static class Response {

    private String key;

    public Response() {

  }

    public String getKey() {

      return this.key;
    }

    public void setKey(final String key) {

      this.key = key;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof LuckoPastebinPaster.Response)) return false;
      final LuckoPastebinPaster.Response other = (LuckoPastebinPaster.Response)o;
      return Objects.equals(this.getKey(), other.getKey());
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getKey());
    }

    @Override
    public String toString() {

      return "LuckoPastebinPaster.Response(key=" + this.getKey() + ")";
    }
  }

  static class JsonPadding {

    private static final String _paster = "QuickShop";
    private String data;

    public JsonPadding(final String data) {

      this.data = data;
    }

    public String getData() {

      return this.data;
    }

    public void setData(final String data) {

      this.data = data;
    }

    @Override
    public boolean equals(final Object o) {

      if(o == this) return true;
      if(!(o instanceof LuckoPastebinPaster.JsonPadding)) return false;
      final LuckoPastebinPaster.JsonPadding other = (LuckoPastebinPaster.JsonPadding)o;
      return Objects.equals(this.getData(), other.getData());
    }

    @Override
    public int hashCode() {

      return Objects.hash(this.getData());
    }

    @Override
    public String toString() {

      return "LuckoPastebinPaster.JsonPadding(data=" + this.getData() + ")";
    }
  }
}

