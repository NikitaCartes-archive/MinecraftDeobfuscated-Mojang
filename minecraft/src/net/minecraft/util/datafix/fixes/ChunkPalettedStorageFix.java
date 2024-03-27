package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.datafix.PackedBitStorage;
import org.slf4j.Logger;

public class ChunkPalettedStorageFix extends DataFix {
	private static final int NORTH_WEST_MASK = 128;
	private static final int WEST_MASK = 64;
	private static final int SOUTH_WEST_MASK = 32;
	private static final int SOUTH_MASK = 16;
	private static final int SOUTH_EAST_MASK = 8;
	private static final int EAST_MASK = 4;
	private static final int NORTH_EAST_MASK = 2;
	private static final int NORTH_MASK = 1;
	static final Logger LOGGER = LogUtils.getLogger();
	static final BitSet VIRTUAL = new BitSet(256);
	static final BitSet FIX = new BitSet(256);
	static final Dynamic<?> PUMPKIN = BlockStateData.parse("{Name:'minecraft:pumpkin'}");
	static final Dynamic<?> SNOWY_PODZOL = BlockStateData.parse("{Name:'minecraft:podzol',Properties:{snowy:'true'}}");
	static final Dynamic<?> SNOWY_GRASS = BlockStateData.parse("{Name:'minecraft:grass_block',Properties:{snowy:'true'}}");
	static final Dynamic<?> SNOWY_MYCELIUM = BlockStateData.parse("{Name:'minecraft:mycelium',Properties:{snowy:'true'}}");
	static final Dynamic<?> UPPER_SUNFLOWER = BlockStateData.parse("{Name:'minecraft:sunflower',Properties:{half:'upper'}}");
	static final Dynamic<?> UPPER_LILAC = BlockStateData.parse("{Name:'minecraft:lilac',Properties:{half:'upper'}}");
	static final Dynamic<?> UPPER_TALL_GRASS = BlockStateData.parse("{Name:'minecraft:tall_grass',Properties:{half:'upper'}}");
	static final Dynamic<?> UPPER_LARGE_FERN = BlockStateData.parse("{Name:'minecraft:large_fern',Properties:{half:'upper'}}");
	static final Dynamic<?> UPPER_ROSE_BUSH = BlockStateData.parse("{Name:'minecraft:rose_bush',Properties:{half:'upper'}}");
	static final Dynamic<?> UPPER_PEONY = BlockStateData.parse("{Name:'minecraft:peony',Properties:{half:'upper'}}");
	static final Map<String, Dynamic<?>> FLOWER_POT_MAP = DataFixUtils.make(Maps.<String, Dynamic<?>>newHashMap(), hashMap -> {
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
	static final Map<String, Dynamic<?>> SKULL_MAP = DataFixUtils.make(Maps.<String, Dynamic<?>>newHashMap(), hashMap -> {
		mapSkull(hashMap, 0, "skeleton", "skull");
		mapSkull(hashMap, 1, "wither_skeleton", "skull");
		mapSkull(hashMap, 2, "zombie", "head");
		mapSkull(hashMap, 3, "player", "head");
		mapSkull(hashMap, 4, "creeper", "head");
		mapSkull(hashMap, 5, "dragon", "head");
	});
	static final Map<String, Dynamic<?>> DOOR_MAP = DataFixUtils.make(Maps.<String, Dynamic<?>>newHashMap(), hashMap -> {
		mapDoor(hashMap, "oak_door", 1024);
		mapDoor(hashMap, "iron_door", 1136);
		mapDoor(hashMap, "spruce_door", 3088);
		mapDoor(hashMap, "birch_door", 3104);
		mapDoor(hashMap, "jungle_door", 3120);
		mapDoor(hashMap, "acacia_door", 3136);
		mapDoor(hashMap, "dark_oak_door", 3152);
	});
	static final Map<String, Dynamic<?>> NOTE_BLOCK_MAP = DataFixUtils.make(Maps.<String, Dynamic<?>>newHashMap(), hashMap -> {
		for (int i = 0; i < 26; i++) {
			hashMap.put("true" + i, BlockStateData.parse("{Name:'minecraft:note_block',Properties:{powered:'true',note:'" + i + "'}}"));
			hashMap.put("false" + i, BlockStateData.parse("{Name:'minecraft:note_block',Properties:{powered:'false',note:'" + i + "'}}"));
		}
	});
	private static final Int2ObjectMap<String> DYE_COLOR_MAP = DataFixUtils.make(new Int2ObjectOpenHashMap<>(), int2ObjectOpenHashMap -> {
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
	static final Map<String, Dynamic<?>> BED_BLOCK_MAP = DataFixUtils.make(Maps.<String, Dynamic<?>>newHashMap(), hashMap -> {
		for (Entry<String> entry : DYE_COLOR_MAP.int2ObjectEntrySet()) {
			if (!Objects.equals(entry.getValue(), "red")) {
				addBeds(hashMap, entry.getIntKey(), (String)entry.getValue());
			}
		}
	});
	static final Map<String, Dynamic<?>> BANNER_BLOCK_MAP = DataFixUtils.make(Maps.<String, Dynamic<?>>newHashMap(), hashMap -> {
		for (Entry<String> entry : DYE_COLOR_MAP.int2ObjectEntrySet()) {
			if (!Objects.equals(entry.getValue(), "white")) {
				addBanners(hashMap, 15 - entry.getIntKey(), (String)entry.getValue());
			}
		}
	});
	static final Dynamic<?> AIR = BlockStateData.getTag(0);
	private static final int SIZE = 4096;

	public ChunkPalettedStorageFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	private static void mapSkull(Map<String, Dynamic<?>> map, int i, String string, String string2) {
		map.put(i + "north", BlockStateData.parse("{Name:'minecraft:" + string + "_wall_" + string2 + "',Properties:{facing:'north'}}"));
		map.put(i + "east", BlockStateData.parse("{Name:'minecraft:" + string + "_wall_" + string2 + "',Properties:{facing:'east'}}"));
		map.put(i + "south", BlockStateData.parse("{Name:'minecraft:" + string + "_wall_" + string2 + "',Properties:{facing:'south'}}"));
		map.put(i + "west", BlockStateData.parse("{Name:'minecraft:" + string + "_wall_" + string2 + "',Properties:{facing:'west'}}"));

		for (int j = 0; j < 16; j++) {
			map.put("" + i + j, BlockStateData.parse("{Name:'minecraft:" + string + "_" + string2 + "',Properties:{rotation:'" + j + "'}}"));
		}
	}

	private static void mapDoor(Map<String, Dynamic<?>> map, String string, int i) {
		map.put(
			"minecraft:" + string + "eastlowerleftfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "eastlowerleftfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "eastlowerlefttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "eastlowerlefttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'left',open:'true',powered:'true'}}")
		);
		map.put("minecraft:" + string + "eastlowerrightfalsefalse", BlockStateData.getTag(i));
		map.put(
			"minecraft:" + string + "eastlowerrightfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'false',powered:'true'}}")
		);
		map.put("minecraft:" + string + "eastlowerrighttruefalse", BlockStateData.getTag(i + 4));
		map.put(
			"minecraft:" + string + "eastlowerrighttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'lower',hinge:'right',open:'true',powered:'true'}}")
		);
		map.put("minecraft:" + string + "eastupperleftfalsefalse", BlockStateData.getTag(i + 8));
		map.put("minecraft:" + string + "eastupperleftfalsetrue", BlockStateData.getTag(i + 10));
		map.put(
			"minecraft:" + string + "eastupperlefttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "eastupperlefttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'upper',hinge:'left',open:'true',powered:'true'}}")
		);
		map.put("minecraft:" + string + "eastupperrightfalsefalse", BlockStateData.getTag(i + 9));
		map.put("minecraft:" + string + "eastupperrightfalsetrue", BlockStateData.getTag(i + 11));
		map.put(
			"minecraft:" + string + "eastupperrighttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "eastupperrighttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'east',half:'upper',hinge:'right',open:'true',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "northlowerleftfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "northlowerleftfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "northlowerlefttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "northlowerlefttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'left',open:'true',powered:'true'}}")
		);
		map.put("minecraft:" + string + "northlowerrightfalsefalse", BlockStateData.getTag(i + 3));
		map.put(
			"minecraft:" + string + "northlowerrightfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'false',powered:'true'}}")
		);
		map.put("minecraft:" + string + "northlowerrighttruefalse", BlockStateData.getTag(i + 7));
		map.put(
			"minecraft:" + string + "northlowerrighttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'lower',hinge:'right',open:'true',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "northupperleftfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "northupperleftfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "northupperlefttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "northupperlefttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'left',open:'true',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "northupperrightfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "northupperrightfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "northupperrighttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "northupperrighttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'north',half:'upper',hinge:'right',open:'true',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "southlowerleftfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "southlowerleftfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "southlowerlefttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "southlowerlefttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'left',open:'true',powered:'true'}}")
		);
		map.put("minecraft:" + string + "southlowerrightfalsefalse", BlockStateData.getTag(i + 1));
		map.put(
			"minecraft:" + string + "southlowerrightfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'false',powered:'true'}}")
		);
		map.put("minecraft:" + string + "southlowerrighttruefalse", BlockStateData.getTag(i + 5));
		map.put(
			"minecraft:" + string + "southlowerrighttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'lower',hinge:'right',open:'true',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "southupperleftfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "southupperleftfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "southupperlefttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "southupperlefttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'left',open:'true',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "southupperrightfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "southupperrightfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "southupperrighttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "southupperrighttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'south',half:'upper',hinge:'right',open:'true',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "westlowerleftfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "westlowerleftfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "westlowerlefttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "westlowerlefttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'left',open:'true',powered:'true'}}")
		);
		map.put("minecraft:" + string + "westlowerrightfalsefalse", BlockStateData.getTag(i + 2));
		map.put(
			"minecraft:" + string + "westlowerrightfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'false',powered:'true'}}")
		);
		map.put("minecraft:" + string + "westlowerrighttruefalse", BlockStateData.getTag(i + 6));
		map.put(
			"minecraft:" + string + "westlowerrighttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'lower',hinge:'right',open:'true',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "westupperleftfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "westupperleftfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "westupperlefttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "westupperlefttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'left',open:'true',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "westupperrightfalsefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "westupperrightfalsetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'false',powered:'true'}}")
		);
		map.put(
			"minecraft:" + string + "westupperrighttruefalse",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'false'}}")
		);
		map.put(
			"minecraft:" + string + "westupperrighttruetrue",
			BlockStateData.parse("{Name:'minecraft:" + string + "',Properties:{facing:'west',half:'upper',hinge:'right',open:'true',powered:'true'}}")
		);
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
		for (int j = 0; j < 16; j++) {
			map.put(j + "_" + i, BlockStateData.parse("{Name:'minecraft:" + string + "_banner',Properties:{rotation:'" + j + "'}}"));
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
		Optional<? extends Dynamic<?>> optional = dynamic.get("Level").result();
		return optional.isPresent() && ((Dynamic)optional.get()).get("Sections").asStreamOpt().result().isPresent()
			? dynamic.set("Level", new ChunkPalettedStorageFix.UpgradeChunk((Dynamic<?>)optional.get()).write())
			: dynamic;
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
			if (bl2) {
				i |= 2;
			} else if (bl) {
				i |= 128;
			} else {
				i |= 1;
			}
		} else if (bl4) {
			if (bl) {
				i |= 32;
			} else if (bl2) {
				i |= 8;
			} else {
				i |= 16;
			}
		} else if (bl2) {
			i |= 4;
		} else if (bl) {
			i |= 64;
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
	}

	static class DataLayer {
		private static final int SIZE = 2048;
		private static final int NIBBLE_SIZE = 4;
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
			return this.isFirst(j << 8 | k << 4 | i) ? this.data[l] & 15 : this.data[l] >> 4 & 15;
		}

		private boolean isFirst(int i) {
			return (i & 1) == 0;
		}

		private int getPosition(int i) {
			return i >> 1;
		}
	}

	public static enum Direction {
		DOWN(ChunkPalettedStorageFix.Direction.AxisDirection.NEGATIVE, ChunkPalettedStorageFix.Direction.Axis.Y),
		UP(ChunkPalettedStorageFix.Direction.AxisDirection.POSITIVE, ChunkPalettedStorageFix.Direction.Axis.Y),
		NORTH(ChunkPalettedStorageFix.Direction.AxisDirection.NEGATIVE, ChunkPalettedStorageFix.Direction.Axis.Z),
		SOUTH(ChunkPalettedStorageFix.Direction.AxisDirection.POSITIVE, ChunkPalettedStorageFix.Direction.Axis.Z),
		WEST(ChunkPalettedStorageFix.Direction.AxisDirection.NEGATIVE, ChunkPalettedStorageFix.Direction.Axis.X),
		EAST(ChunkPalettedStorageFix.Direction.AxisDirection.POSITIVE, ChunkPalettedStorageFix.Direction.Axis.X);

		private final ChunkPalettedStorageFix.Direction.Axis axis;
		private final ChunkPalettedStorageFix.Direction.AxisDirection axisDirection;

		private Direction(ChunkPalettedStorageFix.Direction.AxisDirection axisDirection, ChunkPalettedStorageFix.Direction.Axis axis) {
			this.axis = axis;
			this.axisDirection = axisDirection;
		}

		public ChunkPalettedStorageFix.Direction.AxisDirection getAxisDirection() {
			return this.axisDirection;
		}

		public ChunkPalettedStorageFix.Direction.Axis getAxis() {
			return this.axis;
		}

		public static enum Axis {
			X,
			Y,
			Z;
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
	}

	static class Section {
		private final CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> palette = CrudeIncrementalIntIdentityHashBiMap.create(32);
		private final List<Dynamic<?>> listTag;
		private final Dynamic<?> section;
		private final boolean hasData;
		final Int2ObjectMap<IntList> toFix = new Int2ObjectLinkedOpenHashMap<>();
		final IntList update = new IntArrayList();
		public final int y;
		private final Set<Dynamic<?>> seen = Sets.newIdentityHashSet();
		private final int[] buffer = new int[4096];

		public Section(Dynamic<?> dynamic) {
			this.listTag = Lists.<Dynamic<?>>newArrayList();
			this.section = dynamic;
			this.y = dynamic.get("Y").asInt(0);
			this.hasData = dynamic.get("Blocks").result().isPresent();
		}

		public Dynamic<?> getBlock(int i) {
			if (i >= 0 && i <= 4095) {
				Dynamic<?> dynamic = this.palette.byId(this.buffer[i]);
				return dynamic == null ? ChunkPalettedStorageFix.AIR : dynamic;
			} else {
				return ChunkPalettedStorageFix.AIR;
			}
		}

		public void setBlock(int i, Dynamic<?> dynamic) {
			if (this.seen.add(dynamic)) {
				this.listTag.add("%%FILTER_ME%%".equals(ChunkPalettedStorageFix.getName(dynamic)) ? ChunkPalettedStorageFix.AIR : dynamic);
			}

			this.buffer[i] = ChunkPalettedStorageFix.idFor(this.palette, dynamic);
		}

		public int upgrade(int i) {
			if (!this.hasData) {
				return i;
			} else {
				ByteBuffer byteBuffer = (ByteBuffer)this.section.get("Blocks").asByteBufferOpt().result().get();
				ChunkPalettedStorageFix.DataLayer dataLayer = (ChunkPalettedStorageFix.DataLayer)this.section
					.get("Data")
					.asByteBufferOpt()
					.map(byteBufferx -> new ChunkPalettedStorageFix.DataLayer(DataFixUtils.toArray(byteBufferx)))
					.result()
					.orElseGet(ChunkPalettedStorageFix.DataLayer::new);
				ChunkPalettedStorageFix.DataLayer dataLayer2 = (ChunkPalettedStorageFix.DataLayer)this.section
					.get("Add")
					.asByteBufferOpt()
					.map(byteBufferx -> new ChunkPalettedStorageFix.DataLayer(DataFixUtils.toArray(byteBufferx)))
					.result()
					.orElseGet(ChunkPalettedStorageFix.DataLayer::new);
				this.seen.add(ChunkPalettedStorageFix.AIR);
				ChunkPalettedStorageFix.idFor(this.palette, ChunkPalettedStorageFix.AIR);
				this.listTag.add(ChunkPalettedStorageFix.AIR);

				for (int j = 0; j < 4096; j++) {
					int k = j & 15;
					int l = j >> 8 & 15;
					int m = j >> 4 & 15;
					int n = dataLayer2.get(k, l, m) << 12 | (byteBuffer.get(j) & 255) << 4 | dataLayer.get(k, l, m);
					if (ChunkPalettedStorageFix.FIX.get(n >> 4)) {
						this.addFix(n >> 4, j);
					}

					if (ChunkPalettedStorageFix.VIRTUAL.get(n >> 4)) {
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
		}

		private void addFix(int i, int j) {
			IntList intList = this.toFix.get(i);
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
			} else {
				dynamic = dynamic.set("Palette", dynamic.createList(this.listTag.stream()));
				int i = Math.max(4, DataFixUtils.ceillog2(this.seen.size()));
				PackedBitStorage packedBitStorage = new PackedBitStorage(i, 4096);

				for (int j = 0; j < this.buffer.length; j++) {
					packedBitStorage.set(j, this.buffer[j]);
				}

				dynamic = dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(packedBitStorage.getRaw())));
				dynamic = dynamic.remove("Blocks");
				dynamic = dynamic.remove("Data");
				return dynamic.remove("Add");
			}
		}
	}

	static final class UpgradeChunk {
		private int sides;
		private final ChunkPalettedStorageFix.Section[] sections = new ChunkPalettedStorageFix.Section[16];
		private final Dynamic<?> level;
		private final int x;
		private final int z;
		private final Int2ObjectMap<Dynamic<?>> blockEntities = new Int2ObjectLinkedOpenHashMap<>(16);

		public UpgradeChunk(Dynamic<?> dynamic) {
			this.level = dynamic;
			this.x = dynamic.get("xPos").asInt(0) << 4;
			this.z = dynamic.get("zPos").asInt(0) << 4;
			dynamic.get("TileEntities").asStreamOpt().ifSuccess(stream -> stream.forEach(dynamicx -> {
					int ix = dynamicx.get("x").asInt(0) - this.x & 15;
					int jx = dynamicx.get("y").asInt(0);
					int k = dynamicx.get("z").asInt(0) - this.z & 15;
					int l = jx << 8 | k << 4 | ix;
					if (this.blockEntities.put(l, dynamicx) != null) {
						ChunkPalettedStorageFix.LOGGER.warn("In chunk: {}x{} found a duplicate block entity at position: [{}, {}, {}]", this.x, this.z, ix, jx, k);
					}
				}));
			boolean bl = dynamic.get("convertedFromAlphaFormat").asBoolean(false);
			dynamic.get("Sections").asStreamOpt().ifSuccess(stream -> stream.forEach(dynamicx -> {
					ChunkPalettedStorageFix.Section sectionx = new ChunkPalettedStorageFix.Section(dynamicx);
					this.sides = sectionx.upgrade(this.sides);
					this.sections[sectionx.y] = sectionx;
				}));

			for (ChunkPalettedStorageFix.Section section : this.sections) {
				if (section != null) {
					for (java.util.Map.Entry<Integer, IntList> entry : section.toFix.entrySet()) {
						int i = section.y << 12;
						switch (entry.getKey()) {
							case 2:
								for (int j : (IntList)entry.getValue()) {
									j |= i;
									Dynamic<?> dynamic2 = this.getBlock(j);
									if ("minecraft:grass_block".equals(ChunkPalettedStorageFix.getName(dynamic2))) {
										String string = ChunkPalettedStorageFix.getName(this.getBlock(relative(j, ChunkPalettedStorageFix.Direction.UP)));
										if ("minecraft:snow".equals(string) || "minecraft:snow_layer".equals(string)) {
											this.setBlock(j, ChunkPalettedStorageFix.SNOWY_GRASS);
										}
									}
								}
								break;
							case 3:
								for (int jxxxxxxxxx : (IntList)entry.getValue()) {
									jxxxxxxxxx |= i;
									Dynamic<?> dynamic2 = this.getBlock(jxxxxxxxxx);
									if ("minecraft:podzol".equals(ChunkPalettedStorageFix.getName(dynamic2))) {
										String string = ChunkPalettedStorageFix.getName(this.getBlock(relative(jxxxxxxxxx, ChunkPalettedStorageFix.Direction.UP)));
										if ("minecraft:snow".equals(string) || "minecraft:snow_layer".equals(string)) {
											this.setBlock(jxxxxxxxxx, ChunkPalettedStorageFix.SNOWY_PODZOL);
										}
									}
								}
								break;
							case 25:
								for (int jxxxxx : (IntList)entry.getValue()) {
									jxxxxx |= i;
									Dynamic<?> dynamic2 = this.removeBlockEntity(jxxxxx);
									if (dynamic2 != null) {
										String string = Boolean.toString(dynamic2.get("powered").asBoolean(false)) + (byte)Math.min(Math.max(dynamic2.get("note").asInt(0), 0), 24);
										this.setBlock(
											jxxxxx, (Dynamic<?>)ChunkPalettedStorageFix.NOTE_BLOCK_MAP.getOrDefault(string, (Dynamic)ChunkPalettedStorageFix.NOTE_BLOCK_MAP.get("false0"))
										);
									}
								}
								break;
							case 26:
								for (int jxxxx : (IntList)entry.getValue()) {
									jxxxx |= i;
									Dynamic<?> dynamic2 = this.getBlockEntity(jxxxx);
									Dynamic<?> dynamic3 = this.getBlock(jxxxx);
									if (dynamic2 != null) {
										int k = dynamic2.get("color").asInt(0);
										if (k != 14 && k >= 0 && k < 16) {
											String string2 = ChunkPalettedStorageFix.getProperty(dynamic3, "facing")
												+ ChunkPalettedStorageFix.getProperty(dynamic3, "occupied")
												+ ChunkPalettedStorageFix.getProperty(dynamic3, "part")
												+ k;
											if (ChunkPalettedStorageFix.BED_BLOCK_MAP.containsKey(string2)) {
												this.setBlock(jxxxx, (Dynamic<?>)ChunkPalettedStorageFix.BED_BLOCK_MAP.get(string2));
											}
										}
									}
								}
								break;
							case 64:
							case 71:
							case 193:
							case 194:
							case 195:
							case 196:
							case 197:
								for (int jxxx : (IntList)entry.getValue()) {
									jxxx |= i;
									Dynamic<?> dynamic2 = this.getBlock(jxxx);
									if (ChunkPalettedStorageFix.getName(dynamic2).endsWith("_door")) {
										Dynamic<?> dynamic3 = this.getBlock(jxxx);
										if ("lower".equals(ChunkPalettedStorageFix.getProperty(dynamic3, "half"))) {
											int k = relative(jxxx, ChunkPalettedStorageFix.Direction.UP);
											Dynamic<?> dynamic4 = this.getBlock(k);
											String string4 = ChunkPalettedStorageFix.getName(dynamic3);
											if (string4.equals(ChunkPalettedStorageFix.getName(dynamic4))) {
												String string5 = ChunkPalettedStorageFix.getProperty(dynamic3, "facing");
												String string6 = ChunkPalettedStorageFix.getProperty(dynamic3, "open");
												String string7 = bl ? "left" : ChunkPalettedStorageFix.getProperty(dynamic4, "hinge");
												String string8 = bl ? "false" : ChunkPalettedStorageFix.getProperty(dynamic4, "powered");
												this.setBlock(jxxx, (Dynamic<?>)ChunkPalettedStorageFix.DOOR_MAP.get(string4 + string5 + "lower" + string7 + string6 + string8));
												this.setBlock(k, (Dynamic<?>)ChunkPalettedStorageFix.DOOR_MAP.get(string4 + string5 + "upper" + string7 + string6 + string8));
											}
										}
									}
								}
								break;
							case 86:
								for (int jxxxxxxxx : (IntList)entry.getValue()) {
									jxxxxxxxx |= i;
									Dynamic<?> dynamic2 = this.getBlock(jxxxxxxxx);
									if ("minecraft:carved_pumpkin".equals(ChunkPalettedStorageFix.getName(dynamic2))) {
										String string = ChunkPalettedStorageFix.getName(this.getBlock(relative(jxxxxxxxx, ChunkPalettedStorageFix.Direction.DOWN)));
										if ("minecraft:grass_block".equals(string) || "minecraft:dirt".equals(string)) {
											this.setBlock(jxxxxxxxx, ChunkPalettedStorageFix.PUMPKIN);
										}
									}
								}
								break;
							case 110:
								for (int jxxxxxxx : (IntList)entry.getValue()) {
									jxxxxxxx |= i;
									Dynamic<?> dynamic2 = this.getBlock(jxxxxxxx);
									if ("minecraft:mycelium".equals(ChunkPalettedStorageFix.getName(dynamic2))) {
										String string = ChunkPalettedStorageFix.getName(this.getBlock(relative(jxxxxxxx, ChunkPalettedStorageFix.Direction.UP)));
										if ("minecraft:snow".equals(string) || "minecraft:snow_layer".equals(string)) {
											this.setBlock(jxxxxxxx, ChunkPalettedStorageFix.SNOWY_MYCELIUM);
										}
									}
								}
								break;
							case 140:
								for (int jxx : (IntList)entry.getValue()) {
									jxx |= i;
									Dynamic<?> dynamic2 = this.removeBlockEntity(jxx);
									if (dynamic2 != null) {
										String string = dynamic2.get("Item").asString("") + dynamic2.get("Data").asInt(0);
										this.setBlock(
											jxx, (Dynamic<?>)ChunkPalettedStorageFix.FLOWER_POT_MAP.getOrDefault(string, (Dynamic)ChunkPalettedStorageFix.FLOWER_POT_MAP.get("minecraft:air0"))
										);
									}
								}
								break;
							case 144:
								for (int jxxxxxx : (IntList)entry.getValue()) {
									jxxxxxx |= i;
									Dynamic<?> dynamic2 = this.getBlockEntity(jxxxxxx);
									if (dynamic2 != null) {
										String string = String.valueOf(dynamic2.get("SkullType").asInt(0));
										String string3 = ChunkPalettedStorageFix.getProperty(this.getBlock(jxxxxxx), "facing");
										String string2;
										if (!"up".equals(string3) && !"down".equals(string3)) {
											string2 = string + string3;
										} else {
											string2 = string + dynamic2.get("Rot").asInt(0);
										}

										dynamic2.remove("SkullType");
										dynamic2.remove("facing");
										dynamic2.remove("Rot");
										this.setBlock(jxxxxxx, (Dynamic<?>)ChunkPalettedStorageFix.SKULL_MAP.getOrDefault(string2, (Dynamic)ChunkPalettedStorageFix.SKULL_MAP.get("0north")));
									}
								}
								break;
							case 175:
								for (int jx : (IntList)entry.getValue()) {
									jx |= i;
									Dynamic<?> dynamic2 = this.getBlock(jx);
									if ("upper".equals(ChunkPalettedStorageFix.getProperty(dynamic2, "half"))) {
										Dynamic<?> dynamic3 = this.getBlock(relative(jx, ChunkPalettedStorageFix.Direction.DOWN));
										String string3 = ChunkPalettedStorageFix.getName(dynamic3);
										if ("minecraft:sunflower".equals(string3)) {
											this.setBlock(jx, ChunkPalettedStorageFix.UPPER_SUNFLOWER);
										} else if ("minecraft:lilac".equals(string3)) {
											this.setBlock(jx, ChunkPalettedStorageFix.UPPER_LILAC);
										} else if ("minecraft:tall_grass".equals(string3)) {
											this.setBlock(jx, ChunkPalettedStorageFix.UPPER_TALL_GRASS);
										} else if ("minecraft:large_fern".equals(string3)) {
											this.setBlock(jx, ChunkPalettedStorageFix.UPPER_LARGE_FERN);
										} else if ("minecraft:rose_bush".equals(string3)) {
											this.setBlock(jx, ChunkPalettedStorageFix.UPPER_ROSE_BUSH);
										} else if ("minecraft:peony".equals(string3)) {
											this.setBlock(jx, ChunkPalettedStorageFix.UPPER_PEONY);
										}
									}
								}
								break;
							case 176:
							case 177:
								for (int jxxxxxxxxxx : (IntList)entry.getValue()) {
									jxxxxxxxxxx |= i;
									Dynamic<?> dynamic2 = this.getBlockEntity(jxxxxxxxxxx);
									Dynamic<?> dynamic3 = this.getBlock(jxxxxxxxxxx);
									if (dynamic2 != null) {
										int k = dynamic2.get("Base").asInt(0);
										if (k != 15 && k >= 0 && k < 16) {
											String string2 = ChunkPalettedStorageFix.getProperty(dynamic3, entry.getKey() == 176 ? "rotation" : "facing") + "_" + k;
											if (ChunkPalettedStorageFix.BANNER_BLOCK_MAP.containsKey(string2)) {
												this.setBlock(jxxxxxxxxxx, (Dynamic<?>)ChunkPalettedStorageFix.BANNER_BLOCK_MAP.get(string2));
											}
										}
									}
								}
						}
					}
				}
			}
		}

		@Nullable
		private Dynamic<?> getBlockEntity(int i) {
			return this.blockEntities.get(i);
		}

		@Nullable
		private Dynamic<?> removeBlockEntity(int i) {
			return this.blockEntities.remove(i);
		}

		public static int relative(int i, ChunkPalettedStorageFix.Direction direction) {
			switch (direction.getAxis()) {
				case X:
					int j = (i & 15) + direction.getAxisDirection().getStep();
					return j >= 0 && j <= 15 ? i & -16 | j : -1;
				case Y:
					int k = (i >> 8) + direction.getAxisDirection().getStep();
					return k >= 0 && k <= 255 ? i & 0xFF | k << 8 : -1;
				case Z:
					int l = (i >> 4 & 15) + direction.getAxisDirection().getStep();
					return l >= 0 && l <= 15 ? i & -241 | l << 4 : -1;
				default:
					return -1;
			}
		}

		private void setBlock(int i, Dynamic<?> dynamic) {
			if (i >= 0 && i <= 65535) {
				ChunkPalettedStorageFix.Section section = this.getSection(i);
				if (section != null) {
					section.setBlock(i & 4095, dynamic);
				}
			}
		}

		@Nullable
		private ChunkPalettedStorageFix.Section getSection(int i) {
			int j = i >> 12;
			return j < this.sections.length ? this.sections[j] : null;
		}

		public Dynamic<?> getBlock(int i) {
			if (i >= 0 && i <= 65535) {
				ChunkPalettedStorageFix.Section section = this.getSection(i);
				return section == null ? ChunkPalettedStorageFix.AIR : section.getBlock(i & 4095);
			} else {
				return ChunkPalettedStorageFix.AIR;
			}
		}

		public Dynamic<?> write() {
			Dynamic<?> dynamic = this.level;
			if (this.blockEntities.isEmpty()) {
				dynamic = dynamic.remove("TileEntities");
			} else {
				dynamic = dynamic.set("TileEntities", dynamic.createList(this.blockEntities.values().stream()));
			}

			Dynamic<?> dynamic2 = dynamic.emptyMap();
			List<Dynamic<?>> list = Lists.<Dynamic<?>>newArrayList();

			for (ChunkPalettedStorageFix.Section section : this.sections) {
				if (section != null) {
					list.add(section.write());
					dynamic2 = dynamic2.set(String.valueOf(section.y), dynamic2.createIntList(Arrays.stream(section.update.toIntArray())));
				}
			}

			Dynamic<?> dynamic3 = dynamic.emptyMap();
			dynamic3 = dynamic3.set("Sides", dynamic3.createByte((byte)this.sides));
			dynamic3 = dynamic3.set("Indices", dynamic2);
			return dynamic.set("UpgradeData", dynamic3).set("Sections", dynamic3.createList(list.stream()));
		}
	}
}
