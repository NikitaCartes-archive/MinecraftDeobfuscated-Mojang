package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

public class ChunkHeightAndBiomeFix extends DataFix {
	public static final String DATAFIXER_CONTEXT_TAG = "__context";
	private static final String NAME = "ChunkHeightAndBiomeFix";
	private static final int OLD_SECTION_COUNT = 16;
	private static final int NEW_SECTION_COUNT = 24;
	private static final int NEW_MIN_SECTION_Y = -4;
	private static final int BITS_PER_SECTION = 64;
	private static final int HEIGHTMAP_BITS = 9;
	private static final long HEIGHTMAP_MASK = 511L;
	private static final int HEIGHTMAP_OFFSET = 64;
	private static final String[] HEIGHTMAP_TYPES = new String[]{
		"WORLD_SURFACE_WG", "WORLD_SURFACE", "WORLD_SURFACE_IGNORE_SNOW", "OCEAN_FLOOR_WG", "OCEAN_FLOOR", "MOTION_BLOCKING", "MOTION_BLOCKING_NO_LEAVES"
	};
	private static final Set<String> STATUSES_WITH_NOISE_DATA = Set.of(
		"noise", "surface", "carvers", "liquid_carvers", "features", "light", "spawn", "heightmaps", "full"
	);
	private static final int BIOME_CONTAINER_LAYER_SIZE = 16;
	private static final int BIOME_CONTAINER_SIZE = 64;
	private static final int BIOME_CONTAINER_TOP_LAYER_OFFSET = 1008;
	public static final String DEFAULT_BIOME = "minecraft:plains";
	private static final Int2ObjectMap<String> BIOMES_BY_ID = new Int2ObjectOpenHashMap<>();

	public ChunkHeightAndBiomeFix(Schema schema) {
		super(schema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.CHUNK);
		OpticFinder<?> opticFinder = type.findField("Level");
		OpticFinder<?> opticFinder2 = opticFinder.type().findField("Sections");
		Schema schema = this.getOutputSchema();
		Type<?> type2 = schema.getType(References.CHUNK);
		Type<?> type3 = type2.findField("Level").type();
		Type<?> type4 = type3.findField("Sections").type();
		return this.fixTypeEverywhereTyped(
			"ChunkHeightAndBiomeFix",
			type,
			type2,
			typed -> typed.updateTyped(
					opticFinder,
					type3,
					typed2 -> {
						Dynamic<?> dynamic = typed2.get(DSL.remainderFinder());
						OptionalDynamic<?> optionalDynamic = typed.get(DSL.remainderFinder()).get("__context");
						String string = (String)optionalDynamic.get("dimension").asString().result().orElse("");
						String string2 = (String)optionalDynamic.get("generator").asString().result().orElse("");
						boolean bl = "minecraft:overworld".equals(string);
						MutableBoolean mutableBoolean = new MutableBoolean();
						int i = bl ? -4 : 0;
						Dynamic<?>[] dynamics = getBiomeContainers(dynamic, bl, i, mutableBoolean);
						Dynamic<?> dynamic2 = makePalettedContainer(
							dynamic.createList(Stream.of(dynamic.createMap(ImmutableMap.of(dynamic.createString("Name"), dynamic.createString("minecraft:air")))))
						);
						MutableObject<Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>> mutableObject = new MutableObject<>(() -> null);
						typed2 = typed2.updateTyped(
							opticFinder2,
							type4,
							typedxx -> {
								IntSet intSet = new IntOpenHashSet();
								Dynamic<?> dynamic3 = (Dynamic<?>)typedxx.write().result().orElseThrow(() -> new IllegalStateException("Malformed Chunk.Level.Sections"));
								List<Dynamic<?>> list = (List<Dynamic<?>>)dynamic3.asStream()
									.map(
										dynamic2xx -> {
											int jx = dynamic2xx.get("Y").asInt(0);
											Dynamic<?> dynamic3x = DataFixUtils.orElse(
												dynamic2xx.get("Palette")
													.result()
													.flatMap(dynamic2xxx -> dynamic2xx.get("BlockStates").result().map(dynamic2xxxx -> makeOptimizedPalettedContainer(dynamic2xxx, dynamic2xxxx))),
												dynamic2
											);
											Dynamic<?> dynamic4x = dynamic2xx;
											int kx = jx - i;
											if (kx >= 0 && kx < dynamics.length) {
												dynamic4x = dynamic2xx.set("biomes", dynamics[kx]);
											}

											intSet.add(jx);
											if (dynamic2xx.get("Y").asInt(Integer.MAX_VALUE) == 0) {
												mutableObject.setValue(() -> {
													List<? extends Dynamic<?>> listx = dynamic3x.get("palette").asList(Function.identity());
													long[] ls = dynamic3x.get("data").asLongStream().toArray();
													return new ChunkProtoTickListFix.PoorMansPalettedContainer(listx, ls);
												});
											}

											return dynamic4x.set("block_states", dynamic3x).remove("Palette").remove("BlockStates");
										}
									)
									.collect(Collectors.toCollection(ArrayList::new));

								for (int j = 0; j < dynamics.length; j++) {
									int k = j + i;
									if (intSet.add(k)) {
										Dynamic<?> dynamic4 = dynamic.createMap(Map.of(dynamic.createString("Y"), dynamic.createInt(k)));
										dynamic4 = dynamic4.set("block_states", dynamic2);
										dynamic4 = dynamic4.set("biomes", dynamics[j]);
										list.add(dynamic4);
									}
								}

								return (Typed)((Pair)type4.readTyped(dynamic.createList(list.stream()))
										.result()
										.orElseThrow(() -> new IllegalStateException("ChunkHeightAndBiomeFix failed.")))
									.getFirst();
							}
						);
						return typed2.update(
							DSL.remainderFinder(),
							dynamicx -> updateChunkTag(dynamicx, bl, mutableBoolean.booleanValue(), "minecraft:noise".equals(string2), mutableObject.getValue())
						);
					}
				)
		);
	}

