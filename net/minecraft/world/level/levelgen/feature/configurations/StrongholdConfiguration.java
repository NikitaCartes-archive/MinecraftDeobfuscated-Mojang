/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class StrongholdConfiguration {
    public static final Codec<StrongholdConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("distance")).forGetter(StrongholdConfiguration::distance), ((MapCodec)Codec.INT.fieldOf("spread")).forGetter(StrongholdConfiguration::spread), ((MapCodec)Codec.INT.fieldOf("count")).forGetter(StrongholdConfiguration::count)).apply((Applicative<StrongholdConfiguration, ?>)instance, StrongholdConfiguration::new));
    private final int distance;
    private final int spread;
    private final int count;

    public StrongholdConfiguration(int i, int j, int k) {
        this.distance = i;
        this.spread = j;
        this.count = k;
    }

    public int distance() {
        return this.distance;
    }

    public int spread() {
        return this.spread;
    }

    public int count() {
        return this.count;
    }
}

