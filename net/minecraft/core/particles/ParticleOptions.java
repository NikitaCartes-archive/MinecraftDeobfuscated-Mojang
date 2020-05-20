/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public interface ParticleOptions {
    public ParticleType<?> getType();

    public void writeToNetwork(FriendlyByteBuf var1);

    public String writeToString();

    @Deprecated
    public static interface Deserializer<T extends ParticleOptions> {
        public T fromCommand(ParticleType<T> var1, StringReader var2) throws CommandSyntaxException;

        public T fromNetwork(ParticleType<T> var1, FriendlyByteBuf var2);
    }
}

