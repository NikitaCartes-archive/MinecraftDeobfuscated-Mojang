package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public class DiskConfiguration implements FeatureConfiguration {
	public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("state").forGetter(diskConfiguration -> diskConfiguration.state),
					IntProvider.codec(0, 8).fieldOf("radius").forGetter(diskConfiguration -> diskConfiguration.radius),
					Codec.intRange(0, 4).fieldOf("half_height").forGetter(diskConfiguration -> diskConfiguration.halfHeight),
					BlockState.CODEC.listOf().fieldOf("targets").forGetter(diskConfiguration -> diskConfiguration.targets)
				)
				.apply(instance, DiskConfiguration::new)
	);
	public final BlockState state;
	public final IntProvider radius;
	public final int halfHeight;
	public final List<BlockState> targets;

	public DiskConfiguration(BlockState blockState, IntProvider intProvider, int i, List<BlockState> list) {
		this.state = blockState;
		this.radius = intProvider;
		this.halfHeight = i;
		this.targets = list;
	}
}
