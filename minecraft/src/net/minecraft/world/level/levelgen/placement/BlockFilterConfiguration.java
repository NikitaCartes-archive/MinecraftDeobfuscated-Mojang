package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class BlockFilterConfiguration implements DecoratorConfiguration {
	public static final Codec<BlockFilterConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter(blockFilterConfiguration -> blockFilterConfiguration.predicate))
				.apply(instance, BlockFilterConfiguration::new)
	);
	private final BlockPredicate predicate;

	public BlockFilterConfiguration(BlockPredicate blockPredicate) {
		this.predicate = blockPredicate;
	}

	public BlockPredicate predicate() {
		return this.predicate;
	}
}
