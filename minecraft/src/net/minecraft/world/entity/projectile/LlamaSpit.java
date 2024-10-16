package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LlamaSpit extends Projectile {
	public LlamaSpit(EntityType<? extends LlamaSpit> entityType, Level level) {
		super(entityType, level);
	}

	public LlamaSpit(Level level, Llama llama) {
		this(EntityType.LLAMA_SPIT, level);
		this.setOwner(llama);
		this.setPos(
			llama.getX() - (double)(llama.getBbWidth() + 1.0F) * 0.5 * (double)Mth.sin(llama.yBodyRot * (float) (Math.PI / 180.0)),
			llama.getEyeY() - 0.1F,
			llama.getZ() + (double)(llama.getBbWidth() + 1.0F) * 0.5 * (double)Mth.cos(llama.yBodyRot * (float) (Math.PI / 180.0))
		);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.06;
	}

	@Override
	public void tick() {
		super.tick();
		Vec3 vec3 = this.getDeltaMovement();
		HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
		this.hitTargetOrDeflectSelf(hitResult);
		double d = this.getX() + vec3.x;
		double e = this.getY() + vec3.y;
		double f = this.getZ() + vec3.z;
		this.updateRotation();
		float g = 0.99F;
		if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
			this.discard();
		} else if (this.isInWaterOrBubble()) {
			this.discard();
		} else {
			this.setDeltaMovement(vec3.scale(0.99F));
			this.applyGravity();
			this.setPos(d, e, f);
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (this.getOwner() instanceof LivingEntity livingEntity) {
			Entity entity = entityHitResult.getEntity();
			DamageSource damageSource = this.damageSources().spit(this, livingEntity);
			if (this.level() instanceof ServerLevel serverLevel && entity.hurtServer(serverLevel, damageSource, 1.0F)) {
				EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		if (!this.level().isClientSide) {
			this.discard();
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		double d = clientboundAddEntityPacket.getXa();
		double e = clientboundAddEntityPacket.getYa();
		double f = clientboundAddEntityPacket.getZa();

		for (int i = 0; i < 7; i++) {
			double g = 0.4 + 0.1 * (double)i;
			this.level().addParticle(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), d * g, e, f * g);
		}

		this.setDeltaMovement(d, e, f);
	}
}
