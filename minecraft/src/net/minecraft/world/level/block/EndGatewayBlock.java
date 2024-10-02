package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public class EndGatewayBlock extends BaseEntityBlock implements Portal {
	public static final MapCodec<EndGatewayBlock> CODEC = simpleCodec(EndGatewayBlock::new);

	@Override
	public MapCodec<EndGatewayBlock> codec() {
		return CODEC;
	}

	protected EndGatewayBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new TheEndGatewayBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(
			blockEntityType, BlockEntityType.END_GATEWAY, level.isClientSide ? TheEndGatewayBlockEntity::beamAnimationTick : TheEndGatewayBlockEntity::portalTick
		);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof TheEndGatewayBlockEntity) {
			int i = ((TheEndGatewayBlockEntity)blockEntity).getParticleAmount();

			for (int j = 0; j < i; j++) {
				double d = (double)blockPos.getX() + randomSource.nextDouble();
				double e = (double)blockPos.getY() + randomSource.nextDouble();
				double f = (double)blockPos.getZ() + randomSource.nextDouble();
				double g = (randomSource.nextDouble() - 0.5) * 0.5;
				double h = (randomSource.nextDouble() - 0.5) * 0.5;
				double k = (randomSource.nextDouble() - 0.5) * 0.5;
				int l = randomSource.nextInt(2) * 2 - 1;
				if (randomSource.nextBoolean()) {
					f = (double)blockPos.getZ() + 0.5 + 0.25 * (double)l;
					k = (double)(randomSource.nextFloat() * 2.0F * (float)l);
				} else {
					d = (double)blockPos.getX() + 0.5 + 0.25 * (double)l;
					g = (double)(randomSource.nextFloat() * 2.0F * (float)l);
				}

				level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, k);
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}

	@Override
	protected boolean canBeReplaced(BlockState blockState, Fluid fluid) {
		return false;
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity.canUsePortal(false)
			&& !level.isClientSide
			&& level.getBlockEntity(blockPos) instanceof TheEndGatewayBlockEntity theEndGatewayBlockEntity
			&& !theEndGatewayBlockEntity.isCoolingDown()) {
			entity.setAsInsidePortal(this, blockPos);
			TheEndGatewayBlockEntity.triggerCooldown(level, blockPos, blockState, theEndGatewayBlockEntity);
		}
	}

	@Nullable
	@Override
	public TeleportTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos) {
		if (serverLevel.getBlockEntity(blockPos) instanceof TheEndGatewayBlockEntity theEndGatewayBlockEntity) {
			Vec3 vec3 = theEndGatewayBlockEntity.getPortalPosition(serverLevel, blockPos);
			if (vec3 == null) {
				return null;
			} else {
				return entity instanceof ThrownEnderpearl
					? new TeleportTransition(serverLevel, vec3, Vec3.ZERO, 0.0F, 0.0F, Set.of(), TeleportTransition.PLACE_PORTAL_TICKET)
					: new TeleportTransition(
						serverLevel, vec3, Vec3.ZERO, 0.0F, 0.0F, Relative.union(Relative.DELTA, Relative.ROTATION), TeleportTransition.PLACE_PORTAL_TICKET
					);
			}
		} else {
			return null;
		}
	}
}
