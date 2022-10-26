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
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DustParticleOptions
extends DustParticleOptionsBase {
    public static final Vector3f REDSTONE_PARTICLE_COLOR = Vec3.fromRGB24(0xFF0000).toVector3f();
    public static final DustParticleOptions REDSTONE = new DustParticleOptions(REDSTONE_PARTICLE_COLOR, 1.0f);
    public static final Codec<DustParticleOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.VECTOR3F.fieldOf("color")).forGetter(dustParticleOptions -> dustParticleOptions.color), ((MapCodec)Codec.FLOAT.fieldOf("scale")).forGetter(dustParticleOptions -> Float.valueOf(dustParticleOptions.scale))).apply((Applicative<DustParticleOptions, ?>)instance, DustParticleOptions::new));
    public static final ParticleOptions.Deserializer<DustParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustParticleOptions>(){

        @Override
        public DustParticleOptions fromCommand(ParticleType<DustParticleOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
            Vector3f vector3f = DustParticleOptionsBase.readVector3f(stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            return new DustParticleOptions(vector3f, f);
        }

        @Override
        public DustParticleOptions fromNetwork(ParticleType<DustParticleOptions> particleType, FriendlyByteBuf friendlyByteBuf) {
            return new DustParticleOptions(DustParticleOptionsBase.readVector3f(friendlyByteBuf), friendlyByteBuf.readFloat());
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

    public DustParticleOptions(Vector3f vector3f, float f) {
        super(vector3f, f);
    }

    public ParticleType<DustParticleOptions> getType() {
        return ParticleTypes.DUST;
    }
}

