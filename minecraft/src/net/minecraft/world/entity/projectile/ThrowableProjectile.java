package net.minecraft.world.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowableProjectile extends Projectile {
	protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, Level level) {
		super(entityType, level);
	}

	protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, double d, double e, double f, Level level) {
		this(entityType, level);
		this.setPos(d, e, f);
	}

	protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, LivingEntity livingEntity, Level level) {
		this(entityType, livingEntity.getX(), livingEntity.getEyeY() - 0.1F, livingEntity.getZ(), level);
		this.setOwner(livingEntity);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize() * 4.0;
		if (Double.isNaN(e)) {
			e = 4.0;
		}

		e *= 64.0;
		return d < e * e;
	}

	@Override
	public void tick() {
		super.tick();
		HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
		boolean bl = false;
		if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
			BlockState blockState = this.level.getBlockState(blockPos);
			if (blockState.is(Blocks.NETHER_PORTAL)) {
				this.handleInsidePortal(blockPos);
				bl = true;
			} else if (blockState.is(Blocks.END_GATEWAY)) {
				BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
				if (blockEntity instanceof TheEndGatewayBlockEntity && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
					((TheEndGatewayBlockEntity)blockEntity).teleportEntity(this);
				}

				bl = true;
			}
		}

		if (hitResult.getType() != HitResult.Type.MISS && !bl) {
			this.onHit(hitResult);
		}

		this.checkInsideBlocks();
		Vec3 vec3 = this.getDeltaMovement();
		double d = this.getX() + vec3.x;
		double e = this.getY() + vec3.y;
		double f = this.getZ() + vec3.z;
		this.updateRotation();
		float h;
		if (this.isInWater()) {
			for (int i = 0; i < 4; i++) {
				float g = 0.25F;
				this.level.addParticle(ParticleTypes.BUBBLE, d - vec3.x * 0.25, e - vec3.y * 0.25, f - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
			}

			h = 0.8F;
		} else {
			h = 0.99F;
		}

		this.setDeltaMovement(vec3.scale((double)h));
		if (!this.isNoGravity()) {
			Vec3 vec32 = this.getDeltaMovement();
			this.setDeltaMovement(vec32.x, vec32.y - (double)this.getGravity(), vec32.z);
		}

		this.setPos(d, e, f);
	}

	protected float getGravity() {
		return 0.03F;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}
