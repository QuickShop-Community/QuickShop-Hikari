package org.maxgamer.quickshop.util.mojangapi;

public class MojangApiMcbbsApiMirror implements MojangApiMirror {
    @Override
    public String getLauncherMetaRoot() {
        return "https://download.mcbbs.net";
    }

    @Override
    public String getResourcesDownloadRoot() {
        return "https://download.mcbbs.net/assets";
    }

    @Override
    public String getLibrariesRoot() {
        return "https://download.mcbbs.net/maven";
    }
}
