package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.Data;

@Data
public class ShopCreationLog {

  private static int v = 2;
  private String creator;
  private ShopInfoStorage shop;
  private BlockPos location;

  public ShopCreationLog(QUser creator, ShopInfoStorage shop, BlockPos location) {

    this.creator = creator.serialize();
    this.shop = shop;
    this.location = location;
  }
}
