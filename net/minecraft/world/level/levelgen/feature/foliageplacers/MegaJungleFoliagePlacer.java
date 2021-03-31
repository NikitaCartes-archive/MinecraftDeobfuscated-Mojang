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

public class MegaJungleFoliagePlacer
extends FoliagePlacer {
    public static final Codec<MegaJungleFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> MegaJungleFoliagePlacer.foliagePlacerParts(instance).and(((MapCodec)Codec.intRange(0, 16).fieldOf("height")).forGetter(megaJungleFoliagePlacer -> megaJungleFoliagePlacer.height)).apply((Applicative<MegaJungleFoliagePlacer, ?>)instance, MegaJungleFoliagePlacer::new));
    protected final int height;

    public MegaJungleFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, int i) {
        super(intProvider, intProvider2);
        this.height = i;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int j, int k, int l) {
        int m = foliageAttachment.doubleTrunk() ? j : 1 + random.nextInt(2);
        for (int n = l; n >= l - m; --n) {
            int o = k + foliageAttachment.radiusOffset() + 1 - n;
            this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, foliageAttachment.pos(), o, n, foliageAttachment.doubleTrunk());
        }
    }

    @Override
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
        if (i + k >= 7) {
            return true;
        }
        return i * i + k * k > l * l;
    }
}

