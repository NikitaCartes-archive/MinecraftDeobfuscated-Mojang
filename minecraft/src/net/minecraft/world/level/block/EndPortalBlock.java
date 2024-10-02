package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
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
	protected VoxelShape getEntityInsideCollisionShape(BlockState blockState, Level level, BlockPos blockPos) {
		return blockState.getShape(level, blockPos);
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity.canUsePortal(false)) {
			if (!level.isClientSide && level.dimension() == Level.END && entity instanceof ServerPlayer serverPlayer && !serverPlayer.seenCredits) {
				serverPlayer.showEndCredits();
				return;
			}

			entity.setAsInsidePortal(this, blockPos);
		}
	}

	@Override
	public TeleportTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos) {
		ResourceKey<Level> resourceKey = serverLevel.dimension() == Level.END ? Level.OVERWORLD : Level.END;
		ServerLevel serverLevel2 = serverLevel.getServer().getLevel(resourceKey);
		if (serverLevel2 == null) {
			return null;
		} else {
			boolean bl = resourceKey == Level.END;
			BlockPos blockPos2 = bl ? ServerLevel.END_SPAWN_POINT : serverLevel2.getSharedSpawnPos();
			Vec3 vec3 = blockPos2.getBottomCenter();
			float f;
			Set<Relative> set;
			if (bl) {
				EndPlatformFeature.createEndPlatform(serverLevel2, BlockPos.containing(vec3).below(), true);
				f = Direction.WEST.toYRot();
				set = Relative.union(Relative.DELTA, Set.of(Relative.X_ROT));
				if (entity instanceof ServerPlayer) {
					vec3 = vec3.subtract(0.0, 1.0, 0.0);
				}
			} else {
				f = 0.0F;
				set = Relative.union(Relative.DELTA, Relative.ROTATION);
				if (entity instanceof ServerPlayer serverPlayer) {
					return serverPlayer.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
				}

				vec3 = entity.adjustSpawnLocation(serverLevel2, blockPos2).getBottomCenter();
			}

			return new TeleportTransition(serverLevel2, vec3, Vec3.ZERO, f, 0.0F, set, TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
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
