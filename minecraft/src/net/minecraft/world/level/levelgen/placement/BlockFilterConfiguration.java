package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public record BlockFilterConfiguration() implements DecoratorConfiguration {
	private final List<Block> allowed;
	private final List<Block> disallowed;
	private final BlockPos offset;
	public static final Codec<BlockFilterConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Registry.BLOCK.listOf().optionalFieldOf("allowed", List.of()).forGetter(BlockFilterConfiguration::allowed),
					Registry.BLOCK.listOf().optionalFieldOf("disallowed", List.of()).forGetter(BlockFilterConfiguration::disallowed),
					BlockPos.CODEC.fieldOf("offset").forGetter(BlockFilterConfiguration::offset)
				)
				.apply(instance, BlockFilterConfiguration::new)
	);

	public BlockFilterConfiguration(List<Block> list, List<Block> list2, BlockPos blockPos) {
		this.allowed = list;
		this.disallowed = list2;
		this.offset = blockPos;
	}
}
