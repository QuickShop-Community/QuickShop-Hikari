/*
 *  This file is a part of project QuickShop, the name is ConfigCollectorItem.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class ConfigCollectorItem implements SubPasteItem {
    private final List<File> file = new ArrayList<>();


    public ConfigCollectorItem() {
        file.add(new File(QuickShop.getInstance().getDataFolder(), "config.yml"));
        file.add(new File(QuickShop.getInstance().getDataFolder(), "interaction.yml"));
        file.add(new File(QuickShop.getInstance().getDataFolder(), "price-restriction.yml"));
        file.add(new File("server.properties"));
        file.add(new File("bukkit.yml"));
        file.add(new File("spigot.yml"));
        file.add(new File("paper.yml"));
        file.add(new File("purpur.yml"));
        file.add(new File("pufferfish.yml"));
        file.add(new File("tuinity.yml"));
        file.add(new File("airplane.air"));
    }


    @Override
    public @NotNull String getTitle() {
        return "Configurations";
    }

    @NotNull
    private String buildContent() {
        StringBuilder htmlBuilder = new StringBuilder();
        for (File file : file) {
            String fileContent = readBuildFile(file);
            if (readBuildFile(file) != null) // Hide the file in paste if file doesn't exist
                htmlBuilder.append(fileContent);
        }
        return htmlBuilder.toString();
    }

    @Nullable
    private String readBuildFile(@NotNull File file) {
        if (!file.exists()) {
            return null;
        }
        return "<h5>" + file.getName() + "</h5>" +
                "<textarea name=\"" + StringEscapeUtils.escapeHtml4(file.getName()) + "\" style=\"height: 300px; width: 100%;\">" +
                StringEscapeUtils.escapeHtml4(censor(readFile(file))) +
                "</textarea><br />";
    }

    private String censor(@NotNull String string) {
        String seedList = """
                patch_red_mushroom
                fossil_diamonds
                seagrass_slightly_less_short
                trees_water
                vines
                trees_savanna
                super_birch_bees
                patch_grass
                patch_berry_bush
                warped_forest_vegetation
                iceberg_blue
                ore_blackstone
                rooted_azalea_tree
                moss_patch_ceiling
                end_gateway_delayed
                warped_forest_vegetation_bonemeal
                ore_ancient_debris_large
                trees_sparse_jungle
                fancy_oak_bees_0002
                small_basalt_columns
                large_dripstone
                patch_crimson_roots
                flower_meadow
                meadow_trees
                ore_gold_buried
                moss_vegetation
                dripstone_cluster
                flower_default
                patch_fire
                azalea_tree
                seagrass_mid
                seagrass_tall
                blue_ice
                ice_patch
                oak_bees_005
                patch_soul_fire
                spring_nether_open
                swamp_oak
                patch_taiga_grass
                oak_bees_002
                ore_diamond_large
                warped_fungus
                jungle_tree
                clay_with_dripleaves
                basalt_blobs
                ore_gravel_nether
                crimson_fungus_planted
                glow_lichen
                spring_lava_nether
                ore_diorite
                spruce
                trees_windswept_hills
                birch
                lake_lava
                pile_melon
                large_basalt_columns
                flower_flower_forest
                freeze_top_layer
                underwater_magma
                patch_cactus
                cave_vine
                trees_old_growth_pine_taiga
                ore_gold
                birch_bees_002
                birch_bees_005
                crimson_forest_vegetation
                ore_nether_gold
                spring_nether_closed
                ore_magma
                cave_vine_in_moss
                pile_snow
                ore_coal
                oak
                end_gateway_return
                mega_jungle_tree
                lush_caves_clay
                ore_redstone
                trees_old_growth_spruce_taiga
                flower_plain
                delta
                bonus_chest
                ore_quartz
                patch_melon
                ore_emerald
                fancy_oak
                mushroom_island_vegetation
                birch_bees_0002
                ore_coal_buried
                chorus_plant
                forest_flowers
                mega_pine
                glowstone_extra
                disk_gravel
                ore_dirt
                crimson_fungus
                ore_lapis_buried
                ore_infested
                dark_oak
                ore_ancient_debris_small
                bamboo_no_podzol
                moss_patch
                birch_tall
                blackstone_blobs
                patch_waterlily
                mega_spruce
                patch_large_fern
                spring_lava_frozen
                ore_clay
                trees_grove
                end_island
                trees_birch_and_oak
                pile_pumpkin
                fancy_oak_bees
                huge_red_mushroom
                dark_forest_vegetation
                patch_dead_bush
                trees_plains
                warped_fungus_planted
                trees_taiga
                pile_ice
                desert_well
                ore_granite
                ore_lapis
                ore_gravel
                patch_tall_grass
                ore_copper_large
                flower_swamp
                warm_ocean_vegetation
                crimson_forest_vegetation_bonemeal
                disk_sand
                bamboo_some_podzol
                iceberg_packed
                ice_spike
                spring_lava_overworld
                kelp
                ore_iron
                weeping_vines
                pointed_dripstone
                end_spike
                ore_andesite
                twisting_vines
                twisting_vines_bonemeal
                oak_bees_0002
                ore_soul_sand
                acacia
                ore_copper_small
                trees_flower_forest
                forest_rock
                dripleaf
                ore_diamond_small
                disk_clay
                patch_sugar_cane
                patch_grass_jungle
                spring_water
                pile_hay
                single_piece_of_grass
                patch_sunflower
                void_start_platform
                fossil_coal
                patch_brown_mushroom
                seagrass_short
                monster_room
                seagrass_simple
                nether_sprouts
                basalt_pillar
                ore_iron_small
                moss_patch_bonemeal
                patch_pumpkin
                huge_brown_mushroom
                trees_jungle
                fancy_oak_bees_005
                bamboo_vegetation
                pine
                amethyst_geode
                fancy_oak_bees_002
                jungle_tree_no_vine
                sea_pickle
                jungle_bush
                spore_blossom
                ore_diamond_buried
                super_birch_bees_0002
                ore_tuff
                clay_pool_with_dripleaves
                nether_sprouts_bonemeal
                bamboo
                basalt_columns
                basalt_pillar
                block_pile
                blue_ice
                bonus_chest
                chorus_plant
                coral_claw
                coral_mushroom
                coral_tree
                decorated
                delta_feature
                disk
                dripstone_cluster
                end_island
                end_spike
                flower
                forest_rock
                fossil
                geode
                glow_lichen
                glowstone_blob
                growing_plant
                huge_brown_mushroom
                huge_fungus
                huge_red_mushroom
                ice_patch
                ice_spike
                iceberg
                kelp
                lake
                large_dripstone
                monster_room
                nether_forest_vegetation
                netherrack_replace_blobs
                ore
                random_boolean_selector
                random_patch
                random_selector
                replace_single_block
                root_system
                scattered_ore
                sea_pickle
                seagrass
                simple_block
                simple_random_selector
                small_dripstone
                tree
                twisting_vines
                underwater_magma
                vegetation_patch
                waterlogged_vegetation_patch
                weeping_vines
                buried_treasure
                mineshaft
                                """;
        String[] paperSeedTypes = seedList.trim().split("\n");
        List<String> seedType = Arrays.stream(paperSeedTypes).sorted((o1, o2) -> {
            int i = Integer.compare(o2.length(), o1.length());
            if (i == 0) {
                return o2.compareTo(o1);
            }
            return i;
        }).toList();
        string = string.replaceAll("secret:.*", "secret: ******")
                .replaceAll("user:.*", "user: ******")
                .replaceAll("username:.*", "username: ******")
                .replaceAll("jdbc.*", "jdbc******")
                .replaceAll("password:.*", "password: ******")
                .replaceAll("pass:.*", "pass: ******")
                .replaceAll("host:.*", "host: ******")
                .replaceAll("port:.*", "port: ******")
                .replaceAll("database:.*", "database: ******")
                .replaceAll("seed:.*", "seed: ******")
                .replaceAll("seed-.*:.*", "seed-protected: ******")
                .replaceAll("rcon\\.password=.*", "rcon.password=******")
                .replaceAll("token:.*", "token: ******")
                .replaceAll("key:.*", "key: ******")
                .replaceAll("seed=.*", "seed=******");
        for (String paperSeedType : seedType) {
            string = string.replaceAll(paperSeedType + ":.*", "seed-protected: ******");
        }
        return string;


    }

    @NotNull
    private String readFile(@NotNull File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            StringJoiner joiner = new StringJoiner("\n");
            lines.forEach(joiner::add);
            return joiner.toString();
        } catch (IOException e) {
            return "Fail: " + e.getMessage();
        }
    }

    @Override
    public @NotNull String genBody() {
        return buildContent();
    }
}
