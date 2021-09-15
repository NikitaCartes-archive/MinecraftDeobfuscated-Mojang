package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public record DiskConfiguration() implements FeatureConfiguration {
	private final BlockState state;
	private final IntProvider radius;
	private final int halfHeight;
	private final List<BlockState> targets;
	public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("state").forGetter(DiskConfiguration::state),
					IntProvider.codec(0, 8).fieldOf("radius").forGetter(DiskConfiguration::radius),
					Codec.intRange(0, 4).fieldOf("half_height").forGetter(DiskConfiguration::halfHeight),
					BlockState.CODEC.listOf().fieldOf("targets").forGetter(DiskConfiguration::targets)
				)
				.apply(instance, DiskConfiguration::new)
	);

	public DiskConfiguration(BlockState blockState, IntProvider intProvider, int i, List<BlockState> list) {
		this.state = blockState;
		this.radius = intProvider;
		this.halfHeight = i;
		this.targets = list;
	}
}
