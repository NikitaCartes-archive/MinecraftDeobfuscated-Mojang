package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public record RandomPatchConfiguration() implements FeatureConfiguration {
	private final int tries;
	private final int xzSpread;
	private final int ySpread;
	private final Set<Block> allowedOn;
	private final Set<BlockState> disallowedOn;
	private final boolean onlyInAir;
	private final Supplier<ConfiguredFeature<?, ?>> feature;
	public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter(RandomPatchConfiguration::tries),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(RandomPatchConfiguration::xzSpread),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(RandomPatchConfiguration::ySpread),
					Registry.BLOCK.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("allowed_on").forGetter(RandomPatchConfiguration::allowedOn),
					BlockState.CODEC.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("disallowed_on").forGetter(RandomPatchConfiguration::disallowedOn),
					Codec.BOOL.fieldOf("only_in_air").forGetter(RandomPatchConfiguration::onlyInAir),
					ConfiguredFeature.CODEC.fieldOf("feature").forGetter(RandomPatchConfiguration::feature)
				)
				.apply(instance, RandomPatchConfiguration::new)
	);

	public RandomPatchConfiguration(int i, int j, int k, Set<Block> set, Set<BlockState> set2, boolean bl, Supplier<ConfiguredFeature<?, ?>> supplier) {
		this.tries = i;
		this.xzSpread = j;
		this.ySpread = k;
		this.allowedOn = set;
		this.disallowedOn = set2;
		this.onlyInAir = bl;
		this.feature = supplier;
	}
}
