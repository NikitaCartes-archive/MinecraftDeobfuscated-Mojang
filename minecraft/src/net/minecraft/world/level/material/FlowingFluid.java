package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanFunction;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectFunction;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid extends Fluid {
	public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
	private static final int CACHE_SIZE = 200;
	private static final ThreadLocal<Object2ByteLinkedOpenHashMap<FlowingFluid.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(
		() -> {
			Object2ByteLinkedOpenHashMap<FlowingFluid.BlockStatePairKey> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<FlowingFluid.BlockStatePairKey>(
				200
			) {
				@Override
				protected void rehash(int i) {
				}
			};
			object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
			return object2ByteLinkedOpenHashMap;
		}
	);
	private final Map<FluidState, VoxelShape> shapes = Maps.<FluidState, VoxelShape>newIdentityHashMap();

	@Override
	protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
		builder.add(FALLING);
	}

	@Override
	public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState) {
		double d = 0.0;
		double e = 0.0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			mutableBlockPos.setWithOffset(blockPos, direction);
			FluidState fluidState2 = blockGetter.getFluidState(mutableBlockPos);
			if (this.affectsFlow(fluidState2)) {
				float f = fluidState2.getOwnHeight();
				float g = 0.0F;
				if (f == 0.0F) {
					if (!blockGetter.getBlockState(mutableBlockPos).blocksMotion()) {
						BlockPos blockPos2 = mutableBlockPos.below();
						FluidState fluidState3 = blockGetter.getFluidState(blockPos2);
						if (this.affectsFlow(fluidState3)) {
							f = fluidState3.getOwnHeight();
							if (f > 0.0F) {
								g = fluidState.getOwnHeight() - (f - 0.8888889F);
							}
						}
					}
				} else if (f > 0.0F) {
					g = fluidState.getOwnHeight() - f;
				}

				if (g != 0.0F) {
					d += (double)((float)direction.getStepX() * g);
					e += (double)((float)direction.getStepZ() * g);
				}
			}
		}

		Vec3 vec3 = new Vec3(d, 0.0, e);
		if ((Boolean)fluidState.getValue(FALLING)) {
			for (Direction direction2 : Direction.Plane.HORIZONTAL) {
				mutableBlockPos.setWithOffset(blockPos, direction2);
				if (this.isSolidFace(blockGetter, mutableBlockPos, direction2) || this.isSolidFace(blockGetter, mutableBlockPos.above(), direction2)) {
					vec3 = vec3.normalize().add(0.0, -6.0, 0.0);
					break;
				}
			}
		}

		return vec3.normalize();
	}

	private boolean affectsFlow(FluidState fluidState) {
		return fluidState.isEmpty() || fluidState.getType().isSame(this);
	}

	protected boolean isSolidFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		FluidState fluidState = blockGetter.getFluidState(blockPos);
		if (fluidState.getType().isSame(this)) {
			return false;
		} else if (direction == Direction.UP) {
			return true;
		} else {
			return blockState.getBlock() instanceof IceBlock ? false : blockState.isFaceSturdy(blockGetter, blockPos, direction);
		}
	}

	protected void spread(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
		if (!fluidState.isEmpty()) {
			BlockPos blockPos2 = blockPos.below();
			BlockState blockState2 = serverLevel.getBlockState(blockPos2);
			FluidState fluidState2 = blockState2.getFluidState();
			if (this.canMaybePassThrough(serverLevel, blockPos, blockState, Direction.DOWN, blockPos2, blockState2, fluidState2)) {
				FluidState fluidState3 = this.getNewLiquid(serverLevel, blockPos2, blockState2);
				Fluid fluid = fluidState3.getType();
				if (fluidState2.canBeReplacedWith(serverLevel, blockPos2, fluid, Direction.DOWN) && canHoldSpecificFluid(serverLevel, blockPos2, blockState2, fluid)) {
					this.spreadTo(serverLevel, blockPos2, blockState2, Direction.DOWN, fluidState3);
					if (this.sourceNeighborCount(serverLevel, blockPos) >= 3) {
						this.spreadToSides(serverLevel, blockPos, fluidState, blockState);
					}

					return;
				}
			}

			if (fluidState.isSource() || !this.isWaterHole(serverLevel, blockPos, blockState, blockPos2, blockState2)) {
				this.spreadToSides(serverLevel, blockPos, fluidState, blockState);
			}
		}
	}

	private void spreadToSides(ServerLevel serverLevel, BlockPos blockPos, FluidState fluidState, BlockState blockState) {
		int i = fluidState.getAmount() - this.getDropOff(serverLevel);
		if ((Boolean)fluidState.getValue(FALLING)) {
			i = 7;
		}

		if (i > 0) {
			Map<Direction, FluidState> map = this.getSpread(serverLevel, blockPos, blockState);

			for (Entry<Direction, FluidState> entry : map.entrySet()) {
				Direction direction = (Direction)entry.getKey();
				FluidState fluidState2 = (FluidState)entry.getValue();
				BlockPos blockPos2 = blockPos.relative(direction);
				this.spreadTo(serverLevel, blockPos2, serverLevel.getBlockState(blockPos2), direction, fluidState2);
			}
		}
	}

	protected FluidState getNewLiquid(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
		int i = 0;
		int j = 0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = mutableBlockPos.setWithOffset(blockPos, direction);
			BlockState blockState2 = serverLevel.getBlockState(blockPos2);
			FluidState fluidState = blockState2.getFluidState();
			if (fluidState.getType().isSame(this) && canPassThroughWall(direction, serverLevel, blockPos, blockState, blockPos2, blockState2)) {
				if (fluidState.isSource()) {
					j++;
				}

				i = Math.max(i, fluidState.getAmount());
			}
		}

		if (j >= 2 && this.canConvertToSource(serverLevel)) {
			BlockState blockState3 = serverLevel.getBlockState(mutableBlockPos.setWithOffset(blockPos, Direction.DOWN));
			FluidState fluidState2 = blockState3.getFluidState();
			if (blockState3.isSolid() || this.isSourceBlockOfThisType(fluidState2)) {
				return this.getSource(false);
			}
		}

		BlockPos blockPos3 = mutableBlockPos.setWithOffset(blockPos, Direction.UP);
		BlockState blockState4 = serverLevel.getBlockState(blockPos3);
		FluidState fluidState3 = blockState4.getFluidState();
		if (!fluidState3.isEmpty()
			&& fluidState3.getType().isSame(this)
			&& canPassThroughWall(Direction.UP, serverLevel, blockPos, blockState, blockPos3, blockState4)) {
			return this.getFlowing(8, true);
		} else {
			int k = i - this.getDropOff(serverLevel);
			return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
		}
	}

	private static boolean canPassThroughWall(
		Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2
	) {
		VoxelShape voxelShape = blockState2.getCollisionShape(blockGetter, blockPos2);
		if (voxelShape == Shapes.block()) {
			return false;
		} else {
			VoxelShape voxelShape2 = blockState.getCollisionShape(blockGetter, blockPos);
			if (voxelShape2 == Shapes.block()) {
				return false;
			} else if (voxelShape2 == Shapes.empty() && voxelShape == Shapes.empty()) {
				return true;
			} else {
				Object2ByteLinkedOpenHashMap<FlowingFluid.BlockStatePairKey> object2ByteLinkedOpenHashMap;
				if (!blockState.getBlock().hasDynamicShape() && !blockState2.getBlock().hasDynamicShape()) {
					object2ByteLinkedOpenHashMap = (Object2ByteLinkedOpenHashMap<FlowingFluid.BlockStatePairKey>)OCCLUSION_CACHE.get();
				} else {
					object2ByteLinkedOpenHashMap = null;
				}

				FlowingFluid.BlockStatePairKey blockStatePairKey;
				if (object2ByteLinkedOpenHashMap != null) {
					blockStatePairKey = new FlowingFluid.BlockStatePairKey(blockState, blockState2, direction);
					byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(blockStatePairKey);
					if (b != 127) {
						return b != 0;
					}
				} else {
					blockStatePairKey = null;
				}

				boolean bl = !Shapes.mergedFaceOccludes(voxelShape2, voxelShape, direction);
				if (object2ByteLinkedOpenHashMap != null) {
					if (object2ByteLinkedOpenHashMap.size() == 200) {
						object2ByteLinkedOpenHashMap.removeLastByte();
					}

					object2ByteLinkedOpenHashMap.putAndMoveToFirst(blockStatePairKey, (byte)(bl ? 1 : 0));
				}

				return bl;
			}
		}
	}

	public abstract Fluid getFlowing();

	public FluidState getFlowing(int i, boolean bl) {
		return this.getFlowing().defaultFluidState().setValue(LEVEL, Integer.valueOf(i)).setValue(FALLING, Boolean.valueOf(bl));
	}

	public abstract Fluid getSource();

	public FluidState getSource(boolean bl) {
		return this.getSource().defaultFluidState().setValue(FALLING, Boolean.valueOf(bl));
	}

	protected abstract boolean canConvertToSource(ServerLevel serverLevel);

	protected void spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState) {
		if (blockState.getBlock() instanceof LiquidBlockContainer liquidBlockContainer) {
			liquidBlockContainer.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
		} else {
			if (!blockState.isAir()) {
				this.beforeDestroyingBlock(levelAccessor, blockPos, blockState);
			}

			levelAccessor.setBlock(blockPos, fluidState.createLegacyBlock(), 3);
		}
	}

	protected abstract void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState);

	protected int getSlopeDistance(
		LevelReader levelReader, BlockPos blockPos, int i, Direction direction, BlockState blockState, FlowingFluid.SpreadContext spreadContext
	) {
		int j = 1000;

		for (Direction direction2 : Direction.Plane.HORIZONTAL) {
			if (direction2 != direction) {
				BlockPos blockPos2 = blockPos.relative(direction2);
				BlockState blockState2 = spreadContext.getBlockState(blockPos2);
				FluidState fluidState = blockState2.getFluidState();
				if (this.canPassThrough(levelReader, this.getFlowing(), blockPos, blockState, direction2, blockPos2, blockState2, fluidState)) {
					if (spreadContext.isHole(blockPos2)) {
						return i;
					}

					if (i < this.getSlopeFindDistance(levelReader)) {
						int k = this.getSlopeDistance(levelReader, blockPos2, i + 1, direction2.getOpposite(), blockState2, spreadContext);
						if (k < j) {
							j = k;
						}
					}
				}
			}
		}

		return j;
	}

	boolean isWaterHole(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
		if (!canPassThroughWall(Direction.DOWN, blockGetter, blockPos, blockState, blockPos2, blockState2)) {
			return false;
		} else {
			return blockState2.getFluidState().getType().isSame(this) ? true : canHoldFluid(blockGetter, blockPos2, blockState2, this.getFlowing());
		}
	}

	private boolean canPassThrough(
		BlockGetter blockGetter,
		Fluid fluid,
		BlockPos blockPos,
		BlockState blockState,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		FluidState fluidState
	) {
		return this.canMaybePassThrough(blockGetter, blockPos, blockState, direction, blockPos2, blockState2, fluidState)
			&& canHoldSpecificFluid(blockGetter, blockPos2, blockState2, fluid);
	}

	private boolean canMaybePassThrough(
		BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState
	) {
		return !this.isSourceBlockOfThisType(fluidState)
			&& canHoldAnyFluid(blockState2)
			&& canPassThroughWall(direction, blockGetter, blockPos, blockState, blockPos2, blockState2);
	}

	private boolean isSourceBlockOfThisType(FluidState fluidState) {
		return fluidState.getType().isSame(this) && fluidState.isSource();
	}

	protected abstract int getSlopeFindDistance(LevelReader levelReader);

	private int sourceNeighborCount(LevelReader levelReader, BlockPos blockPos) {
		int i = 0;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = blockPos.relative(direction);
			FluidState fluidState = levelReader.getFluidState(blockPos2);
			if (this.isSourceBlockOfThisType(fluidState)) {
				i++;
			}
		}

		return i;
	}

	protected Map<Direction, FluidState> getSpread(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
		int i = 1000;
		Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
		FlowingFluid.SpreadContext spreadContext = null;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = blockPos.relative(direction);
			BlockState blockState2 = serverLevel.getBlockState(blockPos2);
			FluidState fluidState = blockState2.getFluidState();
			if (this.canMaybePassThrough(serverLevel, blockPos, blockState, direction, blockPos2, blockState2, fluidState)) {
				FluidState fluidState2 = this.getNewLiquid(serverLevel, blockPos2, blockState2);
				if (canHoldSpecificFluid(serverLevel, blockPos2, blockState2, fluidState2.getType())) {
					if (spreadContext == null) {
						spreadContext = new FlowingFluid.SpreadContext(serverLevel, blockPos);
					}

					int j;
					if (spreadContext.isHole(blockPos2)) {
						j = 0;
					} else {
						j = this.getSlopeDistance(serverLevel, blockPos2, 1, direction.getOpposite(), blockState2, spreadContext);
					}

					if (j < i) {
						map.clear();
					}

					if (j <= i) {
						if (fluidState.canBeReplacedWith(serverLevel, blockPos2, fluidState2.getType(), direction)) {
							map.put(direction, fluidState2);
						}

						i = j;
					}
				}
			}
		}

		return map;
	}

	private static boolean canHoldAnyFluid(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof LiquidBlockContainer) {
			return true;
		} else {
			return blockState.blocksMotion()
				? false
				: !(block instanceof DoorBlock)
					&& !blockState.is(BlockTags.SIGNS)
					&& !blockState.is(Blocks.LADDER)
					&& !blockState.is(Blocks.SUGAR_CANE)
					&& !blockState.is(Blocks.BUBBLE_COLUMN)
					&& !blockState.is(Blocks.NETHER_PORTAL)
					&& !blockState.is(Blocks.END_PORTAL)
					&& !blockState.is(Blocks.END_GATEWAY)
					&& !blockState.is(Blocks.STRUCTURE_VOID);
		}
	}

	private static boolean canHoldFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
		return canHoldAnyFluid(blockState) && canHoldSpecificFluid(blockGetter, blockPos, blockState, fluid);
	}

	private static boolean canHoldSpecificFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
		return blockState.getBlock() instanceof LiquidBlockContainer liquidBlockContainer
			? liquidBlockContainer.canPlaceLiquid(null, blockGetter, blockPos, blockState, fluid)
			: true;
	}

	protected abstract int getDropOff(LevelReader levelReader);

	protected int getSpreadDelay(Level level, BlockPos blockPos, FluidState fluidState, FluidState fluidState2) {
		return this.getTickDelay(level);
	}

	@Override
	public void tick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
		if (!fluidState.isSource()) {
			FluidState fluidState2 = this.getNewLiquid(serverLevel, blockPos, serverLevel.getBlockState(blockPos));
			int i = this.getSpreadDelay(serverLevel, blockPos, fluidState, fluidState2);
			if (fluidState2.isEmpty()) {
				fluidState = fluidState2;
				blockState = Blocks.AIR.defaultBlockState();
				serverLevel.setBlock(blockPos, blockState, 3);
			} else if (!fluidState2.equals(fluidState)) {
				fluidState = fluidState2;
				blockState = fluidState2.createLegacyBlock();
				serverLevel.setBlock(blockPos, blockState, 3);
				serverLevel.scheduleTick(blockPos, fluidState2.getType(), i);
			}
		}

		this.spread(serverLevel, blockPos, blockState, fluidState);
	}

	protected static int getLegacyLevel(FluidState fluidState) {
		return fluidState.isSource() ? 0 : 8 - Math.min(fluidState.getAmount(), 8) + (fluidState.getValue(FALLING) ? 8 : 0);
	}

	private static boolean hasSameAbove(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
		return fluidState.getType().isSame(blockGetter.getFluidState(blockPos.above()).getType());
	}

	@Override
	public float getHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
		return hasSameAbove(fluidState, blockGetter, blockPos) ? 1.0F : fluidState.getOwnHeight();
	}

	@Override
	public float getOwnHeight(FluidState fluidState) {
		return (float)fluidState.getAmount() / 9.0F;
	}

	@Override
	public abstract int getAmount(FluidState fluidState);

	@Override
	public VoxelShape getShape(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
		return fluidState.getAmount() == 9 && hasSameAbove(fluidState, blockGetter, blockPos)
			? Shapes.block()
			: (VoxelShape)this.shapes
				.computeIfAbsent(fluidState, fluidStatex -> Shapes.box(0.0, 0.0, 0.0, 1.0, (double)fluidStatex.getHeight(blockGetter, blockPos), 1.0));
	}

	static record BlockStatePairKey(BlockState first, BlockState second, Direction direction) {
		public boolean equals(Object object) {
			if (object instanceof FlowingFluid.BlockStatePairKey blockStatePairKey
				&& this.first == blockStatePairKey.first
				&& this.second == blockStatePairKey.second
				&& this.direction == blockStatePairKey.direction) {
				return true;
			}

			return false;
		}

		public int hashCode() {
			int i = System.identityHashCode(this.first);
			i = 31 * i + System.identityHashCode(this.second);
			return 31 * i + this.direction.hashCode();
		}
	}

	protected class SpreadContext {
		private final BlockGetter level;
		private final BlockPos origin;
		private final Short2ObjectMap<BlockState> stateCache = new Short2ObjectOpenHashMap<>();
		private final Short2BooleanMap holeCache = new Short2BooleanOpenHashMap();

		SpreadContext(final BlockGetter blockGetter, final BlockPos blockPos) {
			this.level = blockGetter;
			this.origin = blockPos;
		}

		public BlockState getBlockState(BlockPos blockPos) {
			return this.getBlockState(blockPos, this.getCacheKey(blockPos));
		}

		private BlockState getBlockState(BlockPos blockPos, short s) {
			return this.stateCache.computeIfAbsent(s, (Short2ObjectFunction<? extends BlockState>)(sx -> this.level.getBlockState(blockPos)));
		}

		public boolean isHole(BlockPos blockPos) {
			return this.holeCache.computeIfAbsent(this.getCacheKey(blockPos), (Short2BooleanFunction)(s -> {
				BlockState blockState = this.getBlockState(blockPos, s);
				BlockPos blockPos2 = blockPos.below();
				BlockState blockState2 = this.level.getBlockState(blockPos2);
				return FlowingFluid.this.isWaterHole(this.level, blockPos, blockState, blockPos2, blockState2);
			}));
		}

		private short getCacheKey(BlockPos blockPos) {
			int i = blockPos.getX() - this.origin.getX();
			int j = blockPos.getZ() - this.origin.getZ();
			return (short)((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
		}
	}
}
