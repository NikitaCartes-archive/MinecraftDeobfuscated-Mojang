package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;

public class SpringConfiguration implements FeatureConfiguration {
	public static final Codec<SpringConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					FluidState.CODEC.fieldOf("state").forGetter(springConfiguration -> springConfiguration.state),
					Codec.BOOL.fieldOf("requires_block_below").orElse(true).forGetter(springConfiguration -> springConfiguration.requiresBlockBelow),
					Codec.INT.fieldOf("rock_count").orElse(4).forGetter(springConfiguration -> springConfiguration.rockCount),
					Codec.INT.fieldOf("hole_count").orElse(1).forGetter(springConfiguration -> springConfiguration.holeCount),
					Registry.BLOCK
						.listOf()
						.fieldOf("valid_blocks")
						.xmap(ImmutableSet::copyOf, ImmutableList::copyOf)
						.forGetter(springConfiguration -> springConfiguration.validBlocks)
				)
				.apply(instance, SpringConfiguration::new)
	);
	public final FluidState state;
	public final boolean requiresBlockBelow;
	public final int rockCount;
	public final int holeCount;
	public final Set<Block> validBlocks;

	public SpringConfiguration(FluidState fluidState, boolean bl, int i, int j, Set<Block> set) {
		this.state = fluidState;
		this.requiresBlockBelow = bl;
		this.rockCount = i;
		this.holeCount = j;
		this.validBlocks = set;
	}
}
