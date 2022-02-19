#!/bin/sh
#
#  This file is a part of project QuickShop, the name is deploy.sh
#  Copyright (C) Ghost_chu and contributors
#
#  This program is free software: you can redistribute it and/or modify it
#  under the terms of the GNU General Public License as published by the
#  Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
#  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
#  for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program. If not, see <http://www.gnu.org/licenses/>.
#
#

echo "Deploy jar to dev.bukkit.org"

xToken=$1
curl -X POST -H "X-Api-Token: $xToken" -F 'metadata={"changelog":"Auto upload by Jenkins CI, see update details at https://www.spigotmc.org/resources/100125/updates","gameVersions":[8503,7915,7667,7330,7105],"releaseType":"release"}' -F "file=@./target/QuickShop.jar" -s "https://dev.bukkit.org/api/projects/320536/upload-file" >fileid.json
cat fileid.json
echo "Finished upload to dev.bukkit.org."

exit 0
