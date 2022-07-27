#!/bin/sh


echo "Deploy jar to dev.bukkit.org"

xToken=$1
curl -X POST -H "X-Api-Token: $xToken" -F 'metadata={"changelog":"Auto upload by Jenkins CI, see update details at https://www.spigotmc.org/resources/100125/updates","gameVersions":[8503,7915,7667,7330,7105],"releaseType":"release"}' -F "file=@./target/QuickShop.jar" -s "https://dev.bukkit.org/api/projects/320536/upload-file" >fileid.json
cat fileid.json
echo "Finished upload to dev.bukkit.org."

exit 0
