/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class WeightedListHeight
extends HeightProvider {
    public static final Codec<WeightedListHeight> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)SimpleWeightedRandomList.wrappedCodec(HeightProvider.CODEC).fieldOf("distribution")).forGetter(weightedListHeight -> weightedListHeight.distribution)).apply((Applicative<WeightedListHeight, ?>)instance, WeightedListHeight::new));
    private final SimpleWeightedRandomList<HeightProvider> distribution;

    public WeightedListHeight(SimpleWeightedRandomList<HeightProvider> simpleWeightedRandomList) {
        this.distribution = simpleWeightedRandomList;
    }

    @Override
    public int sample(Random random, WorldGenerationContext worldGenerationContext) {
        return this.distribution.getRandomValue(random).orElseThrow(IllegalStateException::new).sample(random, worldGenerationContext);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.WEIGHTED_LIST;
    }
}

