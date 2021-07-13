package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GroveSurfaceBuilder extends NoiseMaterialSurfaceBuilder {
	public GroveSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
	}

	@Nullable
	@Override
	protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
		return null;
	}

	@Override
	protected BlockState getTopMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
		return this.getMaterial(0.1, i, j, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.POWDER_SNOW.defaultBlockState(), 0.35, 0.6);
	}

	@Override
	protected BlockState getMidMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
		return this.getMaterial(0.1, i, j, Blocks.DIRT.defaultBlockState(), Blocks.POWDER_SNOW.defaultBlockState(), 0.45, 0.58);
	}
}
