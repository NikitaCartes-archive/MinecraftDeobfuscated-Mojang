package net.minecraft.world.level;

import java.util.Objects;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CollisionSpliterator extends AbstractSpliterator<VoxelShape> {
	@Nullable
	private final Entity source;
	private final AABB box;
	private final CollisionContext context;
	private final Cursor3D cursor;
	private final BlockPos.MutableBlockPos pos;
	private final VoxelShape entityShape;
	private final CollisionGetter collisionGetter;
	private boolean needsBorderCheck;
	private final BiPredicate<BlockState, BlockPos> predicate;

	public CollisionSpliterator(CollisionGetter collisionGetter, @Nullable Entity entity, AABB aABB) {
		this(collisionGetter, entity, aABB, (blockState, blockPos) -> true);
	}

	public CollisionSpliterator(CollisionGetter collisionGetter, @Nullable Entity entity, AABB aABB, BiPredicate<BlockState, BlockPos> biPredicate) {
		super(Long.MAX_VALUE, 1280);
		this.context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
		this.pos = new BlockPos.MutableBlockPos();
		this.entityShape = Shapes.create(aABB);
		this.collisionGetter = collisionGetter;
		this.needsBorderCheck = entity != null;
		this.source = entity;
		this.box = aABB;
		this.predicate = biPredicate;
		int i = Mth.floor(aABB.minX - 1.0E-7) - 1;
		int j = Mth.floor(aABB.maxX + 1.0E-7) + 1;
		int k = Mth.floor(aABB.minY - 1.0E-7) - 1;
		int l = Mth.floor(aABB.maxY + 1.0E-7) + 1;
		int m = Mth.floor(aABB.minZ - 1.0E-7) - 1;
		int n = Mth.floor(aABB.maxZ + 1.0E-7) + 1;
		this.cursor = new Cursor3D(i, k, m, j, l, n);
	}

	public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
		return this.needsBorderCheck && this.worldBorderCheck(consumer) || this.collisionCheck(consumer);
	}

	boolean collisionCheck(Consumer<? super VoxelShape> consumer) {
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
					if (this.predicate.test(blockState, this.pos) && (l != 1 || blockState.hasLargeCollisionShape()) && (l != 2 || blockState.is(Blocks.MOVING_PISTON))) {
						VoxelShape voxelShape = blockState.getCollisionShape(this.collisionGetter, this.pos, this.context);
						if (voxelShape == Shapes.block()) {
							if (this.box.intersects((double)i, (double)j, (double)k, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0)) {
								consumer.accept(voxelShape.move((double)i, (double)j, (double)k));
								return true;
							}
						} else {
							VoxelShape voxelShape2 = voxelShape.move((double)i, (double)j, (double)k);
							if (Shapes.joinIsNotEmpty(voxelShape2, this.entityShape, BooleanOp.AND)) {
								consumer.accept(voxelShape2);
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Nullable
	private BlockGetter getChunk(int i, int j) {
		int k = SectionPos.blockToSectionCoord(i);
		int l = SectionPos.blockToSectionCoord(j);
		return this.collisionGetter.getChunkForCollisions(k, l);
	}

	boolean worldBorderCheck(Consumer<? super VoxelShape> consumer) {
		Objects.requireNonNull(this.source);
		this.needsBorderCheck = false;
		WorldBorder worldBorder = this.collisionGetter.getWorldBorder();
		AABB aABB = this.source.getBoundingBox();
		if (!isBoxFullyWithinWorldBorder(worldBorder, aABB)) {
			VoxelShape voxelShape = worldBorder.getCollisionShape();
			if (!isOutsideBorder(voxelShape, aABB) && isCloseToBorder(voxelShape, aABB)) {
				consumer.accept(voxelShape);
				return true;
			}
		}

		return false;
	}

	private static boolean isCloseToBorder(VoxelShape voxelShape, AABB aABB) {
		return Shapes.joinIsNotEmpty(voxelShape, Shapes.create(aABB.inflate(1.0E-7)), BooleanOp.AND);
	}

	private static boolean isOutsideBorder(VoxelShape voxelShape, AABB aABB) {
		return Shapes.joinIsNotEmpty(voxelShape, Shapes.create(aABB.deflate(1.0E-7)), BooleanOp.AND);
	}

	public static boolean isBoxFullyWithinWorldBorder(WorldBorder worldBorder, AABB aABB) {
		double d = (double)Mth.floor(worldBorder.getMinX());
		double e = (double)Mth.floor(worldBorder.getMinZ());
		double f = (double)Mth.ceil(worldBorder.getMaxX());
		double g = (double)Mth.ceil(worldBorder.getMaxZ());
		return aABB.minX > d && aABB.minX < f && aABB.minZ > e && aABB.minZ < g && aABB.maxX > d && aABB.maxX < f && aABB.maxZ > e && aABB.maxZ < g;
	}
}
