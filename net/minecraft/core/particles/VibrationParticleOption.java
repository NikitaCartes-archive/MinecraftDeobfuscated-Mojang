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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;

public class VibrationParticleOption
implements ParticleOptions {
    public static final Codec<VibrationParticleOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)VibrationPath.CODEC.fieldOf("vibration")).forGetter(vibrationParticleOption -> vibrationParticleOption.vibrationPath)).apply((Applicative<VibrationParticleOption, ?>)instance, VibrationParticleOption::new));
    public static final ParticleOptions.Deserializer<VibrationParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<VibrationParticleOption>(){

        @Override
        public VibrationParticleOption fromCommand(ParticleType<VibrationParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            float f = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float g = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float h = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float i = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float j = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float k = (float)stringReader.readDouble();
            stringReader.expect(' ');
            int l = stringReader.readInt();
            BlockPos blockPos = new BlockPos(f, g, h);
            BlockPos blockPos2 = new BlockPos(i, j, k);
            return new VibrationParticleOption(new VibrationPath(blockPos, new BlockPositionSource(blockPos2), l));
        }

        @Override
        public VibrationParticleOption fromNetwork(ParticleType<VibrationParticleOption> particleType, FriendlyByteBuf friendlyByteBuf) {
            VibrationPath vibrationPath = VibrationPath.read(friendlyByteBuf);
            return new VibrationParticleOption(vibrationPath);
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
    private final VibrationPath vibrationPath;

    public VibrationParticleOption(VibrationPath vibrationPath) {
        this.vibrationPath = vibrationPath;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        VibrationPath.write(friendlyByteBuf, this.vibrationPath);
    }

    @Override
    public String writeToString() {
        BlockPos blockPos = this.vibrationPath.getOrigin();
        double d = blockPos.getX();
        double e = blockPos.getY();
        double f = blockPos.getZ();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %d", Registry.PARTICLE_TYPE.getKey(this.getType()), d, e, f, d, e, f, this.vibrationPath.getArrivalInTicks());
    }

    public ParticleType<VibrationParticleOption> getType() {
        return ParticleTypes.VIBRATION;
    }

    public VibrationPath getVibrationPath() {
        return this.vibrationPath;
    }
}

