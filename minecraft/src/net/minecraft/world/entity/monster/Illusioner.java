package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class Illusioner extends SpellcasterIllager implements RangedAttackMob {
	private static final int NUM_ILLUSIONS = 4;
	private static final int ILLUSION_TRANSITION_TICKS = 3;
	public static final int ILLUSION_SPREAD = 3;
	private int clientSideIllusionTicks;
	private final Vec3[][] clientSideIllusionOffsets;

	public Illusioner(EntityType<? extends Illusioner> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
		this.clientSideIllusionOffsets = new Vec3[2][4];

		for (int i = 0; i < 4; i++) {
			this.clientSideIllusionOffsets[0][i] = Vec3.ZERO;
			this.clientSideIllusionOffsets[1][i] = Vec3.ZERO;
		}
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new SpellcasterIllager.SpellcasterCastingSpellGoal());
		this.goalSelector.addGoal(3, new AvoidEntityGoal(this, Creaking.class, 8.0F, 1.0, 1.2));
		this.goalSelector.addGoal(4, new Illusioner.IllusionerMirrorSpellGoal());
		this.goalSelector.addGoal(5, new Illusioner.IllusionerBlindnessSpellGoal());
		this.goalSelector.addGoal(6, new RangedBowAttackGoal<>(this, 0.5, 20, 15.0F));
		this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, false).setUnseenMemoryTicks(300));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 18.0).add(Attributes.MAX_HEALTH, 32.0);
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
	) {
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide && this.isInvisible()) {
			this.clientSideIllusionTicks--;
			if (this.clientSideIllusionTicks < 0) {
				this.clientSideIllusionTicks = 0;
			}

			if (this.hurtTime == 1 || this.tickCount % 1200 == 0) {
				this.clientSideIllusionTicks = 3;
				float f = -6.0F;
				int i = 13;

				for (int j = 0; j < 4; j++) {
					this.clientSideIllusionOffsets[0][j] = this.clientSideIllusionOffsets[1][j];
					this.clientSideIllusionOffsets[1][j] = new Vec3(
						(double)(-6.0F + (float)this.random.nextInt(13)) * 0.5,
						(double)Math.max(0, this.random.nextInt(6) - 4),
						(double)(-6.0F + (float)this.random.nextInt(13)) * 0.5
					);
				}

				for (int j = 0; j < 16; j++) {
					this.level().addParticle(ParticleTypes.CLOUD, this.getRandomX(0.5), this.getRandomY(), this.getZ(0.5), 0.0, 0.0, 0.0);
				}

				this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F, false);
			} else if (this.hurtTime == this.hurtDuration - 1) {
				this.clientSideIllusionTicks = 3;

				for (int k = 0; k < 4; k++) {
					this.clientSideIllusionOffsets[0][k] = this.clientSideIllusionOffsets[1][k];
					this.clientSideIllusionOffsets[1][k] = new Vec3(0.0, 0.0, 0.0);
				}
			}
		}
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return SoundEvents.ILLUSIONER_AMBIENT;
	}

	public Vec3[] getIllusionOffsets(float f) {
		if (this.clientSideIllusionTicks <= 0) {
			return this.clientSideIllusionOffsets[1];
		} else {
			double d = (double)(((float)this.clientSideIllusionTicks - f) / 3.0F);
			d = Math.pow(d, 0.25);
			Vec3[] vec3s = new Vec3[4];

			for (int i = 0; i < 4; i++) {
				vec3s[i] = this.clientSideIllusionOffsets[1][i].scale(1.0 - d).add(this.clientSideIllusionOffsets[0][i].scale(d));
			}

			return vec3s;
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ILLUSIONER_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ILLUSIONER_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ILLUSIONER_HURT;
	}

	@Override
	protected SoundEvent getCastingSoundEvent() {
		return SoundEvents.ILLUSIONER_CAST_SPELL;
	}

	@Override
	public void applyRaidBuffs(ServerLevel serverLevel, int i, boolean bl) {
	}

	@Override
	public void performRangedAttack(LivingEntity livingEntity, float f) {
		ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
		ItemStack itemStack2 = this.getProjectile(itemStack);
		AbstractArrow abstractArrow = ProjectileUtil.getMobArrow(this, itemStack2, f, itemStack);
		double d = livingEntity.getX() - this.getX();
		double e = livingEntity.getY(0.3333333333333333) - abstractArrow.getY();
		double g = livingEntity.getZ() - this.getZ();
		double h = Math.sqrt(d * d + g * g);
		if (this.level() instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileUsingShoot(abstractArrow, serverLevel, itemStack2, d, e + h * 0.2F, g, 1.6F, (float)(14 - serverLevel.getDifficulty().getId() * 4));
		}

		this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
	}

	@Override
	public AbstractIllager.IllagerArmPose getArmPose() {
		if (this.isCastingSpell()) {
			return AbstractIllager.IllagerArmPose.SPELLCASTING;
		} else {
			return this.isAggressive() ? AbstractIllager.IllagerArmPose.BOW_AND_ARROW : AbstractIllager.IllagerArmPose.CROSSED;
		}
	}

	class IllusionerBlindnessSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
		private int lastTargetId;

		@Override
		public boolean canUse() {
			if (!super.canUse()) {
				return false;
			} else if (Illusioner.this.getTarget() == null) {
				return false;
			} else {
				return Illusioner.this.getTarget().getId() == this.lastTargetId
					? false
					: Illusioner.this.level().getCurrentDifficultyAt(Illusioner.this.blockPosition()).isHarderThan((float)Difficulty.NORMAL.ordinal());
			}
		}

		@Override
		public void start() {
			super.start();
			LivingEntity livingEntity = Illusioner.this.getTarget();
			if (livingEntity != null) {
				this.lastTargetId = livingEntity.getId();
			}
		}

		@Override
		protected int getCastingTime() {
			return 20;
		}

		@Override
		protected int getCastingInterval() {
			return 180;
		}

		@Override
		protected void performSpellCasting() {
			Illusioner.this.getTarget().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400), Illusioner.this);
		}

		@Override
		protected SoundEvent getSpellPrepareSound() {
			return SoundEvents.ILLUSIONER_PREPARE_BLINDNESS;
		}

		@Override
		protected SpellcasterIllager.IllagerSpell getSpell() {
			return SpellcasterIllager.IllagerSpell.BLINDNESS;
		}
	}

	class IllusionerMirrorSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
		@Override
		public boolean canUse() {
			return !super.canUse() ? false : !Illusioner.this.hasEffect(MobEffects.INVISIBILITY);
		}

		@Override
		protected int getCastingTime() {
			return 20;
		}

		@Override
		protected int getCastingInterval() {
			return 340;
		}

		@Override
		protected void performSpellCasting() {
			Illusioner.this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1200));
		}

		@Nullable
		@Override
		protected SoundEvent getSpellPrepareSound() {
			return SoundEvents.ILLUSIONER_PREPARE_MIRROR;
		}

		@Override
		protected SpellcasterIllager.IllagerSpell getSpell() {
			return SpellcasterIllager.IllagerSpell.DISAPPEAR;
		}
	}
}
