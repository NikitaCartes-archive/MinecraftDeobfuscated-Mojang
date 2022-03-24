package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BlockPositionSource implements PositionSource {
	public static final Codec<BlockPositionSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(blockPositionSource -> blockPositionSource.pos)).apply(instance, BlockPositionSource::new)
	);
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

	public static class Type implements PositionSourceType<BlockPositionSource> {
		public BlockPositionSource read(FriendlyByteBuf friendlyByteBuf) {
			return new BlockPositionSource(friendlyByteBuf.readBlockPos());
		}

		public void write(FriendlyByteBuf friendlyByteBuf, BlockPositionSource blockPositionSource) {
			friendlyByteBuf.writeBlockPos(blockPositionSource.pos);
		}

		@Override
		public Codec<BlockPositionSource> codec() {
			return BlockPositionSource.CODEC;
		}
	}
}
