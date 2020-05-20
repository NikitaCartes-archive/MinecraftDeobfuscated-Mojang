package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

public class DiskConfiguration implements FeatureConfiguration {
	public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("state").forGetter(diskConfiguration -> diskConfiguration.state),
					Codec.INT.fieldOf("radius").withDefault(0).forGetter(diskConfiguration -> diskConfiguration.radius),
					Codec.INT.fieldOf("y_size").withDefault(0).forGetter(diskConfiguration -> diskConfiguration.ySize),
					BlockState.CODEC.listOf().fieldOf("targets").forGetter(diskConfiguration -> diskConfiguration.targets)
				)
				.apply(instance, DiskConfiguration::new)
	);
	public final BlockState state;
	public final int radius;
	public final int ySize;
	public final List<BlockState> targets;

	public DiskConfiguration(BlockState blockState, int i, int j, List<BlockState> list) {
		this.state = blockState;
		this.radius = i;
		this.ySize = j;
		this.targets = list;
	}
}
