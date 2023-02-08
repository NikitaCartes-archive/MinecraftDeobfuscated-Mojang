/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record Brightness(int block, int sky) {
    public static final Codec<Integer> LIGHT_VALUE_CODEC = ExtraCodecs.intRange(0, 15);
    public static final Codec<Brightness> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)LIGHT_VALUE_CODEC.fieldOf("block")).forGetter(Brightness::block), ((MapCodec)LIGHT_VALUE_CODEC.fieldOf("sky")).forGetter(Brightness::sky)).apply((Applicative<Brightness, ?>)instance, Brightness::new));
    public static Brightness FULL_BRIGHT = new Brightness(15, 15);

    public int pack() {
        return this.block << 4 | this.sky << 20;
    }

    public static Brightness unpack(int i) {
        int j = i >> 4 & 0xFFFF;
        int k = i >> 20 & 0xFFFF;
        return new Brightness(j, k);
    }
}