	private static Dynamic<?>[] getBiomeContainers(Dynamic<?> dynamic, boolean bl, int i, MutableBoolean mutableBoolean) {
		Dynamic<?>[] dynamics = new Dynamic[bl ? 24 : 16];
		Optional<IntStream> optional = dynamic.get("Biomes").asIntStreamOpt().result();
		if (optional.isPresent()) {
			int[] is = ((IntStream)optional.get()).toArray();
			mutableBoolean.setValue(is.length == 1536);
			if (mutableBoolean.booleanValue()) {
				for (int j = 0; j < 24; j++) {
					dynamics[j] = makeBiomeContainer(dynamic, jx -> is[j * 64 + jx]);
				}
			} else {
				for (int j = 0; j < 16; j++) {
					int k = j - i;
					dynamics[k] = makeBiomeContainer(dynamic, jx -> is[j * 64 + jx]);
				}

				if (bl) {
					Dynamic<?> dynamic2 = makeBiomeContainer(dynamic, ix -> is[ix % 16]);
					Dynamic<?> dynamic3 = makeBiomeContainer(dynamic, ix -> is[ix % 16 + 1008]);

					for (int l = 0; l < 4; l++) {
						dynamics[l] = dynamic2;
					}

					for (int l = 20; l < 24; l++) {
						dynamics[l] = dynamic3;
					}
				}
			}
		} else {
			Arrays.fill(dynamics, makePalettedContainer(dynamic.createList(Stream.of(dynamic.createString("minecraft:plains")))));
		}

		return dynamics;
	}

