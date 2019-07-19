/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public class DustParticleOptions
implements ParticleOptions {
    public static final DustParticleOptions REDSTONE = new DustParticleOptions(1.0f, 0.0f, 0.0f, 1.0f);
    public static final ParticleOptions.Deserializer<DustParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustParticleOptions>(){

        @Override
        public DustParticleOptions fromCommand(ParticleType<DustParticleOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            float f = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float g = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float h = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float i = (float)stringReader.readDouble();
            return new DustParticleOptions(f, g, h, i);
        }

        @Override
        public DustParticleOptions fromNetwork(ParticleType<DustParticleOptions> particleType, FriendlyByteBuf friendlyByteBuf) {
            return new DustParticleOptions(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
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
    private final float r;
    private final float g;
    private final float b;
    private final float scale;

    public DustParticleOptions(float f, float g, float h, float i) {
        this.r = f;
        this.g = g;
        this.b = h;
        this.scale = Mth.clamp(i, 0.01f, 4.0f);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeFloat(this.r);
        friendlyByteBuf.writeFloat(this.g);
        friendlyByteBuf.writeFloat(this.b);
        friendlyByteBuf.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), Float.valueOf(this.r), Float.valueOf(this.g), Float.valueOf(this.b), Float.valueOf(this.scale));
    }

    public ParticleType<DustParticleOptions> getType() {
        return ParticleTypes.DUST;
    }

    @Environment(value=EnvType.CLIENT)
    public float getR() {
        return this.r;
    }

    @Environment(value=EnvType.CLIENT)
    public float getG() {
        return this.g;
    }

    @Environment(value=EnvType.CLIENT)
    public float getB() {
        return this.b;
    }

    @Environment(value=EnvType.CLIENT)
    public float getScale() {
        return this.scale;
    }
}

