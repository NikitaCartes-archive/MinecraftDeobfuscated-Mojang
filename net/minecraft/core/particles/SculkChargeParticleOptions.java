/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;

public record SculkChargeParticleOptions(float roll) implements ParticleOptions
{
    public static final Codec<SculkChargeParticleOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("roll")).forGetter(sculkChargeParticleOptions -> Float.valueOf(sculkChargeParticleOptions.roll))).apply((Applicative<SculkChargeParticleOptions, ?>)instance, SculkChargeParticleOptions::new));
    public static final ParticleOptions.Deserializer<SculkChargeParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<SculkChargeParticleOptions>(){

        @Override
        public SculkChargeParticleOptions fromCommand(ParticleType<SculkChargeParticleOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
            float f = stringReader.readFloat();
            return new SculkChargeParticleOptions(f);
        }

        @Override
        public SculkChargeParticleOptions fromNetwork(ParticleType<SculkChargeParticleOptions> particleType, FriendlyByteBuf friendlyByteBuf) {
            return new SculkChargeParticleOptions(friendlyByteBuf.readFloat());
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

    public ParticleType<SculkChargeParticleOptions> getType() {
        return ParticleTypes.SCULK_CHARGE;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeFloat(this.roll);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), Float.valueOf(this.roll));
    }
}