	private static Dynamic<?> updateChunkTag(
		Dynamic<?> dynamic, boolean bl, boolean bl2, boolean bl3, Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> supplier
	) {
		dynamic = dynamic.remove("Biomes");
		if (!bl) {
			return updateCarvingMasks(dynamic, 16, 0);
		} else if (bl2) {
			return updateCarvingMasks(dynamic, 24, 0);
		} else {
			dynamic = updateHeightmaps(dynamic);
			dynamic = addPaddingEntries(dynamic, "Lights");
			dynamic = addPaddingEntries(dynamic, "LiquidsToBeTicked");
			dynamic = addPaddingEntries(dynamic, "PostProcessing");
			dynamic = addPaddingEntries(dynamic, "ToBeTicked");
			dynamic = updateCarvingMasks(dynamic, 24, 4);
			if (!bl3) {
				return dynamic;
			} else {
				Optional<? extends Dynamic<?>> optional = dynamic.get("Status").result();
				if (optional.isPresent()) {
					Dynamic<?> dynamic2 = (Dynamic<?>)optional.get();
					String string = dynamic2.asString("");
					if (!"empty".equals(string)) {
						boolean bl4 = STATUSES_WITH_NOISE_DATA.contains(string);
						boolean bl5 = bl4 || "biomes".equals(string);
						dynamic = dynamic.set(
							"blending_data",
							dynamic.createMap(
								ImmutableMap.of(dynamic.createString("old_biome"), dynamic.createBoolean(bl5), dynamic.createString("old_noise"), dynamic.createBoolean(bl4))
							)
						);
						ChunkProtoTickListFix.PoorMansPalettedContainer poorMansPalettedContainer = (ChunkProtoTickListFix.PoorMansPalettedContainer)supplier.get();
						if (poorMansPalettedContainer != null) {
							BitSet bitSet = new BitSet(256);

							for (int i = 0; i < 16; i++) {
								for (int j = 0; j < 16; j++) {
									Dynamic<?> dynamic3 = poorMansPalettedContainer.get(j, 0, i);
									boolean bl6 = dynamic3 != null && "minecraft:bedrock".equals(dynamic3.get("Name").asString(""));
									if (!bl6) {
										bitSet.set(i * 16 + j);
									}
								}
							}

							boolean bl7 = bitSet.cardinality() != bitSet.size();
							if (bl7) {
								Dynamic<?> dynamic4 = "full".equals(string) ? dynamic.createString("heightmaps") : dynamic2;
								dynamic = dynamic.set(
									"below_zero_retrogen",
									dynamic.createMap(
										ImmutableMap.of(
											dynamic.createString("target_status"),
											dynamic4,
											dynamic.createString("missing_bedrock"),
											dynamic.createLongList(LongStream.of(bitSet.toLongArray()))
										)
									)
								);
								dynamic = dynamic.set("Status", dynamic.createString("empty"));
								dynamic = dynamic.set("isLightOn", dynamic.createBoolean(false));
							}
						}
					}
				}

				return dynamic;
			}
		}
	}

	private static Dynamic<?> updateCarvingMasks(Dynamic<?> dynamic, int i, int j) {
		Dynamic<?> dynamic2 = dynamic.get("CarvingMasks").orElseEmptyMap();
		dynamic2 = dynamic2.updateMapValues(pair -> {
			long[] ls = BitSet.valueOf(((Dynamic)pair.getSecond()).asByteBuffer().array()).toLongArray();
			long[] ms = new long[64 * i];
			System.arraycopy(ls, 0, ms, 64 * j, ls.length);
			return Pair.of((Dynamic)pair.getFirst(), dynamic.createLongList(LongStream.of(ms)));
		});
		return dynamic.set("CarvingMasks", dynamic2);
	}

	private static Dynamic<?> addPaddingEntries(Dynamic<?> dynamic, String string) {
		List<Dynamic<?>> list = (List<Dynamic<?>>)dynamic.get(string).orElseEmptyList().asStream().collect(Collectors.toCollection(ArrayList::new));
		if (list.size() == 24) {
			return dynamic;
		} else {
			Dynamic<?> dynamic2 = dynamic.emptyList();

			for (int i = 0; i < 4; i++) {
				list.add(0, dynamic2);
				list.add(dynamic2);
			}

			return dynamic.set(string, dynamic.createList(list.stream()));
		}
	}

	private static Dynamic<?> updateHeightmaps(Dynamic<?> dynamic) {
		return dynamic.update("Heightmaps", dynamicx -> {
			for (String string : HEIGHTMAP_TYPES) {
				dynamicx = dynamicx.update(string, ChunkHeightAndBiomeFix::getFixedHeightmap);
			}

			return dynamicx;
		});
	}

	private static Dynamic<?> getFixedHeightmap(Dynamic<?> dynamic) {
		return dynamic.createLongList(dynamic.asLongStream().map(l -> {
			long m = 0L;

			for (int i = 0; i + 9 <= 64; i += 9) {
				long n = l >> i & 511L;
				long o;
				if (n == 0L) {
					o = 0L;
				} else {
					o = Math.min(n + 64L, 511L);
				}

				m |= o << i;
			}

			return m;
		}));
	}

