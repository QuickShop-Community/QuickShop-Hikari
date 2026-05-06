```java

ShopUpdateRequest request = ShopUpdateRequest.builder()
    .actor(player/commandsender)
    .shop(ContainerShop.builder()
        .meta(SimpleMeta.builder()
            .name(name)
            .owner(example)
            .item(itemStack)
            .build())
        .price(SimpleShopPrice.builder()
            .price(100.0d)
            .build())
        .build())
    .options(ShopUpdateOptions.builder()
        .updateSign(true)
        .updateDisplay(true)
        .updateDatabase(true)
        .build())
    .build();

ShopActionResult<ShopChanges> result = QuickShop.getInstance().shopManager().service().update(request);

worldAdapter.apply(result);

ShopItem.asBuilder()....

ShopBuilder toBuilder();

default Shop withChanges(Consumer<ShopBuilder> changes) {
  ShopBuilder builder = toBuilder();
  changes.accept(builder);
  return builder.build();
}

public ActionResult<Shop> updateShop(
        UUID shopId,
        Consumer<ShopBuilder> updater) {
  Shop updated = shops.compute(shopId, (id, existing)->{
    if (existing == null) {
      return null;
    }

    ShopBuilder builder = existing.toBuilder();
    updater.accept(builder);
    return builder.build();
  });

  if(updated == null) {
    return ActionResult.failure("Shop not found");
  }

  return ActionResult.success(updated);
}

ActionResult<Shop> result = shopService.updateShop(shopId, builder->builder
                                                           .price(500)
                                                           .stock(32));

Shop updated = shop.withChanges(builder->builder
                                        .price(500)
                                        .stock(32));


```