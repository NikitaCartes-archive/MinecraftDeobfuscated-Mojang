package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Parrot extends ShoulderRidingEntity implements FlyingAnimal {
	private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);
	private static final Predicate<Mob> NOT_PARROT_PREDICATE = new Predicate<Mob>() {
		public boolean test(@Nullable Mob mob) {
			return mob != null && Parrot.MOB_SOUND_MAP.containsKey(mob.getType());
		}
	};
	private static final Item POISONOUS_FOOD = Items.COOKIE;
	private static final Set<Item> TAME_FOOD = Sets.<Item>newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
	private static final Map<EntityType<?>, SoundEvent> MOB_SOUND_MAP = Util.make(Maps.<EntityType<?>, SoundEvent>newHashMap(), hashMap -> {
		hashMap.put(EntityType.BLAZE, SoundEvents.PARROT_IMITATE_BLAZE);
		hashMap.put(EntityType.CAVE_SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
		hashMap.put(EntityType.CREEPER, SoundEvents.PARROT_IMITATE_CREEPER);
		hashMap.put(EntityType.DROWNED, SoundEvents.PARROT_IMITATE_DROWNED);
		hashMap.put(EntityType.ELDER_GUARDIAN, SoundEvents.PARROT_IMITATE_ELDER_GUARDIAN);
		hashMap.put(EntityType.ENDER_DRAGON, SoundEvents.PARROT_IMITATE_ENDER_DRAGON);
		hashMap.put(EntityType.ENDERMITE, SoundEvents.PARROT_IMITATE_ENDERMITE);
		hashMap.put(EntityType.EVOKER, SoundEvents.PARROT_IMITATE_EVOKER);
		hashMap.put(EntityType.GHAST, SoundEvents.PARROT_IMITATE_GHAST);
		hashMap.put(EntityType.GUARDIAN, SoundEvents.PARROT_IMITATE_GUARDIAN);
		hashMap.put(EntityType.HOGLIN, SoundEvents.PARROT_IMITATE_HOGLIN);
		hashMap.put(EntityType.HUSK, SoundEvents.PARROT_IMITATE_HUSK);
		hashMap.put(EntityType.ILLUSIONER, SoundEvents.PARROT_IMITATE_ILLUSIONER);
		hashMap.put(EntityType.MAGMA_CUBE, SoundEvents.PARROT_IMITATE_MAGMA_CUBE);
		hashMap.put(EntityType.PHANTOM, SoundEvents.PARROT_IMITATE_PHANTOM);
		hashMap.put(EntityType.PIGLIN, SoundEvents.PARROT_IMITATE_PIGLIN);
		hashMap.put(EntityType.PILLAGER, SoundEvents.PARROT_IMITATE_PILLAGER);
		hashMap.put(EntityType.RAVAGER, SoundEvents.PARROT_IMITATE_RAVAGER);
		hashMap.put(EntityType.SHULKER, SoundEvents.PARROT_IMITATE_SHULKER);
		hashMap.put(EntityType.SILVERFISH, SoundEvents.PARROT_IMITATE_SILVERFISH);
		hashMap.put(EntityType.SKELETON, SoundEvents.PARROT_IMITATE_SKELETON);
		hashMap.put(EntityType.SLIME, SoundEvents.PARROT_IMITATE_SLIME);
		hashMap.put(EntityType.SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
		hashMap.put(EntityType.STRAY, SoundEvents.PARROT_IMITATE_STRAY);
		hashMap.put(EntityType.VEX, SoundEvents.PARROT_IMITATE_VEX);
		hashMap.put(EntityType.VINDICATOR, SoundEvents.PARROT_IMITATE_VINDICATOR);
		hashMap.put(EntityType.WITCH, SoundEvents.PARROT_IMITATE_WITCH);
		hashMap.put(EntityType.WITHER, SoundEvents.PARROT_IMITATE_WITHER);
		hashMap.put(EntityType.WITHER_SKELETON, SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
		hashMap.put(EntityType.ZOGLIN, SoundEvents.PARROT_IMITATE_ZOGLIN);
		hashMap.put(EntityType.ZOMBIE, SoundEvents.PARROT_IMITATE_ZOMBIE);
		hashMap.put(EntityType.ZOMBIE_VILLAGER, SoundEvents.PARROT_IMITATE_ZOMBIE_VILLAGER);
	});
	public float flap;
	public float flapSpeed;
	public float oFlapSpeed;
	public float oFlap;
	private float flapping = 1.0F;
	private boolean partyParrot;
	private BlockPos jukebox;

	public Parrot(EntityType<? extends Parrot> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new FlyingMoveControl(this, 10, false);
		this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
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
		this.setVariant(this.random.nextInt(5));
		if (spawnGroupData == null) {
			spawnGroupData = new AgableMob.AgableMobGroupData();
			((AgableMob.AgableMobGroupData)spawnGroupData).setShouldSpawnBaby(false);
		}

		return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Override
	public boolean isBaby() {
		return false;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new PanicGoal(this, 1.25));
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0, 5.0F, 1.0F, true));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0));
		this.goalSelector.addGoal(3, new LandOnOwnersShoulderGoal(this));
		this.goalSelector.addGoal(3, new FollowMobGoal(this, 1.0, 3.0F, 7.0F));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.FLYING_SPEED, 0.4F).add(Attributes.MOVEMENT_SPEED, 0.2F);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
		flyingPathNavigation.setCanOpenDoors(false);
		flyingPathNavigation.setCanFloat(true);
		flyingPathNavigation.setCanPassDoors(true);
		return flyingPathNavigation;
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.6F;
	}

	@Override
	public void aiStep() {
		if (this.jukebox == null || !this.jukebox.closerThan(this.position(), 3.46) || !this.level.getBlockState(this.jukebox).is(Blocks.JUKEBOX)) {
			this.partyParrot = false;
			this.jukebox = null;
		}

		if (this.level.random.nextInt(400) == 0) {
			imitateNearbyMobs(this.level, this);
		}

		super.aiStep();
		this.calculateFlapping();
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void setRecordPlayingNearby(BlockPos blockPos, boolean bl) {
		this.jukebox = blockPos;
		this.partyParrot = bl;
	}

	@Environment(EnvType.CLIENT)
	public boolean isPartyParrot() {
		return this.partyParrot;
	}

	private void calculateFlapping() {
		this.oFlap = this.flap;
		this.oFlapSpeed = this.flapSpeed;
		this.flapSpeed = (float)((double)this.flapSpeed + (double)(!this.onGround && !this.isPassenger() ? 4 : -1) * 0.3);
		this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
		if (!this.onGround && this.flapping < 1.0F) {
			this.flapping = 1.0F;
		}

		this.flapping = (float)((double)this.flapping * 0.9);
		Vec3 vec3 = this.getDeltaMovement();
		if (!this.onGround && vec3.y < 0.0) {
			this.setDeltaMovement(vec3.multiply(1.0, 0.6, 1.0));
		}

		this.flap = this.flap + this.flapping * 2.0F;
	}

	public static boolean imitateNearbyMobs(Level level, Entity entity) {
		if (entity.isAlive() && !entity.isSilent() && level.random.nextInt(2) == 0) {
			List<Mob> list = level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(20.0), NOT_PARROT_PREDICATE);
			if (!list.isEmpty()) {
				Mob mob = (Mob)list.get(level.random.nextInt(list.size()));
				if (!mob.isSilent()) {
					SoundEvent soundEvent = getImitatedSound(mob.getType());
					level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, entity.getSoundSource(), 0.7F, getPitch(level.random));
					return true;
				}
			}

			return false;
		} else {
			return false;
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!this.isTame() && TAME_FOOD.contains(itemStack.getItem())) {
			if (!player.abilities.instabuild) {
				itemStack.shrink(1);
			}

			if (!this.isSilent()) {
				this.level
					.playSound(
						null,
						this.getX(),
						this.getY(),
						this.getZ(),
						SoundEvents.PARROT_EAT,
						this.getSoundSource(),
						1.0F,
						1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
					);
			}

			if (!this.level.isClientSide) {
				if (this.random.nextInt(10) == 0) {
					this.tame(player);
					this.level.broadcastEntityEvent(this, (byte)7);
				} else {
					this.level.broadcastEntityEvent(this, (byte)6);
				}
			}

			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else if (itemStack.getItem() == POISONOUS_FOOD) {
			if (!player.abilities.instabuild) {
				itemStack.shrink(1);
			}

			this.addEffect(new MobEffectInstance(MobEffects.POISON, 900));
			if (player.isCreative() || !this.isInvulnerable()) {
				this.hurt(DamageSource.playerAttack(player), Float.MAX_VALUE);
			}

			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else if (!this.isFlying() && this.isTame() && this.isOwnedBy(player)) {
			if (!this.level.isClientSide) {
				this.setOrderedToSit(!this.isOrderedToSit());
			}

			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return false;
	}

	public static boolean checkParrotSpawnRules(
		EntityType<Parrot> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		BlockState blockState = levelAccessor.getBlockState(blockPos.below());
		return (blockState.is(BlockTags.LEAVES) || blockState.is(Blocks.GRASS_BLOCK) || blockState.is(BlockTags.LOGS) || blockState.is(Blocks.AIR))
			&& levelAccessor.getRawBrightness(blockPos, 0) > 8;
	}

	@Override
	public boolean causeFallDamage(float f, float g) {
		return false;
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
	}

	@Override
	public boolean canMate(Animal animal) {
		return false;
	}

	@Nullable
	@Override
	public AgableMob getBreedOffspring(AgableMob agableMob) {
		return null;
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		return entity.hurt(DamageSource.mobAttack(this), 3.0F);
	}

	@Nullable
	@Override
	public SoundEvent getAmbientSound() {
		return getAmbient(this.level, this.level.random);
	}

	public static SoundEvent getAmbient(Level level, Random random) {
		if (level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(1000) == 0) {
			List<EntityType<?>> list = Lists.<EntityType<?>>newArrayList(MOB_SOUND_MAP.keySet());
			return getImitatedSound((EntityType<?>)list.get(random.nextInt(list.size())));
		} else {
			return SoundEvents.PARROT_AMBIENT;
		}
	}

	private static SoundEvent getImitatedSound(EntityType<?> entityType) {
		return (SoundEvent)MOB_SOUND_MAP.getOrDefault(entityType, SoundEvents.PARROT_AMBIENT);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.PARROT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PARROT_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.PARROT_STEP, 0.15F, 1.0F);
	}

	@Override
	protected float playFlySound(float f) {
		this.playSound(SoundEvents.PARROT_FLY, 0.15F, 1.0F);
		return f + this.flapSpeed / 2.0F;
	}

	@Override
	protected boolean makeFlySound() {
		return true;
	}

	@Override
	protected float getVoicePitch() {
		return getPitch(this.random);
	}

	public static float getPitch(Random random) {
		return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.NEUTRAL;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected void doPush(Entity entity) {
		if (!(entity instanceof Player)) {
			super.doPush(entity);
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			this.setOrderedToSit(false);
			return super.hurt(damageSource, f);
		}
	}

	public int getVariant() {
		return Mth.clamp(this.entityData.get(DATA_VARIANT_ID), 0, 4);
	}

	public void setVariant(int i) {
		this.entityData.set(DATA_VARIANT_ID, i);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_VARIANT_ID, 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Variant", this.getVariant());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setVariant(compoundTag.getInt("Variant"));
	}

	public boolean isFlying() {
		return !this.onGround;
	}
}
