package com.ghostchu.quickshop.api;

import com.ghostchu.quickshop.api.obj.QUser;
import org.jetbrains.annotations.NotNull;

public interface RankLimiter {


  int getShopLimit(@NotNull QUser p);

  boolean isLimit();
}
