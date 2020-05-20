package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;

public class OreConfiguration implements FeatureConfiguration {
	public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					OreConfiguration.Predicates.CODEC.fieldOf("target").forGetter(oreConfiguration -> oreConfiguration.target),
					BlockState.CODEC.fieldOf("state").forGetter(oreConfiguration -> oreConfiguration.state),
					Codec.INT.fieldOf("size").withDefault(0).forGetter(oreConfiguration -> oreConfiguration.size)
				)
				.apply(instance, OreConfiguration::new)
	);
	public final OreConfiguration.Predicates target;
	public final int size;
	public final BlockState state;

	public OreConfiguration(OreConfiguration.Predicates predicates, BlockState blockState, int i) {
		this.size = i;
		this.state = blockState;
		this.target = predicates;
	}

	public static enum Predicates implements StringRepresentable {
		NATURAL_STONE(
			"natural_stone",
			blockState -> blockState == null
					? false
					: blockState.is(Blocks.STONE) || blockState.is(Blocks.GRANITE) || blockState.is(Blocks.DIORITE) || blockState.is(Blocks.ANDESITE)
		),
		NETHERRACK("netherrack", new BlockPredicate(Blocks.NETHERRACK)),
		NETHER_ORE_REPLACEABLES(
			"nether_ore_replaceables",
			blockState -> blockState == null ? false : blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.BASALT) || blockState.is(Blocks.BLACKSTONE)
		);

		public static final Codec<OreConfiguration.Predicates> CODEC = StringRepresentable.fromEnum(
			OreConfiguration.Predicates::values, OreConfiguration.Predicates::byName
		);
		private static final Map<String, OreConfiguration.Predicates> BY_NAME = (Map<String, OreConfiguration.Predicates>)Arrays.stream(values())
			.collect(Collectors.toMap(OreConfiguration.Predicates::getName, predicates -> predicates));
		private final String name;
		private final Predicate<BlockState> predicate;

		private Predicates(String string2, Predicate<BlockState> predicate) {
			this.name = string2;
			this.predicate = predicate;
		}

		public String getName() {
			return this.name;
		}

		public static OreConfiguration.Predicates byName(String string) {
			return (OreConfiguration.Predicates)BY_NAME.get(string);
		}

		public Predicate<BlockState> getPredicate() {
			return this.predicate;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
