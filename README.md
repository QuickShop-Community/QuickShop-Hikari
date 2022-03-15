# QuickShop-Hikari

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a04ef7174d9f4e65b60ae28b09222809)](https://www.codacy.com/gh/Ghost-chu/QuickShop-Hikari/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Ghost-chu/QuickShop-Hikari&amp;utm_campaign=Badge_Grade)
[![CodeFactor](https://www.codefactor.io/repository/github/ghost-chu/quickshop-hikari/badge)](https://www.codefactor.io/repository/github/ghost-chu/quickshop-hikari)
[![CodeScore](https://api.codiga.io/project/32011/score/svg)](https://app.codiga.io/public/project/32011/QuickShop-Hikari/dashboard)
![GitHub license](https://img.shields.io/github/license/Ghost-chu/QuickShop-Hikari.svg)
![TestsPassed](https://img.shields.io/jenkins/tests?compact_message&jobUrl=https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Hikari)
![Contributors](https://img.shields.io/github/contributors/Ghost-chu/QuickShop-Hikari)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari?ref=badge_shield)

![Java](https://img.shields.io/badge/java-version%2017%2B%20(currently%20is%2017--18)-orange)
![MC](https://img.shields.io/badge/minecraft-java%20edition%201.18%2B-blueviolet)

[//]: # (![Ver]&#40;https://img.shields.io/spiget/version/62575?label=version&#41;)

[//]: # (![Downloads]&#40;https://img.shields.io/spiget/downloads/62575?label=downloads&#41;)

[//]: # (![Rating]&#40;https://img.shields.io/spiget/rating/62575?label=rating&#41;)

## Introduction

QuickShop is a shop plugin that allows players to easily sell/buy any items from a chest without any commands. In fact, none of the commands that QuickShop provides are ever needed by a player.  

[Ghost-chu/QuickShop-Hikari(this)](https://github.com/Ghost-chu/QuickShop-Hikari) forked from [PotatoCraft-Studio/QuickShop-Reremake](https://github.com/PotatoCraft-Studio/QuickShop-Reremake/) which it forked from [Ghost-chu/QuickShop-Reremake](https://github.com/Ghost-chu/QuickShop-Reremake) and original repo is [KaiKikuchi/QuickShop](https://github.com/KaiKikuchi/QuickShop). QuickShop-Hikari maintained by Ghost_chu.  

The main purpose of this branch is to modernize the core content of QuickShop and adapt the features of the latest version of Minecraft.


## Community & Support

[Discuss](https://github.com/Ghost-chu/QuickShop-Hikari/discussions)  
[Bug Tracker](https://github.com/Ghost-chu/QuickShop-Hikari/issues)  
[Discord](https://discord.gg/Bu3dVtmsD3)

## Features

- Easy to use
- Toggleable Display Item on top of the chest.
- NBT Data, Enchantment, Tool Damage, Potion, and Mob Egg support.
- Unlimited chest support.
- Blacklist support & bypass permissions.
- Shops that buy and sell items at the same time (Using double chests).
- Customisable permission checks.
- UUID support.
- Better shop protection.
- Item display name i18n.
- Enchantment display name i18n.
- A cool item preview.
- World/region protection plugins support.
- ProtocolLib based Virtual DisplayItem support.
- Powerful API.
- Optimized performance.
- MineDown syntax support.
- H2 (local) or MySQL (remote) datasource supports.
- Supports custom inventory! Use InventoryWrapper API.
- Optimized for Paper, also can run under Spigot (but little hacky and slowly).
- Advanced Transaction mechanism, rollback any Inventory/Economy operation while it failed to prevent dupes.

## Downloads

[ci.codemc.io](https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Hikari/)

## Compatibility Modules

You can download compatibility modules optional. Install them if you need.

### clearlag

Prevent [Clearlag](https://www.spigotmc.org/resources/clearlagg.68271/) remove our display items if QuickShop running under Real DisplayItem mode.

### nocheatplus

A compatibility helper to prevent player trigger NCP's anti-cheat checks while creating the shops.

### openinv

Allow player use command `/qs echest` to turn a shop use player's EnderChest inventory as inventory.

### worldedit

Removal shops that destoryed in a WorldEdit operation to prevent shops turn to a "ghost" shop.

### worldguard

Flag based shop control.

## Contribute

If you're a developer, you can contribute to the QuickShop code! Just make a fork and install the Lombok plugin,
then make a pull request when you're done! Please try to
follow [Google Java Style](https://google.github.io/styleguide/javaguide.html). Also do not increase the plugin version
number. Thank you very much!

And you can make yourself fork then publish it, no request required.

To compile the QuickShop and debug it by yourself, please follow these steps:

0. Make sure you're using Java17+ JDK in your PATH.
1. Compile main-project without signature by using profile: `mvn install -Pgithub` with github profile selected.
2. Start your server and go on.

## Bstats

[![BigImage](https://bstats.org/signatures/bukkit/QuickShop-Hikari.svg)](https://bstats.org/plugin/bukkit/QuickShop-Hikari/14281)

## License

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari?ref=badge_large)

## Developer API

```java
Plugin plugin = Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
if(plugin != null){
    QuickShopAPI api = (QuickShopAPI)plugin;
    api.xxxx;
}
```

## Maven

I'm working on a dependency issue, but at the moment you need to manually add multiple external dependencies.

```xml
    <repositories>
        <repository>
            <id>codemc</id>
            <url>https://repo.codemc.io/repository/maven-public/</url>
        </repository>
    </repositories>
    <dependencies>
        <!-- QuickShop Main Module -->
        <dependency>
            <groupId>com.ghostchu</groupId>
            <artifactId>quickshop-bukkit</artifactId>
            <version>PUT_VERSION_HERE</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
```
