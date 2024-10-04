package com.ghostchu.quickshop.util.mojangapi;

public class MojangApiBmclApiMirror implements MojangApiMirror {

  @Override
  public String getLauncherMetaRoot() {

    return "https://bmclapi2.bangbang93.com";
  }

  @Override
  public String getLibrariesRoot() {

    return "https://bmclapi2.bangbang93.com/maven";
  }

  @Override
  public String getResourcesDownloadRoot() {

    return "https://bmclapi2.bangbang93.com/assets";
  }
}
