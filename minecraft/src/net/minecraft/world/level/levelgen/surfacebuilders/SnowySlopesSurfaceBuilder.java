package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Blocks;

public class SnowySlopesSurfaceBuilder extends GroveSurfaceBuilder {
	private final NoiseMaterialSurfaceBuilder.SteepMaterial steepMaterial = new NoiseMaterialSurfaceBuilder.SteepMaterial(
		Blocks.STONE.defaultBlockState(), true, false, false, true
	);

	public SnowySlopesSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
	}

	@Nullable
	@Override
	protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
		return this.steepMaterial;
	}
}
