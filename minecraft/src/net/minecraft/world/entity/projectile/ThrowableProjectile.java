package net.minecraft.world.entity.projectile;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowableProjectile extends Projectile {
	private int xBlock = -1;
	private int yBlock = -1;
	private int zBlock = -1;
	protected boolean inGround;
	private int shakeTime;
	private boolean leftOwner;

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
		if (this.shakeTime > 0) {
			this.shakeTime--;
		}

		if (this.inGround) {
			this.inGround = false;
			this.setDeltaMovement(
				this.getDeltaMovement()
					.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F))
			);
		}

		AABB aABB = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
		Entity entity = this.getOwner();
		if (entity == null) {
			this.leftOwner = true;
		} else if (!this.leftOwner) {
			boolean bl = false;

			for (Entity entity2 : this.level.getEntities(this, aABB, entityx -> !entityx.isSpectator() && entityx.isPickable())) {
				if (this.isEntityOrVehicle(entity2, entity)) {
					bl = true;
					break;
				}
			}

			if (!bl) {
				this.leftOwner = true;
			}
		}

		Predicate<Entity> predicate = entity2x -> !entity2x.isSpectator() && entity2x.isPickable() && (this.leftOwner || !this.isEntityOrVehicle(entity2x, entity));
		HitResult hitResult = ProjectileUtil.getHitResult(this, aABB, predicate, ClipContext.Block.OUTLINE, true);
		if (hitResult.getType() != HitResult.Type.MISS) {
			if (hitResult.getType() == HitResult.Type.BLOCK && this.level.getBlockState(((BlockHitResult)hitResult).getBlockPos()).getBlock() == Blocks.NETHER_PORTAL) {
				this.handleInsidePortal(((BlockHitResult)hitResult).getBlockPos());
			} else {
				this.onHit(hitResult);
			}
		}

		Vec3 vec3 = this.getDeltaMovement();
		double d = this.getX() + vec3.x;
		double e = this.getY() + vec3.y;
		double f = this.getZ() + vec3.z;
		float g = Mth.sqrt(getHorizontalDistanceSqr(vec3));
		this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI);
		this.xRot = (float)(Mth.atan2(vec3.y, (double)g) * 180.0F / (float)Math.PI);

		while (this.xRot - this.xRotO < -180.0F) {
			this.xRotO -= 360.0F;
		}

		while (this.xRot - this.xRotO >= 180.0F) {
			this.xRotO += 360.0F;
		}

		while (this.yRot - this.yRotO < -180.0F) {
			this.yRotO -= 360.0F;
		}

		while (this.yRot - this.yRotO >= 180.0F) {
			this.yRotO += 360.0F;
		}

		this.xRot = Mth.lerp(0.2F, this.xRotO, this.xRot);
		this.yRot = Mth.lerp(0.2F, this.yRotO, this.yRot);
		float j;
		if (this.isInWater()) {
			for (int i = 0; i < 4; i++) {
				float h = 0.25F;
				this.level.addParticle(ParticleTypes.BUBBLE, d - vec3.x * 0.25, e - vec3.y * 0.25, f - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
			}

			j = 0.8F;
		} else {
			j = 0.99F;
		}

		this.setDeltaMovement(vec3.scale((double)j));
		if (!this.isNoGravity()) {
			Vec3 vec32 = this.getDeltaMovement();
			this.setDeltaMovement(vec32.x, vec32.y - (double)this.getGravity(), vec32.z);
		}

		this.setPos(d, e, f);
	}

	private boolean isEntityOrVehicle(Entity entity, Entity entity2) {
		return entity == entity2 || entity.getPassengers().contains(entity2);
	}

	protected float getGravity() {
		return 0.03F;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("xTile", this.xBlock);
		compoundTag.putInt("yTile", this.yBlock);
		compoundTag.putInt("zTile", this.zBlock);
		compoundTag.putByte("shake", (byte)this.shakeTime);
		compoundTag.putBoolean("inGround", this.inGround);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.xBlock = compoundTag.getInt("xTile");
		this.yBlock = compoundTag.getInt("yTile");
		this.zBlock = compoundTag.getInt("zTile");
		this.shakeTime = compoundTag.getByte("shake") & 255;
		this.inGround = compoundTag.getBoolean("inGround");
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}