	private static Dynamic<?> makeBiomeContainer(Dynamic<?> dynamic, Int2IntFunction int2IntFunction) {
		Int2IntMap int2IntMap = new Int2IntLinkedOpenHashMap();

		for (int i = 0; i < 64; i++) {
			int j = int2IntFunction.applyAsInt(i);
			if (!int2IntMap.containsKey(j)) {
				int2IntMap.put(j, int2IntMap.size());
			}
		}

		Dynamic<?> dynamic2 = dynamic.createList(
			int2IntMap.keySet().stream().map(integer -> dynamic.createString(BIOMES_BY_ID.getOrDefault(integer.intValue(), "minecraft:plains")))
		);
		int j = ceillog2(int2IntMap.size());
		if (j == 0) {
			return makePalettedContainer(dynamic2);
		} else {
			int k = 64 / j;
			int l = (64 + k - 1) / k;
			long[] ls = new long[l];
			int m = 0;
			int n = 0;

			for (int o = 0; o < 64; o++) {
				int p = int2IntFunction.applyAsInt(o);
				ls[m] |= (long)int2IntMap.get(p) << n;
				n += j;
				if (n + j > 64) {
					m++;
					n = 0;
				}
			}

			Dynamic<?> dynamic3 = dynamic.createLongList(Arrays.stream(ls));
			return makePalettedContainer(dynamic2, dynamic3);
		}
	}

	private static Dynamic<?> makePalettedContainer(Dynamic<?> dynamic) {
		return dynamic.createMap(ImmutableMap.of(dynamic.createString("palette"), dynamic));
	}

	private static Dynamic<?> makePalettedContainer(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
		return dynamic.createMap(ImmutableMap.of(dynamic.createString("palette"), dynamic, dynamic.createString("data"), dynamic2));
	}

	private static Dynamic<?> makeOptimizedPalettedContainer(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
		return dynamic.asStream().count() == 1L ? makePalettedContainer(dynamic) : makePalettedContainer(dynamic, dynamic2);
	}

	public static int ceillog2(int i) {
		return i == 0 ? 0 : (int)Math.ceil(Math.log((double)i) / Math.log(2.0));
	}

