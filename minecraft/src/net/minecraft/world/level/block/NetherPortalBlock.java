package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class NetherPortalBlock extends Block implements Portal {
	public static final MapCodec<NetherPortalBlock> CODEC = simpleCodec(NetherPortalBlock::new);
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	private static final Logger LOGGER = LogUtils.getLogger();
	protected static final int AABB_OFFSET = 2;
	protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
	protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

	@Override
	public MapCodec<NetherPortalBlock> codec() {
		return CODEC;
	}

	public NetherPortalBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		switch ((Direction.Axis)blockState.getValue(AXIS)) {
			case Z:
				return Z_AXIS_AABB;
			case X:
			default:
				return X_AXIS_AABB;
		}
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (serverLevel.dimensionType().natural()
			&& serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
			&& randomSource.nextInt(2000) < serverLevel.getDifficulty().getId()) {
			while (serverLevel.getBlockState(blockPos).is(this)) {
				blockPos = blockPos.below();
			}

			if (serverLevel.getBlockState(blockPos).isValidSpawn(serverLevel, blockPos, EntityType.ZOMBIFIED_PIGLIN)) {
				Entity entity = EntityType.ZOMBIFIED_PIGLIN.spawn(serverLevel, blockPos.above(), MobSpawnType.STRUCTURE);
				if (entity != null) {
					entity.setPortalCooldown();
				}
			}
		}
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		Direction.Axis axis = direction.getAxis();
		Direction.Axis axis2 = blockState.getValue(AXIS);
		boolean bl = axis2 != axis && axis.isHorizontal();
		return !bl && !blockState2.is(this) && !new PortalShape(levelAccessor, blockPos, axis2).isComplete()
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity.canUsePortal(false)) {
			entity.setAsInsidePortal(this, blockPos);
		}
	}

	@Override
	public int getPortalTransitionTime(ServerLevel serverLevel, Entity entity) {
		return entity instanceof Player player
			? Math.max(
				1,
				serverLevel.getGameRules()
					.getInt(player.getAbilities().invulnerable ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY)
			)
			: 0;
	}

	@Nullable
	@Override
	public DimensionTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos) {
		ResourceKey<Level> resourceKey = serverLevel.dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
		ServerLevel serverLevel2 = serverLevel.getServer().getLevel(resourceKey);
		boolean bl = serverLevel2.dimension() == Level.NETHER;
		WorldBorder worldBorder = serverLevel2.getWorldBorder();
		double d = DimensionType.getTeleportationScale(serverLevel.dimensionType(), serverLevel2.dimensionType());
		BlockPos blockPos2 = worldBorder.clampToBounds(entity.getX() * d, entity.getY(), entity.getZ() * d);
		return this.getExitPortal(serverLevel2, entity, blockPos, blockPos2, bl, worldBorder);
	}

	@Nullable
	private DimensionTransition getExitPortal(ServerLevel serverLevel, Entity entity, BlockPos blockPos, BlockPos blockPos2, boolean bl, WorldBorder worldBorder) {
		Optional<BlockPos> optional = serverLevel.getPortalForcer().findClosestPortalPosition(blockPos2, bl, worldBorder);
		BlockUtil.FoundRectangle foundRectangle;
		DimensionTransition.PostDimensionTransition postDimensionTransition;
		if (optional.isPresent()) {
			BlockPos blockPos3 = (BlockPos)optional.get();
			BlockState blockState = serverLevel.getBlockState(blockPos3);
			foundRectangle = BlockUtil.getLargestRectangleAround(
				blockPos3,
				blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS),
				21,
				Direction.Axis.Y,
				21,
				blockPosx -> serverLevel.getBlockState(blockPosx) == blockState
			);
			postDimensionTransition = DimensionTransition.PLAY_PORTAL_SOUND.then(entityx -> entityx.placePortalTicket(blockPos3));
		} else {
			Direction.Axis axis = (Direction.Axis)entity.level().getBlockState(blockPos).getOptionalValue(AXIS).orElse(Direction.Axis.X);
			Optional<BlockUtil.FoundRectangle> optional2 = serverLevel.getPortalForcer().createPortal(blockPos2, axis);
			if (optional2.isEmpty()) {
				LOGGER.error("Unable to create a portal, likely target out of worldborder");
				return null;
			}

			foundRectangle = (BlockUtil.FoundRectangle)optional2.get();
			postDimensionTransition = DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET);
		}

		return getDimensionTransitionFromExit(entity, blockPos, foundRectangle, serverLevel, postDimensionTransition);
	}

	private static DimensionTransition getDimensionTransitionFromExit(
		Entity entity,
		BlockPos blockPos,
		BlockUtil.FoundRectangle foundRectangle,
		ServerLevel serverLevel,
		DimensionTransition.PostDimensionTransition postDimensionTransition
	) {
		BlockState blockState = entity.level().getBlockState(blockPos);
		Direction.Axis axis;
		Vec3 vec3;
		if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
			axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
			BlockUtil.FoundRectangle foundRectangle2 = BlockUtil.getLargestRectangleAround(
				blockPos, axis, 21, Direction.Axis.Y, 21, blockPosx -> entity.level().getBlockState(blockPosx) == blockState
			);
			vec3 = entity.getRelativePortalPosition(axis, foundRectangle2);
		} else {
			axis = Direction.Axis.X;
			vec3 = new Vec3(0.5, 0.0, 0.0);
		}

		return createDimensionTransition(
			serverLevel, foundRectangle, axis, vec3, entity, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), postDimensionTransition
		);
	}

	private static DimensionTransition createDimensionTransition(
		ServerLevel serverLevel,
		BlockUtil.FoundRectangle foundRectangle,
		Direction.Axis axis,
		Vec3 vec3,
		Entity entity,
		Vec3 vec32,
		float f,
		float g,
		DimensionTransition.PostDimensionTransition postDimensionTransition
	) {
		BlockPos blockPos = foundRectangle.minCorner;
		BlockState blockState = serverLevel.getBlockState(blockPos);
		Direction.Axis axis2 = (Direction.Axis)blockState.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
		double d = (double)foundRectangle.axis1Size;
		double e = (double)foundRectangle.axis2Size;
		EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
		int i = axis == axis2 ? 0 : 90;
		Vec3 vec33 = axis == axis2 ? vec32 : new Vec3(vec32.z, vec32.y, -vec32.x);
		double h = (double)entityDimensions.width() / 2.0 + (d - (double)entityDimensions.width()) * vec3.x();
		double j = (e - (double)entityDimensions.height()) * vec3.y();
		double k = 0.5 + vec3.z();
		boolean bl = axis2 == Direction.Axis.X;
		Vec3 vec34 = new Vec3((double)blockPos.getX() + (bl ? h : k), (double)blockPos.getY() + j, (double)blockPos.getZ() + (bl ? k : h));
		Vec3 vec35 = PortalShape.findCollisionFreePosition(vec34, serverLevel, entity, entityDimensions);
		return new DimensionTransition(serverLevel, vec35, vec33, f + (float)i, g, postDimensionTransition);
	}

	@Override
	public Portal.Transition getLocalTransition() {
		return Portal.Transition.CONFUSION;
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextInt(100) == 0) {
			level.playLocalSound(
				(double)blockPos.getX() + 0.5,
				(double)blockPos.getY() + 0.5,
				(double)blockPos.getZ() + 0.5,
				SoundEvents.PORTAL_AMBIENT,
				SoundSource.BLOCKS,
				0.5F,
				randomSource.nextFloat() * 0.4F + 0.8F,
				false
			);
		}

		for (int i = 0; i < 4; i++) {
			double d = (double)blockPos.getX() + randomSource.nextDouble();
			double e = (double)blockPos.getY() + randomSource.nextDouble();
			double f = (double)blockPos.getZ() + randomSource.nextDouble();
			double g = ((double)randomSource.nextFloat() - 0.5) * 0.5;
			double h = ((double)randomSource.nextFloat() - 0.5) * 0.5;
			double j = ((double)randomSource.nextFloat() - 0.5) * 0.5;
			int k = randomSource.nextInt(2) * 2 - 1;
			if (!level.getBlockState(blockPos.west()).is(this) && !level.getBlockState(blockPos.east()).is(this)) {
				d = (double)blockPos.getX() + 0.5 + 0.25 * (double)k;
				g = (double)(randomSource.nextFloat() * 2.0F * (float)k);
			} else {
				f = (double)blockPos.getZ() + 0.5 + 0.25 * (double)k;
				j = (double)(randomSource.nextFloat() * 2.0F * (float)k);
			}

			level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, j);
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
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
}
