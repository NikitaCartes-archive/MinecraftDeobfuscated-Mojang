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
		instance -> instance.group(BlockPos.CODEC.fieldOf("pos").xmap(Optional::of, Optional::get).forGetter(blockPositionSource -> blockPositionSource.pos))
				.apply(instance, BlockPositionSource::new)
	);
	final Optional<BlockPos> pos;

	public BlockPositionSource(BlockPos blockPos) {
		this(Optional.of(blockPos));
	}

	BlockPositionSource(Optional<BlockPos> optional) {
		if (optional.isEmpty()) {
			throw new IllegalStateException("Not allowed to be optional");
		} else {
			this.pos = optional;
		}
	}

	@Override
	public Optional<Vec3> getPosition(Level level) {
		return this.pos.map(Vec3::atCenterOf);
	}

	@Override
	public PositionSourceType<?> getType() {
		return PositionSourceType.BLOCK;
	}

	public static class Type implements PositionSourceType<BlockPositionSource> {
		public BlockPositionSource read(FriendlyByteBuf friendlyByteBuf) {
			return new BlockPositionSource(Optional.of(friendlyByteBuf.readBlockPos()));
		}

		public void write(FriendlyByteBuf friendlyByteBuf, BlockPositionSource blockPositionSource) {
			blockPositionSource.pos.ifPresent(friendlyByteBuf::writeBlockPos);
		}

		@Override
		public Codec<BlockPositionSource> codec() {
			return BlockPositionSource.CODEC;
		}
	}
}
