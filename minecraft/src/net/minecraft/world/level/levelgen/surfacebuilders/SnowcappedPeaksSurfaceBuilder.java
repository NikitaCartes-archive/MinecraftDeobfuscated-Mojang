package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SnowcappedPeaksSurfaceBuilder extends NoiseMaterialSurfaceBuilder {
	private final NoiseMaterialSurfaceBuilder.SteepMaterial steepMaterial = new NoiseMaterialSurfaceBuilder.SteepMaterial(
		Blocks.PACKED_ICE.defaultBlockState(), true, false, false, true
	);

	public SnowcappedPeaksSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
	}

	@Nullable
	@Override
	protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
		return this.steepMaterial;
	}

	@Override
	protected BlockState getTopMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
		BlockState blockState = this.getMaterial(0.5, i, j, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.ICE.defaultBlockState(), 0.0, 0.025);
		return this.getMaterial(0.0625, i, j, blockState, Blocks.PACKED_ICE.defaultBlockState(), 0.0, 0.2);
	}

	@Override
	protected BlockState getMidMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
		BlockState blockState = this.getMaterial(0.5, i, j, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.ICE.defaultBlockState(), -0.0625, 0.025);
		return this.getMaterial(0.0625, i, j, blockState, Blocks.PACKED_ICE.defaultBlockState(), -0.5, 0.2);
	}
}
