package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;

public class StonyPeaksSurfaceBuilder extends StripedStoneSurfaceBuilder {
	private static final float STRIPE_MATERIAL_THRESHOLD = 0.025F;

	public StonyPeaksSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
	}

	@Override
	protected SurfaceBuilderBaseConfiguration getStripeMaterial(double d) {
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration;
		if (d < -0.025F) {
			surfaceBuilderBaseConfiguration = SurfaceBuilder.CONFIG_CALCITE;
		} else if (d < 0.0) {
			surfaceBuilderBaseConfiguration = SurfaceBuilder.CONFIG_ANDESITE;
		} else if (d < 0.025F) {
			surfaceBuilderBaseConfiguration = SurfaceBuilder.CONFIG_GRAVEL;
		} else {
			surfaceBuilderBaseConfiguration = SurfaceBuilder.CONFIG_GRANITE;
		}

		return surfaceBuilderBaseConfiguration;
	}
}
