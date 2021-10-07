/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;

public class BlockColumnFeature
extends Feature<BlockColumnConfiguration> {
    public BlockColumnFeature(Codec<BlockColumnConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockColumnConfiguration> featurePlaceContext) {
        int l;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockColumnConfiguration blockColumnConfiguration = featurePlaceContext.config();
        Random random = featurePlaceContext.random();
        int i = blockColumnConfiguration.layers().size();
        int[] is = new int[i];
        int j = 0;
        for (int k = 0; k < i; ++k) {
            is[k] = blockColumnConfiguration.layers().get(k).height().sample(random);
            j += is[k];
        }
        if (j == 0) {
            return false;
        }
        BlockPos.MutableBlockPos mutableBlockPos = featurePlaceContext.origin().mutable();
        BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.mutable().move(blockColumnConfiguration.direction());
        for (l = 0; l < j; ++l) {
            if (!blockColumnConfiguration.allowedPlacement().test(worldGenLevel, mutableBlockPos2)) {
                BlockColumnFeature.truncate(is, j, l, blockColumnConfiguration.prioritizeTip());
                break;
            }
            mutableBlockPos2.move(blockColumnConfiguration.direction());
        }
        for (l = 0; l < i; ++l) {
            int m = is[l];
            if (m == 0) continue;
            BlockColumnConfiguration.Layer layer = blockColumnConfiguration.layers().get(l);
            for (int n = 0; n < m; ++n) {
                worldGenLevel.setBlock(mutableBlockPos, layer.state().getState(random, mutableBlockPos), 2);
                mutableBlockPos.move(blockColumnConfiguration.direction());
            }
        }
        return true;
    }

    private static void truncate(int[] is, int i, int j, boolean bl) {
        int q;
        int k = i - j;
        int l = bl ? 1 : -1;
        int m = bl ? 0 : is.length - 1;
        int n = bl ? is.length : -1;
        for (int o = m; o != n && k > 0; k -= q, o += l) {
            int p = is[o];
            q = Math.min(p, k);
            int n2 = o;
            is[n2] = is[n2] - q;
        }
    }
}

