package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.material.FluidState;

public abstract class StructurePiece {
	protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
	protected BoundingBox boundingBox;
	@Nullable
	private Direction orientation;
	private Mirror mirror;
	private Rotation rotation;
	protected int genDepth;
	private final StructurePieceType type;
	private static final Set<Block> SHAPE_CHECK_BLOCKS = ImmutableSet.<Block>builder()
		.add(Blocks.NETHER_BRICK_FENCE)
		.add(Blocks.TORCH)
		.add(Blocks.WALL_TORCH)
		.add(Blocks.OAK_FENCE)
		.add(Blocks.SPRUCE_FENCE)
		.add(Blocks.DARK_OAK_FENCE)
		.add(Blocks.ACACIA_FENCE)
		.add(Blocks.BIRCH_FENCE)
		.add(Blocks.JUNGLE_FENCE)
		.add(Blocks.LADDER)
		.add(Blocks.IRON_BARS)
		.build();

	protected StructurePiece(StructurePieceType structurePieceType, int i) {
		this.type = structurePieceType;
		this.genDepth = i;
	}

	public StructurePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
		this(structurePieceType, compoundTag.getInt("GD"));
		if (compoundTag.contains("BB")) {
			this.boundingBox = new BoundingBox(compoundTag.getIntArray("BB"));
		}

