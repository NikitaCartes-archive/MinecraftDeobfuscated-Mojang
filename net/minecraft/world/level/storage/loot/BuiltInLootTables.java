/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public class BuiltInLootTables {
    private static final Set<ResourceLocation> LOCATIONS = Sets.newHashSet();
    private static final Set<ResourceLocation> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);
    public static final ResourceLocation EMPTY = new ResourceLocation("empty");
    public static final ResourceLocation SPAWN_BONUS_CHEST = BuiltInLootTables.register("chests/spawn_bonus_chest");
    public static final ResourceLocation END_CITY_TREASURE = BuiltInLootTables.register("chests/end_city_treasure");
    public static final ResourceLocation SIMPLE_DUNGEON = BuiltInLootTables.register("chests/simple_dungeon");
    public static final ResourceLocation VILLAGE_WEAPONSMITH = BuiltInLootTables.register("chests/village/village_weaponsmith");
    public static final ResourceLocation VILLAGE_TOOLSMITH = BuiltInLootTables.register("chests/village/village_toolsmith");
    public static final ResourceLocation VILLAGE_ARMORER = BuiltInLootTables.register("chests/village/village_armorer");
    public static final ResourceLocation VILLAGE_CARTOGRAPHER = BuiltInLootTables.register("chests/village/village_cartographer");
    public static final ResourceLocation VILLAGE_MASON = BuiltInLootTables.register("chests/village/village_mason");
    public static final ResourceLocation VILLAGE_SHEPHERD = BuiltInLootTables.register("chests/village/village_shepherd");
    public static final ResourceLocation VILLAGE_BUTCHER = BuiltInLootTables.register("chests/village/village_butcher");
    public static final ResourceLocation VILLAGE_FLETCHER = BuiltInLootTables.register("chests/village/village_fletcher");
    public static final ResourceLocation VILLAGE_FISHER = BuiltInLootTables.register("chests/village/village_fisher");
    public static final ResourceLocation VILLAGE_TANNERY = BuiltInLootTables.register("chests/village/village_tannery");
    public static final ResourceLocation VILLAGE_TEMPLE = BuiltInLootTables.register("chests/village/village_temple");
    public static final ResourceLocation VILLAGE_DESERT_HOUSE = BuiltInLootTables.register("chests/village/village_desert_house");
    public static final ResourceLocation VILLAGE_PLAINS_HOUSE = BuiltInLootTables.register("chests/village/village_plains_house");
    public static final ResourceLocation VILLAGE_TAIGA_HOUSE = BuiltInLootTables.register("chests/village/village_taiga_house");
    public static final ResourceLocation VILLAGE_SNOWY_HOUSE = BuiltInLootTables.register("chests/village/village_snowy_house");
    public static final ResourceLocation VILLAGE_SAVANNA_HOUSE = BuiltInLootTables.register("chests/village/village_savanna_house");
    public static final ResourceLocation ABANDONED_MINESHAFT = BuiltInLootTables.register("chests/abandoned_mineshaft");
    public static final ResourceLocation NETHER_BRIDGE = BuiltInLootTables.register("chests/nether_bridge");
    public static final ResourceLocation STRONGHOLD_LIBRARY = BuiltInLootTables.register("chests/stronghold_library");
    public static final ResourceLocation STRONGHOLD_CROSSING = BuiltInLootTables.register("chests/stronghold_crossing");
    public static final ResourceLocation STRONGHOLD_CORRIDOR = BuiltInLootTables.register("chests/stronghold_corridor");
    public static final ResourceLocation DESERT_PYRAMID = BuiltInLootTables.register("chests/desert_pyramid");
    public static final ResourceLocation JUNGLE_TEMPLE = BuiltInLootTables.register("chests/jungle_temple");
    public static final ResourceLocation JUNGLE_TEMPLE_DISPENSER = BuiltInLootTables.register("chests/jungle_temple_dispenser");
    public static final ResourceLocation IGLOO_CHEST = BuiltInLootTables.register("chests/igloo_chest");
    public static final ResourceLocation WOODLAND_MANSION = BuiltInLootTables.register("chests/woodland_mansion");
    public static final ResourceLocation UNDERWATER_RUIN_SMALL = BuiltInLootTables.register("chests/underwater_ruin_small");
    public static final ResourceLocation UNDERWATER_RUIN_BIG = BuiltInLootTables.register("chests/underwater_ruin_big");
    public static final ResourceLocation BURIED_TREASURE = BuiltInLootTables.register("chests/buried_treasure");
    public static final ResourceLocation SHIPWRECK_MAP = BuiltInLootTables.register("chests/shipwreck_map");
    public static final ResourceLocation SHIPWRECK_SUPPLY = BuiltInLootTables.register("chests/shipwreck_supply");
    public static final ResourceLocation SHIPWRECK_TREASURE = BuiltInLootTables.register("chests/shipwreck_treasure");
    public static final ResourceLocation PILLAGER_OUTPOST = BuiltInLootTables.register("chests/pillager_outpost");
    public static final ResourceLocation SHEEP_WHITE = BuiltInLootTables.register("entities/sheep/white");
    public static final ResourceLocation SHEEP_ORANGE = BuiltInLootTables.register("entities/sheep/orange");
    public static final ResourceLocation SHEEP_MAGENTA = BuiltInLootTables.register("entities/sheep/magenta");
    public static final ResourceLocation SHEEP_LIGHT_BLUE = BuiltInLootTables.register("entities/sheep/light_blue");
    public static final ResourceLocation SHEEP_YELLOW = BuiltInLootTables.register("entities/sheep/yellow");
    public static final ResourceLocation SHEEP_LIME = BuiltInLootTables.register("entities/sheep/lime");
    public static final ResourceLocation SHEEP_PINK = BuiltInLootTables.register("entities/sheep/pink");
    public static final ResourceLocation SHEEP_GRAY = BuiltInLootTables.register("entities/sheep/gray");
    public static final ResourceLocation SHEEP_LIGHT_GRAY = BuiltInLootTables.register("entities/sheep/light_gray");
    public static final ResourceLocation SHEEP_CYAN = BuiltInLootTables.register("entities/sheep/cyan");
    public static final ResourceLocation SHEEP_PURPLE = BuiltInLootTables.register("entities/sheep/purple");
    public static final ResourceLocation SHEEP_BLUE = BuiltInLootTables.register("entities/sheep/blue");
    public static final ResourceLocation SHEEP_BROWN = BuiltInLootTables.register("entities/sheep/brown");
    public static final ResourceLocation SHEEP_GREEN = BuiltInLootTables.register("entities/sheep/green");
    public static final ResourceLocation SHEEP_RED = BuiltInLootTables.register("entities/sheep/red");
    public static final ResourceLocation SHEEP_BLACK = BuiltInLootTables.register("entities/sheep/black");
    public static final ResourceLocation FISHING = BuiltInLootTables.register("gameplay/fishing");
    public static final ResourceLocation FISHING_JUNK = BuiltInLootTables.register("gameplay/fishing/junk");
    public static final ResourceLocation FISHING_TREASURE = BuiltInLootTables.register("gameplay/fishing/treasure");
    public static final ResourceLocation FISHING_FISH = BuiltInLootTables.register("gameplay/fishing/fish");
    public static final ResourceLocation CAT_MORNING_GIFT = BuiltInLootTables.register("gameplay/cat_morning_gift");
    public static final ResourceLocation ARMORER_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/armorer_gift");
    public static final ResourceLocation BUTCHER_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/butcher_gift");
    public static final ResourceLocation CARTOGRAPHER_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/cartographer_gift");
    public static final ResourceLocation CLERIC_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/cleric_gift");
    public static final ResourceLocation FARMER_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/farmer_gift");
    public static final ResourceLocation FISHERMAN_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/fisherman_gift");
    public static final ResourceLocation FLETCHER_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/fletcher_gift");
    public static final ResourceLocation LEATHERWORKER_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/leatherworker_gift");
    public static final ResourceLocation LIBRARIAN_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/librarian_gift");
    public static final ResourceLocation MASON_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/mason_gift");
    public static final ResourceLocation SHEPHERD_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/shepherd_gift");
    public static final ResourceLocation TOOLSMITH_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/toolsmith_gift");
    public static final ResourceLocation WEAPONSMITH_GIFT = BuiltInLootTables.register("gameplay/hero_of_the_village/weaponsmith_gift");

    private static ResourceLocation register(String string) {
        return BuiltInLootTables.register(new ResourceLocation(string));
    }

    private static ResourceLocation register(ResourceLocation resourceLocation) {
        if (LOCATIONS.add(resourceLocation)) {
            return resourceLocation;
        }
        throw new IllegalArgumentException(resourceLocation + " is already a registered built-in loot table");
    }

    public static Set<ResourceLocation> all() {
        return IMMUTABLE_LOCATIONS;
    }
}

