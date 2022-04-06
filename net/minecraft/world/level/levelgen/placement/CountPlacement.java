/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public class CountPlacement
extends RepeatingPlacement {
    public static final Codec<CountPlacement> CODEC = ((MapCodec)IntProvider.codec(0, 256).fieldOf("count")).xmap(CountPlacement::new, countPlacement -> countPlacement.count).codec();
    private final IntProvider count;

    private CountPlacement(IntProvider intProvider) {
        this.count = intProvider;
    }

    public static CountPlacement of(IntProvider intProvider) {
        return new CountPlacement(intProvider);
    }

    public static CountPlacement of(int i) {
        return CountPlacement.of(ConstantInt.of(i));
    }

    @Override
    protected int count(RandomSource randomSource, BlockPos blockPos) {
        return this.count.sample(randomSource);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.COUNT;
    }
}

