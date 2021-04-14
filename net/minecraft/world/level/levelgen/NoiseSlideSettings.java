/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public class NoiseSlideSettings {
    public static final Codec<NoiseSlideSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("target")).forGetter(NoiseSlideSettings::target), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("size")).forGetter(NoiseSlideSettings::size), ((MapCodec)Codec.INT.fieldOf("offset")).forGetter(NoiseSlideSettings::offset)).apply((Applicative<NoiseSlideSettings, ?>)instance, NoiseSlideSettings::new));
    private final int target;
    private final int size;
    private final int offset;

    public NoiseSlideSettings(int i, int j, int k) {
        this.target = i;
        this.size = j;
        this.offset = k;
    }

    public int target() {
        return this.target;
    }

    public int size() {
        return this.size;
    }

    public int offset() {
        return this.offset;
    }
}

