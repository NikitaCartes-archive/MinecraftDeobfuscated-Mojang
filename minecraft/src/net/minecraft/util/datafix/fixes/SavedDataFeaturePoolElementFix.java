package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SavedDataFeaturePoolElementFix extends DataFix {
	private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]");
	private static final Set<String> PIECE_TYPE = Sets.<String>newHashSet(
		"minecraft:jigsaw", "minecraft:nvi", "minecraft:pcp", "minecraft:bastionremnant", "minecraft:runtime"
	);
	private static final Set<String> FEATURES = Sets.<String>newHashSet("minecraft:tree", "minecraft:flower", "minecraft:block_pile", "minecraft:random_patch");

	public SavedDataFeaturePoolElementFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.writeFixAndRead(
			"SavedDataFeaturePoolElementFix",
			this.getInputSchema().getType(References.STRUCTURE_FEATURE),
			this.getOutputSchema().getType(References.STRUCTURE_FEATURE),
			SavedDataFeaturePoolElementFix::fixTag
		);
	}

	private static <T> Dynamic<T> fixTag(Dynamic<T> dynamic) {
		return dynamic.update("Children", SavedDataFeaturePoolElementFix::updateChildren);
	}

	private static <T> Dynamic<T> updateChildren(Dynamic<T> dynamic) {
		return (Dynamic<T>)dynamic.asStreamOpt().map(SavedDataFeaturePoolElementFix::updateChildren).map(dynamic::createList).result().orElse(dynamic);
	}

	private static Stream<? extends Dynamic<?>> updateChildren(Stream<? extends Dynamic<?>> stream) {
		return stream.map(
			dynamic -> {
				String string = dynamic.get("id").asString("");
				if (!PIECE_TYPE.contains(string)) {
					return dynamic;
				} else {
					OptionalDynamic<?> optionalDynamic = dynamic.get("pool_element");
					return !optionalDynamic.get("element_type").asString("").equals("minecraft:feature_pool_element")
						? dynamic
						: dynamic.update("pool_element", dynamicx -> dynamicx.update("feature", SavedDataFeaturePoolElementFix::fixFeature));
				}
			}
		);
	}

	private static <T> OptionalDynamic<T> get(Dynamic<T> dynamic, String... strings) {
		if (strings.length == 0) {
			throw new IllegalArgumentException("Missing path");
		} else {
			OptionalDynamic<T> optionalDynamic = dynamic.get(strings[0]);

			for (int i = 1; i < strings.length; i++) {
				String string = strings[i];
				Matcher matcher = INDEX_PATTERN.matcher(string);
				if (matcher.matches()) {
					int j = Integer.parseInt(matcher.group(1));
					List<? extends Dynamic<T>> list = optionalDynamic.asList(Function.identity());
					if (j >= 0 && j < list.size()) {
						optionalDynamic = new OptionalDynamic<>(dynamic.getOps(), DataResult.success((Dynamic<T>)list.get(j)));
					} else {
						optionalDynamic = new OptionalDynamic<>(dynamic.getOps(), DataResult.error(() -> "Missing id:" + j));
					}
				} else {
					optionalDynamic = optionalDynamic.get(string);
				}
			}

			return optionalDynamic;
		}
	}

	@VisibleForTesting
	protected static Dynamic<?> fixFeature(Dynamic<?> dynamic) {
		Optional<String> optional = getReplacement(
			get(dynamic, "type").asString(""),
			get(dynamic, "name").asString(""),
			get(dynamic, "config", "state_provider", "type").asString(""),
			get(dynamic, "config", "state_provider", "state", "Name").asString(""),
			get(dynamic, "config", "state_provider", "entries", "[0]", "data", "Name").asString(""),
			get(dynamic, "config", "foliage_placer", "type").asString(""),
			get(dynamic, "config", "leaves_provider", "state", "Name").asString("")
		);
		return optional.isPresent() ? dynamic.createString((String)optional.get()) : dynamic;
	}

	private static Optional<String> getReplacement(String string, String string2, String string3, String string4, String string5, String string6, String string7) {
		String string8;
		if (!string.isEmpty()) {
			string8 = string;
		} else {
			if (string2.isEmpty()) {
				return Optional.empty();
			}

			if ("minecraft:normal_tree".equals(string2)) {
				string8 = "minecraft:tree";
			} else {
				string8 = string2;
			}
		}

		if (FEATURES.contains(string8)) {
			if ("minecraft:random_patch".equals(string8)) {
				if ("minecraft:simple_state_provider".equals(string3)) {
					if ("minecraft:sweet_berry_bush".equals(string4)) {
						return Optional.of("minecraft:patch_berry_bush");
					}

					if ("minecraft:cactus".equals(string4)) {
						return Optional.of("minecraft:patch_cactus");
					}
				} else if ("minecraft:weighted_state_provider".equals(string3) && ("minecraft:grass".equals(string5) || "minecraft:fern".equals(string5))) {
					return Optional.of("minecraft:patch_taiga_grass");
				}
			} else if ("minecraft:block_pile".equals(string8)) {
				if (!"minecraft:simple_state_provider".equals(string3) && !"minecraft:rotated_block_provider".equals(string3)) {
					if ("minecraft:weighted_state_provider".equals(string3)) {
						if ("minecraft:packed_ice".equals(string5) || "minecraft:blue_ice".equals(string5)) {
							return Optional.of("minecraft:pile_ice");
						}

						if ("minecraft:jack_o_lantern".equals(string5) || "minecraft:pumpkin".equals(string5)) {
							return Optional.of("minecraft:pile_pumpkin");
						}
					}
				} else {
					if ("minecraft:hay_block".equals(string4)) {
						return Optional.of("minecraft:pile_hay");
					}

					if ("minecraft:melon".equals(string4)) {
						return Optional.of("minecraft:pile_melon");
					}

					if ("minecraft:snow".equals(string4)) {
						return Optional.of("minecraft:pile_snow");
					}
				}
			} else {
				if ("minecraft:flower".equals(string8)) {
					return Optional.of("minecraft:flower_plain");
				}

				if ("minecraft:tree".equals(string8)) {
					if ("minecraft:acacia_foliage_placer".equals(string6)) {
						return Optional.of("minecraft:acacia");
					}

					if ("minecraft:blob_foliage_placer".equals(string6) && "minecraft:oak_leaves".equals(string7)) {
						return Optional.of("minecraft:oak");
					}

					if ("minecraft:pine_foliage_placer".equals(string6)) {
						return Optional.of("minecraft:pine");
					}

					if ("minecraft:spruce_foliage_placer".equals(string6)) {
						return Optional.of("minecraft:spruce");
					}
				}
			}
		}

		return Optional.empty();
	}
}
