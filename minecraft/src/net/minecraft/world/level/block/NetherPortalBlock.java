package net.minecraft.world.level.block;

import com.google.common.cache.LoadingCache;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherPortalBlock extends Block {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
	protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

	public NetherPortalBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		switch ((Direction.Axis)blockState.getValue(AXIS)) {
			case Z:
				return Z_AXIS_AABB;
			case X:
			default:
				return X_AXIS_AABB;
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (serverLevel.dimension.isNaturalDimension()
			&& serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
			&& random.nextInt(2000) < serverLevel.getDifficulty().getId()) {
			while (serverLevel.getBlockState(blockPos).getBlock() == this) {
				blockPos = blockPos.below();
			}

			if (serverLevel.getBlockState(blockPos).isValidSpawn(serverLevel, blockPos, EntityType.ZOMBIE_PIGMAN)) {
				Entity entity = EntityType.ZOMBIE_PIGMAN.spawn(serverLevel, null, null, null, blockPos.above(), MobSpawnType.STRUCTURE, false, false);
				if (entity != null) {
					entity.changingDimensionDelay = entity.getDimensionChangingDelay();
				}
			}
		}
	}

	public boolean trySpawnPortal(LevelAccessor levelAccessor, BlockPos blockPos) {
		NetherPortalBlock.PortalShape portalShape = this.isPortal(levelAccessor, blockPos);
		if (portalShape != null) {
			portalShape.createPortalBlocks();
			return true;
		} else {
			return false;
		}
	}

	@Nullable
	public NetherPortalBlock.PortalShape isPortal(LevelAccessor levelAccessor, BlockPos blockPos) {
		NetherPortalBlock.PortalShape portalShape = new NetherPortalBlock.PortalShape(levelAccessor, blockPos, Direction.Axis.X);
		if (portalShape.isValid() && portalShape.numPortalBlocks == 0) {
			return portalShape;
		} else {
			NetherPortalBlock.PortalShape portalShape2 = new NetherPortalBlock.PortalShape(levelAccessor, blockPos, Direction.Axis.Z);
			return portalShape2.isValid() && portalShape2.numPortalBlocks == 0 ? portalShape2 : null;
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		Direction.Axis axis = direction.getAxis();
		Direction.Axis axis2 = blockState.getValue(AXIS);
		boolean bl = axis2 != axis && axis.isHorizontal();
		return !bl && blockState2.getBlock() != this && !new NetherPortalBlock.PortalShape(levelAccessor, blockPos, axis2).isComplete()
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public BlockLayer getRenderLayer() {
		return BlockLayer.TRANSLUCENT;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!entity.isPassenger() && !entity.isVehicle() && entity.canChangeDimensions()) {
			entity.handleInsidePortal(blockPos);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if (random.nextInt(100) == 0) {
			level.playLocalSound(
				(double)blockPos.getX() + 0.5,
				(double)blockPos.getY() + 0.5,
				(double)blockPos.getZ() + 0.5,
				SoundEvents.PORTAL_AMBIENT,
				SoundSource.BLOCKS,
				0.5F,
				random.nextFloat() * 0.4F + 0.8F,
				false
			);
		}

		for (int i = 0; i < 4; i++) {
			double d = (double)((float)blockPos.getX() + random.nextFloat());
			double e = (double)((float)blockPos.getY() + random.nextFloat());
			double f = (double)((float)blockPos.getZ() + random.nextFloat());
			double g = ((double)random.nextFloat() - 0.5) * 0.5;
			double h = ((double)random.nextFloat() - 0.5) * 0.5;
			double j = ((double)random.nextFloat() - 0.5) * 0.5;
			int k = random.nextInt(2) * 2 - 1;
			if (level.getBlockState(blockPos.west()).getBlock() != this && level.getBlockState(blockPos.east()).getBlock() != this) {
				d = (double)blockPos.getX() + 0.5 + 0.25 * (double)k;
				g = (double)(random.nextFloat() * 2.0F * (float)k);
			} else {
				f = (double)blockPos.getZ() + 0.5 + 0.25 * (double)k;
				j = (double)(random.nextFloat() * 2.0F * (float)k);
			}

			level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, j);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:
				switch ((Direction.Axis)blockState.getValue(AXIS)) {
					case Z:
						return blockState.setValue(AXIS, Direction.Axis.X);
					case X:
						return blockState.setValue(AXIS, Direction.Axis.Z);
					default:
						return blockState;
				}
			default:
				return blockState;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	public static BlockPattern.BlockPatternMatch getPortalShape(LevelAccessor levelAccessor, BlockPos blockPos) {
		Direction.Axis axis = Direction.Axis.Z;
		NetherPortalBlock.PortalShape portalShape = new NetherPortalBlock.PortalShape(levelAccessor, blockPos, Direction.Axis.X);
		LoadingCache<BlockPos, BlockInWorld> loadingCache = BlockPattern.createLevelCache(levelAccessor, true);
		if (!portalShape.isValid()) {
			axis = Direction.Axis.X;
			portalShape = new NetherPortalBlock.PortalShape(levelAccessor, blockPos, Direction.Axis.Z);
		}

		if (!portalShape.isValid()) {
			return new BlockPattern.BlockPatternMatch(blockPos, Direction.NORTH, Direction.UP, loadingCache, 1, 1, 1);
		} else {
			int[] is = new int[Direction.AxisDirection.values().length];
			Direction direction = portalShape.rightDir.getCounterClockWise();
			BlockPos blockPos2 = portalShape.bottomLeft.above(portalShape.getHeight() - 1);

			for (Direction.AxisDirection axisDirection : Direction.AxisDirection.values()) {
				BlockPattern.BlockPatternMatch blockPatternMatch = new BlockPattern.BlockPatternMatch(
					direction.getAxisDirection() == axisDirection ? blockPos2 : blockPos2.relative(portalShape.rightDir, portalShape.getWidth() - 1),
					Direction.get(axisDirection, axis),
					Direction.UP,
					loadingCache,
					portalShape.getWidth(),
					portalShape.getHeight(),
					1
				);

				for (int i = 0; i < portalShape.getWidth(); i++) {
					for (int j = 0; j < portalShape.getHeight(); j++) {
						BlockInWorld blockInWorld = blockPatternMatch.getBlock(i, j, 1);
						if (!blockInWorld.getState().isAir()) {
							is[axisDirection.ordinal()]++;
						}
					}
				}
			}

			Direction.AxisDirection axisDirection2 = Direction.AxisDirection.POSITIVE;

			for (Direction.AxisDirection axisDirection3 : Direction.AxisDirection.values()) {
				if (is[axisDirection3.ordinal()] < is[axisDirection2.ordinal()]) {
					axisDirection2 = axisDirection3;
				}
			}

			return new BlockPattern.BlockPatternMatch(
				direction.getAxisDirection() == axisDirection2 ? blockPos2 : blockPos2.relative(portalShape.rightDir, portalShape.getWidth() - 1),
				Direction.get(axisDirection2, axis),
				Direction.UP,
				loadingCache,
				portalShape.getWidth(),
				portalShape.getHeight(),
				1
			);
		}
	}

	public static class PortalShape {
		private final LevelAccessor level;
		private final Direction.Axis axis;
		private final Direction rightDir;
		private final Direction leftDir;
		private int numPortalBlocks;
		@Nullable
		private BlockPos bottomLeft;
		private int height;
		private int width;

		public PortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis) {
			this.level = levelAccessor;
			this.axis = axis;
			if (axis == Direction.Axis.X) {
				this.leftDir = Direction.EAST;
				this.rightDir = Direction.WEST;
			} else {
				this.leftDir = Direction.NORTH;
				this.rightDir = Direction.SOUTH;
			}

			BlockPos blockPos2 = blockPos;

			while (blockPos.getY() > blockPos2.getY() - 21 && blockPos.getY() > 0 && this.isEmpty(levelAccessor.getBlockState(blockPos.below()))) {
				blockPos = blockPos.below();
			}

			int i = this.getDistanceUntilEdge(blockPos, this.leftDir) - 1;
			if (i >= 0) {
				this.bottomLeft = blockPos.relative(this.leftDir, i);
				this.width = this.getDistanceUntilEdge(this.bottomLeft, this.rightDir);
				if (this.width < 2 || this.width > 21) {
					this.bottomLeft = null;
					this.width = 0;
				}
			}

			if (this.bottomLeft != null) {
				this.height = this.calculatePortalHeight();
			}
		}

		protected int getDistanceUntilEdge(BlockPos blockPos, Direction direction) {
			int i;
			for (i = 0; i < 22; i++) {
				BlockPos blockPos2 = blockPos.relative(direction, i);
				if (!this.isEmpty(this.level.getBlockState(blockPos2)) || this.level.getBlockState(blockPos2.below()).getBlock() != Blocks.OBSIDIAN) {
					break;
				}
			}

			Block block = this.level.getBlockState(blockPos.relative(direction, i)).getBlock();
			return block == Blocks.OBSIDIAN ? i : 0;
		}

		public int getHeight() {
			return this.height;
		}

		public int getWidth() {
			return this.width;
		}

		protected int calculatePortalHeight() {
			label56:
			for (this.height = 0; this.height < 21; this.height++) {
				for (int i = 0; i < this.width; i++) {
					BlockPos blockPos = this.bottomLeft.relative(this.rightDir, i).above(this.height);
					BlockState blockState = this.level.getBlockState(blockPos);
					if (!this.isEmpty(blockState)) {
						break label56;
					}

					Block block = blockState.getBlock();
					if (block == Blocks.NETHER_PORTAL) {
						this.numPortalBlocks++;
					}

					if (i == 0) {
						block = this.level.getBlockState(blockPos.relative(this.leftDir)).getBlock();
						if (block != Blocks.OBSIDIAN) {
							break label56;
						}
					} else if (i == this.width - 1) {
						block = this.level.getBlockState(blockPos.relative(this.rightDir)).getBlock();
						if (block != Blocks.OBSIDIAN) {
							break label56;
						}
					}
				}
			}

			for (int i = 0; i < this.width; i++) {
				if (this.level.getBlockState(this.bottomLeft.relative(this.rightDir, i).above(this.height)).getBlock() != Blocks.OBSIDIAN) {
					this.height = 0;
					break;
				}
			}

			if (this.height <= 21 && this.height >= 3) {
				return this.height;
			} else {
				this.bottomLeft = null;
				this.width = 0;
				this.height = 0;
				return 0;
			}
		}

		protected boolean isEmpty(BlockState blockState) {
			Block block = blockState.getBlock();
			return blockState.isAir() || block == Blocks.FIRE || block == Blocks.NETHER_PORTAL;
		}

		public boolean isValid() {
			return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
		}

		public void createPortalBlocks() {
			for (int i = 0; i < this.width; i++) {
				BlockPos blockPos = this.bottomLeft.relative(this.rightDir, i);

				for (int j = 0; j < this.height; j++) {
					this.level.setBlock(blockPos.above(j), Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis), 18);
				}
			}
		}

		private boolean hasAllPortalBlocks() {
			return this.numPortalBlocks >= this.width * this.height;
		}

		public boolean isComplete() {
			return this.isValid() && this.hasAllPortalBlocks();
		}
	}
}