		int i = compoundTag.getInt("O");
		this.setOrientation(i == -1 ? null : Direction.from2DDataValue(i));
	}

	public final CompoundTag createTag() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("id", Registry.STRUCTURE_PIECE.getKey(this.getType()).toString());
		compoundTag.put("BB", this.boundingBox.createTag());
		Direction direction = this.getOrientation();
		compoundTag.putInt("O", direction == null ? -1 : direction.get2DDataValue());
		compoundTag.putInt("GD", this.genDepth);
		this.addAdditionalSaveData(compoundTag);
		return compoundTag;
	}

	protected abstract void addAdditionalSaveData(CompoundTag compoundTag);

	public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
	}

	public abstract boolean postProcess(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<?> chunkGenerator,
		Random random,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		BlockPos blockPos
	);

	public BoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	public int getGenDepth() {
		return this.genDepth;
	}

	public boolean isCloseToChunk(ChunkPos chunkPos, int i) {
		int j = chunkPos.x << 4;
		int k = chunkPos.z << 4;
		return this.boundingBox.intersects(j - i, k - i, j + 15 + i, k + 15 + i);
	}

	public static StructurePiece findCollisionPiece(List<StructurePiece> list, BoundingBox boundingBox) {
		for (StructurePiece structurePiece : list) {
			if (structurePiece.getBoundingBox() != null && structurePiece.getBoundingBox().intersects(boundingBox)) {
				return structurePiece;
			}
		}

		return null;
	}

	protected boolean edgesLiquid(BlockGetter blockGetter, BoundingBox boundingBox) {
		int i = Math.max(this.boundingBox.x0 - 1, boundingBox.x0);
		int j = Math.max(this.boundingBox.y0 - 1, boundingBox.y0);
		int k = Math.max(this.boundingBox.z0 - 1, boundingBox.z0);
		int l = Math.min(this.boundingBox.x1 + 1, boundingBox.x1);
		int m = Math.min(this.boundingBox.y1 + 1, boundingBox.y1);
		int n = Math.min(this.boundingBox.z1 + 1, boundingBox.z1);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int o = i; o <= l; o++) {
			for (int p = k; p <= n; p++) {
				if (blockGetter.getBlockState(mutableBlockPos.set(o, j, p)).getMaterial().isLiquid()) {
					return true;
				}

				if (blockGetter.getBlockState(mutableBlockPos.set(o, m, p)).getMaterial().isLiquid()) {
					return true;
				}
			}
		}

		for (int o = i; o <= l; o++) {
			for (int p = j; p <= m; p++) {
				if (blockGetter.getBlockState(mutableBlockPos.set(o, p, k)).getMaterial().isLiquid()) {
					return true;
				}

				if (blockGetter.getBlockState(mutableBlockPos.set(o, p, n)).getMaterial().isLiquid()) {
					return true;
				}
			}
		}

		for (int o = k; o <= n; o++) {
			for (int p = j; p <= m; p++) {
				if (blockGetter.getBlockState(mutableBlockPos.set(i, p, o)).getMaterial().isLiquid()) {
					return true;
				}

				if (blockGetter.getBlockState(mutableBlockPos.set(l, p, o)).getMaterial().isLiquid()) {
					return true;
				}
			}
		}

		return false;
	}

	protected int getWorldX(int i, int j) {
		Direction direction = this.getOrientation();
		if (direction == null) {
			return i;
		} else {
			switch (direction) {
				case NORTH:
				case SOUTH:
					return this.boundingBox.x0 + i;
				case WEST:
					return this.boundingBox.x1 - j;
				case EAST:
					return this.boundingBox.x0 + j;
				default:
					return i;
			}
		}
	}

	protected int getWorldY(int i) {
		return this.getOrientation() == null ? i : i + this.boundingBox.y0;
	}

	protected int getWorldZ(int i, int j) {
		Direction direction = this.getOrientation();
		if (direction == null) {
			return j;
		} else {
			switch (direction) {
				case NORTH:
					return this.boundingBox.z1 - j;
				case SOUTH:
					return this.boundingBox.z0 + j;
				case WEST:
				case EAST:
					return this.boundingBox.z0 + i;
				default:
					return j;
			}
		}
	}

	protected void placeBlock(LevelAccessor levelAccessor, BlockState blockState, int i, int j, int k, BoundingBox boundingBox) {
		BlockPos blockPos = new BlockPos(this.getWorldX(i, k), this.getWorldY(j), this.getWorldZ(i, k));
		if (boundingBox.isInside(blockPos)) {
			if (this.mirror != Mirror.NONE) {
				blockState = blockState.mirror(this.mirror);
			}

			if (this.rotation != Rotation.NONE) {
				blockState = blockState.rotate(this.rotation);
			}

			levelAccessor.setBlock(blockPos, blockState, 2);
			FluidState fluidState = levelAccessor.getFluidState(blockPos);
			if (!fluidState.isEmpty()) {
				levelAccessor.getLiquidTicks().scheduleTick(blockPos, fluidState.getType(), 0);
			}

			if (SHAPE_CHECK_BLOCKS.contains(blockState.getBlock())) {
				levelAccessor.getChunk(blockPos).markPosForPostprocessing(blockPos);
			}
		}
	}

	protected BlockState getBlock(BlockGetter blockGetter, int i, int j, int k, BoundingBox boundingBox) {
		int l = this.getWorldX(i, k);
		int m = this.getWorldY(j);
		int n = this.getWorldZ(i, k);
		BlockPos blockPos = new BlockPos(l, m, n);
		return !boundingBox.isInside(blockPos) ? Blocks.AIR.defaultBlockState() : blockGetter.getBlockState(blockPos);
	}

	protected boolean isInterior(LevelReader levelReader, int i, int j, int k, BoundingBox boundingBox) {
		int l = this.getWorldX(i, k);
		int m = this.getWorldY(j + 1);
		int n = this.getWorldZ(i, k);
		BlockPos blockPos = new BlockPos(l, m, n);
		return !boundingBox.isInside(blockPos) ? false : m < levelReader.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, l, n);
	}

	protected void generateAirBox(LevelAccessor levelAccessor, BoundingBox boundingBox, int i, int j, int k, int l, int m, int n) {
		for (int o = j; o <= m; o++) {
			for (int p = i; p <= l; p++) {
				for (int q = k; q <= n; q++) {
					this.placeBlock(levelAccessor, Blocks.AIR.defaultBlockState(), p, o, q, boundingBox);
				}
			}
		}
	}

	protected void generateBox(
		LevelAccessor levelAccessor, BoundingBox boundingBox, int i, int j, int k, int l, int m, int n, BlockState blockState, BlockState blockState2, boolean bl
	) {
		for (int o = j; o <= m; o++) {
			for (int p = i; p <= l; p++) {
				for (int q = k; q <= n; q++) {
					if (!bl || !this.getBlock(levelAccessor, p, o, q, boundingBox).isAir()) {
						if (o != j && o != m && p != i && p != l && q != k && q != n) {
							this.placeBlock(levelAccessor, blockState2, p, o, q, boundingBox);
						} else {
							this.placeBlock(levelAccessor, blockState, p, o, q, boundingBox);
						}
					}
				}
			}
		}
	}

	protected void generateBox(
		LevelAccessor levelAccessor,
		BoundingBox boundingBox,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		boolean bl,
		Random random,
		StructurePiece.BlockSelector blockSelector
	) {
		for (int o = j; o <= m; o++) {
			for (int p = i; p <= l; p++) {
				for (int q = k; q <= n; q++) {
					if (!bl || !this.getBlock(levelAccessor, p, o, q, boundingBox).isAir()) {
						blockSelector.next(random, p, o, q, o == j || o == m || p == i || p == l || q == k || q == n);
						this.placeBlock(levelAccessor, blockSelector.getNext(), p, o, q, boundingBox);
					}
				}
			}
		}
	}

	protected void generateMaybeBox(
		LevelAccessor levelAccessor,
		BoundingBox boundingBox,
		Random random,
		float f,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		BlockState blockState,
		BlockState blockState2,
		boolean bl,
		boolean bl2
	) {
		for (int o = j; o <= m; o++) {
			for (int p = i; p <= l; p++) {
				for (int q = k; q <= n; q++) {
					if (!(random.nextFloat() > f)
						&& (!bl || !this.getBlock(levelAccessor, p, o, q, boundingBox).isAir())
						&& (!bl2 || this.isInterior(levelAccessor, p, o, q, boundingBox))) {
						if (o != j && o != m && p != i && p != l && q != k && q != n) {
							this.placeBlock(levelAccessor, blockState2, p, o, q, boundingBox);
						} else {
							this.placeBlock(levelAccessor, blockState, p, o, q, boundingBox);
						}
					}
				}
			}
		}
	}

	protected void maybeGenerateBlock(LevelAccessor levelAccessor, BoundingBox boundingBox, Random random, float f, int i, int j, int k, BlockState blockState) {
		if (random.nextFloat() < f) {
			this.placeBlock(levelAccessor, blockState, i, j, k, boundingBox);
		}
	}

	protected void generateUpperHalfSphere(
		LevelAccessor levelAccessor, BoundingBox boundingBox, int i, int j, int k, int l, int m, int n, BlockState blockState, boolean bl
	) {
		float f = (float)(l - i + 1);
		float g = (float)(m - j + 1);
		float h = (float)(n - k + 1);
		float o = (float)i + f / 2.0F;
		float p = (float)k + h / 2.0F;

		for (int q = j; q <= m; q++) {
			float r = (float)(q - j) / g;

			for (int s = i; s <= l; s++) {
				float t = ((float)s - o) / (f * 0.5F);

				for (int u = k; u <= n; u++) {
					float v = ((float)u - p) / (h * 0.5F);
					if (!bl || !this.getBlock(levelAccessor, s, q, u, boundingBox).isAir()) {
						float w = t * t + r * r + v * v;
						if (w <= 1.05F) {
							this.placeBlock(levelAccessor, blockState, s, q, u, boundingBox);
						}
					}
				}
			}
		}
	}

	protected void fillColumnDown(LevelAccessor levelAccessor, BlockState blockState, int i, int j, int k, BoundingBox boundingBox) {
		int l = this.getWorldX(i, k);
		int m = this.getWorldY(j);
		int n = this.getWorldZ(i, k);
		if (boundingBox.isInside(new BlockPos(l, m, n))) {
			while ((levelAccessor.isEmptyBlock(new BlockPos(l, m, n)) || levelAccessor.getBlockState(new BlockPos(l, m, n)).getMaterial().isLiquid()) && m > 1) {
				levelAccessor.setBlock(new BlockPos(l, m, n), blockState, 2);
				m--;
			}
		}
	}

	protected boolean createChest(LevelAccessor levelAccessor, BoundingBox boundingBox, Random random, int i, int j, int k, ResourceLocation resourceLocation) {
		BlockPos blockPos = new BlockPos(this.getWorldX(i, k), this.getWorldY(j), this.getWorldZ(i, k));
		return this.createChest(levelAccessor, boundingBox, random, blockPos, resourceLocation, null);
	}

	public static BlockState reorient(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		Direction direction = null;

		for (Direction direction2 : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = blockPos.relative(direction2);
			BlockState blockState2 = blockGetter.getBlockState(blockPos2);
			if (blockState2.getBlock() == Blocks.CHEST) {
				return blockState;
			}

			if (blockState2.isSolidRender(blockGetter, blockPos2)) {
				if (direction != null) {
					direction = null;
					break;
				}

				direction = direction2;
			}
		}

		if (direction != null) {
			return blockState.setValue(HorizontalDirectionalBlock.FACING, direction.getOpposite());
		} else {
			Direction direction3 = blockState.getValue(HorizontalDirectionalBlock.FACING);
			BlockPos blockPos3 = blockPos.relative(direction3);
			if (blockGetter.getBlockState(blockPos3).isSolidRender(blockGetter, blockPos3)) {
				direction3 = direction3.getOpposite();
				blockPos3 = blockPos.relative(direction3);
			}

			if (blockGetter.getBlockState(blockPos3).isSolidRender(blockGetter, blockPos3)) {
				direction3 = direction3.getClockWise();
				blockPos3 = blockPos.relative(direction3);
			}

			if (blockGetter.getBlockState(blockPos3).isSolidRender(blockGetter, blockPos3)) {
				direction3 = direction3.getOpposite();
				blockPos3 = blockPos.relative(direction3);
			}

			return blockState.setValue(HorizontalDirectionalBlock.FACING, direction3);
		}
	}

	protected boolean createChest(
		LevelAccessor levelAccessor, BoundingBox boundingBox, Random random, BlockPos blockPos, ResourceLocation resourceLocation, @Nullable BlockState blockState
	) {
		if (boundingBox.isInside(blockPos) && levelAccessor.getBlockState(blockPos).getBlock() != Blocks.CHEST) {
			if (blockState == null) {
				blockState = reorient(levelAccessor, blockPos, Blocks.CHEST.defaultBlockState());
			}

			levelAccessor.setBlock(blockPos, blockState, 2);
			BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
			if (blockEntity instanceof ChestBlockEntity) {
				((ChestBlockEntity)blockEntity).setLootTable(resourceLocation, random.nextLong());
			}

			return true;
		} else {
			return false;
		}
	}

	protected boolean createDispenser(
		LevelAccessor levelAccessor, BoundingBox boundingBox, Random random, int i, int j, int k, Direction direction, ResourceLocation resourceLocation
	) {
		BlockPos blockPos = new BlockPos(this.getWorldX(i, k), this.getWorldY(j), this.getWorldZ(i, k));
		if (boundingBox.isInside(blockPos) && levelAccessor.getBlockState(blockPos).getBlock() != Blocks.DISPENSER) {
			this.placeBlock(levelAccessor, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, direction), i, j, k, boundingBox);
			BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
			if (blockEntity instanceof DispenserBlockEntity) {
				((DispenserBlockEntity)blockEntity).setLootTable(resourceLocation, random.nextLong());
			}

			return true;
		} else {
			return false;
		}
	}

	public void move(int i, int j, int k) {
		this.boundingBox.move(i, j, k);
	}

	@Nullable
	public Direction getOrientation() {
		return this.orientation;
	}

	public void setOrientation(@Nullable Direction direction) {
		this.orientation = direction;
		if (direction == null) {
			this.rotation = Rotation.NONE;
			this.mirror = Mirror.NONE;
		} else {
			switch (direction) {
				case SOUTH:
					this.mirror = Mirror.LEFT_RIGHT;
					this.rotation = Rotation.NONE;
					break;
				case WEST:
					this.mirror = Mirror.LEFT_RIGHT;
					this.rotation = Rotation.CLOCKWISE_90;
					break;
				case EAST:
					this.mirror = Mirror.NONE;
					this.rotation = Rotation.CLOCKWISE_90;
					break;
				default:
					this.mirror = Mirror.NONE;
					this.rotation = Rotation.NONE;
			}
		}
	}

	public Rotation getRotation() {
		return this.rotation;
	}

	public StructurePieceType getType() {
		return this.type;
	}

	public abstract static class BlockSelector {
		protected BlockState next = Blocks.AIR.defaultBlockState();

		protected BlockSelector() {
		}

		public abstract void next(Random random, int i, int j, int k, boolean bl);

		public BlockState getNext() {
			return this.next;
		}
	}
}
