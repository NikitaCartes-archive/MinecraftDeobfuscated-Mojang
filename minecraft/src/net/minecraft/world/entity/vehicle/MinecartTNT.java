package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class MinecartTNT extends AbstractMinecart {
	private static final byte EVENT_PRIME = 10;
	private int fuse = -1;

	public MinecartTNT(EntityType<? extends MinecartTNT> entityType, Level level) {
		super(entityType, level);
	}

	public MinecartTNT(Level level, double d, double e, double f) {
		super(EntityType.TNT_MINECART, level, d, e, f);
	}

	@Override
	public AbstractMinecart.Type getMinecartType() {
		return AbstractMinecart.Type.TNT;
	}

	@Override
	public BlockState getDefaultDisplayBlockState() {
		return Blocks.TNT.defaultBlockState();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.fuse > 0) {
			this.fuse--;
			this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
		} else if (this.fuse == 0) {
			this.explode(this.getDeltaMovement().horizontalDistanceSqr());
		}

		if (this.horizontalCollision) {
			double d = this.getDeltaMovement().horizontalDistanceSqr();
			if (d >= 0.01F) {
				this.explode(d);
			}
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (damageSource.getDirectEntity() instanceof AbstractArrow abstractArrow && abstractArrow.isOnFire()) {
			this.explode(abstractArrow.getDeltaMovement().lengthSqr());
		}

		return super.hurt(damageSource, f);
	}

	@Override
	public void destroy(DamageSource damageSource) {
		double d = this.getDeltaMovement().horizontalDistanceSqr();
		if (!damageSource.isFire() && !damageSource.isExplosion() && !(d >= 0.01F)) {
			super.destroy(damageSource);
			if (!damageSource.isExplosion() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
				this.spawnAtLocation(Blocks.TNT);
			}
		} else {
			if (this.fuse < 0) {
				this.primeFuse();
				this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
			}
		}
	}

	protected void explode(double d) {
		if (!this.level.isClientSide) {
			double e = Math.sqrt(d);
			if (e > 5.0) {
				e = 5.0;
			}

			this.level.explode(this, this.getX(), this.getY(), this.getZ(), (float)(4.0 + this.random.nextDouble() * 1.5 * e), Explosion.BlockInteraction.BREAK);
			this.discard();
		}
	}

	@Override
	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		if (f >= 3.0F) {
			float h = f / 10.0F;
			this.explode((double)(h * h));
		}

		return super.causeFallDamage(f, g, damageSource);
	}

	@Override
	public void activateMinecart(int i, int j, int k, boolean bl) {
		if (bl && this.fuse < 0) {
			this.primeFuse();
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 10) {
			this.primeFuse();
		} else {
			super.handleEntityEvent(b);
		}
	}

	public void primeFuse() {
		this.fuse = 80;
		if (!this.level.isClientSide) {
			this.level.broadcastEntityEvent(this, (byte)10);
			if (!this.isSilent()) {
				this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		}
	}

	public int getFuse() {
		return this.fuse;
	}

	public boolean isPrimed() {
		return this.fuse > -1;
	}

	@Override
	public float getBlockExplosionResistance(
		Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, float f
	) {
		return !this.isPrimed() || !blockState.is(BlockTags.RAILS) && !blockGetter.getBlockState(blockPos.above()).is(BlockTags.RAILS)
			? super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState, f)
			: 0.0F;
	}

	@Override
	public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
		return !this.isPrimed() || !blockState.is(BlockTags.RAILS) && !blockGetter.getBlockState(blockPos.above()).is(BlockTags.RAILS)
			? super.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f)
			: false;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("TNTFuse", 99)) {
			this.fuse = compoundTag.getInt("TNTFuse");
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("TNTFuse", this.fuse);
	}
}
