/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.BaseDiskFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature
extends BaseDiskFeature {
    public DiskReplaceFeature(Codec<DiskConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> featurePlaceContext) {
        if (!featurePlaceContext.level().getBlockState(featurePlaceContext.origin()).is(featurePlaceContext.config().canOriginReplace())) {
            return false;
        }
        return super.place(featurePlaceContext);
    }
}

