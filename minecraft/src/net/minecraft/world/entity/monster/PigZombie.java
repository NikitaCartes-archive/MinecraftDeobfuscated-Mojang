package net.minecraft.world.entity.monster;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class PigZombie extends Zombie {
	private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
	private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(
			SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.05, AttributeModifier.Operation.ADDITION
		)
		.setSerialize(false);
	private int angerTime;
	private int playAngrySoundIn;
	private UUID lastHurtByUUID;

	public PigZombie(EntityType<? extends PigZombie> entityType, Level level) {
		super(entityType, level);
		this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
	}

	@Override
	public void setLastHurtByMob(@Nullable LivingEntity livingEntity) {
		super.setLastHurtByMob(livingEntity);
		if (livingEntity != null) {
			this.lastHurtByUUID = livingEntity.getUUID();
		}
	}

	@Override
	protected void addBehaviourGoals() {
		this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.targetSelector.addGoal(1, new PigZombie.PigZombieHurtByOtherGoal(this));
		this.targetSelector.addGoal(2, new PigZombie.PigZombieAngerTargetGoal(this));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23F);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0);
	}

	@Override
	protected boolean convertsInWater() {
		return false;
	}

	@Override
	protected void customServerAiStep() {
		AttributeInstance attributeInstance = this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		LivingEntity livingEntity = this.getLastHurtByMob();
		if (this.isAngry()) {
			if (!this.isBaby() && !attributeInstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
				attributeInstance.addModifier(SPEED_MODIFIER_ATTACKING);
			}

			this.angerTime--;
			LivingEntity livingEntity2 = livingEntity != null ? livingEntity : this.getTarget();
			if (!this.isAngry() && livingEntity2 != null) {
				if (!this.canSee(livingEntity2)) {
					this.setLastHurtByMob(null);
					this.setTarget(null);
				} else {
					this.angerTime = this.getAngerTime();
				}
			}
		} else if (attributeInstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
			attributeInstance.removeModifier(SPEED_MODIFIER_ATTACKING);
		}

		if (this.playAngrySoundIn > 0 && --this.playAngrySoundIn == 0) {
			this.playSound(SoundEvents.ZOMBIE_PIGMAN_ANGRY, this.getSoundVolume() * 2.0F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 1.8F);
		}

		if (this.isAngry() && this.lastHurtByUUID != null && livingEntity == null) {
			Player player = this.level.getPlayerByUUID(this.lastHurtByUUID);
			this.setLastHurtByMob(player);
			this.lastHurtByPlayer = player;
			this.lastHurtByPlayerTime = this.getLastHurtByMobTimestamp();
		}

		super.customServerAiStep();
	}

	public static boolean checkPigZombieSpawnRules(
		EntityType<PigZombie> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return levelAccessor.getDifficulty() != Difficulty.PEACEFUL;
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		return levelReader.isUnobstructed(this) && !levelReader.containsAnyLiquid(this.getBoundingBox());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putShort("Anger", (short)this.angerTime);
		if (this.lastHurtByUUID != null) {
			compoundTag.putString("HurtBy", this.lastHurtByUUID.toString());
		} else {
			compoundTag.putString("HurtBy", "");
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.angerTime = compoundTag.getShort("Anger");
		String string = compoundTag.getString("HurtBy");
		if (!string.isEmpty()) {
			this.lastHurtByUUID = UUID.fromString(string);
			Player player = this.level.getPlayerByUUID(this.lastHurtByUUID);
			this.setLastHurtByMob(player);
			if (player != null) {
				this.lastHurtByPlayer = player;
				this.lastHurtByPlayerTime = this.getLastHurtByMobTimestamp();
			}
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			Entity entity = damageSource.getEntity();
			if (entity instanceof Player && !((Player)entity).isCreative() && this.canSee(entity)) {
				this.makeAngry((LivingEntity)entity);
			}

			return super.hurt(damageSource, f);
		}
	}

	private boolean makeAngry(LivingEntity livingEntity) {
		this.angerTime = this.getAngerTime();
		this.playAngrySoundIn = this.random.nextInt(40);
		this.setLastHurtByMob(livingEntity);
		return true;
	}

	private int getAngerTime() {
		return 400 + this.random.nextInt(400);
	}

	private boolean isAngry() {
		return this.angerTime > 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ZOMBIE_PIGMAN_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ZOMBIE_PIGMAN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ZOMBIE_PIGMAN_DEATH;
	}

	@Override
	protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
	}

	@Override
	protected ItemStack getSkull() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isPreventingPlayerRest(Player player) {
		return this.isAngry();
	}

	static class PigZombieAngerTargetGoal extends NearestAttackableTargetGoal<Player> {
		public PigZombieAngerTargetGoal(PigZombie pigZombie) {
			super(pigZombie, Player.class, true);
		}

		@Override
		public boolean canUse() {
			return ((PigZombie)this.mob).isAngry() && super.canUse();
		}
	}

	static class PigZombieHurtByOtherGoal extends HurtByTargetGoal {
		public PigZombieHurtByOtherGoal(PigZombie pigZombie) {
			super(pigZombie);
			this.setAlertOthers(new Class[]{Zombie.class});
		}

		@Override
		protected void alertOther(Mob mob, LivingEntity livingEntity) {
			if (mob instanceof PigZombie && this.mob.canSee(livingEntity) && ((PigZombie)mob).makeAngry(livingEntity)) {
				mob.setTarget(livingEntity);
			}
		}
	}
}
