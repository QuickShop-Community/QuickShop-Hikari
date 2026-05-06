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
```