package com.ghostchu.quickshop.addon.webui;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.webui.handler.global.Recent;
import com.ghostchu.quickshop.addon.webui.handler.shop.ServerShopsListing;
import com.ghostchu.quickshop.addon.webui.handler.shop.ShopDetails;
import com.ghostchu.quickshop.addon.webui.handler.shop.ShopHistory;
import com.ghostchu.quickshop.addon.webui.handler.user.UserHistory;
import com.ghostchu.quickshop.addon.webui.handler.user.UserShopsListing;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
    static Main instance;
    private QuickShop plugin;
    private Vertx vertx;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    @Override

    public void onEnable() {
        saveDefaultConfig();
        plugin = QuickShop.getInstance();
        vertx = Vertx.vertx(new VertxOptions().setUseDaemonThread(true));
        HttpServer server = vertx.createHttpServer();
        Router restAPI = Router.router(vertx);
        registerRoute(restAPI);
        server.requestHandler(restAPI).listen(plugin.getConfig().getInt("httpd.port"));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void registerRoute(Router restAPI) {
        restAPI.get("/static/*").handler(this::handleStaticAssets);
        restAPI.get("/localized").handler(this::handleFrontendRequestLocalizedFiles);
        restAPI.get("/global/recent").handler(Recent::new);
        restAPI.get("/user/shops").handler(UserShopsListing::new);
        restAPI.get("/user/history").handler(UserHistory::new);
        restAPI.get("/shops").handler(ServerShopsListing::new);
        restAPI.get("/shop/:id").handler(ShopDetails::new);
        restAPI.get("/shop/:id/history").handler(ShopHistory::new);
    }


    private void handleFrontendRequestLocalizedFiles(RoutingContext routingContext) {
    }

    private void handleStaticAssets(RoutingContext ctx) {

    }
}
