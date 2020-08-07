package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.block.state.BlockState;

public class DiskConfiguration implements FeatureConfiguration {
	public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("state").forGetter(diskConfiguration -> diskConfiguration.state),
					UniformInt.codec(0, 4, 4).fieldOf("radius").forGetter(diskConfiguration -> diskConfiguration.radius),
					Codec.intRange(0, 4).fieldOf("half_height").forGetter(diskConfiguration -> diskConfiguration.halfHeight),
					BlockState.CODEC.listOf().fieldOf("targets").forGetter(diskConfiguration -> diskConfiguration.targets)
				)
				.apply(instance, DiskConfiguration::new)
	);
	public final BlockState state;
	public final UniformInt radius;
	public final int halfHeight;
	public final List<BlockState> targets;

	public DiskConfiguration(BlockState blockState, UniformInt uniformInt, int i, List<BlockState> list) {
		this.state = blockState;
		this.radius = uniformInt;
		this.halfHeight = i;
		this.targets = list;
	}
}
