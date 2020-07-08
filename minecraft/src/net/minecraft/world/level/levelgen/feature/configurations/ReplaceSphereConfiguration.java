package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceSphereConfiguration implements FeatureConfiguration {
	public static final Codec<ReplaceSphereConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("target").forGetter(replaceSphereConfiguration -> replaceSphereConfiguration.targetState),
					BlockState.CODEC.fieldOf("state").forGetter(replaceSphereConfiguration -> replaceSphereConfiguration.replaceState),
					UniformInt.CODEC.fieldOf("radius").forGetter(replaceSphereConfiguration -> replaceSphereConfiguration.radius)
				)
				.apply(instance, ReplaceSphereConfiguration::new)
	);
	public final BlockState targetState;
	public final BlockState replaceState;
	private final UniformInt radius;

	public ReplaceSphereConfiguration(BlockState blockState, BlockState blockState2, UniformInt uniformInt) {
		this.targetState = blockState;
		this.replaceState = blockState2;
		this.radius = uniformInt;
	}

	public UniformInt radius() {
		return this.radius;
	}
}
