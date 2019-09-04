/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.stats;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Stats {
    public static final StatType<Block> BLOCK_MINED = Stats.makeRegistryStatType("mined", Registry.BLOCK);
    public static final StatType<Item> ITEM_CRAFTED = Stats.makeRegistryStatType("crafted", Registry.ITEM);
    public static final StatType<Item> ITEM_USED = Stats.makeRegistryStatType("used", Registry.ITEM);
    public static final StatType<Item> ITEM_BROKEN = Stats.makeRegistryStatType("broken", Registry.ITEM);
    public static final StatType<Item> ITEM_PICKED_UP = Stats.makeRegistryStatType("picked_up", Registry.ITEM);
    public static final StatType<Item> ITEM_DROPPED = Stats.makeRegistryStatType("dropped", Registry.ITEM);
    public static final StatType<EntityType<?>> ENTITY_KILLED = Stats.makeRegistryStatType("killed", Registry.ENTITY_TYPE);
    public static final StatType<EntityType<?>> ENTITY_KILLED_BY = Stats.makeRegistryStatType("killed_by", Registry.ENTITY_TYPE);
    public static final StatType<ResourceLocation> CUSTOM = Stats.makeRegistryStatType("custom", Registry.CUSTOM_STAT);
    public static final ResourceLocation LEAVE_GAME = Stats.makeCustomStat("leave_game", StatFormatter.DEFAULT);
    public static final ResourceLocation PLAY_ONE_MINUTE = Stats.makeCustomStat("play_one_minute", StatFormatter.TIME);
    public static final ResourceLocation TIME_SINCE_DEATH = Stats.makeCustomStat("time_since_death", StatFormatter.TIME);
    public static final ResourceLocation TIME_SINCE_REST = Stats.makeCustomStat("time_since_rest", StatFormatter.TIME);
    public static final ResourceLocation CROUCH_TIME = Stats.makeCustomStat("sneak_time", StatFormatter.TIME);
    public static final ResourceLocation WALK_ONE_CM = Stats.makeCustomStat("walk_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation CROUCH_ONE_CM = Stats.makeCustomStat("crouch_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation SPRINT_ONE_CM = Stats.makeCustomStat("sprint_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation WALK_ON_WATER_ONE_CM = Stats.makeCustomStat("walk_on_water_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation FALL_ONE_CM = Stats.makeCustomStat("fall_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation CLIMB_ONE_CM = Stats.makeCustomStat("climb_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation FLY_ONE_CM = Stats.makeCustomStat("fly_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation WALK_UNDER_WATER_ONE_CM = Stats.makeCustomStat("walk_under_water_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation MINECART_ONE_CM = Stats.makeCustomStat("minecart_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation BOAT_ONE_CM = Stats.makeCustomStat("boat_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation PIG_ONE_CM = Stats.makeCustomStat("pig_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation HORSE_ONE_CM = Stats.makeCustomStat("horse_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation AVIATE_ONE_CM = Stats.makeCustomStat("aviate_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation SWIM_ONE_CM = Stats.makeCustomStat("swim_one_cm", StatFormatter.DISTANCE);
    public static final ResourceLocation JUMP = Stats.makeCustomStat("jump", StatFormatter.DEFAULT);
    public static final ResourceLocation DROP = Stats.makeCustomStat("drop", StatFormatter.DEFAULT);
    public static final ResourceLocation DAMAGE_DEALT = Stats.makeCustomStat("damage_dealt", StatFormatter.DIVIDE_BY_TEN);
    public static final ResourceLocation DAMAGE_DEALT_ABSORBED = Stats.makeCustomStat("damage_dealt_absorbed", StatFormatter.DIVIDE_BY_TEN);
    public static final ResourceLocation DAMAGE_DEALT_RESISTED = Stats.makeCustomStat("damage_dealt_resisted", StatFormatter.DIVIDE_BY_TEN);
    public static final ResourceLocation DAMAGE_TAKEN = Stats.makeCustomStat("damage_taken", StatFormatter.DIVIDE_BY_TEN);
    public static final ResourceLocation DAMAGE_BLOCKED_BY_SHIELD = Stats.makeCustomStat("damage_blocked_by_shield", StatFormatter.DIVIDE_BY_TEN);
    public static final ResourceLocation DAMAGE_ABSORBED = Stats.makeCustomStat("damage_absorbed", StatFormatter.DIVIDE_BY_TEN);
    public static final ResourceLocation DAMAGE_RESISTED = Stats.makeCustomStat("damage_resisted", StatFormatter.DIVIDE_BY_TEN);
    public static final ResourceLocation DEATHS = Stats.makeCustomStat("deaths", StatFormatter.DEFAULT);
    public static final ResourceLocation MOB_KILLS = Stats.makeCustomStat("mob_kills", StatFormatter.DEFAULT);
    public static final ResourceLocation ANIMALS_BRED = Stats.makeCustomStat("animals_bred", StatFormatter.DEFAULT);
    public static final ResourceLocation PLAYER_KILLS = Stats.makeCustomStat("player_kills", StatFormatter.DEFAULT);
    public static final ResourceLocation FISH_CAUGHT = Stats.makeCustomStat("fish_caught", StatFormatter.DEFAULT);
    public static final ResourceLocation TALKED_TO_VILLAGER = Stats.makeCustomStat("talked_to_villager", StatFormatter.DEFAULT);
    public static final ResourceLocation TRADED_WITH_VILLAGER = Stats.makeCustomStat("traded_with_villager", StatFormatter.DEFAULT);
    public static final ResourceLocation EAT_CAKE_SLICE = Stats.makeCustomStat("eat_cake_slice", StatFormatter.DEFAULT);
    public static final ResourceLocation FILL_CAULDRON = Stats.makeCustomStat("fill_cauldron", StatFormatter.DEFAULT);
    public static final ResourceLocation USE_CAULDRON = Stats.makeCustomStat("use_cauldron", StatFormatter.DEFAULT);
    public static final ResourceLocation CLEAN_ARMOR = Stats.makeCustomStat("clean_armor", StatFormatter.DEFAULT);
    public static final ResourceLocation CLEAN_BANNER = Stats.makeCustomStat("clean_banner", StatFormatter.DEFAULT);
    public static final ResourceLocation CLEAN_SHULKER_BOX = Stats.makeCustomStat("clean_shulker_box", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_BREWINGSTAND = Stats.makeCustomStat("interact_with_brewingstand", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_BEACON = Stats.makeCustomStat("interact_with_beacon", StatFormatter.DEFAULT);
    public static final ResourceLocation INSPECT_DROPPER = Stats.makeCustomStat("inspect_dropper", StatFormatter.DEFAULT);
    public static final ResourceLocation INSPECT_HOPPER = Stats.makeCustomStat("inspect_hopper", StatFormatter.DEFAULT);
    public static final ResourceLocation INSPECT_DISPENSER = Stats.makeCustomStat("inspect_dispenser", StatFormatter.DEFAULT);
    public static final ResourceLocation PLAY_NOTEBLOCK = Stats.makeCustomStat("play_noteblock", StatFormatter.DEFAULT);
    public static final ResourceLocation TUNE_NOTEBLOCK = Stats.makeCustomStat("tune_noteblock", StatFormatter.DEFAULT);
    public static final ResourceLocation POT_FLOWER = Stats.makeCustomStat("pot_flower", StatFormatter.DEFAULT);
    public static final ResourceLocation TRIGGER_TRAPPED_CHEST = Stats.makeCustomStat("trigger_trapped_chest", StatFormatter.DEFAULT);
    public static final ResourceLocation OPEN_ENDERCHEST = Stats.makeCustomStat("open_enderchest", StatFormatter.DEFAULT);
    public static final ResourceLocation ENCHANT_ITEM = Stats.makeCustomStat("enchant_item", StatFormatter.DEFAULT);
    public static final ResourceLocation PLAY_RECORD = Stats.makeCustomStat("play_record", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_FURNACE = Stats.makeCustomStat("interact_with_furnace", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_CRAFTING_TABLE = Stats.makeCustomStat("interact_with_crafting_table", StatFormatter.DEFAULT);
    public static final ResourceLocation OPEN_CHEST = Stats.makeCustomStat("open_chest", StatFormatter.DEFAULT);
    public static final ResourceLocation SLEEP_IN_BED = Stats.makeCustomStat("sleep_in_bed", StatFormatter.DEFAULT);
    public static final ResourceLocation OPEN_SHULKER_BOX = Stats.makeCustomStat("open_shulker_box", StatFormatter.DEFAULT);
    public static final ResourceLocation OPEN_BARREL = Stats.makeCustomStat("open_barrel", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_BLAST_FURNACE = Stats.makeCustomStat("interact_with_blast_furnace", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_SMOKER = Stats.makeCustomStat("interact_with_smoker", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_LECTERN = Stats.makeCustomStat("interact_with_lectern", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_CAMPFIRE = Stats.makeCustomStat("interact_with_campfire", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_CARTOGRAPHY_TABLE = Stats.makeCustomStat("interact_with_cartography_table", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_LOOM = Stats.makeCustomStat("interact_with_loom", StatFormatter.DEFAULT);
    public static final ResourceLocation INTERACT_WITH_STONECUTTER = Stats.makeCustomStat("interact_with_stonecutter", StatFormatter.DEFAULT);
    public static final ResourceLocation BELL_RING = Stats.makeCustomStat("bell_ring", StatFormatter.DEFAULT);
    public static final ResourceLocation RAID_TRIGGER = Stats.makeCustomStat("raid_trigger", StatFormatter.DEFAULT);
    public static final ResourceLocation RAID_WIN = Stats.makeCustomStat("raid_win", StatFormatter.DEFAULT);

    private static ResourceLocation makeCustomStat(String string, StatFormatter statFormatter) {
        ResourceLocation resourceLocation = new ResourceLocation(string);
        Registry.register(Registry.CUSTOM_STAT, string, resourceLocation);
        CUSTOM.get(resourceLocation, statFormatter);
        return resourceLocation;
    }

    private static <T> StatType<T> makeRegistryStatType(String string, Registry<T> registry) {
        return Registry.register(Registry.STAT_TYPE, string, new StatType<T>(registry));
    }
}

