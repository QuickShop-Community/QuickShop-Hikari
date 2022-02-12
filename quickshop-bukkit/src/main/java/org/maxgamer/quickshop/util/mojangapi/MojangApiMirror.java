package org.maxgamer.quickshop.util.mojangapi;

public interface MojangApiMirror {
    /**
     * https://launchermeta.mojang.com
     *
     * @return The url root
     */
    String getLauncherMetaRoot();

    /**
     * https://resources.download.minecraft.net
     *
     * @return The url root
     */
    String getResourcesDownloadRoot();

    /**
     * https://libraries.minecraft.net
     *
     * @return The url root
     */
    String getLibrariesRoot();
}
