package net.minecraft.world.entity.monster;

import com.google.common.collect.Maps;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Vindicator extends AbstractIllager {
	private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = difficulty -> difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD;
	private boolean isJohnny;

	public Vindicator(EntityType<? extends Vindicator> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new Vindicator.VindicatorBreakDoorGoal(this));
		this.goalSelector.addGoal(2, new AbstractIllager.RaiderOpenDoorGoal(this));
		this.goalSelector.addGoal(3, new Raider.HoldGroundAttackGoal(this, 10.0F));
		this.goalSelector.addGoal(4, new Vindicator.VindicatorMeleeAttackGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
		this.targetSelector.addGoal(4, new Vindicator.VindicatorJohnnyAttackGoal(this));
		this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
	}

	@Override
	protected void customServerAiStep() {
		if (!this.isNoAi()) {
			PathNavigation pathNavigation = this.getNavigation();
			if (pathNavigation instanceof GroundPathNavigation) {
				boolean bl = ((ServerLevel)this.level).isRaided(this.blockPosition());
				((GroundPathNavigation)pathNavigation).setCanOpenDoors(bl);
			}
		}

		super.customServerAiStep();
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35F);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(12.0);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.isJohnny) {
			compoundTag.putBoolean("Johnny", true);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public AbstractIllager.IllagerArmPose getArmPose() {
		if (this.isAggressive()) {
			return AbstractIllager.IllagerArmPose.ATTACKING;
		} else {
			return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("Johnny", 99)) {
			this.isJohnny = compoundTag.getBoolean("Johnny");
		}
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return SoundEvents.VINDICATOR_CELEBRATE;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		LevelAccessor levelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		SpawnGroupData spawnGroupData2 = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
		((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
		this.populateDefaultEquipmentSlots(difficultyInstance);
		this.populateDefaultEquipmentEnchantments(difficultyInstance);
		return spawnGroupData2;
	}

	@Override
	protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
		if (this.getCurrentRaid() == null) {
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
		}
	}

	@Override
	public boolean isAlliedTo(Entity entity) {
		if (super.isAlliedTo(entity)) {
			return true;
		} else {
			return entity instanceof LivingEntity && ((LivingEntity)entity).getMobType() == MobType.ILLAGER ? this.getTeam() == null && entity.getTeam() == null : false;
		}
	}

	@Override
	public void setCustomName(@Nullable Component component) {
		super.setCustomName(component);
		if (!this.isJohnny && component != null && component.getString().equals("Johnny")) {
			this.isJohnny = true;
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.VINDICATOR_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.VINDICATOR_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.VINDICATOR_HURT;
	}

	@Override
	public void applyRaidBuffs(int i, boolean bl) {
		ItemStack itemStack = new ItemStack(Items.IRON_AXE);
		Raid raid = this.getCurrentRaid();
		int j = 1;
		if (i > raid.getNumGroups(Difficulty.NORMAL)) {
			j = 2;
		}

		boolean bl2 = this.random.nextFloat() <= raid.getEnchantOdds();
		if (bl2) {
			Map<Enchantment, Integer> map = Maps.<Enchantment, Integer>newHashMap();
			map.put(Enchantments.SHARPNESS, j);
			EnchantmentHelper.setEnchantments(map, itemStack);
		}

		this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
	}

	static class VindicatorBreakDoorGoal extends BreakDoorGoal {
		public VindicatorBreakDoorGoal(Mob mob) {
			super(mob, 6, Vindicator.DOOR_BREAKING_PREDICATE);
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canContinueToUse() {
			Vindicator vindicator = (Vindicator)this.mob;
			return vindicator.hasActiveRaid() && super.canContinueToUse();
		}

		@Override
		public boolean canUse() {
			Vindicator vindicator = (Vindicator)this.mob;
			return vindicator.hasActiveRaid() && vindicator.random.nextInt(10) == 0 && super.canUse();
		}

		@Override
		public void start() {
			super.start();
			this.mob.setNoActionTime(0);
		}
	}

	static class VindicatorJohnnyAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
		public VindicatorJohnnyAttackGoal(Vindicator vindicator) {
			super(vindicator, LivingEntity.class, 0, true, true, LivingEntity::attackable);
		}

		@Override
		public boolean canUse() {
			return ((Vindicator)this.mob).isJohnny && super.canUse();
		}

		@Override
		public void start() {
			super.start();
			this.mob.setNoActionTime(0);
		}
	}

	class VindicatorMeleeAttackGoal extends MeleeAttackGoal {
		public VindicatorMeleeAttackGoal(Vindicator vindicator2) {
			super(vindicator2, 1.0, false);
		}

		@Override
		protected double getAttackReachSqr(LivingEntity livingEntity) {
			if (this.mob.getVehicle() instanceof Ravager) {
				float f = this.mob.getVehicle().getBbWidth() - 0.1F;
				return (double)(f * 2.0F * f * 2.0F + livingEntity.getBbWidth());
			} else {
				return super.getAttackReachSqr(livingEntity);
			}
		}
	}
}
