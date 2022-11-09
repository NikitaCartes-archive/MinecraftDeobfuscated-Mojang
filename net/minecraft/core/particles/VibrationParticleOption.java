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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;

public class VibrationParticleOption
implements ParticleOptions {
    public static final Codec<VibrationParticleOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)PositionSource.CODEC.fieldOf("destination")).forGetter(vibrationParticleOption -> vibrationParticleOption.destination), ((MapCodec)Codec.INT.fieldOf("arrival_in_ticks")).forGetter(vibrationParticleOption -> vibrationParticleOption.arrivalInTicks)).apply((Applicative<VibrationParticleOption, ?>)instance, VibrationParticleOption::new));
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
            int i = stringReader.readInt();
            BlockPos blockPos = new BlockPos(f, g, h);
            return new VibrationParticleOption(new BlockPositionSource(blockPos), i);
        }

        @Override
        public VibrationParticleOption fromNetwork(ParticleType<VibrationParticleOption> particleType, FriendlyByteBuf friendlyByteBuf) {
            PositionSource positionSource = PositionSourceType.fromNetwork(friendlyByteBuf);
            int i = friendlyByteBuf.readVarInt();
            return new VibrationParticleOption(positionSource, i);
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
    private final PositionSource destination;
    private final int arrivalInTicks;

    public VibrationParticleOption(PositionSource positionSource, int i) {
        this.destination = positionSource;
        this.arrivalInTicks = i;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        PositionSourceType.toNetwork(this.destination, friendlyByteBuf);
        friendlyByteBuf.writeVarInt(this.arrivalInTicks);
    }

    @Override
    public String writeToString() {
        Vec3 vec3 = this.destination.getPosition(null).get();
        double d = vec3.x();
        double e = vec3.y();
        double f = vec3.z();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), d, e, f, this.arrivalInTicks);
    }

    public ParticleType<VibrationParticleOption> getType() {
        return ParticleTypes.VIBRATION;
    }

    public PositionSource getDestination() {
        return this.destination;
    }

    public int getArrivalInTicks() {
        return this.arrivalInTicks;
    }
}

