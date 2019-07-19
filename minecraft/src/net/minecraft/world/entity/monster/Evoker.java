package net.minecraft.world.entity.monster;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Evoker extends SpellcasterIllager {
	private Sheep wololoTarget;

	public Evoker(EntityType<? extends Evoker> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 10;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new Evoker.EvokerCastingSpellGoal());
		this.goalSelector.addGoal(2, new AvoidEntityGoal(this, Player.class, 8.0F, 0.6, 1.0));
		this.goalSelector.addGoal(4, new Evoker.EvokerSummonSpellGoal());
		this.goalSelector.addGoal(5, new Evoker.EvokerAttackSpellGoal());
		this.goalSelector.addGoal(6, new Evoker.EvokerWololoSpellGoal());
		this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, false));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(12.0);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return SoundEvents.EVOKER_CELEBRATE;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public boolean isAlliedTo(Entity entity) {
		if (entity == null) {
			return false;
		} else if (entity == this) {
			return true;
		} else if (super.isAlliedTo(entity)) {
			return true;
		} else if (entity instanceof Vex) {
			return this.isAlliedTo(((Vex)entity).getOwner());
		} else {
			return entity instanceof LivingEntity && ((LivingEntity)entity).getMobType() == MobType.ILLAGER ? this.getTeam() == null && entity.getTeam() == null : false;
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.EVOKER_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.EVOKER_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.EVOKER_HURT;
	}

	private void setWololoTarget(@Nullable Sheep sheep) {
		this.wololoTarget = sheep;
	}

	@Nullable
	private Sheep getWololoTarget() {
		return this.wololoTarget;
	}

	@Override
	protected SoundEvent getCastingSoundEvent() {
		return SoundEvents.EVOKER_CAST_SPELL;
	}

	@Override
	public void applyRaidBuffs(int i, boolean bl) {
	}

	class EvokerAttackSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
		private EvokerAttackSpellGoal() {
		}

		@Override
		protected int getCastingTime() {
			return 40;
		}

		@Override
		protected int getCastingInterval() {
			return 100;
		}

		@Override
		protected void performSpellCasting() {
			LivingEntity livingEntity = Evoker.this.getTarget();
			double d = Math.min(livingEntity.y, Evoker.this.y);
			double e = Math.max(livingEntity.y, Evoker.this.y) + 1.0;
			float f = (float)Mth.atan2(livingEntity.z - Evoker.this.z, livingEntity.x - Evoker.this.x);
			if (Evoker.this.distanceToSqr(livingEntity) < 9.0) {
				for (int i = 0; i < 5; i++) {
					float g = f + (float)i * (float) Math.PI * 0.4F;
					this.createSpellEntity(Evoker.this.x + (double)Mth.cos(g) * 1.5, Evoker.this.z + (double)Mth.sin(g) * 1.5, d, e, g, 0);
				}

				for (int i = 0; i < 8; i++) {
					float g = f + (float)i * (float) Math.PI * 2.0F / 8.0F + (float) (Math.PI * 2.0 / 5.0);
					this.createSpellEntity(Evoker.this.x + (double)Mth.cos(g) * 2.5, Evoker.this.z + (double)Mth.sin(g) * 2.5, d, e, g, 3);
				}
			} else {
				for (int i = 0; i < 16; i++) {
					double h = 1.25 * (double)(i + 1);
					int j = 1 * i;
					this.createSpellEntity(Evoker.this.x + (double)Mth.cos(f) * h, Evoker.this.z + (double)Mth.sin(f) * h, d, e, f, j);
				}
			}
		}

		private void createSpellEntity(double d, double e, double f, double g, float h, int i) {
			BlockPos blockPos = new BlockPos(d, g, e);
			boolean bl = false;
			double j = 0.0;

			do {
				BlockPos blockPos2 = blockPos.below();
				BlockState blockState = Evoker.this.level.getBlockState(blockPos2);
				if (blockState.isFaceSturdy(Evoker.this.level, blockPos2, Direction.UP)) {
					if (!Evoker.this.level.isEmptyBlock(blockPos)) {
						BlockState blockState2 = Evoker.this.level.getBlockState(blockPos);
						VoxelShape voxelShape = blockState2.getCollisionShape(Evoker.this.level, blockPos);
						if (!voxelShape.isEmpty()) {
							j = voxelShape.max(Direction.Axis.Y);
						}
					}

					bl = true;
					break;
				}

				blockPos = blockPos.below();
			} while (blockPos.getY() >= Mth.floor(f) - 1);

			if (bl) {
				Evoker.this.level.addFreshEntity(new EvokerFangs(Evoker.this.level, d, (double)blockPos.getY() + j, e, h, i, Evoker.this));
			}
		}

		@Override
		protected SoundEvent getSpellPrepareSound() {
			return SoundEvents.EVOKER_PREPARE_ATTACK;
		}

		@Override
		protected SpellcasterIllager.IllagerSpell getSpell() {
			return SpellcasterIllager.IllagerSpell.FANGS;
		}
	}

	class EvokerCastingSpellGoal extends SpellcasterIllager.SpellcasterCastingSpellGoal {
		private EvokerCastingSpellGoal() {
		}

		@Override
		public void tick() {
			if (Evoker.this.getTarget() != null) {
				Evoker.this.getLookControl().setLookAt(Evoker.this.getTarget(), (float)Evoker.this.getMaxHeadYRot(), (float)Evoker.this.getMaxHeadXRot());
			} else if (Evoker.this.getWololoTarget() != null) {
				Evoker.this.getLookControl().setLookAt(Evoker.this.getWololoTarget(), (float)Evoker.this.getMaxHeadYRot(), (float)Evoker.this.getMaxHeadXRot());
			}
		}
	}

	class EvokerSummonSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
		private final TargetingConditions vexCountTargeting = new TargetingConditions()
			.range(16.0)
			.allowUnseeable()
			.ignoreInvisibilityTesting()
			.allowInvulnerable()
			.allowSameTeam();

		private EvokerSummonSpellGoal() {
		}

		@Override
		public boolean canUse() {
			if (!super.canUse()) {
				return false;
			} else {
				int i = Evoker.this.level.getNearbyEntities(Vex.class, this.vexCountTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0)).size();
				return Evoker.this.random.nextInt(8) + 1 > i;
			}
		}

		@Override
		protected int getCastingTime() {
			return 100;
		}

		@Override
		protected int getCastingInterval() {
			return 340;
		}

		@Override
		protected void performSpellCasting() {
			for (int i = 0; i < 3; i++) {
				BlockPos blockPos = new BlockPos(Evoker.this).offset(-2 + Evoker.this.random.nextInt(5), 1, -2 + Evoker.this.random.nextInt(5));
				Vex vex = EntityType.VEX.create(Evoker.this.level);
				vex.moveTo(blockPos, 0.0F, 0.0F);
				vex.finalizeSpawn(Evoker.this.level, Evoker.this.level.getCurrentDifficultyAt(blockPos), MobSpawnType.MOB_SUMMONED, null, null);
				vex.setOwner(Evoker.this);
				vex.setBoundOrigin(blockPos);
				vex.setLimitedLife(20 * (30 + Evoker.this.random.nextInt(90)));
				Evoker.this.level.addFreshEntity(vex);
			}
		}

		@Override
		protected SoundEvent getSpellPrepareSound() {
			return SoundEvents.EVOKER_PREPARE_SUMMON;
		}

		@Override
		protected SpellcasterIllager.IllagerSpell getSpell() {
			return SpellcasterIllager.IllagerSpell.SUMMON_VEX;
		}
	}

	public class EvokerWololoSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
		private final TargetingConditions wololoTargeting = new TargetingConditions()
			.range(16.0)
			.allowInvulnerable()
			.selector(livingEntity -> ((Sheep)livingEntity).getColor() == DyeColor.BLUE);

		@Override
		public boolean canUse() {
			if (Evoker.this.getTarget() != null) {
				return false;
			} else if (Evoker.this.isCastingSpell()) {
				return false;
			} else if (Evoker.this.tickCount < this.nextAttackTickCount) {
				return false;
			} else if (!Evoker.this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
				return false;
			} else {
				List<Sheep> list = Evoker.this.level
					.getNearbyEntities(Sheep.class, this.wololoTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0, 4.0, 16.0));
				if (list.isEmpty()) {
					return false;
				} else {
					Evoker.this.setWololoTarget((Sheep)list.get(Evoker.this.random.nextInt(list.size())));
					return true;
				}
			}
		}

		@Override
		public boolean canContinueToUse() {
			return Evoker.this.getWololoTarget() != null && this.attackWarmupDelay > 0;
		}

		@Override
		public void stop() {
			super.stop();
			Evoker.this.setWololoTarget(null);
		}

		@Override
		protected void performSpellCasting() {
			Sheep sheep = Evoker.this.getWololoTarget();
			if (sheep != null && sheep.isAlive()) {
				sheep.setColor(DyeColor.RED);
			}
		}

		@Override
		protected int getCastWarmupTime() {
			return 40;
		}

		@Override
		protected int getCastingTime() {
			return 60;
		}

		@Override
		protected int getCastingInterval() {
			return 140;
		}

		@Override
		protected SoundEvent getSpellPrepareSound() {
			return SoundEvents.EVOKER_PREPARE_WOLOLO;
		}

		@Override
		protected SpellcasterIllager.IllagerSpell getSpell() {
			return SpellcasterIllager.IllagerSpell.WOLOLO;
		}
	}
}
