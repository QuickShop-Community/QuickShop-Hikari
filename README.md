# QuickShop-Hikari

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a04ef7174d9f4e65b60ae28b09222809)](https://www.codacy.com/gh/Ghost-chu/QuickShop-Hikari/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Ghost-chu/QuickShop-Hikari&amp;utm_campaign=Badge_Grade)
[![CodeFactor](https://www.codefactor.io/repository/github/ghost-chu/quickshop-hikari/badge)](https://www.codefactor.io/repository/github/ghost-chu/quickshop-hikari)
![GitHub license](https://img.shields.io/github/license/Ghost-chu/QuickShop-Hikari.svg)
![TestsPassed](https://img.shields.io/jenkins/tests?compact_message&jobUrl=https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake)
![Contributors](https://img.shields.io/github/contributors/Ghost-chu/QuickShop-Hikari)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FGhost-chu%2FQuickShop-Hikari?ref=badge_shield)

![Java](https://img.shields.io/badge/java-version%208%2B%20(currently%20is%2016--17)-orange)
![MC](https://img.shields.io/badge/minecraft-java%20edition%201.16%2B-blueviolet)

[//]: # (![Ver]&#40;https://img.shields.io/spiget/version/62575?label=version&#41;)

[//]: # (![Downloads]&#40;https://img.shields.io/spiget/downloads/62575?label=downloads&#41;)

[//]: # (![Rating]&#40;https://img.shields.io/spiget/rating/62575?label=rating&#41;)

---
QuickShop-Hikari forked from QuickShop-Reremake and is maintained by Ghost_chu.  

The main purpose of this branch is to modernize the core content of QuickShop and adapt the features of the latest version of Minecraft.


## Community & Support

[Discuss](https://github.com/Ghost-chu/QuickShop-Hikari/discussions)  
[Bug Tracker](https://github.com/Ghost-chu/QuickShop-Hikari/issues)

## Features

- Easy to use
- Toggleable Display Item on top of the chest
- NBT Data, Enchantment, Tool Damage, Potion, and Mob Egg support
- Unlimited chest support
- Blacklist support & bypass permissions
- Shops that buy and sell items at the same time (Using double chests)
- Customisable permission checks
- UUID support
- Better shop protection
- Item display name i18n
- Enchantment display name i18n
- A cool item preview
- World/region protection plugins support
- ProtocolLib based Virtual DisplayItem support
- Powerful API
- Optimized performance
- MineDown syntax support

## Downloads

No releases yet.

## Contribute

If you're a developer, you can contribute to the QuickShop code! Just make a fork and install the Lombok plugin,
then make a pull request when you're done! Please try to
follow [Google Java Style](https://google.github.io/styleguide/javaguide.html). Also do not increase the plugin version
number. Thank you very much!

And you can make yourself fork then publish it, no request required.

To compile the QuickShop and debug it by yourself, please follow these steps:

0. Make sure you're using Java16 JDK in your PATH.
1. Compile main-project without signature by using debug profile: `mvn install -Pdebug`
2. Start your server and go on.

## Maven


No maven yet.

[//]: # (```XML)

[//]: # ()
[//]: # (<repository>)

[//]: # (    <id>quickshop-repo</id>)

[//]: # (    <url>https://repo.codemc.io/repository/maven-public/</url>)

[//]: # (</repository>)

[//]: # ()
[//]: # (<dependency>)

[//]: # (<groupId>org.maxgamer</groupId>)

[//]: # (<artifactId>QuickShop</artifactId>)

[//]: # (<version>{VERSION}</version>)

[//]: # (<scope>provided</scope>)

[//]: # (</dependency>)

[//]: # (```)

[//]: # (## Bstats)

[//]: # ()
[//]: # ([![BigImage]&#40;https://bstats.org/signatures/bukkit/QuickShop-Reremake.svg&#41;]&#40;https://bstats.org/plugin/bukkit/QuickShop-Reremake/3320&#41;)

[//]: # ()
[//]: # (## License)

[//]: # ()
[//]: # ([![FOSSA Status]&#40;https://app.fossa.com/api/projects/git%2Bgithub.com%2FPotatoCraft-Studio%2FQuickShop-Reremake.svg?type=large&#41;]&#40;https://app.fossa.com/projects/git%2Bgithub.com%2FPotatoCraft-Studio%2FQuickShop-Reremake?ref=badge_large&#41;)

## Developer API

```java
Plugin plugin = Bukkit.getPluginManager().getPlugin("QuickShop");
if(plugin != null){
    QuickShopAPI api = (QuickShopAPI)plugin;
    api.xxxx;
}
```
