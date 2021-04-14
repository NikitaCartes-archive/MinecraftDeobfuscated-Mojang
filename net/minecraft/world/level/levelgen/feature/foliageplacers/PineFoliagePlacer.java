/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class PineFoliagePlacer
extends FoliagePlacer {
    public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> PineFoliagePlacer.foliagePlacerParts(instance).and(((MapCodec)IntProvider.codec(0, 24).fieldOf("height")).forGetter(pineFoliagePlacer -> pineFoliagePlacer.height)).apply((Applicative<PineFoliagePlacer, ?>)instance, PineFoliagePlacer::new));
    private final IntProvider height;

    public PineFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3) {
        super(intProvider, intProvider2);
        this.height = intProvider3;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.PINE_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int j, int k, int l) {
        int m = 0;
        for (int n = l; n >= l - j; --n) {
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, foliageAttachment.pos(), m, n, foliageAttachment.doubleTrunk());
            if (m >= 1 && n == l - j + 1) {
                --m;
                continue;
            }
            if (m >= k + foliageAttachment.radiusOffset()) continue;
            ++m;
        }
    }

    @Override
    public int foliageRadius(Random random, int i) {
        return super.foliageRadius(random, i) + random.nextInt(Math.max(i + 1, 1));
    }

    @Override
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return this.height.sample(random);
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
        return i == l && k == l && l > 0;
    }
}

