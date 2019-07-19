package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ItemStackTheFlatteningFix extends DataFix {
	private static final Map<String, String> MAP = DataFixUtils.make(Maps.<String, String>newHashMap(), hashMap -> {
		hashMap.put("minecraft:stone.0", "minecraft:stone");
		hashMap.put("minecraft:stone.1", "minecraft:granite");
		hashMap.put("minecraft:stone.2", "minecraft:polished_granite");
		hashMap.put("minecraft:stone.3", "minecraft:diorite");
		hashMap.put("minecraft:stone.4", "minecraft:polished_diorite");
		hashMap.put("minecraft:stone.5", "minecraft:andesite");
		hashMap.put("minecraft:stone.6", "minecraft:polished_andesite");
		hashMap.put("minecraft:dirt.0", "minecraft:dirt");
		hashMap.put("minecraft:dirt.1", "minecraft:coarse_dirt");
		hashMap.put("minecraft:dirt.2", "minecraft:podzol");
		hashMap.put("minecraft:leaves.0", "minecraft:oak_leaves");
		hashMap.put("minecraft:leaves.1", "minecraft:spruce_leaves");
		hashMap.put("minecraft:leaves.2", "minecraft:birch_leaves");
		hashMap.put("minecraft:leaves.3", "minecraft:jungle_leaves");
		hashMap.put("minecraft:leaves2.0", "minecraft:acacia_leaves");
		hashMap.put("minecraft:leaves2.1", "minecraft:dark_oak_leaves");
		hashMap.put("minecraft:log.0", "minecraft:oak_log");
		hashMap.put("minecraft:log.1", "minecraft:spruce_log");
		hashMap.put("minecraft:log.2", "minecraft:birch_log");
		hashMap.put("minecraft:log.3", "minecraft:jungle_log");
		hashMap.put("minecraft:log2.0", "minecraft:acacia_log");
		hashMap.put("minecraft:log2.1", "minecraft:dark_oak_log");
		hashMap.put("minecraft:sapling.0", "minecraft:oak_sapling");
		hashMap.put("minecraft:sapling.1", "minecraft:spruce_sapling");
		hashMap.put("minecraft:sapling.2", "minecraft:birch_sapling");
		hashMap.put("minecraft:sapling.3", "minecraft:jungle_sapling");
		hashMap.put("minecraft:sapling.4", "minecraft:acacia_sapling");
		hashMap.put("minecraft:sapling.5", "minecraft:dark_oak_sapling");
		hashMap.put("minecraft:planks.0", "minecraft:oak_planks");
		hashMap.put("minecraft:planks.1", "minecraft:spruce_planks");
		hashMap.put("minecraft:planks.2", "minecraft:birch_planks");
		hashMap.put("minecraft:planks.3", "minecraft:jungle_planks");
		hashMap.put("minecraft:planks.4", "minecraft:acacia_planks");
		hashMap.put("minecraft:planks.5", "minecraft:dark_oak_planks");
		hashMap.put("minecraft:sand.0", "minecraft:sand");
		hashMap.put("minecraft:sand.1", "minecraft:red_sand");
		hashMap.put("minecraft:quartz_block.0", "minecraft:quartz_block");
		hashMap.put("minecraft:quartz_block.1", "minecraft:chiseled_quartz_block");
		hashMap.put("minecraft:quartz_block.2", "minecraft:quartz_pillar");
		hashMap.put("minecraft:anvil.0", "minecraft:anvil");
		hashMap.put("minecraft:anvil.1", "minecraft:chipped_anvil");
		hashMap.put("minecraft:anvil.2", "minecraft:damaged_anvil");
		hashMap.put("minecraft:wool.0", "minecraft:white_wool");
		hashMap.put("minecraft:wool.1", "minecraft:orange_wool");
		hashMap.put("minecraft:wool.2", "minecraft:magenta_wool");
		hashMap.put("minecraft:wool.3", "minecraft:light_blue_wool");
		hashMap.put("minecraft:wool.4", "minecraft:yellow_wool");
		hashMap.put("minecraft:wool.5", "minecraft:lime_wool");
		hashMap.put("minecraft:wool.6", "minecraft:pink_wool");
		hashMap.put("minecraft:wool.7", "minecraft:gray_wool");
		hashMap.put("minecraft:wool.8", "minecraft:light_gray_wool");
		hashMap.put("minecraft:wool.9", "minecraft:cyan_wool");
		hashMap.put("minecraft:wool.10", "minecraft:purple_wool");
		hashMap.put("minecraft:wool.11", "minecraft:blue_wool");
		hashMap.put("minecraft:wool.12", "minecraft:brown_wool");
		hashMap.put("minecraft:wool.13", "minecraft:green_wool");
		hashMap.put("minecraft:wool.14", "minecraft:red_wool");
		hashMap.put("minecraft:wool.15", "minecraft:black_wool");
		hashMap.put("minecraft:carpet.0", "minecraft:white_carpet");
		hashMap.put("minecraft:carpet.1", "minecraft:orange_carpet");
		hashMap.put("minecraft:carpet.2", "minecraft:magenta_carpet");
		hashMap.put("minecraft:carpet.3", "minecraft:light_blue_carpet");
		hashMap.put("minecraft:carpet.4", "minecraft:yellow_carpet");
		hashMap.put("minecraft:carpet.5", "minecraft:lime_carpet");
		hashMap.put("minecraft:carpet.6", "minecraft:pink_carpet");
		hashMap.put("minecraft:carpet.7", "minecraft:gray_carpet");
		hashMap.put("minecraft:carpet.8", "minecraft:light_gray_carpet");
		hashMap.put("minecraft:carpet.9", "minecraft:cyan_carpet");
		hashMap.put("minecraft:carpet.10", "minecraft:purple_carpet");
		hashMap.put("minecraft:carpet.11", "minecraft:blue_carpet");
		hashMap.put("minecraft:carpet.12", "minecraft:brown_carpet");
		hashMap.put("minecraft:carpet.13", "minecraft:green_carpet");
		hashMap.put("minecraft:carpet.14", "minecraft:red_carpet");
		hashMap.put("minecraft:carpet.15", "minecraft:black_carpet");
		hashMap.put("minecraft:hardened_clay.0", "minecraft:terracotta");
		hashMap.put("minecraft:stained_hardened_clay.0", "minecraft:white_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.1", "minecraft:orange_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.2", "minecraft:magenta_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.3", "minecraft:light_blue_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.4", "minecraft:yellow_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.5", "minecraft:lime_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.6", "minecraft:pink_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.7", "minecraft:gray_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.8", "minecraft:light_gray_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.9", "minecraft:cyan_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.10", "minecraft:purple_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.11", "minecraft:blue_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.12", "minecraft:brown_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.13", "minecraft:green_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.14", "minecraft:red_terracotta");
		hashMap.put("minecraft:stained_hardened_clay.15", "minecraft:black_terracotta");
		hashMap.put("minecraft:silver_glazed_terracotta.0", "minecraft:light_gray_glazed_terracotta");
		hashMap.put("minecraft:stained_glass.0", "minecraft:white_stained_glass");
		hashMap.put("minecraft:stained_glass.1", "minecraft:orange_stained_glass");
		hashMap.put("minecraft:stained_glass.2", "minecraft:magenta_stained_glass");
		hashMap.put("minecraft:stained_glass.3", "minecraft:light_blue_stained_glass");
		hashMap.put("minecraft:stained_glass.4", "minecraft:yellow_stained_glass");
		hashMap.put("minecraft:stained_glass.5", "minecraft:lime_stained_glass");
		hashMap.put("minecraft:stained_glass.6", "minecraft:pink_stained_glass");
		hashMap.put("minecraft:stained_glass.7", "minecraft:gray_stained_glass");
		hashMap.put("minecraft:stained_glass.8", "minecraft:light_gray_stained_glass");
		hashMap.put("minecraft:stained_glass.9", "minecraft:cyan_stained_glass");
		hashMap.put("minecraft:stained_glass.10", "minecraft:purple_stained_glass");
		hashMap.put("minecraft:stained_glass.11", "minecraft:blue_stained_glass");
		hashMap.put("minecraft:stained_glass.12", "minecraft:brown_stained_glass");
		hashMap.put("minecraft:stained_glass.13", "minecraft:green_stained_glass");
		hashMap.put("minecraft:stained_glass.14", "minecraft:red_stained_glass");
		hashMap.put("minecraft:stained_glass.15", "minecraft:black_stained_glass");
		hashMap.put("minecraft:stained_glass_pane.0", "minecraft:white_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.1", "minecraft:orange_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.2", "minecraft:magenta_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.3", "minecraft:light_blue_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.4", "minecraft:yellow_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.5", "minecraft:lime_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.6", "minecraft:pink_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.7", "minecraft:gray_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.8", "minecraft:light_gray_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.9", "minecraft:cyan_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.10", "minecraft:purple_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.11", "minecraft:blue_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.12", "minecraft:brown_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.13", "minecraft:green_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.14", "minecraft:red_stained_glass_pane");
		hashMap.put("minecraft:stained_glass_pane.15", "minecraft:black_stained_glass_pane");
		hashMap.put("minecraft:prismarine.0", "minecraft:prismarine");
		hashMap.put("minecraft:prismarine.1", "minecraft:prismarine_bricks");
		hashMap.put("minecraft:prismarine.2", "minecraft:dark_prismarine");
		hashMap.put("minecraft:concrete.0", "minecraft:white_concrete");
		hashMap.put("minecraft:concrete.1", "minecraft:orange_concrete");
		hashMap.put("minecraft:concrete.2", "minecraft:magenta_concrete");
		hashMap.put("minecraft:concrete.3", "minecraft:light_blue_concrete");
		hashMap.put("minecraft:concrete.4", "minecraft:yellow_concrete");
		hashMap.put("minecraft:concrete.5", "minecraft:lime_concrete");
		hashMap.put("minecraft:concrete.6", "minecraft:pink_concrete");
		hashMap.put("minecraft:concrete.7", "minecraft:gray_concrete");
		hashMap.put("minecraft:concrete.8", "minecraft:light_gray_concrete");
		hashMap.put("minecraft:concrete.9", "minecraft:cyan_concrete");
		hashMap.put("minecraft:concrete.10", "minecraft:purple_concrete");
		hashMap.put("minecraft:concrete.11", "minecraft:blue_concrete");
		hashMap.put("minecraft:concrete.12", "minecraft:brown_concrete");
		hashMap.put("minecraft:concrete.13", "minecraft:green_concrete");
		hashMap.put("minecraft:concrete.14", "minecraft:red_concrete");
		hashMap.put("minecraft:concrete.15", "minecraft:black_concrete");
		hashMap.put("minecraft:concrete_powder.0", "minecraft:white_concrete_powder");
		hashMap.put("minecraft:concrete_powder.1", "minecraft:orange_concrete_powder");
		hashMap.put("minecraft:concrete_powder.2", "minecraft:magenta_concrete_powder");
		hashMap.put("minecraft:concrete_powder.3", "minecraft:light_blue_concrete_powder");
		hashMap.put("minecraft:concrete_powder.4", "minecraft:yellow_concrete_powder");
		hashMap.put("minecraft:concrete_powder.5", "minecraft:lime_concrete_powder");
		hashMap.put("minecraft:concrete_powder.6", "minecraft:pink_concrete_powder");
		hashMap.put("minecraft:concrete_powder.7", "minecraft:gray_concrete_powder");
		hashMap.put("minecraft:concrete_powder.8", "minecraft:light_gray_concrete_powder");
		hashMap.put("minecraft:concrete_powder.9", "minecraft:cyan_concrete_powder");
		hashMap.put("minecraft:concrete_powder.10", "minecraft:purple_concrete_powder");
		hashMap.put("minecraft:concrete_powder.11", "minecraft:blue_concrete_powder");
		hashMap.put("minecraft:concrete_powder.12", "minecraft:brown_concrete_powder");
		hashMap.put("minecraft:concrete_powder.13", "minecraft:green_concrete_powder");
		hashMap.put("minecraft:concrete_powder.14", "minecraft:red_concrete_powder");
		hashMap.put("minecraft:concrete_powder.15", "minecraft:black_concrete_powder");
		hashMap.put("minecraft:cobblestone_wall.0", "minecraft:cobblestone_wall");
		hashMap.put("minecraft:cobblestone_wall.1", "minecraft:mossy_cobblestone_wall");
		hashMap.put("minecraft:sandstone.0", "minecraft:sandstone");
		hashMap.put("minecraft:sandstone.1", "minecraft:chiseled_sandstone");
		hashMap.put("minecraft:sandstone.2", "minecraft:cut_sandstone");
		hashMap.put("minecraft:red_sandstone.0", "minecraft:red_sandstone");
		hashMap.put("minecraft:red_sandstone.1", "minecraft:chiseled_red_sandstone");
		hashMap.put("minecraft:red_sandstone.2", "minecraft:cut_red_sandstone");
		hashMap.put("minecraft:stonebrick.0", "minecraft:stone_bricks");
		hashMap.put("minecraft:stonebrick.1", "minecraft:mossy_stone_bricks");
		hashMap.put("minecraft:stonebrick.2", "minecraft:cracked_stone_bricks");
		hashMap.put("minecraft:stonebrick.3", "minecraft:chiseled_stone_bricks");
		hashMap.put("minecraft:monster_egg.0", "minecraft:infested_stone");
		hashMap.put("minecraft:monster_egg.1", "minecraft:infested_cobblestone");
		hashMap.put("minecraft:monster_egg.2", "minecraft:infested_stone_bricks");
		hashMap.put("minecraft:monster_egg.3", "minecraft:infested_mossy_stone_bricks");
		hashMap.put("minecraft:monster_egg.4", "minecraft:infested_cracked_stone_bricks");
		hashMap.put("minecraft:monster_egg.5", "minecraft:infested_chiseled_stone_bricks");
		hashMap.put("minecraft:yellow_flower.0", "minecraft:dandelion");
		hashMap.put("minecraft:red_flower.0", "minecraft:poppy");
		hashMap.put("minecraft:red_flower.1", "minecraft:blue_orchid");
		hashMap.put("minecraft:red_flower.2", "minecraft:allium");
		hashMap.put("minecraft:red_flower.3", "minecraft:azure_bluet");
		hashMap.put("minecraft:red_flower.4", "minecraft:red_tulip");
		hashMap.put("minecraft:red_flower.5", "minecraft:orange_tulip");
		hashMap.put("minecraft:red_flower.6", "minecraft:white_tulip");
		hashMap.put("minecraft:red_flower.7", "minecraft:pink_tulip");
		hashMap.put("minecraft:red_flower.8", "minecraft:oxeye_daisy");
		hashMap.put("minecraft:double_plant.0", "minecraft:sunflower");
		hashMap.put("minecraft:double_plant.1", "minecraft:lilac");
		hashMap.put("minecraft:double_plant.2", "minecraft:tall_grass");
		hashMap.put("minecraft:double_plant.3", "minecraft:large_fern");
		hashMap.put("minecraft:double_plant.4", "minecraft:rose_bush");
		hashMap.put("minecraft:double_plant.5", "minecraft:peony");
		hashMap.put("minecraft:deadbush.0", "minecraft:dead_bush");
		hashMap.put("minecraft:tallgrass.0", "minecraft:dead_bush");
		hashMap.put("minecraft:tallgrass.1", "minecraft:grass");
		hashMap.put("minecraft:tallgrass.2", "minecraft:fern");
		hashMap.put("minecraft:sponge.0", "minecraft:sponge");
		hashMap.put("minecraft:sponge.1", "minecraft:wet_sponge");
		hashMap.put("minecraft:purpur_slab.0", "minecraft:purpur_slab");
		hashMap.put("minecraft:stone_slab.0", "minecraft:stone_slab");
		hashMap.put("minecraft:stone_slab.1", "minecraft:sandstone_slab");
		hashMap.put("minecraft:stone_slab.2", "minecraft:petrified_oak_slab");
		hashMap.put("minecraft:stone_slab.3", "minecraft:cobblestone_slab");
		hashMap.put("minecraft:stone_slab.4", "minecraft:brick_slab");
		hashMap.put("minecraft:stone_slab.5", "minecraft:stone_brick_slab");
		hashMap.put("minecraft:stone_slab.6", "minecraft:nether_brick_slab");
		hashMap.put("minecraft:stone_slab.7", "minecraft:quartz_slab");
		hashMap.put("minecraft:stone_slab2.0", "minecraft:red_sandstone_slab");
		hashMap.put("minecraft:wooden_slab.0", "minecraft:oak_slab");
		hashMap.put("minecraft:wooden_slab.1", "minecraft:spruce_slab");
		hashMap.put("minecraft:wooden_slab.2", "minecraft:birch_slab");
		hashMap.put("minecraft:wooden_slab.3", "minecraft:jungle_slab");
		hashMap.put("minecraft:wooden_slab.4", "minecraft:acacia_slab");
		hashMap.put("minecraft:wooden_slab.5", "minecraft:dark_oak_slab");
		hashMap.put("minecraft:coal.0", "minecraft:coal");
		hashMap.put("minecraft:coal.1", "minecraft:charcoal");
		hashMap.put("minecraft:fish.0", "minecraft:cod");
		hashMap.put("minecraft:fish.1", "minecraft:salmon");
		hashMap.put("minecraft:fish.2", "minecraft:clownfish");
		hashMap.put("minecraft:fish.3", "minecraft:pufferfish");
		hashMap.put("minecraft:cooked_fish.0", "minecraft:cooked_cod");
		hashMap.put("minecraft:cooked_fish.1", "minecraft:cooked_salmon");
		hashMap.put("minecraft:skull.0", "minecraft:skeleton_skull");
		hashMap.put("minecraft:skull.1", "minecraft:wither_skeleton_skull");
		hashMap.put("minecraft:skull.2", "minecraft:zombie_head");
		hashMap.put("minecraft:skull.3", "minecraft:player_head");
		hashMap.put("minecraft:skull.4", "minecraft:creeper_head");
		hashMap.put("minecraft:skull.5", "minecraft:dragon_head");
		hashMap.put("minecraft:golden_apple.0", "minecraft:golden_apple");
		hashMap.put("minecraft:golden_apple.1", "minecraft:enchanted_golden_apple");
		hashMap.put("minecraft:fireworks.0", "minecraft:firework_rocket");
		hashMap.put("minecraft:firework_charge.0", "minecraft:firework_star");
		hashMap.put("minecraft:dye.0", "minecraft:ink_sac");
		hashMap.put("minecraft:dye.1", "minecraft:rose_red");
		hashMap.put("minecraft:dye.2", "minecraft:cactus_green");
		hashMap.put("minecraft:dye.3", "minecraft:cocoa_beans");
		hashMap.put("minecraft:dye.4", "minecraft:lapis_lazuli");
		hashMap.put("minecraft:dye.5", "minecraft:purple_dye");
		hashMap.put("minecraft:dye.6", "minecraft:cyan_dye");
		hashMap.put("minecraft:dye.7", "minecraft:light_gray_dye");
		hashMap.put("minecraft:dye.8", "minecraft:gray_dye");
		hashMap.put("minecraft:dye.9", "minecraft:pink_dye");
		hashMap.put("minecraft:dye.10", "minecraft:lime_dye");
		hashMap.put("minecraft:dye.11", "minecraft:dandelion_yellow");
		hashMap.put("minecraft:dye.12", "minecraft:light_blue_dye");
		hashMap.put("minecraft:dye.13", "minecraft:magenta_dye");
		hashMap.put("minecraft:dye.14", "minecraft:orange_dye");
		hashMap.put("minecraft:dye.15", "minecraft:bone_meal");
		hashMap.put("minecraft:silver_shulker_box.0", "minecraft:light_gray_shulker_box");
		hashMap.put("minecraft:fence.0", "minecraft:oak_fence");
		hashMap.put("minecraft:fence_gate.0", "minecraft:oak_fence_gate");
		hashMap.put("minecraft:wooden_door.0", "minecraft:oak_door");
		hashMap.put("minecraft:boat.0", "minecraft:oak_boat");
		hashMap.put("minecraft:lit_pumpkin.0", "minecraft:jack_o_lantern");
		hashMap.put("minecraft:pumpkin.0", "minecraft:carved_pumpkin");
		hashMap.put("minecraft:trapdoor.0", "minecraft:oak_trapdoor");
		hashMap.put("minecraft:nether_brick.0", "minecraft:nether_bricks");
		hashMap.put("minecraft:red_nether_brick.0", "minecraft:red_nether_bricks");
		hashMap.put("minecraft:netherbrick.0", "minecraft:nether_brick");
		hashMap.put("minecraft:wooden_button.0", "minecraft:oak_button");
		hashMap.put("minecraft:wooden_pressure_plate.0", "minecraft:oak_pressure_plate");
		hashMap.put("minecraft:noteblock.0", "minecraft:note_block");
		hashMap.put("minecraft:bed.0", "minecraft:white_bed");
		hashMap.put("minecraft:bed.1", "minecraft:orange_bed");
		hashMap.put("minecraft:bed.2", "minecraft:magenta_bed");
		hashMap.put("minecraft:bed.3", "minecraft:light_blue_bed");
		hashMap.put("minecraft:bed.4", "minecraft:yellow_bed");
		hashMap.put("minecraft:bed.5", "minecraft:lime_bed");
		hashMap.put("minecraft:bed.6", "minecraft:pink_bed");
		hashMap.put("minecraft:bed.7", "minecraft:gray_bed");
		hashMap.put("minecraft:bed.8", "minecraft:light_gray_bed");
		hashMap.put("minecraft:bed.9", "minecraft:cyan_bed");
		hashMap.put("minecraft:bed.10", "minecraft:purple_bed");
		hashMap.put("minecraft:bed.11", "minecraft:blue_bed");
		hashMap.put("minecraft:bed.12", "minecraft:brown_bed");
		hashMap.put("minecraft:bed.13", "minecraft:green_bed");
		hashMap.put("minecraft:bed.14", "minecraft:red_bed");
		hashMap.put("minecraft:bed.15", "minecraft:black_bed");
		hashMap.put("minecraft:banner.15", "minecraft:white_banner");
		hashMap.put("minecraft:banner.14", "minecraft:orange_banner");
		hashMap.put("minecraft:banner.13", "minecraft:magenta_banner");
		hashMap.put("minecraft:banner.12", "minecraft:light_blue_banner");
		hashMap.put("minecraft:banner.11", "minecraft:yellow_banner");
		hashMap.put("minecraft:banner.10", "minecraft:lime_banner");
		hashMap.put("minecraft:banner.9", "minecraft:pink_banner");
		hashMap.put("minecraft:banner.8", "minecraft:gray_banner");
		hashMap.put("minecraft:banner.7", "minecraft:light_gray_banner");
		hashMap.put("minecraft:banner.6", "minecraft:cyan_banner");
		hashMap.put("minecraft:banner.5", "minecraft:purple_banner");
		hashMap.put("minecraft:banner.4", "minecraft:blue_banner");
		hashMap.put("minecraft:banner.3", "minecraft:brown_banner");
		hashMap.put("minecraft:banner.2", "minecraft:green_banner");
		hashMap.put("minecraft:banner.1", "minecraft:red_banner");
		hashMap.put("minecraft:banner.0", "minecraft:black_banner");
		hashMap.put("minecraft:grass.0", "minecraft:grass_block");
		hashMap.put("minecraft:brick_block.0", "minecraft:bricks");
		hashMap.put("minecraft:end_bricks.0", "minecraft:end_stone_bricks");
		hashMap.put("minecraft:golden_rail.0", "minecraft:powered_rail");
		hashMap.put("minecraft:magma.0", "minecraft:magma_block");
		hashMap.put("minecraft:quartz_ore.0", "minecraft:nether_quartz_ore");
		hashMap.put("minecraft:reeds.0", "minecraft:sugar_cane");
		hashMap.put("minecraft:slime.0", "minecraft:slime_block");
		hashMap.put("minecraft:stone_stairs.0", "minecraft:cobblestone_stairs");
		hashMap.put("minecraft:waterlily.0", "minecraft:lily_pad");
		hashMap.put("minecraft:web.0", "minecraft:cobweb");
		hashMap.put("minecraft:snow.0", "minecraft:snow_block");
		hashMap.put("minecraft:snow_layer.0", "minecraft:snow");
		hashMap.put("minecraft:record_11.0", "minecraft:music_disc_11");
		hashMap.put("minecraft:record_13.0", "minecraft:music_disc_13");
		hashMap.put("minecraft:record_blocks.0", "minecraft:music_disc_blocks");
		hashMap.put("minecraft:record_cat.0", "minecraft:music_disc_cat");
		hashMap.put("minecraft:record_chirp.0", "minecraft:music_disc_chirp");
		hashMap.put("minecraft:record_far.0", "minecraft:music_disc_far");
		hashMap.put("minecraft:record_mall.0", "minecraft:music_disc_mall");
		hashMap.put("minecraft:record_mellohi.0", "minecraft:music_disc_mellohi");
		hashMap.put("minecraft:record_stal.0", "minecraft:music_disc_stal");
		hashMap.put("minecraft:record_strad.0", "minecraft:music_disc_strad");
		hashMap.put("minecraft:record_wait.0", "minecraft:music_disc_wait");
		hashMap.put("minecraft:record_ward.0", "minecraft:music_disc_ward");
	});
	private static final Set<String> IDS = (Set<String>)MAP.keySet().stream().map(string -> string.substring(0, string.indexOf(46))).collect(Collectors.toSet());
	private static final Set<String> DAMAGE_IDS = Sets.<String>newHashSet(
		"minecraft:bow",
		"minecraft:carrot_on_a_stick",
		"minecraft:chainmail_boots",
		"minecraft:chainmail_chestplate",
		"minecraft:chainmail_helmet",
		"minecraft:chainmail_leggings",
		"minecraft:diamond_axe",
		"minecraft:diamond_boots",
		"minecraft:diamond_chestplate",
		"minecraft:diamond_helmet",
		"minecraft:diamond_hoe",
		"minecraft:diamond_leggings",
		"minecraft:diamond_pickaxe",
		"minecraft:diamond_shovel",
		"minecraft:diamond_sword",
		"minecraft:elytra",
		"minecraft:fishing_rod",
		"minecraft:flint_and_steel",
		"minecraft:golden_axe",
		"minecraft:golden_boots",
		"minecraft:golden_chestplate",
		"minecraft:golden_helmet",
		"minecraft:golden_hoe",
		"minecraft:golden_leggings",
		"minecraft:golden_pickaxe",
		"minecraft:golden_shovel",
		"minecraft:golden_sword",
		"minecraft:iron_axe",
		"minecraft:iron_boots",
		"minecraft:iron_chestplate",
		"minecraft:iron_helmet",
		"minecraft:iron_hoe",
		"minecraft:iron_leggings",
		"minecraft:iron_pickaxe",
		"minecraft:iron_shovel",
		"minecraft:iron_sword",
		"minecraft:leather_boots",
		"minecraft:leather_chestplate",
		"minecraft:leather_helmet",
		"minecraft:leather_leggings",
		"minecraft:shears",
		"minecraft:shield",
		"minecraft:stone_axe",
		"minecraft:stone_hoe",
		"minecraft:stone_pickaxe",
		"minecraft:stone_shovel",
		"minecraft:stone_sword",
		"minecraft:wooden_axe",
		"minecraft:wooden_hoe",
		"minecraft:wooden_pickaxe",
		"minecraft:wooden_shovel",
		"minecraft:wooden_sword"
	);

	public ItemStackTheFlatteningFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), DSL.namespacedString()));
		OpticFinder<?> opticFinder2 = type.findField("tag");
		return this.fixTypeEverywhereTyped("ItemInstanceTheFlatteningFix", type, typed -> {
			Optional<Pair<String, String>> optional = typed.getOptional(opticFinder);
			if (!optional.isPresent()) {
				return typed;
			} else {
				Typed<?> typed2 = typed;
				Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
				int i = dynamic.get("Damage").asInt(0);
				String string = updateItem((String)((Pair)optional.get()).getSecond(), i);
				if (string != null) {
					typed2 = typed.set(opticFinder, Pair.of(References.ITEM_NAME.typeName(), string));
				}

				if (DAMAGE_IDS.contains(((Pair)optional.get()).getSecond())) {
					Typed<?> typed3 = typed.getOrCreateTyped(opticFinder2);
					Dynamic<?> dynamic2 = typed3.get(DSL.remainderFinder());
					dynamic2 = dynamic2.set("Damage", dynamic2.createInt(i));
					typed2 = typed2.set(opticFinder2, typed3.set(DSL.remainderFinder(), dynamic2));
				}

				return typed2.set(DSL.remainderFinder(), dynamic.remove("Damage"));
			}
		});
	}

	@Nullable
	public static String updateItem(@Nullable String string, int i) {
		if (IDS.contains(string)) {
			String string2 = (String)MAP.get(string + '.' + i);
			return string2 == null ? (String)MAP.get(string + ".0") : string2;
		} else {
			return null;
		}
	}
}
