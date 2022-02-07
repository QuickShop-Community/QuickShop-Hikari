package org.maxgamer.quickshop.util.mojangapi;

public class MojangApiBmclApiMirror implements MojangApiMirror {
    @Override
    public String getLauncherMetaRoot() {
        return "https://bmclapi2.bangbang93.com";
    }

    @Override
    public String getResourcesDownloadRoot() {
        return "https://bmclapi2.bangbang93.com/assets";
    }

    @Override
    public String getLibrariesRoot() {
        return "https://bmclapi2.bangbang93.com/maven";
    }
}
