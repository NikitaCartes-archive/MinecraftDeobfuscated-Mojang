/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class WeightedListInt
extends IntProvider {
    public static final Codec<WeightedListInt> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)SimpleWeightedRandomList.wrappedCodec(IntProvider.CODEC).fieldOf("distribution")).forGetter(weightedListInt -> weightedListInt.distribution)).apply((Applicative<WeightedListInt, ?>)instance, WeightedListInt::new));
    private final SimpleWeightedRandomList<IntProvider> distribution;
    private final int minValue;
    private final int maxValue;

    public WeightedListInt(SimpleWeightedRandomList<IntProvider> simpleWeightedRandomList) {
        this.distribution = simpleWeightedRandomList;
        List list = simpleWeightedRandomList.unwrap();
        int i = Integer.MAX_VALUE;
        int j = Integer.MIN_VALUE;
        for (WeightedEntry.Wrapper wrapper : list) {
            int k = ((IntProvider)wrapper.getData()).getMinValue();
            int l = ((IntProvider)wrapper.getData()).getMaxValue();
            i = Math.min(i, k);
            j = Math.max(j, l);
        }
        this.minValue = i;
        this.maxValue = j;
    }

    @Override
    public int sample(Random random) {
        return this.distribution.getRandomValue(random).orElseThrow(IllegalStateException::new).sample(random);
    }

    @Override
    public int getMinValue() {
        return this.minValue;
    }

    @Override
    public int getMaxValue() {
        return this.maxValue;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.WEIGHTED_LIST;
    }
}

