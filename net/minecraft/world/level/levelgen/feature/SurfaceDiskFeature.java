/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.BaseDiskFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class SurfaceDiskFeature
extends BaseDiskFeature {
    public SurfaceDiskFeature(Codec<DiskConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> featurePlaceContext) {
        if (!featurePlaceContext.level().getBlockState(featurePlaceContext.origin().below()).is(featurePlaceContext.config().canOriginReplace())) {
            return false;
        }
        return super.place(featurePlaceContext);
    }

    @Override
    protected boolean placeColumn(DiskConfiguration diskConfiguration, WorldGenLevel worldGenLevel, int i, int j, BlockPos.MutableBlockPos mutableBlockPos) {
        if (!worldGenLevel.isEmptyBlock(mutableBlockPos.setY(i + 1))) {
            return false;
        }
        for (int k = i; k > j; --k) {
            BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos.setY(k));
            if (this.matchesTargetBlock(diskConfiguration, blockState)) {
                worldGenLevel.setBlock(mutableBlockPos, diskConfiguration.state(), 2);
                this.markAboveForPostProcessing(worldGenLevel, mutableBlockPos);
                return true;
            }
            if (blockState.isAir()) continue;
            return false;
        }
        return false;
    }
}

