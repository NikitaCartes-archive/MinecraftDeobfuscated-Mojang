/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;

public class BlockPositionSource
implements PositionSource {
    public static final Codec<BlockPositionSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockPos.CODEC.fieldOf("pos")).forGetter(blockPositionSource -> blockPositionSource.pos)).apply((Applicative<BlockPositionSource, ?>)instance, BlockPositionSource::new));
    final BlockPos pos;

    public BlockPositionSource(BlockPos blockPos) {
        this.pos = blockPos;
    }

    @Override
    public Optional<Vec3> getPosition(Level level) {
        return Optional.of(Vec3.atCenterOf(this.pos));
    }

    @Override
    public PositionSourceType<?> getType() {
        return PositionSourceType.BLOCK;
    }

    public static class Type
    implements PositionSourceType<BlockPositionSource> {
        @Override
        public BlockPositionSource read(FriendlyByteBuf friendlyByteBuf) {
            return new BlockPositionSource(friendlyByteBuf.readBlockPos());
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, BlockPositionSource blockPositionSource) {
            friendlyByteBuf.writeBlockPos(blockPositionSource.pos);
        }

        @Override
        public Codec<BlockPositionSource> codec() {
            return CODEC;
        }

        @Override
        public /* synthetic */ PositionSource read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    }
}

