package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicLike;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class WorldGenSettingsFix extends DataFix {
	private static final ImmutableMap<String, WorldGenSettingsFix.StructureFeatureConfiguration> DEFAULTS = ImmutableMap.<String, WorldGenSettingsFix.StructureFeatureConfiguration>builder()
		.put("minecraft:village", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 10387312))
		.put("minecraft:desert_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357617))
		.put("minecraft:igloo", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357618))
		.put("minecraft:jungle_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357619))
		.put("minecraft:swamp_hut", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357620))
		.put("minecraft:pillager_outpost", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 165745296))
		.put("minecraft:monument", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 5, 10387313))
		.put("minecraft:endcity", new WorldGenSettingsFix.StructureFeatureConfiguration(20, 11, 10387313))
		.put("minecraft:mansion", new WorldGenSettingsFix.StructureFeatureConfiguration(80, 20, 10387319))
		.build();

	public WorldGenSettingsFix(Schema schema) {
		super(schema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"WorldGenSettings building",
			this.getInputSchema().getType(References.WORLD_GEN_SETTINGS),
			typed -> typed.update(DSL.remainderFinder(), WorldGenSettingsFix::fix)
		);
	}

	private static <T> Dynamic<T> noise(long l, DynamicLike<T> dynamicLike, Dynamic<T> dynamic, Dynamic<T> dynamic2) {
		return dynamicLike.createMap(
			ImmutableMap.of(
				dynamicLike.createString("type"),
				dynamicLike.createString("minecraft:noise"),
				dynamicLike.createString("biome_source"),
				dynamic2,
				dynamicLike.createString("seed"),
				dynamicLike.createLong(l),
				dynamicLike.createString("settings"),
				dynamic
			)
		);
	}

	private static <T> Dynamic<T> vanillaBiomeSource(Dynamic<T> dynamic, long l, boolean bl, boolean bl2) {
		Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.<Dynamic<T>, Dynamic<T>>builder()
			.put(dynamic.createString("type"), dynamic.createString("minecraft:vanilla_layered"))
			.put(dynamic.createString("seed"), dynamic.createLong(l))
			.put(dynamic.createString("large_biomes"), dynamic.createBoolean(bl2));
		if (bl) {
			builder.put(dynamic.createString("legacy_biome_init_layer"), dynamic.createBoolean(bl));
		}

		return dynamic.createMap(builder.build());
	}

	private static <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		DynamicOps<T> dynamicOps = dynamic.getOps();
		long l = dynamic.get("RandomSeed").asLong(0L);
		Optional<String> optional = dynamic.get("generatorName").asString().<String>map(string -> string.toLowerCase(Locale.ROOT)).result();
		Optional<String> optional2 = (Optional<String>)dynamic.get("legacy_custom_options")
			.asString()
			.result()
			.map(Optional::of)
			.orElseGet(() -> optional.equals(Optional.of("customized")) ? dynamic.get("generatorOptions").asString().result() : Optional.empty());
		boolean bl = false;
		Dynamic<T> dynamic2;
		if (optional.equals(Optional.of("customized"))) {
			dynamic2 = defaultOverworld(dynamic, l);
		} else if (!optional.isPresent()) {
			dynamic2 = defaultOverworld(dynamic, l);
		} else {
			String bl6 = (String)optional.get();
			switch (bl6) {
				case "flat":
					OptionalDynamic<T> optionalDynamic = dynamic.get("generatorOptions");
					Map<Dynamic<T>, Dynamic<T>> map = fixFlatStructures(dynamicOps, optionalDynamic);
					dynamic2 = dynamic.createMap(
						ImmutableMap.of(
							dynamic.createString("type"),
							dynamic.createString("minecraft:flat"),
							dynamic.createString("settings"),
							dynamic.createMap(
								ImmutableMap.of(
									dynamic.createString("structures"),
									dynamic.createMap(map),
									dynamic.createString("layers"),
									(Dynamic<?>)optionalDynamic.get("layers")
										.result()
										.orElseGet(
											() -> dynamic.createList(
													Stream.of(
														dynamic.createMap(
															ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(1), dynamic.createString("block"), dynamic.createString("minecraft:bedrock"))
														),
														dynamic.createMap(
															ImmutableMap.of(dynamic.createString("height"), dynamic.createInt(2), dynamic.createString("block"), dynamic.createString("minecraft:dirt"))
														),
														dynamic.createMap(
															ImmutableMap.of(
																dynamic.createString("height"), dynamic.createInt(1), dynamic.createString("block"), dynamic.createString("minecraft:grass_block")
															)
														)
													)
												)
										),
									dynamic.createString("biome"),
									dynamic.createString(optionalDynamic.get("biome").asString("minecraft:plains"))
								)
							)
						)
					);
					break;
				case "debug_all_block_states":
					dynamic2 = dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:debug")));
					break;
				case "buffet":
					OptionalDynamic<T> optionalDynamic2 = dynamic.get("generatorOptions");
					OptionalDynamic<?> optionalDynamic3 = optionalDynamic2.get("chunk_generator");
					Optional<String> optional3 = optionalDynamic3.get("type").asString().result();
					Dynamic<T> dynamic3;
					if (Objects.equals(optional3, Optional.of("minecraft:caves"))) {
						dynamic3 = dynamic.createString("minecraft:caves");
						bl = true;
					} else if (Objects.equals(optional3, Optional.of("minecraft:floating_islands"))) {
						dynamic3 = dynamic.createString("minecraft:floating_islands");
					} else {
						dynamic3 = dynamic.createString("minecraft:overworld");
					}

					Dynamic<T> dynamic4 = (Dynamic<T>)optionalDynamic2.get("biome_source")
						.result()
						.orElseGet(() -> dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:fixed"))));
					Dynamic<T> dynamic5;
					if (dynamic4.get("type").asString().result().equals(Optional.of("minecraft:fixed"))) {
						String string = (String)dynamic4.get("options")
							.get("biomes")
							.asStream()
							.findFirst()
							.flatMap(dynamicx -> dynamicx.asString().result())
							.orElse("minecraft:ocean");
						dynamic5 = dynamic4.remove("options").set("biome", dynamic.createString(string));
					} else {
						dynamic5 = dynamic4;
					}

					dynamic2 = noise(l, dynamic, dynamic3, dynamic5);
					break;
				default:
					boolean bl2 = ((String)optional.get()).equals("default");
					boolean bl3 = ((String)optional.get()).equals("default_1_1") || bl2 && dynamic.get("generatorVersion").asInt(0) == 0;
					boolean bl4 = ((String)optional.get()).equals("amplified");
					boolean bl5 = ((String)optional.get()).equals("largebiomes");
					dynamic2 = noise(l, dynamic, dynamic.createString(bl4 ? "minecraft:amplified" : "minecraft:overworld"), vanillaBiomeSource(dynamic, l, bl3, bl5));
			}
		}

		boolean bl6 = dynamic.get("MapFeatures").asBoolean(true);
		boolean bl7 = dynamic.get("BonusChest").asBoolean(false);
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("seed"), dynamicOps.createLong(l));
		builder.put(dynamicOps.createString("generate_features"), dynamicOps.createBoolean(bl6));
		builder.put(dynamicOps.createString("bonus_chest"), dynamicOps.createBoolean(bl7));
		builder.put(dynamicOps.createString("dimensions"), vanillaLevels(dynamic, l, dynamic2, bl));
		optional2.ifPresent(string -> builder.put(dynamicOps.createString("legacy_custom_options"), dynamicOps.createString(string)));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
	}

	protected static <T> Dynamic<T> defaultOverworld(Dynamic<T> dynamic, long l) {
		return noise(l, dynamic, dynamic.createString("minecraft:overworld"), vanillaBiomeSource(dynamic, l, false, false));
	}

	protected static <T> T vanillaLevels(Dynamic<T> dynamic, long l, Dynamic<T> dynamic2, boolean bl) {
		DynamicOps<T> dynamicOps = dynamic.getOps();
		return dynamicOps.createMap(
			ImmutableMap.of(
				dynamicOps.createString("minecraft:overworld"),
				dynamicOps.createMap(
					ImmutableMap.of(
						dynamicOps.createString("type"),
						dynamicOps.createString("minecraft:overworld" + (bl ? "_caves" : "")),
						dynamicOps.createString("generator"),
						dynamic2.getValue()
					)
				),
				dynamicOps.createString("minecraft:the_nether"),
				dynamicOps.createMap(
					ImmutableMap.of(
						dynamicOps.createString("type"),
						dynamicOps.createString("minecraft:the_nether"),
						dynamicOps.createString("generator"),
						noise(
								l,
								dynamic,
								dynamic.createString("minecraft:nether"),
								dynamic.createMap(
									ImmutableMap.of(
										dynamic.createString("type"),
										dynamic.createString("minecraft:multi_noise"),
										dynamic.createString("seed"),
										dynamic.createLong(l),
										dynamic.createString("preset"),
										dynamic.createString("minecraft:nether")
									)
								)
							)
							.getValue()
					)
				),
				dynamicOps.createString("minecraft:the_end"),
				dynamicOps.createMap(
					ImmutableMap.of(
						dynamicOps.createString("type"),
						dynamicOps.createString("minecraft:the_end"),
						dynamicOps.createString("generator"),
						noise(
								l,
								dynamic,
								dynamic.createString("minecraft:end"),
								dynamic.createMap(
									ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:the_end"), dynamic.createString("seed"), dynamic.createLong(l))
								)
							)
							.getValue()
					)
				)
			)
		);
	}

	private static <T> Map<Dynamic<T>, Dynamic<T>> fixFlatStructures(DynamicOps<T> dynamicOps, OptionalDynamic<T> optionalDynamic) {
		MutableInt mutableInt = new MutableInt(32);
		MutableInt mutableInt2 = new MutableInt(3);
		MutableInt mutableInt3 = new MutableInt(128);
		MutableBoolean mutableBoolean = new MutableBoolean(false);
		Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> map = Maps.<String, WorldGenSettingsFix.StructureFeatureConfiguration>newHashMap();
		if (!optionalDynamic.result().isPresent()) {
			mutableBoolean.setTrue();
			map.put("minecraft:village", DEFAULTS.get("minecraft:village"));
		}

		optionalDynamic.get("structures")
			.flatMap(Dynamic::getMapValues)
			.result()
			.ifPresent(
				map2 -> map2.forEach(
						(dynamic, dynamic2) -> dynamic2.getMapValues()
								.result()
								.ifPresent(
									map2x -> map2x.forEach(
											(dynamic2x, dynamic3) -> {
												String string = dynamic.asString("");
												String string2 = dynamic2x.asString("");
												String string3 = dynamic3.asString("");
												if ("stronghold".equals(string)) {
													mutableBoolean.setTrue();
													switch (string2) {
														case "distance":
															mutableInt.setValue(getInt(string3, mutableInt.getValue(), 1));
															return;
														case "spread":
															mutableInt2.setValue(getInt(string3, mutableInt2.getValue(), 1));
															return;
														case "count":
															mutableInt3.setValue(getInt(string3, mutableInt3.getValue(), 1));
															return;
													}
												} else {
													switch (string2) {
														case "distance":
															switch (string) {
																case "village":
																	setSpacing(map, "minecraft:village", string3, 9);
																	return;
																case "biome_1":
																	setSpacing(map, "minecraft:desert_pyramid", string3, 9);
																	setSpacing(map, "minecraft:igloo", string3, 9);
																	setSpacing(map, "minecraft:jungle_pyramid", string3, 9);
																	setSpacing(map, "minecraft:swamp_hut", string3, 9);
																	setSpacing(map, "minecraft:pillager_outpost", string3, 9);
																	return;
																case "endcity":
																	setSpacing(map, "minecraft:endcity", string3, 1);
																	return;
																case "mansion":
																	setSpacing(map, "minecraft:mansion", string3, 1);
																	return;
																default:
																	return;
															}
														case "separation":
															if ("oceanmonument".equals(string)) {
																WorldGenSettingsFix.StructureFeatureConfiguration structureFeatureConfiguration = (WorldGenSettingsFix.StructureFeatureConfiguration)map.getOrDefault(
																	"minecraft:monument", DEFAULTS.get("minecraft:monument")
																);
																int i = getInt(string3, structureFeatureConfiguration.separation, 1);
																map.put(
																	"minecraft:monument",
																	new WorldGenSettingsFix.StructureFeatureConfiguration(i, structureFeatureConfiguration.separation, structureFeatureConfiguration.salt)
																);
															}

															return;
														case "spacing":
															if ("oceanmonument".equals(string)) {
																setSpacing(map, "minecraft:monument", string3, 1);
															}

															return;
													}
												}
											}
										)
								)
					)
			);
		Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.builder();
		builder.put(
			optionalDynamic.createString("structures"),
			optionalDynamic.createMap(
				(Map<? extends Dynamic<?>, ? extends Dynamic<?>>)map.entrySet()
					.stream()
					.collect(
						Collectors.toMap(
							entry -> optionalDynamic.createString((String)entry.getKey()),
							entry -> ((WorldGenSettingsFix.StructureFeatureConfiguration)entry.getValue()).serialize(dynamicOps)
						)
					)
			)
		);
		if (mutableBoolean.isTrue()) {
			builder.put(
				optionalDynamic.createString("stronghold"),
				optionalDynamic.createMap(
					ImmutableMap.of(
						optionalDynamic.createString("distance"),
						optionalDynamic.createInt(mutableInt.getValue()),
						optionalDynamic.createString("spread"),
						optionalDynamic.createInt(mutableInt2.getValue()),
						optionalDynamic.createString("count"),
						optionalDynamic.createInt(mutableInt3.getValue())
					)
				)
			);
		}

		return builder.build();
	}

	private static int getInt(String string, int i) {
		return NumberUtils.toInt(string, i);
	}

	private static int getInt(String string, int i, int j) {
		return Math.max(j, getInt(string, i));
	}

	private static void setSpacing(Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> map, String string, String string2, int i) {
		WorldGenSettingsFix.StructureFeatureConfiguration structureFeatureConfiguration = (WorldGenSettingsFix.StructureFeatureConfiguration)map.getOrDefault(
			string, DEFAULTS.get(string)
		);
		int j = getInt(string2, structureFeatureConfiguration.spacing, i);
		map.put(string, new WorldGenSettingsFix.StructureFeatureConfiguration(j, structureFeatureConfiguration.separation, structureFeatureConfiguration.salt));
	}

	static final class StructureFeatureConfiguration {
		public static final Codec<WorldGenSettingsFix.StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("spacing").forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.spacing),
						Codec.INT.fieldOf("separation").forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.separation),
						Codec.INT.fieldOf("salt").forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.salt)
					)
					.apply(instance, WorldGenSettingsFix.StructureFeatureConfiguration::new)
		);
		private final int spacing;
		private final int separation;
		private final int salt;

		public StructureFeatureConfiguration(int i, int j, int k) {
			this.spacing = i;
			this.separation = j;
			this.salt = k;
		}

		public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
			return new Dynamic<>(dynamicOps, (T)CODEC.encodeStart(dynamicOps, this).result().orElse(dynamicOps.emptyMap()));
		}
	}
}
