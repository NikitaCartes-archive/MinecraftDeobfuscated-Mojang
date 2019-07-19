/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.BitStorage;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.References;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ChunkPalettedStorageFix
extends DataFix {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BitSet VIRTUAL = new BitSet(256);
    private static final BitSet FIX = new BitSet(256);
    private static final Dynamic<?> PUMPKIN = BlockStateData.parse("{Name:'minecraft:pumpkin'}");
    private static final Dynamic<?> SNOWY_PODZOL = BlockStateData.parse("{Name:'minecraft:podzol',Properties:{snowy:'true'}}");
    private static final Dynamic<?> SNOWY_GRASS = BlockStateData.parse("{Name:'minecraft:grass_block',Properties:{snowy:'true'}}");
    private static final Dynamic<?> SNOWY_MYCELIUM = BlockStateData.parse("{Name:'minecraft:mycelium',Properties:{snowy:'true'}}");
    private static final Dynamic<?> UPPER_SUNFLOWER = BlockStateData.parse("{Name:'minecraft:sunflower',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_LILAC = BlockStateData.parse("{Name:'minecraft:lilac',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_TALL_GRASS = BlockStateData.parse("{Name:'minecraft:tall_grass',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_LARGE_FERN = BlockStateData.parse("{Name:'minecraft:large_fern',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_ROSE_BUSH = BlockStateData.parse("{Name:'minecraft:rose_bush',Properties:{half:'upper'}}");
    private static final Dynamic<?> UPPER_PEONY = BlockStateData.parse("{Name:'minecraft:peony',Properties:{half:'upper'}}");
    private static final Map<String, Dynamic<?>> FLOWER_POT_MAP = DataFixUtils.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("minecraft:air0", BlockStateData.parse("{Name:'minecraft:flower_pot'}"));
        hashMap.put("minecraft:red_flower0", BlockStateData.parse("{Name:'minecraft:potted_poppy'}"));
        hashMap.put("minecraft:red_flower1", BlockStateData.parse("{Name:'minecraft:potted_blue_orchid'}"));
        hashMap.put("minecraft:red_flower2", BlockStateData.parse("{Name:'minecraft:potted_allium'}"));
        hashMap.put("minecraft:red_flower3", BlockStateData.parse("{Name:'minecraft:potted_azure_bluet'}"));
        hashMap.put("minecraft:red_flower4", BlockStateData.parse("{Name:'minecraft:potted_red_tulip'}"));
        hashMap.put("minecraft:red_flower5", BlockStateData.parse("{Name:'minecraft:potted_orange_tulip'}"));
        hashMap.put("minecraft:red_flower6", BlockStateData.parse("{Name:'minecraft:potted_white_tulip'}"));
        hashMap.put("minecraft:red_flower7", BlockStateData.parse("{Name:'minecraft:potted_pink_tulip'}"));
        hashMap.put("minecraft:red_flower8", BlockStateData.parse("{Name:'minecraft:potted_oxeye_daisy'}"));
        hashMap.put("minecraft:yellow_flower0", BlockStateData.parse("{Name:'minecraft:potted_dandelion'}"));
        hashMap.put("minecraft:sapling0", BlockStateData.parse("{Name:'minecraft:potted_oak_sapling'}"));
        hashMap.put("minecraft:sapling1", BlockStateData.parse("{Name:'minecraft:potted_spruce_sapling'}"));
        hashMap.put("minecraft:sapling2", BlockStateData.parse("{Name:'minecraft:potted_birch_sapling'}"));
        hashMap.put("minecraft:sapling3", BlockStateData.parse("{Name:'minecraft:potted_jungle_sapling'}"));
        hashMap.put("minecraft:sapling4", BlockStateData.parse("{Name:'minecraft:potted_acacia_sapling'}"));
        hashMap.put("minecraft:sapling5", BlockStateData.parse("{Name:'minecraft:potted_dark_oak_sapling'}"));
        hashMap.put("minecraft:red_mushroom0", BlockStateData.parse("{Name:'minecraft:potted_red_mushroom'}"));
        hashMap.put("minecraft:brown_mushroom0", BlockStateData.parse("{Name:'minecraft:potted_brown_mushroom'}"));
        hashMap.put("minecraft:deadbush0", BlockStateData.parse("{Name:'minecraft:potted_dead_bush'}"));
        hashMap.put("minecraft:tallgrass2", BlockStateData.parse("{Name:'minecraft:potted_fern'}"));
        hashMap.put("minecraft:cactus0", BlockStateData.getTag(2240));
    });
    private static final Map<String, Dynamic<?>> SKULL_MAP = DataFixUtils.make(Maps.newHashMap(), hashMap -> {
        ChunkPalettedStorageFix.mapSkull(hashMap, 0, "skeleton", "skull");
        ChunkPalettedStorageFix.mapSkull(hashMap, 1, "wither_skeleton", "skull");
        ChunkPalettedStorageFix.mapSkull(hashMap, 2, "zombie", "head");
        ChunkPalettedStorageFix.mapSkull(hashMap, 3, "player", "head");
        ChunkPalettedStorageFix.mapSkull(hashMap, 4, "creeper", "head");
        ChunkPalettedStorageFix.mapSkull(hashMap, 5, "dragon", "head");
    });
    private static final Map<String, Dynamic<?>> DOOR_MAP = DataFixUtils.make(Maps.newHashMap(), hashMap -> {
        ChunkPalettedStorageFix.mapDoor(hashMap, "oak_door", 1024);
        ChunkPalettedStorageFix.mapDoor(hashMap, "iron_door", 1136);
        ChunkPalettedStorageFix.mapDoor(hashMap, "spruce_door", 3088);
        ChunkPalettedStorageFix.mapDoor(hashMap, "birch_door", 3104);
        ChunkPalettedStorageFix.mapDoor(hashMap, "jungle_door", 3120);
        ChunkPalettedStorageFix.mapDoor(hashMap, "acacia_door", 3136);
        ChunkPalettedStorageFix.mapDoor(hashMap, "dark_oak_door", 3152);
    });
    private static final Map<String, Dynamic<?>> NOTE_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), hashMap -> {
        for (int i = 0; i < 26; ++i) {
            hashMap.put("true" + i, BlockStateData.parse("{Name:'minecraft:note_block',Properties:{powered:'true',note:'" + i + "'}}"));
            hashMap.put("false" + i, BlockStateData.parse("{Name:'minecraft:note_block',Properties:{powered:'false',note:'" + i + "'}}"));
        }
    });
    private static final Int2ObjectMap<String> DYE_COLOR_MAP = DataFixUtils.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
        int2ObjectOpenHashMap.put(0, "white");
        int2ObjectOpenHashMap.put(1, "orange");
        int2ObjectOpenHashMap.put(2, "magenta");
        int2ObjectOpenHashMap.put(3, "light_blue");
        int2ObjectOpenHashMap.put(4, "yellow");
        int2ObjectOpenHashMap.put(5, "lime");
        int2ObjectOpenHashMap.put(6, "pink");
        int2ObjectOpenHashMap.put(7, "gray");
        int2ObjectOpenHashMap.put(8, "light_gray");
        int2ObjectOpenHashMap.put(9, "cyan");
        int2ObjectOpenHashMap.put(10, "purple");
        int2ObjectOpenHashMap.put(11, "blue");
        int2ObjectOpenHashMap.put(12, "brown");
        int2ObjectOpenHashMap.put(13, "green");
        int2ObjectOpenHashMap.put(14, "red");
        int2ObjectOpenHashMap.put(15, "black");
    });
    private static final Map<String, Dynamic<?>> BED_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), hashMap -> {
        for (Int2ObjectMap.Entry entry : DYE_COLOR_MAP.int2ObjectEntrySet()) {
            if (Objects.equals(entry.getValue(), "red")) continue;
            ChunkPalettedStorageFix.addBeds(hashMap, entry.getIntKey(), (String)entry.getValue());
        }
    });
    private static final Map<String, Dynamic<?>> BANNER_BLOCK_MAP = DataFixUtils.make(Maps.newHashMap(), hashMap -> {
        for (Int2ObjectMap.Entry entry : DYE_COLOR_MAP.int2ObjectEntrySet()) {
            if (Objects.equals(entry.getValue(), "white")) continue;
            ChunkPalettedStorageFix.addBanners(hashMap, 15 - entry.getIntKey(), (String)entry.getValue());
        }
    });
    private static final Dynamic<?> AIR;

    public ChunkPalettedStorageFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    private static void mapSkull(Map<String, Dynamic<?>> map, int i, String string, String string2) {
        map.put(i + "north", BlockStateData.parse("{Name:'minecraft:" + string + "_wall_" + string2 + "',Properties:{facing:'north'}}"));
        map.put(i + "east", BlockStateData.parse("{Name:'minecraft:" + string + "_wall_" + string2 + "',Properties:{facing:'east'}}"));
        map.put(i + "south", BlockStateData.parse("{Name:'minecraft:" + string + "_wall_" + string2 + "',Properties:{facing:'south'}}"));
        map.put(i + "west", BlockStateData.parse("{Name:'minecraft:" + string + "_wall_" + string2 + "',Properties:{facing:'west'}}"));
        for (int j = 0; j < 16; ++j) {
            map.put(i + "" + j, BlockStateData.parse("{Name:'minecraft:" + string + "_" + string2 + "',Properties:{rotation:'" + j + "'}}"));
        }
    }

    private static void mapDoor(Map<String, Dynamic<?>> map, String string, int i) {
        map.put("minecraft:" + string + "eastlowerleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "eastlowerleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "eastlowerlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "eastlowerlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "eastlowerrightfalsefalse", BlockStateData.getTag(i));
        map.put("minecraft:" + string + "eastlowerrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "eastlowerrighttruefalse", BlockStateData.getTag(i + 4));
        map.put("minecraft:" + string + "eastlowerrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "eastupperleftfalsefalse", BlockStateData.getTag(i + 8));
        map.put("minecraft:" + string + "eastupperleftfalsetrue", BlockStateData.getTag(i + 10));
        map.put("minecraft:" + string + "eastupperlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "eastupperlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "eastupperrightfalsefalse", BlockStateData.getTag(i + 9));
        map.put("minecraft:" + string + "eastupperrightfalsetrue", BlockStateData.getTag(i + 11));
        map.put("minecraft:" + string + "eastupperrighttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "eastupperrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "northlowerleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "northlowerleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "northlowerlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "northlowerlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "northlowerrightfalsefalse", BlockStateData.getTag(i + 3));
        map.put("minecraft:" + string + "northlowerrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "northlowerrighttruefalse", BlockStateData.getTag(i + 7));
        map.put("minecraft:" + string + "northlowerrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "northupperleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "northupperleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "northupperlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "northupperlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "northupperrightfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "northupperrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "northupperrighttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "northupperrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "southlowerleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "southlowerleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "southlowerlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "southlowerlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "southlowerrightfalsefalse", BlockStateData.getTag(i + 1));
        map.put("minecraft:" + string + "southlowerrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "southlowerrighttruefalse", BlockStateData.getTag(i + 5));
        map.put("minecraft:" + string + "southlowerrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "southupperleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "southupperleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "southupperlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "southupperlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "southupperrightfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "southupperrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "southupperrighttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "southupperrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "westlowerleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "westlowerleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "westlowerlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "westlowerlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "westlowerrightfalsefalse", BlockStateData.getTag(i + 2));
        map.put("minecraft:" + string + "westlowerrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "westlowerrighttruefalse", BlockStateData.getTag(i + 6));
        map.put("minecraft:" + string + "westlowerrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "westupperleftfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "westupperleftfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "westupperlefttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "westupperlefttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'true'}}"));
        map.put("minecraft:" + string + "westupperrightfalsefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'false'}}"));
        map.put("minecraft:" + string + "westupperrightfalsetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'true'}}"));
        map.put("minecraft:" + string + "westupperrighttruefalse", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'false'}}"));
        map.put("minecraft:" + string + "westupperrighttruetrue", BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'true'}}"));
    }

    private static void addBeds(Map<String, Dynamic<?>> map, int i, String string) {
        map.put("southfalsefoot" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'south',occupied:'false',part:'foot'}}"));
        map.put("westfalsefoot" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'west',occupied:'false',part:'foot'}}"));
        map.put("northfalsefoot" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'north',occupied:'false',part:'foot'}}"));
        map.put("eastfalsefoot" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'east',occupied:'false',part:'foot'}}"));
        map.put("southfalsehead" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'south',occupied:'false',part:'head'}}"));
        map.put("westfalsehead" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'west',occupied:'false',part:'head'}}"));
        map.put("northfalsehead" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'north',occupied:'false',part:'head'}}"));
        map.put("eastfalsehead" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'east',occupied:'false',part:'head'}}"));
        map.put("southtruehead" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'south',occupied:'true',part:'head'}}"));
        map.put("westtruehead" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'west',occupied:'true',part:'head'}}"));
        map.put("northtruehead" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'north',occupied:'true',part:'head'}}"));
        map.put("easttruehead" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_bed',Properties:{facing:'east',occupied:'true',part:'head'}}"));
    }

    private static void addBanners(Map<String, Dynamic<?>> map, int i, String string) {
        for (int j = 0; j < 16; ++j) {
            map.put("" + j + "_" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_banner',Properties:{rotation:'" + j + "'}}"));
        }
        map.put("north_" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_wall_banner',Properties:{facing:'north'}}"));
        map.put("south_" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_wall_banner',Properties:{facing:'south'}}"));
        map.put("west_" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_wall_banner',Properties:{facing:'west'}}"));
        map.put("east_" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_wall_banner',Properties:{facing:'east'}}"));
    }

    public static String getName(Dynamic<?> dynamic) {
        return dynamic.get("Name").asString("");
    }

    public static String getProperty(Dynamic<?> dynamic, String string) {
        return dynamic.get("Properties").get(string).asString("");
    }

    public static int idFor(CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> crudeIncrementalIntIdentityHashBiMap, Dynamic<?> dynamic) {
        int i = crudeIncrementalIntIdentityHashBiMap.getId(dynamic);
        if (i == -1) {
            i = crudeIncrementalIntIdentityHashBiMap.add(dynamic);
        }
        return i;
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        Optional<Dynamic<?>> optional = dynamic.get("Level").get();
        if (optional.isPresent() && optional.get().get("Sections").asStreamOpt().isPresent()) {
            return dynamic.set("Level", new UpgradeChunk(optional.get()).write());
        }
        return dynamic;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        Type<?> type2 = this.getOutputSchema().getType(References.CHUNK);
        return this.writeFixAndRead("ChunkPalettedStorageFix", type, type2, this::fix);
    }

    public static int getSideMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        int i = 0;
        if (bl3) {
            i = bl2 ? (i |= 2) : (bl ? (i |= 0x80) : (i |= 1));
        } else if (bl4) {
            i = bl ? (i |= 0x20) : (bl2 ? (i |= 8) : (i |= 0x10));
        } else if (bl2) {
            i |= 4;
        } else if (bl) {
            i |= 0x40;
        }
        return i;
    }

    static {
        FIX.set(2);
        FIX.set(3);
        FIX.set(110);
        FIX.set(140);
        FIX.set(144);
        FIX.set(25);
        FIX.set(86);
        FIX.set(26);
        FIX.set(176);
        FIX.set(177);
        FIX.set(175);
        FIX.set(64);
        FIX.set(71);
        FIX.set(193);
        FIX.set(194);
        FIX.set(195);
        FIX.set(196);
        FIX.set(197);
        VIRTUAL.set(54);
        VIRTUAL.set(146);
        VIRTUAL.set(25);
        VIRTUAL.set(26);
        VIRTUAL.set(51);
        VIRTUAL.set(53);
        VIRTUAL.set(67);
        VIRTUAL.set(108);
        VIRTUAL.set(109);
        VIRTUAL.set(114);
        VIRTUAL.set(128);
        VIRTUAL.set(134);
        VIRTUAL.set(135);
        VIRTUAL.set(136);
        VIRTUAL.set(156);
        VIRTUAL.set(163);
        VIRTUAL.set(164);
        VIRTUAL.set(180);
        VIRTUAL.set(203);
        VIRTUAL.set(55);
        VIRTUAL.set(85);
        VIRTUAL.set(113);
        VIRTUAL.set(188);
        VIRTUAL.set(189);
        VIRTUAL.set(190);
        VIRTUAL.set(191);
        VIRTUAL.set(192);
        VIRTUAL.set(93);
        VIRTUAL.set(94);
        VIRTUAL.set(101);
        VIRTUAL.set(102);
        VIRTUAL.set(160);
        VIRTUAL.set(106);
        VIRTUAL.set(107);
        VIRTUAL.set(183);
        VIRTUAL.set(184);
        VIRTUAL.set(185);
        VIRTUAL.set(186);
        VIRTUAL.set(187);
        VIRTUAL.set(132);
        VIRTUAL.set(139);
        VIRTUAL.set(199);
        AIR = BlockStateData.getTag(0);
    }

    public static enum Direction {
        DOWN(AxisDirection.NEGATIVE, Axis.Y),
        UP(AxisDirection.POSITIVE, Axis.Y),
        NORTH(AxisDirection.NEGATIVE, Axis.Z),
        SOUTH(AxisDirection.POSITIVE, Axis.Z),
        WEST(AxisDirection.NEGATIVE, Axis.X),
        EAST(AxisDirection.POSITIVE, Axis.X);

        private final Axis axis;
        private final AxisDirection axisDirection;

        private Direction(AxisDirection axisDirection, Axis axis) {
            this.axis = axis;
            this.axisDirection = axisDirection;
        }

        public AxisDirection getAxisDirection() {
            return this.axisDirection;
        }

        public Axis getAxis() {
            return this.axis;
        }

        public static enum AxisDirection {
            POSITIVE(1),
            NEGATIVE(-1);

            private final int step;

            private AxisDirection(int j) {
                this.step = j;
            }

            public int getStep() {
                return this.step;
            }
        }

        public static enum Axis {
            X,
            Y,
            Z;

        }
    }

    static class DataLayer {
        private final byte[] data;

        public DataLayer() {
            this.data = new byte[2048];
        }

        public DataLayer(byte[] bs) {
            this.data = bs;
            if (bs.length != 2048) {
                throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + bs.length);
            }
        }

        public int get(int i, int j, int k) {
            int l = this.getPosition(j << 8 | k << 4 | i);
            if (this.isFirst(j << 8 | k << 4 | i)) {
                return this.data[l] & 0xF;
            }
            return this.data[l] >> 4 & 0xF;
        }

        private boolean isFirst(int i) {
            return (i & 1) == 0;
        }

        private int getPosition(int i) {
            return i >> 1;
        }
    }

    static final class UpgradeChunk {
        private int sides;
        private final Section[] sections = new Section[16];
        private final Dynamic<?> level;
        private final int x;
        private final int z;
        private final Int2ObjectMap<Dynamic<?>> blockEntities = new Int2ObjectLinkedOpenHashMap(16);

        public UpgradeChunk(Dynamic<?> dynamic) {
            this.level = dynamic;
            this.x = dynamic.get("xPos").asInt(0) << 4;
            this.z = dynamic.get("zPos").asInt(0) << 4;
            dynamic.get("TileEntities").asStreamOpt().ifPresent(stream -> stream.forEach(dynamic -> {
                int k;
                int i = dynamic.get("x").asInt(0) - this.x & 0xF;
                int j = dynamic.get("y").asInt(0);
                int l = j << 8 | (k = dynamic.get("z").asInt(0) - this.z & 0xF) << 4 | i;
                if (this.blockEntities.put(l, (Dynamic<?>)dynamic) != null) {
                    LOGGER.warn("In chunk: {}x{} found a duplicate block entity at position: [{}, {}, {}]", (Object)this.x, (Object)this.z, (Object)i, (Object)j, (Object)k);
                }
            }));
            boolean bl = dynamic.get("convertedFromAlphaFormat").asBoolean(false);
            dynamic.get("Sections").asStreamOpt().ifPresent(stream -> stream.forEach(dynamic -> {
                Section section = new Section((Dynamic<?>)dynamic);
                this.sides = section.upgrade(this.sides);
                this.sections[section.y] = section;
            }));
            for (Section section : this.sections) {
                if (section == null) continue;
                block14: for (Map.Entry entry : section.toFix.entrySet()) {
                    int i = section.y << 12;
                    switch ((Integer)entry.getKey()) {
                        case 2: {
                            String string;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.getBlock(j |= i);
                                if (!"minecraft:grass_block".equals(ChunkPalettedStorageFix.getName(dynamic2)) || !"minecraft:snow".equals(string = ChunkPalettedStorageFix.getName(this.getBlock(UpgradeChunk.relative(j, Direction.UP)))) && !"minecraft:snow_layer".equals(string)) continue;
                                this.setBlock(j, SNOWY_GRASS);
                            }
                            continue block14;
                        }
                        case 3: {
                            String string;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.getBlock(j |= i);
                                if (!"minecraft:podzol".equals(ChunkPalettedStorageFix.getName(dynamic2)) || !"minecraft:snow".equals(string = ChunkPalettedStorageFix.getName(this.getBlock(UpgradeChunk.relative(j, Direction.UP)))) && !"minecraft:snow_layer".equals(string)) continue;
                                this.setBlock(j, SNOWY_PODZOL);
                            }
                            continue block14;
                        }
                        case 110: {
                            String string;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.getBlock(j |= i);
                                if (!"minecraft:mycelium".equals(ChunkPalettedStorageFix.getName(dynamic2)) || !"minecraft:snow".equals(string = ChunkPalettedStorageFix.getName(this.getBlock(UpgradeChunk.relative(j, Direction.UP)))) && !"minecraft:snow_layer".equals(string)) continue;
                                this.setBlock(j, SNOWY_MYCELIUM);
                            }
                            continue block14;
                        }
                        case 25: {
                            String string;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.removeBlockEntity(j |= i);
                                if (dynamic2 == null) continue;
                                string = Boolean.toString(dynamic2.get("powered").asBoolean(false)) + (byte)Math.min(Math.max(dynamic2.get("note").asInt(0), 0), 24);
                                this.setBlock(j, (Dynamic)NOTE_BLOCK_MAP.getOrDefault(string, NOTE_BLOCK_MAP.get("false0")));
                            }
                            continue block14;
                        }
                        case 26: {
                            String string2;
                            Dynamic<?> dynamic3;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                int k;
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.getBlockEntity(j |= i);
                                dynamic3 = this.getBlock(j);
                                if (dynamic2 == null || (k = dynamic2.get("color").asInt(0)) == 14 || k < 0 || k >= 16) continue;
                                string2 = ChunkPalettedStorageFix.getProperty(dynamic3, "facing") + ChunkPalettedStorageFix.getProperty(dynamic3, "occupied") + ChunkPalettedStorageFix.getProperty(dynamic3, "part") + k;
                                if (!BED_BLOCK_MAP.containsKey(string2)) continue;
                                this.setBlock(j, (Dynamic)BED_BLOCK_MAP.get(string2));
                            }
                            continue block14;
                        }
                        case 176: 
                        case 177: {
                            String string2;
                            Dynamic<?> dynamic3;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                int k;
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.getBlockEntity(j |= i);
                                dynamic3 = this.getBlock(j);
                                if (dynamic2 == null || (k = dynamic2.get("Base").asInt(0)) == 15 || k < 0 || k >= 16) continue;
                                string2 = ChunkPalettedStorageFix.getProperty(dynamic3, (Integer)entry.getKey() == 176 ? "rotation" : "facing") + "_" + k;
                                if (!BANNER_BLOCK_MAP.containsKey(string2)) continue;
                                this.setBlock(j, (Dynamic)BANNER_BLOCK_MAP.get(string2));
                            }
                            continue block14;
                        }
                        case 86: {
                            String string;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.getBlock(j |= i);
                                if (!"minecraft:carved_pumpkin".equals(ChunkPalettedStorageFix.getName(dynamic2)) || !"minecraft:grass_block".equals(string = ChunkPalettedStorageFix.getName(this.getBlock(UpgradeChunk.relative(j, Direction.DOWN)))) && !"minecraft:dirt".equals(string)) continue;
                                this.setBlock(j, PUMPKIN);
                            }
                            continue block14;
                        }
                        case 140: {
                            String string;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.removeBlockEntity(j |= i);
                                if (dynamic2 == null) continue;
                                string = dynamic2.get("Item").asString("") + dynamic2.get("Data").asInt(0);
                                this.setBlock(j, (Dynamic)FLOWER_POT_MAP.getOrDefault(string, FLOWER_POT_MAP.get("minecraft:air0")));
                            }
                            continue block14;
                        }
                        case 144: {
                            String string2;
                            String string;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.getBlockEntity(j |= i);
                                if (dynamic2 == null) continue;
                                string = String.valueOf(dynamic2.get("SkullType").asInt(0));
                                String string3 = ChunkPalettedStorageFix.getProperty(this.getBlock(j), "facing");
                                string2 = "up".equals(string3) || "down".equals(string3) ? string + String.valueOf(dynamic2.get("Rot").asInt(0)) : string + string3;
                                dynamic2.remove("SkullType");
                                dynamic2.remove("facing");
                                dynamic2.remove("Rot");
                                this.setBlock(j, (Dynamic)SKULL_MAP.getOrDefault(string2, SKULL_MAP.get("0north")));
                            }
                            continue block14;
                        }
                        case 64: 
                        case 71: 
                        case 193: 
                        case 194: 
                        case 195: 
                        case 196: 
                        case 197: {
                            Dynamic<?> dynamic3;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.getBlock(j |= i);
                                if (!ChunkPalettedStorageFix.getName(dynamic2).endsWith("_door") || !"lower".equals(ChunkPalettedStorageFix.getProperty(dynamic3 = this.getBlock(j), "half"))) continue;
                                int k = UpgradeChunk.relative(j, Direction.UP);
                                Dynamic<?> dynamic4 = this.getBlock(k);
                                String string4 = ChunkPalettedStorageFix.getName(dynamic3);
                                if (!string4.equals(ChunkPalettedStorageFix.getName(dynamic4))) continue;
                                String string5 = ChunkPalettedStorageFix.getProperty(dynamic3, "facing");
                                String string6 = ChunkPalettedStorageFix.getProperty(dynamic3, "open");
                                String string7 = bl ? "left" : ChunkPalettedStorageFix.getProperty(dynamic4, "hinge");
                                String string8 = bl ? "false" : ChunkPalettedStorageFix.getProperty(dynamic4, "powered");
                                this.setBlock(j, (Dynamic)DOOR_MAP.get(string4 + string5 + "lower" + string7 + string6 + string8));
                                this.setBlock(k, (Dynamic)DOOR_MAP.get(string4 + string5 + "upper" + string7 + string6 + string8));
                            }
                            continue block14;
                        }
                        case 175: {
                            Dynamic<?> dynamic3;
                            Dynamic<?> dynamic2;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic2 = this.getBlock(j |= i);
                                if (!"upper".equals(ChunkPalettedStorageFix.getProperty(dynamic2, "half"))) continue;
                                dynamic3 = this.getBlock(UpgradeChunk.relative(j, Direction.DOWN));
                                String string3 = ChunkPalettedStorageFix.getName(dynamic3);
                                if ("minecraft:sunflower".equals(string3)) {
                                    this.setBlock(j, UPPER_SUNFLOWER);
                                    continue;
                                }
                                if ("minecraft:lilac".equals(string3)) {
                                    this.setBlock(j, UPPER_LILAC);
                                    continue;
                                }
                                if ("minecraft:tall_grass".equals(string3)) {
                                    this.setBlock(j, UPPER_TALL_GRASS);
                                    continue;
                                }
                                if ("minecraft:large_fern".equals(string3)) {
                                    this.setBlock(j, UPPER_LARGE_FERN);
                                    continue;
                                }
                                if ("minecraft:rose_bush".equals(string3)) {
                                    this.setBlock(j, UPPER_ROSE_BUSH);
                                    continue;
                                }
                                if (!"minecraft:peony".equals(string3)) continue;
                                this.setBlock(j, UPPER_PEONY);
                            }
                            break;
                        }
                    }
                }
            }
        }

        @Nullable
        private Dynamic<?> getBlockEntity(int i) {
            return (Dynamic)this.blockEntities.get(i);
        }

        @Nullable
        private Dynamic<?> removeBlockEntity(int i) {
            return (Dynamic)this.blockEntities.remove(i);
        }

        public static int relative(int i, Direction direction) {
            switch (direction.getAxis()) {
                case X: {
                    int j = (i & 0xF) + direction.getAxisDirection().getStep();
                    return j < 0 || j > 15 ? -1 : i & 0xFFFFFFF0 | j;
                }
                case Y: {
                    int k = (i >> 8) + direction.getAxisDirection().getStep();
                    return k < 0 || k > 255 ? -1 : i & 0xFF | k << 8;
                }
                case Z: {
                    int l = (i >> 4 & 0xF) + direction.getAxisDirection().getStep();
                    return l < 0 || l > 15 ? -1 : i & 0xFFFFFF0F | l << 4;
                }
            }
            return -1;
        }

        private void setBlock(int i, Dynamic<?> dynamic) {
            if (i < 0 || i > 65535) {
                return;
            }
            Section section = this.getSection(i);
            if (section == null) {
                return;
            }
            section.setBlock(i & 0xFFF, dynamic);
        }

        @Nullable
        private Section getSection(int i) {
            int j = i >> 12;
            return j < this.sections.length ? this.sections[j] : null;
        }

        public Dynamic<?> getBlock(int i) {
            if (i < 0 || i > 65535) {
                return AIR;
            }
            Section section = this.getSection(i);
            if (section == null) {
                return AIR;
            }
            return section.getBlock(i & 0xFFF);
        }

        public Dynamic<?> write() {
            Dynamic<Object> dynamic = this.level;
            dynamic = this.blockEntities.isEmpty() ? dynamic.remove("TileEntities") : dynamic.set("TileEntities", dynamic.createList(this.blockEntities.values().stream()));
            Dynamic dynamic2 = dynamic.emptyMap();
            Dynamic dynamic3 = dynamic.emptyList();
            for (Section section : this.sections) {
                if (section == null) continue;
                dynamic3 = dynamic3.merge(section.write());
                dynamic2 = dynamic2.set(String.valueOf(section.y), dynamic2.createIntList(Arrays.stream(section.update.toIntArray())));
            }
            Dynamic dynamic4 = dynamic.emptyMap();
            dynamic4 = dynamic4.set("Sides", dynamic4.createByte((byte)this.sides));
            dynamic4 = dynamic4.set("Indices", dynamic2);
            return dynamic.set("UpgradeData", dynamic4).set("Sections", dynamic3);
        }
    }

    static class Section {
        private final CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> palette = new CrudeIncrementalIntIdentityHashBiMap(32);
        private Dynamic<?> listTag;
        private final Dynamic<?> section;
        private final boolean hasData;
        private final Int2ObjectMap<IntList> toFix = new Int2ObjectLinkedOpenHashMap<IntList>();
        private final IntList update = new IntArrayList();
        public final int y;
        private final Set<Dynamic<?>> seen = Sets.newIdentityHashSet();
        private final int[] buffer = new int[4096];

        public Section(Dynamic<?> dynamic) {
            this.listTag = dynamic.emptyList();
            this.section = dynamic;
            this.y = dynamic.get("Y").asInt(0);
            this.hasData = dynamic.get("Blocks").get().isPresent();
        }

        public Dynamic<?> getBlock(int i) {
            if (i < 0 || i > 4095) {
                return AIR;
            }
            Dynamic<?> dynamic = this.palette.byId(this.buffer[i]);
            return dynamic == null ? AIR : dynamic;
        }

        public void setBlock(int i, Dynamic<?> dynamic) {
            if (this.seen.add(dynamic)) {
                this.listTag = this.listTag.merge("%%FILTER_ME%%".equals(ChunkPalettedStorageFix.getName(dynamic)) ? AIR : dynamic);
            }
            this.buffer[i] = ChunkPalettedStorageFix.idFor(this.palette, dynamic);
        }

        public int upgrade(int i) {
            if (!this.hasData) {
                return i;
            }
            ByteBuffer byteBuffer2 = this.section.get("Blocks").asByteBufferOpt().get();
            DataLayer dataLayer = this.section.get("Data").asByteBufferOpt().map(byteBuffer -> new DataLayer(DataFixUtils.toArray(byteBuffer))).orElseGet(DataLayer::new);
            DataLayer dataLayer2 = this.section.get("Add").asByteBufferOpt().map(byteBuffer -> new DataLayer(DataFixUtils.toArray(byteBuffer))).orElseGet(DataLayer::new);
            this.seen.add(AIR);
            ChunkPalettedStorageFix.idFor(this.palette, AIR);
            this.listTag = this.listTag.merge(AIR);
            for (int j = 0; j < 4096; ++j) {
                int k = j & 0xF;
                int l = j >> 8 & 0xF;
                int m = j >> 4 & 0xF;
                int n = dataLayer2.get(k, l, m) << 12 | (byteBuffer2.get(j) & 0xFF) << 4 | dataLayer.get(k, l, m);
                if (FIX.get(n >> 4)) {
                    this.addFix(n >> 4, j);
                }
                if (VIRTUAL.get(n >> 4)) {
                    int o = ChunkPalettedStorageFix.getSideMask(k == 0, k == 15, m == 0, m == 15);
                    if (o == 0) {
                        this.update.add(j);
                    } else {
                        i |= o;
                    }
                }
                this.setBlock(j, BlockStateData.getTag(n));
            }
            return i;
        }

        private void addFix(int i, int j) {
            IntList intList = (IntList)this.toFix.get(i);
            if (intList == null) {
                intList = new IntArrayList();
                this.toFix.put(i, intList);
            }
            intList.add(j);
        }

        public Dynamic<?> write() {
            Dynamic<?> dynamic = this.section;
            if (!this.hasData) {
                return dynamic;
            }
            dynamic = dynamic.set("Palette", this.listTag);
            int i = Math.max(4, DataFixUtils.ceillog2(this.seen.size()));
            BitStorage bitStorage = new BitStorage(i, 4096);
            for (int j = 0; j < this.buffer.length; ++j) {
                bitStorage.set(j, this.buffer[j]);
            }
            dynamic = dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(bitStorage.getRaw())));
            dynamic = dynamic.remove("Blocks");
            dynamic = dynamic.remove("Data");
            dynamic = dynamic.remove("Add");
            return dynamic;
        }
    }
}

