/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BlobFoliagePlacer
extends FoliagePlacer {
    public static final Codec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> BlobFoliagePlacer.blobParts(instance).apply((Applicative)instance, BlobFoliagePlacer::new));
    protected final int height;

    protected static <P extends BlobFoliagePlacer> Products.P5<RecordCodecBuilder.Mu<P>, Integer, Integer, Integer, Integer, Integer> blobParts(RecordCodecBuilder.Instance<P> instance) {
        return BlobFoliagePlacer.foliagePlacerParts(instance).and(((MapCodec)Codec.INT.fieldOf("height")).forGetter(blobFoliagePlacer -> blobFoliagePlacer.height));
    }

    protected BlobFoliagePlacer(int i, int j, int k, int l, int m, FoliagePlacerType<?> foliagePlacerType) {
        super(i, j, k, l);
        this.height = m;
    }

    public BlobFoliagePlacer(int i, int j, int k, int l, int m) {
        this(i, j, k, l, m, FoliagePlacerType.BLOB_FOLIAGE_PLACER);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int j, int k, Set<BlockPos> set, int l, BoundingBox boundingBox) {
        for (int m = l; m >= l - j; --m) {
            int n = Math.max(k + foliageAttachment.radiusOffset() - 1 - m / 2, 0);
            this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), n, set, m, foliageAttachment.doubleTrunk(), boundingBox);
        }
    }

    @Override
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
        return i == l && k == l && (random.nextInt(2) == 0 || j == 0);
    }
}

