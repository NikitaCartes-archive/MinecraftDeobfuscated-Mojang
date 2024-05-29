package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EndPortalBlock extends BaseEntityBlock implements Portal {
	public static final MapCodec<EndPortalBlock> CODEC = simpleCodec(EndPortalBlock::new);
	protected static final VoxelShape SHAPE = Block.box(0.0, 6.0, 0.0, 16.0, 12.0, 16.0);

	@Override
	public MapCodec<EndPortalBlock> codec() {
		return CODEC;
	}

	protected EndPortalBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new TheEndPortalBlockEntity(blockPos, blockState);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity.canChangeDimensions()
			&& Shapes.joinIsNotEmpty(
				Shapes.create(entity.getBoundingBox().move((double)(-blockPos.getX()), (double)(-blockPos.getY()), (double)(-blockPos.getZ()))),
				blockState.getShape(level, blockPos),
				BooleanOp.AND
			)) {
			if (!level.isClientSide && level.dimension() == Level.END && entity instanceof ServerPlayer serverPlayer && !serverPlayer.seenCredits) {
				serverPlayer.showEndCredits();
				return;
			}

			entity.setAsInsidePortal(this, blockPos);
		}
	}

	@Override
	public DimensionTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos) {
		ResourceKey<Level> resourceKey = serverLevel.dimension() == Level.END ? Level.OVERWORLD : Level.END;
		ServerLevel serverLevel2 = serverLevel.getServer().getLevel(resourceKey);
		boolean bl = resourceKey == Level.END;
		BlockPos blockPos2 = bl ? ServerLevel.END_SPAWN_POINT : serverLevel2.getSharedSpawnPos();
		Vec3 vec3 = blockPos2.getBottomCenter();
		if (bl) {
			this.createEndPlatform(serverLevel2, BlockPos.containing(vec3).below());
			if (entity instanceof ServerPlayer) {
				vec3 = vec3.subtract(0.0, 1.0, 0.0);
			}
		} else {
			if (entity instanceof ServerPlayer serverPlayer) {
				return serverPlayer.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
			}

			vec3 = entity.adjustSpawnLocation(serverLevel2, blockPos2).getBottomCenter();
		}

		return new DimensionTransition(serverLevel2, vec3, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.PLACE_PORTAL_TICKET);
	}

	private void createEndPlatform(ServerLevel serverLevel, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				for (int k = -1; k < 3; k++) {
					BlockPos blockPos2 = mutableBlockPos.set(blockPos).move(j, k, i);
					Block block = k == -1 ? Blocks.OBSIDIAN : Blocks.AIR;
					if (!serverLevel.getBlockState(blockPos2).is(block)) {
						serverLevel.destroyBlock(blockPos2, true, null);
						serverLevel.setBlockAndUpdate(blockPos2, block.defaultBlockState());
					}
				}
			}
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		double d = (double)blockPos.getX() + randomSource.nextDouble();
		double e = (double)blockPos.getY() + 0.8;
		double f = (double)blockPos.getZ() + randomSource.nextDouble();
		level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}

	@Override
	protected boolean canBeReplaced(BlockState blockState, Fluid fluid) {
		return false;
	}
}
