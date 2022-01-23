# QuickShop-Reremake

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/e33e2fafe3ac4d4eb9048d154bbd874e)](https://www.codacy.com/gh/PotatoCraft-Studio/QuickShop-Reremake/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=PotatoCraft-Studio/QuickShop-Reremake&amp;utm_campaign=Badge_Grade)
[![CodeFactor](https://www.codefactor.io/repository/github/potatocraft-studio/quickshop-reremake/badge)](https://www.codefactor.io/repository/github/potatocraft-studio/quickshop-reremake)
![BuildStatus](https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake/21/badge/icon)
![TestsPassed](https://img.shields.io/jenkins/tests?compact_message&jobUrl=https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake)
![Contributors](https://img.shields.io/github/contributors/potatocraft-studio/QuickShop-Reremake)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FPotatoCraft-Studio%2FQuickShop-Reremake.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FPotatoCraft-Studio%2FQuickShop-Reremake?ref=badge_shield)
---

![Java](https://img.shields.io/badge/java-version%208%2B%20(currently%20is%208--16)-orange)
![MC](https://img.shields.io/badge/minecraft-java%20edition%201.15%2B-blueviolet)
![Ver](https://img.shields.io/spiget/version/62575?label=version)
![Downloads](https://img.shields.io/spiget/downloads/62575?label=downloads)
![Rating](https://img.shields.io/spiget/rating/62575?label=rating)
---

QuickShop is a **FREE** shop plugin that allows players to easily sell/buy any items from a chest without any commands. In fact,
none of the commands that QuickShop provides are ever needed by a player. QuickShop-Reremake is a **FREE** fork of QuickShop
NotLikeMe with more features, bug fixes and other improvements.  
QuickShop-Reremake is made by PotatoCraft Studio
from [KaiKikuchi's QuickShop upstream repository](https://github.com/KaiKikuchi/QuickShop).

## Support

| <a href="https://discord.gg/bfefw2E"/> <img src="/.github/icons/Discord.svg" width="100" height="100" />| <a href="https://github.com/PotatoCraft-Studio/QuickShop-Reremake/issues"><img src="/.github/icons/Github.png" width="100" height="100" />|
| :---: | :---: |
| [**Discord**](https://discord.gg/bfefw2E) | [**Github Issues**](https://github.com/PotatoCraft-Studio/QuickShop-Reremake/issues) |

## Features

- Easy to use
- Toggleable Display Item on top of the chest
- NBT Data, Enchantment, Tool Damage, Potion, and Mob Egg support
- Unlimited chest support
- Blacklist support & bypass permissions
- Shops that buy and sell items at the same time (Using double chests)
- Customisable permission checks
- UUID support
- Better shop protection [Reremake]
- Item display name i18n [Reremake]
- Enchantment display name i18n [Reremake]
- A cool item preview [Reremake]
- World/region protection plugins support [Reremake]
- ProtocolLib based Virtual DisplayItem support [Reremake]
- Powerful API [Reremake]
- Optimized performance [Reremake]

## Downloads

| <a href="https://www.spigotmc.org/resources/62575/"><img src="/.github/icons/Spigot.png" width="100" height="90" /></a> | <a href="https://dev.bukkit.org/projects/quickshop-reremake"><img src="/.github/icons/Bukkit.png" width="100" height="100" /></a></a> | <a href="http://www.mcbbs.net/thread-809496-1-1.html"><img src="/.github/icons/MCBBS.png" width="100" height="100" /></a> | <a href="https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake-SNAPSHOT/"><img src="/.github/icons/Jenkins.svg" width="85" height="100" /></a>
 | :---: | :---: | :---: | :---: |
| **Spigot** | **BukkitDev** | **MCBBS** | **Jenkins** |

## Contribute

If you're a developer, you can contribute to the QuickShop code! Just make a fork and install the Lombok plugin,
then make a pull request when you're done! Please try to
follow [Google Java Style](https://google.github.io/styleguide/javaguide.html). Also do not increase the plugin version
number. Thank you very much!

To compile the QuickShop and debug it by yourself, please follow these steps:

0. Make sure you're using Java16 JDK in your PATH.
1. Compile main-project without signature by using debug profile: `mvn install -Pdebug`
2. Start your server with extra flag to skip the QuickShop signature
   checks: `-Dorg.maxgamer.quickshop.util.envcheck.skip.SIGNATURE_VERIFY`

## Maven

```XML

<repository>
    <id>quickshop-repo</id>
    <url>https://repo.codemc.io/repository/maven-public/</url>
</repository>

<dependency>
<groupId>org.maxgamer</groupId>
<artifactId>QuickShop</artifactId>
<version>{VERSION}</version>
<scope>provided</scope>
</dependency>
```

## Bstats

[![BigImage](https://bstats.org/signatures/bukkit/QuickShop-Reremake.svg)](https://bstats.org/plugin/bukkit/QuickShop-Reremake/3320)

## License

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FPotatoCraft-Studio%2FQuickShop-Reremake.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2FPotatoCraft-Studio%2FQuickShop-Reremake?ref=badge_large)

## Developer API

```java
Plugin plugin = Bukkit.getPluginManager().getPlugin("QuickShop");
if(plugin != null){
    QuickShopAPI api = (QuickShopAPI)plugin;
    api.xxxx;
}
```
