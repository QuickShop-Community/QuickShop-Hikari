# QuickShop-Apollo

[//]: # ([![Codacy Badge]&#40;https://app.codacy.com/project/badge/Grade/e33e2fafe3ac4d4eb9048d154bbd874e&#41;]&#40;https://www.codacy.com/gh/PotatoCraft-Studio/QuickShop-Reremake/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=PotatoCraft-Studio/QuickShop-Reremake&amp;utm_campaign=Badge_Grade&#41;)

[//]: # ([![CodeFactor]&#40;https://www.codefactor.io/repository/github/potatocraft-studio/quickshop-reremake/badge&#41;]&#40;https://www.codefactor.io/repository/github/potatocraft-studio/quickshop-reremake&#41;)

[//]: # (![BuildStatus]&#40;https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake/21/badge/icon&#41;)

[//]: # (![TestsPassed]&#40;https://img.shields.io/jenkins/tests?compact_message&jobUrl=https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake&#41;)

[//]: # (![Contributors]&#40;https://img.shields.io/github/contributors/potatocraft-studio/QuickShop-Reremake&#41;)

[//]: # ([![FOSSA Status]&#40;https://app.fossa.com/api/projects/git%2Bgithub.com%2FPotatoCraft-Studio%2FQuickShop-Reremake.svg?type=shield&#41;]&#40;https://app.fossa.com/projects/git%2Bgithub.com%2FPotatoCraft-Studio%2FQuickShop-Reremake?ref=badge_shield&#41;)

[//]: # (---)

[//]: # ()
[//]: # (![Java]&#40;https://img.shields.io/badge/java-version%208%2B%20&#40;currently%20is%208--16&#41;-orange&#41;)

[//]: # (![MC]&#40;https://img.shields.io/badge/minecraft-java%20edition%201.15%2B-blueviolet&#41;)

[//]: # (![Ver]&#40;https://img.shields.io/spiget/version/62575?label=version&#41;)

[//]: # (![Downloads]&#40;https://img.shields.io/spiget/downloads/62575?label=downloads&#41;)

[//]: # (![Rating]&#40;https://img.shields.io/spiget/rating/62575?label=rating&#41;)

---
QuickShop is a **FREE** shop plugin that allows players to easily sell/buy any items from a chest without any commands. In fact,
none of the commands that QuickShop provides are ever needed by a player. QuickShop-Reremake is a **FREE** fork of QuickShop
NotLikeMe with more features, bug fixes and other improvements.  
QuickShop-Reremake is made by PotatoCraft Studio
from [KaiKikuchi's QuickShop upstream repository](https://github.com/KaiKikuchi/QuickShop).

## Support

Use GitHub issue.

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

No releases yet.

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
