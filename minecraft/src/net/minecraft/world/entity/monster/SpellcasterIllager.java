package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public abstract class SpellcasterIllager extends AbstractIllager {
	private static final EntityDataAccessor<Byte> DATA_SPELL_CASTING_ID = SynchedEntityData.defineId(SpellcasterIllager.class, EntityDataSerializers.BYTE);
	protected int spellCastingTickCount;
	private SpellcasterIllager.IllagerSpell currentSpell = SpellcasterIllager.IllagerSpell.NONE;

	protected SpellcasterIllager(EntityType<? extends SpellcasterIllager> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_SPELL_CASTING_ID, (byte)0);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.spellCastingTickCount = compoundTag.getInt("SpellTicks");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("SpellTicks", this.spellCastingTickCount);
	}

	@Override
	public AbstractIllager.IllagerArmPose getArmPose() {
		if (this.isCastingSpell()) {
			return AbstractIllager.IllagerArmPose.SPELLCASTING;
		} else {
			return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
		}
	}

	public boolean isCastingSpell() {
		return this.level().isClientSide ? this.entityData.get(DATA_SPELL_CASTING_ID) > 0 : this.spellCastingTickCount > 0;
	}

	public void setIsCastingSpell(SpellcasterIllager.IllagerSpell illagerSpell) {
		this.currentSpell = illagerSpell;
		this.entityData.set(DATA_SPELL_CASTING_ID, (byte)illagerSpell.id);
	}

	protected SpellcasterIllager.IllagerSpell getCurrentSpell() {
		return !this.level().isClientSide ? this.currentSpell : SpellcasterIllager.IllagerSpell.byId(this.entityData.get(DATA_SPELL_CASTING_ID));
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (this.spellCastingTickCount > 0) {
			this.spellCastingTickCount--;
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide && this.isCastingSpell()) {
			SpellcasterIllager.IllagerSpell illagerSpell = this.getCurrentSpell();
			float f = (float)illagerSpell.spellColor[0];
			float g = (float)illagerSpell.spellColor[1];
			float h = (float)illagerSpell.spellColor[2];
			float i = this.yBodyRot * (float) (Math.PI / 180.0) + Mth.cos((float)this.tickCount * 0.6662F) * 0.25F;
			float j = Mth.cos(i);
			float k = Mth.sin(i);
			double d = 0.6 * (double)this.getScale();
			double e = 1.8 * (double)this.getScale();
			this.level()
				.addParticle(
					ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, f, g, h), this.getX() + (double)j * d, this.getY() + e, this.getZ() + (double)k * d, 0.0, 0.0, 0.0
				);
			this.level()
				.addParticle(
					ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, f, g, h), this.getX() - (double)j * d, this.getY() + e, this.getZ() - (double)k * d, 0.0, 0.0, 0.0
				);
		}
	}

	protected int getSpellCastingTime() {
		return this.spellCastingTickCount;
	}

	protected abstract SoundEvent getCastingSoundEvent();

	protected static enum IllagerSpell {
		NONE(0, 0.0, 0.0, 0.0),
		SUMMON_VEX(1, 0.7, 0.7, 0.8),
		FANGS(2, 0.4, 0.3, 0.35),
		WOLOLO(3, 0.7, 0.5, 0.2),
		DISAPPEAR(4, 0.3, 0.3, 0.8),
		BLINDNESS(5, 0.1, 0.1, 0.2);

		private static final IntFunction<SpellcasterIllager.IllagerSpell> BY_ID = ByIdMap.continuous(
			illagerSpell -> illagerSpell.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO
		);
		final int id;
		final double[] spellColor;

		private IllagerSpell(final int j, final double d, final double e, final double f) {
			this.id = j;
			this.spellColor = new double[]{d, e, f};
		}

		public static SpellcasterIllager.IllagerSpell byId(int i) {
			return (SpellcasterIllager.IllagerSpell)BY_ID.apply(i);
		}
	}

	protected class SpellcasterCastingSpellGoal extends Goal {
		public SpellcasterCastingSpellGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return SpellcasterIllager.this.getSpellCastingTime() > 0;
		}

		@Override
		public void start() {
			super.start();
			SpellcasterIllager.this.navigation.stop();
		}

		@Override
		public void stop() {
			super.stop();
			SpellcasterIllager.this.setIsCastingSpell(SpellcasterIllager.IllagerSpell.NONE);
		}

		@Override
		public void tick() {
			if (SpellcasterIllager.this.getTarget() != null) {
				SpellcasterIllager.this.getLookControl()
					.setLookAt(SpellcasterIllager.this.getTarget(), (float)SpellcasterIllager.this.getMaxHeadYRot(), (float)SpellcasterIllager.this.getMaxHeadXRot());
			}
		}
	}

	protected abstract class SpellcasterUseSpellGoal extends Goal {
		protected int attackWarmupDelay;
		protected int nextAttackTickCount;

		@Override
		public boolean canUse() {
			LivingEntity livingEntity = SpellcasterIllager.this.getTarget();
			if (livingEntity == null || !livingEntity.isAlive()) {
				return false;
			} else {
				return SpellcasterIllager.this.isCastingSpell() ? false : SpellcasterIllager.this.tickCount >= this.nextAttackTickCount;
			}
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity livingEntity = SpellcasterIllager.this.getTarget();
			return livingEntity != null && livingEntity.isAlive() && this.attackWarmupDelay > 0;
		}

		@Override
		public void start() {
			this.attackWarmupDelay = this.adjustedTickDelay(this.getCastWarmupTime());
			SpellcasterIllager.this.spellCastingTickCount = this.getCastingTime();
			this.nextAttackTickCount = SpellcasterIllager.this.tickCount + this.getCastingInterval();
			SoundEvent soundEvent = this.getSpellPrepareSound();
			if (soundEvent != null) {
				SpellcasterIllager.this.playSound(soundEvent, 1.0F, 1.0F);
			}

			SpellcasterIllager.this.setIsCastingSpell(this.getSpell());
		}

		@Override
		public void tick() {
			this.attackWarmupDelay--;
			if (this.attackWarmupDelay == 0) {
				this.performSpellCasting();
				SpellcasterIllager.this.playSound(SpellcasterIllager.this.getCastingSoundEvent(), 1.0F, 1.0F);
			}
		}

		protected abstract void performSpellCasting();

		protected int getCastWarmupTime() {
			return 20;
		}

		protected abstract int getCastingTime();

		protected abstract int getCastingInterval();

		@Nullable
		protected abstract SoundEvent getSpellPrepareSound();

		protected abstract SpellcasterIllager.IllagerSpell getSpell();
	}
}
