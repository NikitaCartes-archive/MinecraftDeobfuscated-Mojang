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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class DustColorTransitionOptions
extends DustParticleOptionsBase {
    public static final Vec3 SCULK_PARTICLE_COLOR = Vec3.fromRGB24(3790560);
    public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(SCULK_PARTICLE_COLOR, DustParticleOptions.REDSTONE_PARTICLE_COLOR, 1.0f);
    public static final Codec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Vec3.CODEC.fieldOf("fromColor")).forGetter(dustColorTransitionOptions -> dustColorTransitionOptions.color), ((MapCodec)Vec3.CODEC.fieldOf("toColor")).forGetter(dustColorTransitionOptions -> dustColorTransitionOptions.toColor), ((MapCodec)Codec.FLOAT.fieldOf("scale")).forGetter(dustColorTransitionOptions -> Float.valueOf(dustColorTransitionOptions.scale))).apply((Applicative<DustColorTransitionOptions, ?>)instance, DustColorTransitionOptions::new));
    public static final ParticleOptions.Deserializer<DustColorTransitionOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustColorTransitionOptions>(){

        @Override
        public DustColorTransitionOptions fromCommand(ParticleType<DustColorTransitionOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
            Vec3 vec3 = DustParticleOptionsBase.readVec3(stringReader);
            stringReader.expect(' ');
            float f = (float)stringReader.readDouble();
            Vec3 vec32 = DustParticleOptionsBase.readVec3(stringReader);
            return new DustColorTransitionOptions(vec3, vec32, f);
        }

        @Override
        public DustColorTransitionOptions fromNetwork(ParticleType<DustColorTransitionOptions> particleType, FriendlyByteBuf friendlyByteBuf) {
            return new DustColorTransitionOptions(new Vec3(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat()), new Vec3(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat()), friendlyByteBuf.readFloat());
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
    private final Vec3 toColor;

    public DustColorTransitionOptions(Vec3 vec3, Vec3 vec32, float f) {
        super(vec3, f);
        this.toColor = vec32;
    }

    @Environment(value=EnvType.CLIENT)
    public Vec3 getFromColor() {
        return this.color;
    }

    @Environment(value=EnvType.CLIENT)
    public Vec3 getToColor() {
        return this.toColor;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        super.writeToNetwork(friendlyByteBuf);
        friendlyByteBuf.writeDouble(this.color.x);
        friendlyByteBuf.writeDouble(this.color.y);
        friendlyByteBuf.writeDouble(this.color.z);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), this.color.x, this.color.y, this.color.z, Float.valueOf(this.scale), this.toColor.x, this.toColor.y, this.toColor.z);
    }

    public ParticleType<DustColorTransitionOptions> getType() {
        return ParticleTypes.DUST_COLOR_TRANSITION;
    }
}

