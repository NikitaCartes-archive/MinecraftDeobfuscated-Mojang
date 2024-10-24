package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class StructuresBecomeConfiguredFix extends DataFix {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<String, StructuresBecomeConfiguredFix.Conversion> CONVERSION_MAP = ImmutableMap.<String, StructuresBecomeConfiguredFix.Conversion>builder()
		.put(
			"mineshaft",
			StructuresBecomeConfiguredFix.Conversion.biomeMapped(
				Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"), "minecraft:mineshaft"
			)
		)
		.put(
			"shipwreck",
			StructuresBecomeConfiguredFix.Conversion.biomeMapped(
				Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck"
			)
		)
		.put(
			"ocean_ruin",
			StructuresBecomeConfiguredFix.Conversion.biomeMapped(
				Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"),
				"minecraft:ocean_ruin_cold"
			)
		)
		.put(
			"village",
			StructuresBecomeConfiguredFix.Conversion.biomeMapped(
				Map.of(
					List.of("minecraft:desert"),
					"minecraft:village_desert",
					List.of("minecraft:savanna"),
					"minecraft:village_savanna",
					List.of("minecraft:snowy_plains"),
					"minecraft:village_snowy",
					List.of("minecraft:taiga"),
					"minecraft:village_taiga"
				),
				"minecraft:village_plains"
			)
		)
		.put(
			"ruined_portal",
			StructuresBecomeConfiguredFix.Conversion.biomeMapped(
				Map.of(
					List.of("minecraft:desert"),
					"minecraft:ruined_portal_desert",
					List.of(
						"minecraft:badlands",
						"minecraft:eroded_badlands",
						"minecraft:wooded_badlands",
						"minecraft:windswept_hills",
						"minecraft:windswept_forest",
						"minecraft:windswept_gravelly_hills",
						"minecraft:savanna_plateau",
						"minecraft:windswept_savanna",
						"minecraft:stony_shore",
						"minecraft:meadow",
						"minecraft:frozen_peaks",
						"minecraft:jagged_peaks",
						"minecraft:stony_peaks",
						"minecraft:snowy_slopes"
					),
					"minecraft:ruined_portal_mountain",
					List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"),
					"minecraft:ruined_portal_jungle",
					List.of(
						"minecraft:deep_frozen_ocean",
						"minecraft:deep_cold_ocean",
						"minecraft:deep_ocean",
						"minecraft:deep_lukewarm_ocean",
						"minecraft:frozen_ocean",
						"minecraft:ocean",
						"minecraft:cold_ocean",
						"minecraft:lukewarm_ocean",
						"minecraft:warm_ocean"
					),
					"minecraft:ruined_portal_ocean"
				),
				"minecraft:ruined_portal"
			)
		)
		.put("pillager_outpost", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:pillager_outpost"))
		.put("mansion", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:mansion"))
		.put("jungle_pyramid", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:jungle_pyramid"))
		.put("desert_pyramid", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:desert_pyramid"))
		.put("igloo", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:igloo"))
		.put("swamp_hut", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:swamp_hut"))
		.put("stronghold", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:stronghold"))
		.put("monument", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:monument"))
		.put("fortress", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:fortress"))
		.put("endcity", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:end_city"))
		.put("buried_treasure", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:buried_treasure"))
		.put("nether_fossil", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:nether_fossil"))
		.put("bastion_remnant", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:bastion_remnant"))
		.build();

	public StructuresBecomeConfiguredFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.CHUNK);
		Type<?> type2 = this.getInputSchema().getType(References.CHUNK);
		return this.writeFixAndRead("StucturesToConfiguredStructures", type, type2, this::fix);
	}

	private Dynamic<?> fix(Dynamic<?> dynamic) {
		return dynamic.update(
			"structures",
			dynamic2 -> dynamic2.update("starts", dynamic2x -> this.updateStarts(dynamic2x, dynamic))
					.update("References", dynamic2x -> this.updateReferences(dynamic2x, dynamic))
		);
	}

	private Dynamic<?> updateStarts(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
		Map<? extends Dynamic<?>, ? extends Dynamic<?>> map = (Map<? extends Dynamic<?>, ? extends Dynamic<?>>)dynamic.getMapValues().result().orElse(Map.of());
		HashMap<Dynamic<?>, Dynamic<?>> hashMap = Maps.newHashMap();
		map.forEach((dynamic2x, dynamic3) -> {
			if (!dynamic3.get("id").asString("INVALID").equals("INVALID")) {
				Dynamic<?> dynamic4 = this.findUpdatedStructureType(dynamic2x, dynamic2);
				if (dynamic4 == null) {
					LOGGER.warn("Encountered unknown structure in datafixer: " + dynamic2x.asString("<missing key>"));
				} else {
					hashMap.computeIfAbsent(dynamic4, dynamic3x -> dynamic3.set("id", dynamic4));
				}
			}
		});
		return dynamic2.createMap(hashMap);
	}

	private Dynamic<?> updateReferences(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
		Map<? extends Dynamic<?>, ? extends Dynamic<?>> map = (Map<? extends Dynamic<?>, ? extends Dynamic<?>>)dynamic.getMapValues().result().orElse(Map.of());
		HashMap<Dynamic<?>, Dynamic<?>> hashMap = Maps.newHashMap();
		map.forEach(
			(dynamic2x, dynamic3) -> {
				if (dynamic3.asLongStream().count() != 0L) {
					Dynamic<?> dynamic4 = this.findUpdatedStructureType(dynamic2x, dynamic2);
					if (dynamic4 == null) {
						LOGGER.warn("Encountered unknown structure in datafixer: " + dynamic2x.asString("<missing key>"));
					} else {
						hashMap.compute(
							dynamic4,
							(dynamic2xx, dynamic3x) -> dynamic3x == null ? dynamic3 : dynamic3.createLongList(LongStream.concat(dynamic3x.asLongStream(), dynamic3.asLongStream()))
						);
					}
				}
			}
		);
		return dynamic2.createMap(hashMap);
	}

	@Nullable
	private Dynamic<?> findUpdatedStructureType(Dynamic<?> dynamic, Dynamic<?> dynamic2) {
		String string = dynamic.asString("UNKNOWN").toLowerCase(Locale.ROOT);
		StructuresBecomeConfiguredFix.Conversion conversion = (StructuresBecomeConfiguredFix.Conversion)CONVERSION_MAP.get(string);
		if (conversion == null) {
			return null;
		} else {
			String string2 = conversion.fallback;
			if (!conversion.biomeMapping().isEmpty()) {
				Optional<String> optional = this.guessConfiguration(dynamic2, conversion);
				if (optional.isPresent()) {
					string2 = (String)optional.get();
				}
			}

			return dynamic2.createString(string2);
		}
	}

	private Optional<String> guessConfiguration(Dynamic<?> dynamic, StructuresBecomeConfiguredFix.Conversion conversion) {
		Object2IntArrayMap<String> object2IntArrayMap = new Object2IntArrayMap<>();
		dynamic.get("sections")
			.asList(Function.identity())
			.forEach(dynamicx -> dynamicx.get("biomes").get("palette").asList(Function.identity()).forEach(dynamicxx -> {
					String string = (String)conversion.biomeMapping().get(dynamicxx.asString(""));
					if (string != null) {
						object2IntArrayMap.mergeInt(string, 1, Integer::sum);
					}
				}));
		return object2IntArrayMap.object2IntEntrySet()
			.stream()
			.max(Comparator.comparingInt(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry::getIntValue))
			.map(Entry::getKey);
	}

	static record Conversion(Map<String, String> biomeMapping, String fallback) {

		public static StructuresBecomeConfiguredFix.Conversion trivial(String string) {
			return new StructuresBecomeConfiguredFix.Conversion(Map.of(), string);
		}

		public static StructuresBecomeConfiguredFix.Conversion biomeMapped(Map<List<String>, String> map, String string) {
			return new StructuresBecomeConfiguredFix.Conversion(unpack(map), string);
		}

		private static Map<String, String> unpack(Map<List<String>, String> map) {
			Builder<String, String> builder = ImmutableMap.builder();

			for (Entry<List<String>, String> entry : map.entrySet()) {
				((List)entry.getKey()).forEach(string -> builder.put(string, (String)entry.getValue()));
			}

			return builder.build();
		}
	}
}