	static {
		BIOMES_BY_ID.put(0, "minecraft:ocean");
		BIOMES_BY_ID.put(1, "minecraft:plains");
		BIOMES_BY_ID.put(2, "minecraft:desert");
		BIOMES_BY_ID.put(3, "minecraft:mountains");
		BIOMES_BY_ID.put(4, "minecraft:forest");
		BIOMES_BY_ID.put(5, "minecraft:taiga");
		BIOMES_BY_ID.put(6, "minecraft:swamp");
		BIOMES_BY_ID.put(7, "minecraft:river");
		BIOMES_BY_ID.put(8, "minecraft:nether_wastes");
		BIOMES_BY_ID.put(9, "minecraft:the_end");
		BIOMES_BY_ID.put(10, "minecraft:frozen_ocean");
		BIOMES_BY_ID.put(11, "minecraft:frozen_river");
		BIOMES_BY_ID.put(12, "minecraft:snowy_tundra");
		BIOMES_BY_ID.put(13, "minecraft:snowy_mountains");
		BIOMES_BY_ID.put(14, "minecraft:mushroom_fields");
		BIOMES_BY_ID.put(15, "minecraft:mushroom_field_shore");
		BIOMES_BY_ID.put(16, "minecraft:beach");
		BIOMES_BY_ID.put(17, "minecraft:desert_hills");
		BIOMES_BY_ID.put(18, "minecraft:wooded_hills");
		BIOMES_BY_ID.put(19, "minecraft:taiga_hills");
		BIOMES_BY_ID.put(20, "minecraft:mountain_edge");
		BIOMES_BY_ID.put(21, "minecraft:jungle");
		BIOMES_BY_ID.put(22, "minecraft:jungle_hills");
		BIOMES_BY_ID.put(23, "minecraft:jungle_edge");
		BIOMES_BY_ID.put(24, "minecraft:deep_ocean");
		BIOMES_BY_ID.put(25, "minecraft:stone_shore");
		BIOMES_BY_ID.put(26, "minecraft:snowy_beach");
		BIOMES_BY_ID.put(27, "minecraft:birch_forest");
		BIOMES_BY_ID.put(28, "minecraft:birch_forest_hills");
		BIOMES_BY_ID.put(29, "minecraft:dark_forest");
		BIOMES_BY_ID.put(30, "minecraft:snowy_taiga");
		BIOMES_BY_ID.put(31, "minecraft:snowy_taiga_hills");
		BIOMES_BY_ID.put(32, "minecraft:giant_tree_taiga");
		BIOMES_BY_ID.put(33, "minecraft:giant_tree_taiga_hills");
		BIOMES_BY_ID.put(34, "minecraft:wooded_mountains");
		BIOMES_BY_ID.put(35, "minecraft:savanna");
		BIOMES_BY_ID.put(36, "minecraft:savanna_plateau");
		BIOMES_BY_ID.put(37, "minecraft:badlands");
		BIOMES_BY_ID.put(38, "minecraft:wooded_badlands_plateau");
		BIOMES_BY_ID.put(39, "minecraft:badlands_plateau");
		BIOMES_BY_ID.put(40, "minecraft:small_end_islands");
		BIOMES_BY_ID.put(41, "minecraft:end_midlands");
		BIOMES_BY_ID.put(42, "minecraft:end_highlands");
		BIOMES_BY_ID.put(43, "minecraft:end_barrens");
		BIOMES_BY_ID.put(44, "minecraft:warm_ocean");
		BIOMES_BY_ID.put(45, "minecraft:lukewarm_ocean");
		BIOMES_BY_ID.put(46, "minecraft:cold_ocean");
		BIOMES_BY_ID.put(47, "minecraft:deep_warm_ocean");
		BIOMES_BY_ID.put(48, "minecraft:deep_lukewarm_ocean");
		BIOMES_BY_ID.put(49, "minecraft:deep_cold_ocean");
		BIOMES_BY_ID.put(50, "minecraft:deep_frozen_ocean");
		BIOMES_BY_ID.put(127, "minecraft:the_void");
		BIOMES_BY_ID.put(129, "minecraft:sunflower_plains");
		BIOMES_BY_ID.put(130, "minecraft:desert_lakes");
		BIOMES_BY_ID.put(131, "minecraft:gravelly_mountains");
		BIOMES_BY_ID.put(132, "minecraft:flower_forest");
		BIOMES_BY_ID.put(133, "minecraft:taiga_mountains");
		BIOMES_BY_ID.put(134, "minecraft:swamp_hills");
		BIOMES_BY_ID.put(140, "minecraft:ice_spikes");
		BIOMES_BY_ID.put(149, "minecraft:modified_jungle");
		BIOMES_BY_ID.put(151, "minecraft:modified_jungle_edge");
		BIOMES_BY_ID.put(155, "minecraft:tall_birch_forest");
		BIOMES_BY_ID.put(156, "minecraft:tall_birch_hills");
		BIOMES_BY_ID.put(157, "minecraft:dark_forest_hills");
		BIOMES_BY_ID.put(158, "minecraft:snowy_taiga_mountains");
		BIOMES_BY_ID.put(160, "minecraft:giant_spruce_taiga");
		BIOMES_BY_ID.put(161, "minecraft:giant_spruce_taiga_hills");
		BIOMES_BY_ID.put(162, "minecraft:modified_gravelly_mountains");
		BIOMES_BY_ID.put(163, "minecraft:shattered_savanna");
		BIOMES_BY_ID.put(164, "minecraft:shattered_savanna_plateau");
		BIOMES_BY_ID.put(165, "minecraft:eroded_badlands");
		BIOMES_BY_ID.put(166, "minecraft:modified_wooded_badlands_plateau");
		BIOMES_BY_ID.put(167, "minecraft:modified_badlands_plateau");
		BIOMES_BY_ID.put(168, "minecraft:bamboo_jungle");
		BIOMES_BY_ID.put(169, "minecraft:bamboo_jungle_hills");
		BIOMES_BY_ID.put(170, "minecraft:soul_sand_valley");
		BIOMES_BY_ID.put(171, "minecraft:crimson_forest");
		BIOMES_BY_ID.put(172, "minecraft:warped_forest");
		BIOMES_BY_ID.put(173, "minecraft:basalt_deltas");
		BIOMES_BY_ID.put(174, "minecraft:dripstone_caves");
		BIOMES_BY_ID.put(175, "minecraft:lush_caves");
		BIOMES_BY_ID.put(177, "minecraft:meadow");
		BIOMES_BY_ID.put(178, "minecraft:grove");
		BIOMES_BY_ID.put(179, "minecraft:snowy_slopes");
		BIOMES_BY_ID.put(180, "minecraft:snowcapped_peaks");
		BIOMES_BY_ID.put(181, "minecraft:lofty_peaks");
		BIOMES_BY_ID.put(182, "minecraft:stony_peaks");
	}
}
