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
import net.minecraft.world.level.levelgen.Heightmap;
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
		Vec3 vec3 = new Vec3((double)blockPos2.getX() + 0.5, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5);
		if (bl) {
			this.createEndPlatform(serverLevel2, BlockPos.containing(vec3).below());
		} else {
			if (entity instanceof ServerPlayer serverPlayer) {
				return serverPlayer.findRespawnPositionAndUseSpawnBlock(false);
			}

			int i = serverLevel2.getChunkAt(blockPos2).getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos2.getX(), blockPos2.getZ()) + 1;
			vec3 = new Vec3(vec3.x, (double)i, vec3.z);
		}

		return new DimensionTransition(serverLevel2, vec3, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
	}

	private void createEndPlatform(ServerLevel serverLevel, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				for (int k = -1; k < 3; k++) {
					BlockState blockState = k == -1 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
					serverLevel.setBlockAndUpdate(mutableBlockPos.set(blockPos).move(j, k, i), blockState);
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
