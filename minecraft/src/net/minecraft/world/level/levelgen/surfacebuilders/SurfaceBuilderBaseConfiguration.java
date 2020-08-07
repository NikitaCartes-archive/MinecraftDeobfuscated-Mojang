package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public class SurfaceBuilderBaseConfiguration implements SurfaceBuilderConfiguration {
	public static final Codec<SurfaceBuilderBaseConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("top_material").forGetter(surfaceBuilderBaseConfiguration -> surfaceBuilderBaseConfiguration.topMaterial),
					BlockState.CODEC.fieldOf("under_material").forGetter(surfaceBuilderBaseConfiguration -> surfaceBuilderBaseConfiguration.underMaterial),
					BlockState.CODEC.fieldOf("underwater_material").forGetter(surfaceBuilderBaseConfiguration -> surfaceBuilderBaseConfiguration.underwaterMaterial)
				)
				.apply(instance, SurfaceBuilderBaseConfiguration::new)
	);
	private final BlockState topMaterial;
	private final BlockState underMaterial;
	private final BlockState underwaterMaterial;

	public SurfaceBuilderBaseConfiguration(BlockState blockState, BlockState blockState2, BlockState blockState3) {
		this.topMaterial = blockState;
		this.underMaterial = blockState2;
		this.underwaterMaterial = blockState3;
	}

	@Override
	public BlockState getTopMaterial() {
		return this.topMaterial;
	}

	@Override
	public BlockState getUnderMaterial() {
		return this.underMaterial;
	}

	public BlockState getUnderwaterMaterial() {
		return this.underwaterMaterial;
	}
}
