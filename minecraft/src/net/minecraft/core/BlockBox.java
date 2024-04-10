package net.minecraft.core;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public record BlockBox(BlockPos min, BlockPos max) implements Iterable<BlockPos> {
	public static final StreamCodec<ByteBuf, BlockBox> STREAM_CODEC = new StreamCodec<ByteBuf, BlockBox>() {
		public BlockBox decode(ByteBuf byteBuf) {
			return new BlockBox(FriendlyByteBuf.readBlockPos(byteBuf), FriendlyByteBuf.readBlockPos(byteBuf));
		}

		public void encode(ByteBuf byteBuf, BlockBox blockBox) {
			FriendlyByteBuf.writeBlockPos(byteBuf, blockBox.min());
			FriendlyByteBuf.writeBlockPos(byteBuf, blockBox.max());
		}
	};

	public BlockBox(final BlockPos min, final BlockPos max) {
		this.min = BlockPos.min(min, max);
		this.max = BlockPos.max(min, max);
	}

	public static BlockBox of(BlockPos blockPos) {
		return new BlockBox(blockPos, blockPos);
	}

	public static BlockBox of(BlockPos blockPos, BlockPos blockPos2) {
		return new BlockBox(blockPos, blockPos2);
	}

	public BlockBox include(BlockPos blockPos) {
		return new BlockBox(BlockPos.min(this.min, blockPos), BlockPos.max(this.max, blockPos));
	}

	public boolean isBlock() {
		return this.min.equals(this.max);
	}

	public boolean contains(BlockPos blockPos) {
		return blockPos.getX() >= this.min.getX()
			&& blockPos.getY() >= this.min.getY()
			&& blockPos.getZ() >= this.min.getZ()
			&& blockPos.getX() <= this.max.getX()
			&& blockPos.getY() <= this.max.getY()
			&& blockPos.getZ() <= this.max.getZ();
	}

	public AABB aabb() {
		return AABB.encapsulatingFullBlocks(this.min, this.max);
	}

	public Iterator<BlockPos> iterator() {
		return BlockPos.betweenClosed(this.min, this.max).iterator();
	}

	public int sizeX() {
		return this.max.getX() - this.min.getX() + 1;
	}

	public int sizeY() {
		return this.max.getY() - this.min.getY() + 1;
	}

	public int sizeZ() {
		return this.max.getZ() - this.min.getZ() + 1;
	}

	public BlockBox extend(Direction direction, int i) {
		if (i == 0) {
			return this;
		} else {
			return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE
				? of(this.min, BlockPos.max(this.min, this.max.relative(direction, i)))
				: of(BlockPos.min(this.min.relative(direction, i), this.max), this.max);
		}
	}

	public BlockBox move(Direction direction, int i) {
		return i == 0 ? this : new BlockBox(this.min.relative(direction, i), this.max.relative(direction, i));
	}

	public BlockBox offset(Vec3i vec3i) {
		return new BlockBox(this.min.offset(vec3i), this.max.offset(vec3i));
	}
}
