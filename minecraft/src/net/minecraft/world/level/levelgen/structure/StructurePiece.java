package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
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
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

public abstract class StructurePiece {
	private static final Logger LOGGER = LogUtils.getLogger();
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

	protected StructurePiece(StructurePieceType structurePieceType, int i, BoundingBox boundingBox) {
		this.type = structurePieceType;
		this.genDepth = i;
		this.boundingBox = boundingBox;
	}

	public StructurePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
		this(
			structurePieceType,
			compoundTag.getInt("GD"),
			(BoundingBox)BoundingBox.CODEC
				.parse(NbtOps.INSTANCE, compoundTag.get("BB"))
				.resultOrPartial(LOGGER::error)
				.orElseThrow(() -> new IllegalArgumentException("Invalid boundingbox"))
		);
		int i = compoundTag.getInt("O");
		this.setOrientation(i == -1 ? null : Direction.from2DDataValue(i));
	}

	protected static BoundingBox makeBoundingBox(int i, int j, int k, Direction direction, int l, int m, int n) {
		return direction.getAxis() == Direction.Axis.Z
			? new BoundingBox(i, j, k, i + l - 1, j + m - 1, k + n - 1)
			: new BoundingBox(i, j, k, i + n - 1, j + m - 1, k + l - 1);
	}

	protected static Direction getRandomHorizontalDirection(RandomSource randomSource) {
		return Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
	}

	public final CompoundTag createTag(StructurePieceSerializationContext structurePieceSerializationContext) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("id", Registry.STRUCTURE_PIECE.getKey(this.getType()).toString());
		BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("BB", tag));
		Direction direction = this.getOrientation();
		compoundTag.putInt("O", direction == null ? -1 : direction.get2DDataValue());
		compoundTag.putInt("GD", this.genDepth);
		this.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
		return compoundTag;
	}

	protected abstract void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag);

	public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
	}

	public abstract void postProcess(
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
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

	public void setGenDepth(int i) {
		this.genDepth = i;
	}

	public boolean isCloseToChunk(ChunkPos chunkPos, int i) {
		int j = chunkPos.getMinBlockX();
		int k = chunkPos.getMinBlockZ();
		return this.boundingBox.intersects(j - i, k - i, j + 15 + i, k + 15 + i);
	}

	public BlockPos getLocatorPosition() {
		return new BlockPos(this.boundingBox.getCenter());
	}

	protected BlockPos.MutableBlockPos getWorldPos(int i, int j, int k) {
		return new BlockPos.MutableBlockPos(this.getWorldX(i, k), this.getWorldY(j), this.getWorldZ(i, k));
	}

	protected int getWorldX(int i, int j) {
		Direction direction = this.getOrientation();
		if (direction == null) {
			return i;
		} else {
			switch (direction) {
				case NORTH:
				case SOUTH:
					return this.boundingBox.minX() + i;
				case WEST:
					return this.boundingBox.maxX() - j;
				case EAST:
					return this.boundingBox.minX() + j;
				default:
					return i;
			}
		}
	}

	protected int getWorldY(int i) {
		return this.getOrientation() == null ? i : i + this.boundingBox.minY();
	}

	protected int getWorldZ(int i, int j) {
		Direction direction = this.getOrientation();
		if (direction == null) {
			return j;
		} else {
			switch (direction) {
				case NORTH:
					return this.boundingBox.maxZ() - j;
				case SOUTH:
					return this.boundingBox.minZ() + j;
				case WEST:
				case EAST:
					return this.boundingBox.minZ() + i;
				default:
					return j;
			}
		}
	}

	protected void placeBlock(WorldGenLevel worldGenLevel, BlockState blockState, int i, int j, int k, BoundingBox boundingBox) {
		BlockPos blockPos = this.getWorldPos(i, j, k);
		if (boundingBox.isInside(blockPos)) {
			if (this.canBeReplaced(worldGenLevel, i, j, k, boundingBox)) {
				if (this.mirror != Mirror.NONE) {
					blockState = blockState.mirror(this.mirror);
				}

				if (this.rotation != Rotation.NONE) {
					blockState = blockState.rotate(this.rotation);
				}

				worldGenLevel.setBlock(blockPos, blockState, 2);
				FluidState fluidState = worldGenLevel.getFluidState(blockPos);
				if (!fluidState.isEmpty()) {
					worldGenLevel.scheduleTick(blockPos, fluidState.getType(), 0);
				}

				if (SHAPE_CHECK_BLOCKS.contains(blockState.getBlock())) {
					worldGenLevel.getChunk(blockPos).markPosForPostprocessing(blockPos);
				}
			}
		}
	}

	protected boolean canBeReplaced(LevelReader levelReader, int i, int j, int k, BoundingBox boundingBox) {
		return true;
	}

	protected BlockState getBlock(BlockGetter blockGetter, int i, int j, int k, BoundingBox boundingBox) {
		BlockPos blockPos = this.getWorldPos(i, j, k);
		return !boundingBox.isInside(blockPos) ? Blocks.AIR.defaultBlockState() : blockGetter.getBlockState(blockPos);
	}

	protected boolean isInterior(LevelReader levelReader, int i, int j, int k, BoundingBox boundingBox) {
		BlockPos blockPos = this.getWorldPos(i, j + 1, k);
		return !boundingBox.isInside(blockPos) ? false : blockPos.getY() < levelReader.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockPos.getX(), blockPos.getZ());
	}

	protected void generateAirBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k, int l, int m, int n) {
		for (int o = j; o <= m; o++) {
			for (int p = i; p <= l; p++) {
				for (int q = k; q <= n; q++) {
					this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), p, o, q, boundingBox);
				}
			}
		}
	}

	protected void generateBox(
		WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k, int l, int m, int n, BlockState blockState, BlockState blockState2, boolean bl
	) {
		for (int o = j; o <= m; o++) {
			for (int p = i; p <= l; p++) {
				for (int q = k; q <= n; q++) {
					if (!bl || !this.getBlock(worldGenLevel, p, o, q, boundingBox).isAir()) {
						if (o != j && o != m && p != i && p != l && q != k && q != n) {
							this.placeBlock(worldGenLevel, blockState2, p, o, q, boundingBox);
						} else {
							this.placeBlock(worldGenLevel, blockState, p, o, q, boundingBox);
						}
					}
				}
			}
		}
	}

	protected void generateBox(
		WorldGenLevel worldGenLevel, BoundingBox boundingBox, BoundingBox boundingBox2, BlockState blockState, BlockState blockState2, boolean bl
	) {
		this.generateBox(
			worldGenLevel,
			boundingBox,
			boundingBox2.minX(),
			boundingBox2.minY(),
			boundingBox2.minZ(),
			boundingBox2.maxX(),
			boundingBox2.maxY(),
			boundingBox2.maxZ(),
			blockState,
			blockState2,
			bl
		);
	}

	protected void generateBox(
		WorldGenLevel worldGenLevel,
		BoundingBox boundingBox,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		boolean bl,
		RandomSource randomSource,
		StructurePiece.BlockSelector blockSelector
	) {
		for (int o = j; o <= m; o++) {
			for (int p = i; p <= l; p++) {
				for (int q = k; q <= n; q++) {
					if (!bl || !this.getBlock(worldGenLevel, p, o, q, boundingBox).isAir()) {
						blockSelector.next(randomSource, p, o, q, o == j || o == m || p == i || p == l || q == k || q == n);
						this.placeBlock(worldGenLevel, blockSelector.getNext(), p, o, q, boundingBox);
					}
				}
			}
		}
	}

	protected void generateBox(
		WorldGenLevel worldGenLevel,
		BoundingBox boundingBox,
		BoundingBox boundingBox2,
		boolean bl,
		RandomSource randomSource,
		StructurePiece.BlockSelector blockSelector
	) {
		this.generateBox(
			worldGenLevel,
			boundingBox,
			boundingBox2.minX(),
			boundingBox2.minY(),
			boundingBox2.minZ(),
			boundingBox2.maxX(),
			boundingBox2.maxY(),
			boundingBox2.maxZ(),
			bl,
			randomSource,
			blockSelector
		);
	}

	protected void generateMaybeBox(
		WorldGenLevel worldGenLevel,
		BoundingBox boundingBox,
		RandomSource randomSource,
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
					if (!(randomSource.nextFloat() > f)
						&& (!bl || !this.getBlock(worldGenLevel, p, o, q, boundingBox).isAir())
						&& (!bl2 || this.isInterior(worldGenLevel, p, o, q, boundingBox))) {
						if (o != j && o != m && p != i && p != l && q != k && q != n) {
							this.placeBlock(worldGenLevel, blockState2, p, o, q, boundingBox);
						} else {
							this.placeBlock(worldGenLevel, blockState, p, o, q, boundingBox);
						}
					}
				}
			}
		}
	}

	protected void maybeGenerateBlock(
		WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, float f, int i, int j, int k, BlockState blockState
	) {
		if (randomSource.nextFloat() < f) {
			this.placeBlock(worldGenLevel, blockState, i, j, k, boundingBox);
		}
	}

	protected void generateUpperHalfSphere(
		WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int j, int k, int l, int m, int n, BlockState blockState, boolean bl
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
					if (!bl || !this.getBlock(worldGenLevel, s, q, u, boundingBox).isAir()) {
						float w = t * t + r * r + v * v;
						if (w <= 1.05F) {
							this.placeBlock(worldGenLevel, blockState, s, q, u, boundingBox);
						}
					}
				}
			}
		}
	}

	protected void fillColumnDown(WorldGenLevel worldGenLevel, BlockState blockState, int i, int j, int k, BoundingBox boundingBox) {
		BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(i, j, k);
		if (boundingBox.isInside(mutableBlockPos)) {
			while (this.isReplaceableByStructures(worldGenLevel.getBlockState(mutableBlockPos)) && mutableBlockPos.getY() > worldGenLevel.getMinBuildHeight() + 1) {
				worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
				mutableBlockPos.move(Direction.DOWN);
			}
		}
	}

	protected boolean isReplaceableByStructures(BlockState blockState) {
		return blockState.isAir()
			|| blockState.getMaterial().isLiquid()
			|| blockState.is(Blocks.GLOW_LICHEN)
			|| blockState.is(Blocks.SEAGRASS)
			|| blockState.is(Blocks.TALL_SEAGRASS);
	}

	protected boolean createChest(
		WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, int i, int j, int k, ResourceLocation resourceLocation
	) {
		return this.createChest(worldGenLevel, boundingBox, randomSource, this.getWorldPos(i, j, k), resourceLocation, null);
	}

	public static BlockState reorient(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		Direction direction = null;

		for (Direction direction2 : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = blockPos.relative(direction2);
			BlockState blockState2 = blockGetter.getBlockState(blockPos2);
			if (blockState2.is(Blocks.CHEST)) {
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
		ServerLevelAccessor serverLevelAccessor,
		BoundingBox boundingBox,
		RandomSource randomSource,
		BlockPos blockPos,
		ResourceLocation resourceLocation,
		@Nullable BlockState blockState
	) {
		if (boundingBox.isInside(blockPos) && !serverLevelAccessor.getBlockState(blockPos).is(Blocks.CHEST)) {
			if (blockState == null) {
				blockState = reorient(serverLevelAccessor, blockPos, Blocks.CHEST.defaultBlockState());
			}

			serverLevelAccessor.setBlock(blockPos, blockState, 2);
			BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos);
			if (blockEntity instanceof ChestBlockEntity) {
				((ChestBlockEntity)blockEntity).setLootTable(resourceLocation, randomSource.nextLong());
			}

			return true;
		} else {
			return false;
		}
	}

	protected boolean createDispenser(
		WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, int i, int j, int k, Direction direction, ResourceLocation resourceLocation
	) {
		BlockPos blockPos = this.getWorldPos(i, j, k);
		if (boundingBox.isInside(blockPos) && !worldGenLevel.getBlockState(blockPos).is(Blocks.DISPENSER)) {
			this.placeBlock(worldGenLevel, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, direction), i, j, k, boundingBox);
			BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos);
			if (blockEntity instanceof DispenserBlockEntity) {
				((DispenserBlockEntity)blockEntity).setLootTable(resourceLocation, randomSource.nextLong());
			}

			return true;
		} else {
			return false;
		}
	}

	public void move(int i, int j, int k) {
		this.boundingBox.move(i, j, k);
	}

	public static BoundingBox createBoundingBox(Stream<StructurePiece> stream) {
		return (BoundingBox)BoundingBox.encapsulatingBoxes(stream.map(StructurePiece::getBoundingBox)::iterator)
			.orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox without pieces"));
	}

	@Nullable
	public static StructurePiece findCollisionPiece(List<StructurePiece> list, BoundingBox boundingBox) {
		for (StructurePiece structurePiece : list) {
			if (structurePiece.getBoundingBox().intersects(boundingBox)) {
				return structurePiece;
			}
		}

		return null;
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

	public Mirror getMirror() {
		return this.mirror;
	}

	public StructurePieceType getType() {
		return this.type;
	}

	public abstract static class BlockSelector {
		protected BlockState next = Blocks.AIR.defaultBlockState();

		public abstract void next(RandomSource randomSource, int i, int j, int k, boolean bl);

		public BlockState getNext() {
			return this.next;
		}
	}
}
