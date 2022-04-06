package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record DiskConfiguration(BlockState state, IntProvider radius, int halfHeight, List<BlockState> targets, HolderSet<Block> canOriginReplace)
	implements FeatureConfiguration {
	public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("state").forGetter(DiskConfiguration::state),
					IntProvider.codec(0, 8).fieldOf("radius").forGetter(DiskConfiguration::radius),
					Codec.intRange(0, 4).fieldOf("half_height").forGetter(DiskConfiguration::halfHeight),
					BlockState.CODEC.listOf().fieldOf("targets").forGetter(DiskConfiguration::targets),
					RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("can_origin_replace").forGetter(diskConfiguration -> diskConfiguration.canOriginReplace)
				)
				.apply(instance, DiskConfiguration::new)
	);
}
