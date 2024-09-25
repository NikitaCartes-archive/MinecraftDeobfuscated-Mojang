package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class BlockAttachedEntity extends Entity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private int checkInterval;
	protected BlockPos pos;

	protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level) {
		super(entityType, level);
	}

	protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level, BlockPos blockPos) {
		this(entityType, level);
		this.pos = blockPos;
	}

	protected abstract void recalculateBoundingBox();

	@Override
	public void tick() {
		if (this.level() instanceof ServerLevel serverLevel) {
			this.checkBelowWorld();
			if (this.checkInterval++ == 100) {
				this.checkInterval = 0;
				if (!this.isRemoved() && !this.survives()) {
					this.discard();
					this.dropItem(serverLevel, null);
				}
			}
		}
	}

	public abstract boolean survives();

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean skipAttackInteraction(Entity entity) {
		if (entity instanceof Player player) {
			return !this.level().mayInteract(player, this.pos) ? true : this.hurtOrSimulate(this.damageSources().playerAttack(player), 0.0F);
		} else {
			return false;
		}
	}

	@Override
	public boolean hurtClient(DamageSource damageSource) {
		return !this.isInvulnerableToBase(damageSource);
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
		if (this.isInvulnerableToBase(damageSource)) {
			return false;
		} else if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && damageSource.getEntity() instanceof Mob) {
			return false;
		} else {
			if (!this.isRemoved()) {
				this.kill(serverLevel);
				this.markHurt();
				this.dropItem(serverLevel, damageSource.getEntity());
			}

			return true;
		}
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return explosion.shouldAffectBlocklikeEntities() ? super.ignoreExplosion(explosion) : true;
	}

	@Override
	public void move(MoverType moverType, Vec3 vec3) {
		if (this.level() instanceof ServerLevel serverLevel && !this.isRemoved() && vec3.lengthSqr() > 0.0) {
			this.kill(serverLevel);
			this.dropItem(serverLevel, null);
		}
	}

	@Override
	public void push(double d, double e, double f) {
		if (this.level() instanceof ServerLevel serverLevel && !this.isRemoved() && d * d + e * e + f * f > 0.0) {
			this.kill(serverLevel);
			this.dropItem(serverLevel, null);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		BlockPos blockPos = this.getPos();
		compoundTag.putInt("TileX", blockPos.getX());
		compoundTag.putInt("TileY", blockPos.getY());
		compoundTag.putInt("TileZ", blockPos.getZ());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		BlockPos blockPos = new BlockPos(compoundTag.getInt("TileX"), compoundTag.getInt("TileY"), compoundTag.getInt("TileZ"));
		if (!blockPos.closerThan(this.blockPosition(), 16.0)) {
			LOGGER.error("Block-attached entity at invalid position: {}", blockPos);
		} else {
			this.pos = blockPos;
		}
	}

	public abstract void dropItem(ServerLevel serverLevel, @Nullable Entity entity);

	@Override
	protected boolean repositionEntityAfterLoad() {
		return false;
	}

	@Override
	public void setPos(double d, double e, double f) {
		this.pos = BlockPos.containing(d, e, f);
		this.recalculateBoundingBox();
		this.hasImpulse = true;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	@Override
	public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
	}

	@Override
	public void refreshDimensions() {
	}
}
