package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class Goat extends Animal {
	public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.scalable(0.9F, 1.3F).scale(0.7F);
	private static final int ADULT_ATTACK_DAMAGE = 2;
	private static final int BABY_ATTACK_DAMAGE = 1;
	protected static final ImmutableList<SensorType<? extends Sensor<? super Goat>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES,
		SensorType.NEAREST_PLAYERS,
		SensorType.NEAREST_ITEMS,
		SensorType.NEAREST_ADULT,
		SensorType.HURT_BY,
		SensorType.GOAT_TEMPTATIONS
	);
	protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.ATE_RECENTLY,
		MemoryModuleType.BREED_TARGET,
		MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
		MemoryModuleType.LONG_JUMP_MID_JUMP,
		MemoryModuleType.TEMPTING_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ADULT,
		MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
		MemoryModuleType.IS_TEMPTED,
		MemoryModuleType.RAM_COOLDOWN_TICKS,
		MemoryModuleType.RAM_TARGET
	);
	public static final int GOAT_FALL_DAMAGE_REDUCTION = 10;
	public static final double GOAT_SCREAMING_CHANCE = 0.02;
	private static final EntityDataAccessor<Boolean> DATA_IS_SCREAMING_GOAT = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
	private boolean isLoweringHead;
	private int lowerHeadTick;

	public Goat(EntityType<? extends Goat> entityType, Level level) {
		super(entityType, level);
		this.getNavigation().setCanFloat(true);
	}

	@Override
	protected Brain.Provider<Goat> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return GoatAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2F).add(Attributes.ATTACK_DAMAGE, 2.0);
	}

	@Override
	protected void ageBoundaryReached() {
		if (this.isBaby()) {
			this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1.0);
		} else {
			this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0);
		}
	}

	@Override
	protected int calculateFallDamage(float f, float g) {
		return super.calculateFallDamage(f, g) - 10;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_AMBIENT : SoundEvents.GOAT_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_HURT : SoundEvents.GOAT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_DEATH : SoundEvents.GOAT_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.GOAT_STEP, 0.15F, 1.0F);
	}

	protected SoundEvent getMilkingSound() {
		return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
	}

	public Goat getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Goat goat = EntityType.GOAT.create(serverLevel);
		if (goat != null) {
			GoatAi.initMemories(goat);
			boolean bl = ageableMob instanceof Goat && ((Goat)ageableMob).isScreamingGoat();
			goat.setScreamingGoat(bl || serverLevel.getRandom().nextDouble() < 0.02);
		}

		return goat;
	}

	@Override
	public Brain<Goat> getBrain() {
		return (Brain<Goat>)super.getBrain();
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("goatBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		this.level.getProfiler().push("goatActivityUpdate");
		GoatAi.updateActivity(this);
		this.level.getProfiler().pop();
		super.customServerAiStep();
	}

	@Override
	public int getMaxHeadYRot() {
		return 15;
	}

	@Override
	public void setYHeadRot(float f) {
		int i = this.getMaxHeadYRot();
		float g = Mth.degreesDifference(this.yBodyRot, f);
		float h = Mth.clamp(g, (float)(-i), (float)i);
		super.setYHeadRot(this.yBodyRot + h);
	}

	@Override
	public SoundEvent getEatingSound(ItemStack itemStack) {
		return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_EAT : SoundEvents.GOAT_EAT;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.is(Items.BUCKET) && !this.isBaby()) {
			player.playSound(this.getMilkingSound(), 1.0F, 1.0F);
			ItemStack itemStack2 = ItemUtils.createFilledResult(itemStack, player, Items.MILK_BUCKET.getDefaultInstance());
			player.setItemInHand(interactionHand, itemStack2);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else {
			InteractionResult interactionResult = super.mobInteract(player, interactionHand);
			if (interactionResult.consumesAction() && this.isFood(itemStack)) {
				this.level.playSound(null, this, this.getEatingSound(itemStack), SoundSource.NEUTRAL, 1.0F, Mth.randomBetween(this.level.random, 0.8F, 1.2F));
			}

			return interactionResult;
		}
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		GoatAi.initMemories(this);
		this.setScreamingGoat(serverLevelAccessor.getRandom().nextDouble() < 0.02);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return pose == Pose.LONG_JUMPING ? LONG_JUMPING_DIMENSIONS.scale(this.getScale()) : super.getDimensions(pose);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("IsScreamingGoat", this.isScreamingGoat());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setScreamingGoat(compoundTag.getBoolean("IsScreamingGoat"));
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 58) {
			this.isLoweringHead = true;
		} else if (b == 59) {
			this.isLoweringHead = false;
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Override
	public void aiStep() {
		if (this.isLoweringHead) {
			this.lowerHeadTick++;
		} else {
			this.lowerHeadTick -= 2;
		}

		this.lowerHeadTick = Mth.clamp(this.lowerHeadTick, 0, 20);
		super.aiStep();
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_IS_SCREAMING_GOAT, false);
	}

	public boolean isScreamingGoat() {
		return this.entityData.get(DATA_IS_SCREAMING_GOAT);
	}

	public void setScreamingGoat(boolean bl) {
		this.entityData.set(DATA_IS_SCREAMING_GOAT, bl);
	}

	public float getRammingXHeadRot() {
		return (float)this.lowerHeadTick / 20.0F * 30.0F * (float) (Math.PI / 180.0);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new Goat.GoatPathNavigation(this, level);
	}

	public static boolean checkGoatSpawnRules(
		EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.GOATS_SPAWNABLE_ON) && isBrightEnoughToSpawn(levelAccessor, blockPos);
	}

	static class GoatNodeEvaluator extends WalkNodeEvaluator {
		private final BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();

		@Override
		public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
			this.belowPos.set(i, j - 1, k);
			BlockPathTypes blockPathTypes = getBlockPathTypeRaw(blockGetter, this.belowPos);
			return blockPathTypes == BlockPathTypes.POWDER_SNOW ? BlockPathTypes.BLOCKED : getBlockPathTypeStatic(blockGetter, this.belowPos.move(Direction.UP));
		}
	}

	static class GoatPathNavigation extends GroundPathNavigation {
		GoatPathNavigation(Goat goat, Level level) {
			super(goat, level);
		}

		@Override
		protected PathFinder createPathFinder(int i) {
			this.nodeEvaluator = new Goat.GoatNodeEvaluator();
			return new PathFinder(this.nodeEvaluator, i);
		}
	}
}
