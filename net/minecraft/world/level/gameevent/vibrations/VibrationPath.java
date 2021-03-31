/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;

public class VibrationPath {
    public static final Codec<VibrationPath> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockPos.CODEC.fieldOf("origin")).forGetter(vibrationPath -> vibrationPath.origin), ((MapCodec)PositionSource.CODEC.fieldOf("destination")).forGetter(vibrationPath -> vibrationPath.destination), ((MapCodec)Codec.INT.fieldOf("arrival_in_ticks")).forGetter(vibrationPath -> vibrationPath.arrivalInTicks)).apply((Applicative<VibrationPath, ?>)instance, VibrationPath::new));
    private final BlockPos origin;
    private final PositionSource destination;
    private final int arrivalInTicks;

    public VibrationPath(BlockPos blockPos, PositionSource positionSource, int i) {
        this.origin = blockPos;
        this.destination = positionSource;
        this.arrivalInTicks = i;
    }

    public int getArrivalInTicks() {
        return this.arrivalInTicks;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public PositionSource getDestination() {
        return this.destination;
    }

    public static VibrationPath read(FriendlyByteBuf friendlyByteBuf) {
        BlockPos blockPos = friendlyByteBuf.readBlockPos();
        PositionSource positionSource = PositionSourceType.fromNetwork(friendlyByteBuf);
        int i = friendlyByteBuf.readVarInt();
        return new VibrationPath(blockPos, positionSource, i);
    }

    public static void write(FriendlyByteBuf friendlyByteBuf, VibrationPath vibrationPath) {
        friendlyByteBuf.writeBlockPos(vibrationPath.origin);
        PositionSourceType.toNetwork(vibrationPath.destination, friendlyByteBuf);
        friendlyByteBuf.writeVarInt(vibrationPath.arrivalInTicks);
    }
}

