/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class SimpleParticleType
extends ParticleType<SimpleParticleType>
implements ParticleOptions {
    private static final ParticleOptions.Deserializer<SimpleParticleType> DESERIALIZER = new ParticleOptions.Deserializer<SimpleParticleType>(){

        @Override
        public SimpleParticleType fromCommand(ParticleType<SimpleParticleType> particleType, StringReader stringReader) {
            return (SimpleParticleType)particleType;
        }

        @Override
        public SimpleParticleType fromNetwork(ParticleType<SimpleParticleType> particleType, FriendlyByteBuf friendlyByteBuf) {
            return (SimpleParticleType)particleType;
        }

        @Override
        public /* synthetic */ ParticleOptions fromNetwork(ParticleType particleType, FriendlyByteBuf friendlyByteBuf) {
            return this.fromNetwork(particleType, friendlyByteBuf);
        }

        @Override
        public /* synthetic */ ParticleOptions fromCommand(ParticleType particleType, StringReader stringReader) throws CommandSyntaxException {
            return this.fromCommand(particleType, stringReader);
        }
    };
    private final Codec<SimpleParticleType> codec = Codec.unit(this::getType);

    protected SimpleParticleType(boolean bl) {
        super(bl, DESERIALIZER);
    }

    public SimpleParticleType getType() {
        return this;
    }

    @Override
    public Codec<SimpleParticleType> codec() {
        return this.codec;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this).toString();
    }

    public /* synthetic */ ParticleType getType() {
        return this.getType();
    }
}

