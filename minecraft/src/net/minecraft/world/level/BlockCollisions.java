package net.minecraft.world.level;

import com.google.common.collect.AbstractIterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockCollisions extends AbstractIterator<VoxelShape> {
	private final AABB box;
	private final CollisionContext context;
	private final Cursor3D cursor;
	private final BlockPos.MutableBlockPos pos;
	private final VoxelShape entityShape;
	private final CollisionGetter collisionGetter;
	private final boolean onlySuffocatingBlocks;
	@Nullable
	private BlockGetter cachedBlockGetter;
	private long cachedBlockGetterPos;

	public BlockCollisions(CollisionGetter collisionGetter, @Nullable Entity entity, AABB aABB) {
		this(collisionGetter, entity, aABB, false);
	}

	public BlockCollisions(CollisionGetter collisionGetter, @Nullable Entity entity, AABB aABB, boolean bl) {
		this.context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
		this.pos = new BlockPos.MutableBlockPos();
		this.entityShape = Shapes.create(aABB);
		this.collisionGetter = collisionGetter;
		this.box = aABB;
		this.onlySuffocatingBlocks = bl;
		int i = Mth.floor(aABB.minX - 1.0E-7) - 1;
		int j = Mth.floor(aABB.maxX + 1.0E-7) + 1;
		int k = Mth.floor(aABB.minY - 1.0E-7) - 1;
		int l = Mth.floor(aABB.maxY + 1.0E-7) + 1;
		int m = Mth.floor(aABB.minZ - 1.0E-7) - 1;
		int n = Mth.floor(aABB.maxZ + 1.0E-7) + 1;
		this.cursor = new Cursor3D(i, k, m, j, l, n);
	}

	@Nullable
	private BlockGetter getChunk(int i, int j) {
		int k = SectionPos.blockToSectionCoord(i);
		int l = SectionPos.blockToSectionCoord(j);
		long m = ChunkPos.asLong(k, l);
		if (this.cachedBlockGetter != null && this.cachedBlockGetterPos == m) {
			return this.cachedBlockGetter;
		} else {
			BlockGetter blockGetter = this.collisionGetter.getChunkForCollisions(k, l);
			this.cachedBlockGetter = blockGetter;
			this.cachedBlockGetterPos = m;
			return blockGetter;
		}
	}

	protected VoxelShape computeNext() {
		while (this.cursor.advance()) {
			int i = this.cursor.nextX();
			int j = this.cursor.nextY();
			int k = this.cursor.nextZ();
			int l = this.cursor.getNextType();
			if (l != 3) {
				BlockGetter blockGetter = this.getChunk(i, k);
				if (blockGetter != null) {
					this.pos.set(i, j, k);
					BlockState blockState = blockGetter.getBlockState(this.pos);
					if ((!this.onlySuffocatingBlocks || blockState.isSuffocating(blockGetter, this.pos))
						&& (l != 1 || blockState.hasLargeCollisionShape())
						&& (l != 2 || blockState.is(Blocks.MOVING_PISTON))) {
						VoxelShape voxelShape = blockState.getCollisionShape(this.collisionGetter, this.pos, this.context);
						if (voxelShape == Shapes.block()) {
							if (this.box.intersects((double)i, (double)j, (double)k, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0)) {
								return voxelShape.move((double)i, (double)j, (double)k);
							}
						} else {
							VoxelShape voxelShape2 = voxelShape.move((double)i, (double)j, (double)k);
							if (Shapes.joinIsNotEmpty(voxelShape2, this.entityShape, BooleanOp.AND)) {
								return voxelShape2;
							}
						}
					}
				}
			}
		}

		return this.endOfData();
	}
}
