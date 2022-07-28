package com.ghostchu.quickshop.util.mojangapi;

public interface MojangApiMirror {
    /**
     * <a href="https://launchermeta.mojang.com">https://launchermeta.mojang.com</a>
     *
     * @return The url root
     */
    String getLauncherMetaRoot();

    /**
     * <a href="https://resources.download.minecraft.net">https://resources.download.minecraft.net</a>
     *
     * @return The url root
     */
    String getResourcesDownloadRoot();

    /**
     * <a href="https://libraries.minecraft.net">https://libraries.minecraft.net</a>
     *
     * @return The url root
     */
    String getLibrariesRoot();
}
