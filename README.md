<!-- Links -->
[codacy]: https://www.codacy.com/gh/Ghost-chu/QuickShop-Hikari/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Ghost-chu/QuickShop-Hikari&amp;utm_campaign=Badge_Grade
[codeFactor]: https://www.codefactor.io/repository/github/ghost-chu/quickshop-hikari
[codeScore]: https://app.codiga.io/public/project/32011/QuickShop-Hikari/dashboard

[license]: https://github.com/Ghost-chu/QuickShop-Hikari/blob/hikari/LICENSE
[contributors]: https://github.com/Ghost-chu/QuickShop-Hikari/graphs/contributors

[fossaStatus]: https://app.fossa.com/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari?ref=badge_shield
[fossaStatusLarge]: https://app.fossa.com/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari?ref=badge_large

[quickshop-hikari]: https://github.com/Ghost-chu/QuickShop-Hikari
[quickshop-potato]: https://github.com/PotatoCraft-Studio/QuickShop-Reremake/
[quickshop-ghostchu]: https://github.com/Ghost-chu/QuickShop-Reremake
[quickshop-original]: https://github.com/KaiKikuchi/QuickShop

[codemc]: https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Hikari/

[clearlagg]: https://www.spigotmc.org/resources/68271/

[googlejava]: https://google.github.io/styleguide/javaguide.html

[adoptium]: https://adoptium.net/

[bStats]: https://bstats.org/plugin/bukkit/QuickShop-Hikari/14281

<!-- Images/Badges -->
[codacyBadge]: https://app.codacy.com/project/badge/Grade/a04ef7174d9f4e65b60ae28b09222809
[codeFactorBadge]: https://www.codefactor.io/repository/github/ghost-chu/quickshop-hikari/badge
[codeScoreBadge]: https://api.codiga.io/project/32011/score/svg

[licenseBadge]: https://img.shields.io/github/license/Ghost-chu/QuickShop-Hikari.svg
[contributorsBadge]: https://img.shields.io/github/contributors/Ghost-chu/QuickShop-Hikari

[passedTests]: https://img.shields.io/jenkins/tests?compact_message&jobUrl=https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Hikari

[fossaStatusBadge]: https://app.fossa.com/api/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari.svg?type=shield
[fossaStatusImageLarge]: https://app.fossa.com/api/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari.svg?type=large

[javaVersion]: https://img.shields.io/badge/Java-Versions_17_+_18-orange.svg
[minecraftVersion]: https://img.shields.io/badge/Minecraft-Java_Edition_1.18.x-blueviolet.svg

[bStatsImage]: https://bstats.org/signatures/bukkit/QuickShop-Hikari.svg

<!-- Unused? -->
<!-- [//]: # (![Ver]&#40;https://img.shields.io/spiget/version/62575?label=version&#41;) -->
<!-- [//]: # (![Downloads]&#40;https://img.shields.io/spiget/downloads/62575?label=downloads&#41;) -->
<!-- [//]: # (![Rating]&#40;https://img.shields.io/spiget/rating/62575?label=rating&#41;) -->

<!-- Start of README -->
# QuickShop-Hikari

[![codacyBadge]][codacy]
[![codeFactorBadge]][codeFactor]
[![codeScoreBadge]][codeScore]
[![licenseBadge]][license]
[![contributorsBadge]][contributors]
![passedTests]
[![fossaStatusBadge]][fossaStatus]

![javaVersion]
![minecraftVersion]

## Introduction

QuickShop Hikari is a Shop plugin that allows players to create Chest Shops to easily sell and buy items, without the need of any commands.  
In fact are all available commands in QuickShop not needed for normal gameplay.

This version of QuickShop ([Ghost-chu/QuickShop-Hikari][quickshop-hikari]) is a fork from [PotatoCraft-Studio's version][quickshop-potato] which itself is a fork from the [Reremake][quickshop-ghostchu] of the [original QuickShop][quickshop-original].

QuickShop Hikari is maintained by Ghost-Chu and has the goal to modernize the core content of QuickShop and adabt it to the latest versions of Minecraft.

## Community & Support

[Discussions](https://github.com/Ghost-chu/QuickShop-Hikari/discussions)  
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
- i18n support for displayed Item names.
- i18n support for displayed Enchantment names.
- A cool item preview.
- World/region protection plugins support.
- ProtocolLib based Virtual Display Item support.
- Powerful API.
- Optimized performance.
- MiniMessage syntax support.
- H2 (local) or MySQL (remote) datasource supports.
- Supports custom inventory! Use the InventoryWrapper API.
- Optimized for Paper, but also runs on Spigot (Tho slower and with a more "hacky" aproach).
- Advanced Transaction System. Undo any Inventory/Economy operation with a shop when it failed to prevent duplications
  and exploits.
- Per-shop permission management.

## Downloads

Obtain the latest version from [ci.codemc.io][codemc]

## Compatibility Modules

You can download optional modules for compatability with other plugins.

### clearlagg

Stops [ClearLagg][clearlagg] from deleting the Display Item on any Shop.

### nocheatplus

Prevents NCP's anti-cheat checks from triggering when creating a shop.

### openinv

Allow the usage of a Player's Ender Chest as Shop inventory by using `/qs echest`.

### worldedit

Removes Shops that got deleted during a WorldEdit operation, to reduce "Ghost Shops".

### worldguard

Flag-based shop control.

## Contribute

If you're a developer, you can contribute to the QuickShop source code! Just make a fork, install the Lombok plugin if not done automatically),
make your changes and create a pull request when you're done!  
Please try to follow [Google's Java Code Style][googlejava] and do not increase the Plugin version. Thank you!

### Distributing forks

You're allowed to create your own fork to share. No permission needed.  
Tho it would be nice if you could pr your changes into this repository when they are good ones.

### Compile and Debugging

To compile and debug QuickShop, please do the following steps:

0. Make sure you're using Java 17 or 18. You can get the latest Java versions from the [Adoptium project][adoptium].
1. Compile the main project without a signature by using `mvn install -Pgithub` with the GitHub Profile selected.
2. Put the compiled jar into your Test-server's `plugins` folder, start the server and begin debugging!

To compile the QuickShop and debug it by yourself, please follow these steps:

0. Make sure you're using Java17+ JDK in your PATH.
1. Compile main-project without signature by using profile: `mvn install -Pgithub` with github profile selected.
2. Start your server and go on.

## Bstats

[![bStatsImage]][bStats]

## License

[![fossaStatusImageLarge]][fossaStatusLarge]

## Developer API

```java
Plugin plugin = Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
if(plugin != null){
    QuickShopAPI api = (QuickShopAPI)plugin;
    api.xxxx;
}
```

## Maven


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
            <version>VERSION HERE</version>
            <scope>provided</scope>
            <classifier>shaded</classifier>
        </dependency>
    </dependencies>
```
