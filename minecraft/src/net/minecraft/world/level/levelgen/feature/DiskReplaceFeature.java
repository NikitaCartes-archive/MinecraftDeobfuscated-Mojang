package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature extends BaseDiskFeature {
	public DiskReplaceFeature(Codec<DiskConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<DiskConfiguration> featurePlaceContext) {
		return !featurePlaceContext.level().getFluidState(featurePlaceContext.origin()).is(FluidTags.WATER) ? false : super.place(featurePlaceContext);
	}
}
