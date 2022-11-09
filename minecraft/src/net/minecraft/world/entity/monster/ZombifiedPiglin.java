package net.minecraft.world.entity.monster;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;

public class ZombifiedPiglin extends Zombie implements NeutralMob {
	private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
	private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(
		SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.05, AttributeModifier.Operation.ADDITION
	);
	private static final UniformInt FIRST_ANGER_SOUND_DELAY = TimeUtil.rangeOfSeconds(0, 1);
	private int playFirstAngerSoundIn;
	private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
	private int remainingPersistentAngerTime;
	@Nullable
	private UUID persistentAngerTarget;
	private static final int ALERT_RANGE_Y = 10;
	private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
	private int ticksUntilNextAlert;
	private static final float ZOMBIFIED_PIGLIN_EYE_HEIGHT = 1.79F;

	public ZombifiedPiglin(EntityType<? extends ZombifiedPiglin> entityType, Level level) {
		super(entityType, level);
		this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
	}

	@Override
	public void setPersistentAngerTarget(@Nullable UUID uUID) {
		this.persistentAngerTarget = uUID;
	}

	@Override
	public double getMyRidingOffset() {
		return this.isBaby() ? -0.05 : -0.45;
	}

	@Override
	protected void addBehaviourGoals() {
		this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, 10, true, false, this::isAngryAt));
		this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes().add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0).add(Attributes.MOVEMENT_SPEED, 0.23F).add(Attributes.ATTACK_DAMAGE, 5.0);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return this.isBaby() ? 0.97999996F : 1.79F;
	}

	@Override
	protected boolean convertsInWater() {
		return false;
	}

	@Override
	protected void customServerAiStep() {
		AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
		if (this.isAngry()) {
			if (!this.isBaby() && !attributeInstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
				attributeInstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
			}

			this.maybePlayFirstAngerSound();
		} else if (attributeInstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
			attributeInstance.removeModifier(SPEED_MODIFIER_ATTACKING);
		}

		this.updatePersistentAnger((ServerLevel)this.level, true);
		if (this.getTarget() != null) {
			this.maybeAlertOthers();
		}

		if (this.isAngry()) {
			this.lastHurtByPlayerTime = this.tickCount;
		}

		super.customServerAiStep();
	}

	private void maybePlayFirstAngerSound() {
		if (this.playFirstAngerSoundIn > 0) {
			this.playFirstAngerSoundIn--;
			if (this.playFirstAngerSoundIn == 0) {
				this.playAngerSound();
			}
		}
	}

	private void maybeAlertOthers() {
		if (this.ticksUntilNextAlert > 0) {
			this.ticksUntilNextAlert--;
		} else {
			if (this.getSensing().hasLineOfSight(this.getTarget())) {
				this.alertOthers();
			}

			this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
		}
	}

	private void alertOthers() {
		double d = this.getAttributeValue(Attributes.FOLLOW_RANGE);
		AABB aABB = AABB.unitCubeFromLowerCorner(this.position()).inflate(d, 10.0, d);
		this.level
			.getEntitiesOfClass(ZombifiedPiglin.class, aABB, EntitySelector.NO_SPECTATORS)
			.stream()
			.filter(zombifiedPiglin -> zombifiedPiglin != this)
			.filter(zombifiedPiglin -> zombifiedPiglin.getTarget() == null)
			.filter(zombifiedPiglin -> !zombifiedPiglin.isAlliedTo(this.getTarget()))
			.forEach(zombifiedPiglin -> zombifiedPiglin.setTarget(this.getTarget()));
	}

	private void playAngerSound() {
		this.playSound(SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, this.getSoundVolume() * 2.0F, this.getVoicePitch() * 1.8F);
	}

	@Override
	public void setTarget(@Nullable LivingEntity livingEntity) {
		if (this.getTarget() == null && livingEntity != null) {
			this.playFirstAngerSoundIn = FIRST_ANGER_SOUND_DELAY.sample(this.random);
			this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
		}

		if (livingEntity instanceof Player) {
			this.setLastHurtByPlayer((Player)livingEntity);
		}

		super.setTarget(livingEntity);
	}

	@Override
	public void startPersistentAngerTimer() {
		this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
	}

	public static boolean checkZombifiedPiglinSpawnRules(
		EntityType<ZombifiedPiglin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return levelAccessor.getDifficulty() != Difficulty.PEACEFUL && !levelAccessor.getBlockState(blockPos.below()).is(Blocks.NETHER_WART_BLOCK);
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		return levelReader.isUnobstructed(this) && !levelReader.containsAnyLiquid(this.getBoundingBox());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		this.addPersistentAngerSaveData(compoundTag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.readPersistentAngerSaveData(this.level, compoundTag);
	}

	@Override
	public void setRemainingPersistentAngerTime(int i) {
		this.remainingPersistentAngerTime = i;
	}

	@Override
	public int getRemainingPersistentAngerTime() {
		return this.remainingPersistentAngerTime;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.isAngry() ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
	}

	@Override
	protected ItemStack getSkull() {
		return ItemStack.EMPTY;
	}

	@Override
	protected void randomizeReinforcementsChance() {
		this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0);
	}

	@Nullable
	@Override
	public UUID getPersistentAngerTarget() {
		return this.persistentAngerTarget;
	}

	@Override
	public boolean isPreventingPlayerRest(Player player) {
		return this.isAngryAt(player);
	}

	@Override
	public boolean wantsToPickUp(ItemStack itemStack) {
		return this.canHoldItem(itemStack);
	}
}
