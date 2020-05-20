/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class DepthAverageConfigation
implements DecoratorConfiguration {
    public static final Codec<DepthAverageConfigation> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("count")).forGetter(depthAverageConfigation -> depthAverageConfigation.count), ((MapCodec)Codec.INT.fieldOf("baseline")).forGetter(depthAverageConfigation -> depthAverageConfigation.baseline), ((MapCodec)Codec.INT.fieldOf("spread")).forGetter(depthAverageConfigation -> depthAverageConfigation.spread)).apply((Applicative<DepthAverageConfigation, ?>)instance, DepthAverageConfigation::new));
    public final int count;
    public final int baseline;
    public final int spread;

    public DepthAverageConfigation(int i, int j, int k) {
        this.count = i;
        this.baseline = j;
        this.spread = k;
    }
}

