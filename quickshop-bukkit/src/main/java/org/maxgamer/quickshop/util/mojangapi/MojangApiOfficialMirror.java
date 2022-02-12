package org.maxgamer.quickshop.util.mojangapi;

public class MojangApiOfficialMirror implements MojangApiMirror {
    @Override
    public String getLauncherMetaRoot() {
        return "https://launchermeta.mojang.com";
    }

    @Override
    public String getResourcesDownloadRoot() {
        return "https://resources.download.minecraft.net";
    }

    @Override
    public String getLibrariesRoot() {
        return "https://libraries.minecraft.net";
    }
}
