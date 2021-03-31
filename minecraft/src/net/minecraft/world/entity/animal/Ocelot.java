package net.minecraft.world.entity.animal;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Ocelot extends Animal {
	public static final double CROUCH_SPEED_MOD = 0.6;
	public static final double WALK_SPEED_MOD = 0.8;
	public static final double SPRINT_SPEED_MOD = 1.33;
	private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(Items.COD, Items.SALMON);
	private static final EntityDataAccessor<Boolean> DATA_TRUSTING = SynchedEntityData.defineId(Ocelot.class, EntityDataSerializers.BOOLEAN);
	private Ocelot.OcelotAvoidEntityGoal<Player> ocelotAvoidPlayersGoal;
	private Ocelot.OcelotTemptGoal temptGoal;

	public Ocelot(EntityType<? extends Ocelot> entityType, Level level) {
		super(entityType, level);
		this.reassessTrustingGoals();
	}

	private boolean isTrusting() {
		return this.entityData.get(DATA_TRUSTING);
	}

	private void setTrusting(boolean bl) {
		this.entityData.set(DATA_TRUSTING, bl);
		this.reassessTrustingGoals();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("Trusting", this.isTrusting());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setTrusting(compoundTag.getBoolean("Trusting"));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_TRUSTING, false);
	}

	@Override
	protected void registerGoals() {
		this.temptGoal = new Ocelot.OcelotTemptGoal(this, 0.6, TEMPT_INGREDIENT, true);
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(3, this.temptGoal);
		this.goalSelector.addGoal(7, new LeapAtTargetGoal(this, 0.3F));
		this.goalSelector.addGoal(8, new OcelotAttackGoal(this));
		this.goalSelector.addGoal(9, new BreedGoal(this, 0.8));
		this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
		this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Chicken.class, false));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR));
	}

	@Override
	public void customServerAiStep() {
		if (this.getMoveControl().hasWanted()) {
			double d = this.getMoveControl().getSpeedModifier();
			if (d == 0.6) {
				this.setPose(Pose.CROUCHING);
				this.setSprinting(false);
			} else if (d == 1.33) {
				this.setPose(Pose.STANDING);
				this.setSprinting(true);
			} else {
				this.setPose(Pose.STANDING);
				this.setSprinting(false);
			}
		} else {
			this.setPose(Pose.STANDING);
			this.setSprinting(false);
		}
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.isTrusting() && this.tickCount > 2400;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 3.0);
	}

	@Override
	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		return false;
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.OCELOT_AMBIENT;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 900;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.OCELOT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.OCELOT_DEATH;
	}

	private float getAttackDamage() {
		return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		return entity.hurt(DamageSource.mobAttack(this), this.getAttackDamage());
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if ((this.temptGoal == null || this.temptGoal.isRunning()) && !this.isTrusting() && this.isFood(itemStack) && player.distanceToSqr(this) < 9.0) {
			this.usePlayerItem(player, interactionHand, itemStack);
			if (!this.level.isClientSide) {
				if (this.random.nextInt(3) == 0) {
					this.setTrusting(true);
					this.spawnTrustingParticles(true);
					this.level.broadcastEntityEvent(this, (byte)41);
				} else {
					this.spawnTrustingParticles(false);
					this.level.broadcastEntityEvent(this, (byte)40);
				}
			}

			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 41) {
			this.spawnTrustingParticles(true);
		} else if (b == 40) {
			this.spawnTrustingParticles(false);
		} else {
			super.handleEntityEvent(b);
		}
	}

	private void spawnTrustingParticles(boolean bl) {
		ParticleOptions particleOptions = ParticleTypes.HEART;
		if (!bl) {
			particleOptions = ParticleTypes.SMOKE;
		}

		for (int i = 0; i < 7; i++) {
			double d = this.random.nextGaussian() * 0.02;
			double e = this.random.nextGaussian() * 0.02;
			double f = this.random.nextGaussian() * 0.02;
			this.level.addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
		}
	}

	protected void reassessTrustingGoals() {
		if (this.ocelotAvoidPlayersGoal == null) {
			this.ocelotAvoidPlayersGoal = new Ocelot.OcelotAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8, 1.33);
		}

		this.goalSelector.removeGoal(this.ocelotAvoidPlayersGoal);
		if (!this.isTrusting()) {
			this.goalSelector.addGoal(4, this.ocelotAvoidPlayersGoal);
		}
	}

	public Ocelot getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.OCELOT.create(serverLevel);
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return TEMPT_INGREDIENT.test(itemStack);
	}

	public static boolean checkOcelotSpawnRules(
		EntityType<Ocelot> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return random.nextInt(3) != 0;
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		if (levelReader.isUnobstructed(this) && !levelReader.containsAnyLiquid(this.getBoundingBox())) {
			BlockPos blockPos = this.blockPosition();
			if (blockPos.getY() < levelReader.getSeaLevel()) {
				return false;
			}

			BlockState blockState = levelReader.getBlockState(blockPos.below());
			if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(BlockTags.LEAVES)) {
				return true;
			}
		}

		return false;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		if (spawnGroupData == null) {
			spawnGroupData = new AgeableMob.AgeableMobGroupData(1.0F);
		}

		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Override
	public Vec3 getLeashOffset() {
		return new Vec3(0.0, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
	}

	@Override
	public boolean isSteppingCarefully() {
		return this.getPose() == Pose.CROUCHING || super.isSteppingCarefully();
	}

	static class OcelotAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
		private final Ocelot ocelot;

		public OcelotAvoidEntityGoal(Ocelot ocelot, Class<T> class_, float f, double d, double e) {
			super(ocelot, class_, f, d, e, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
			this.ocelot = ocelot;
		}

		@Override
		public boolean canUse() {
			return !this.ocelot.isTrusting() && super.canUse();
		}

		@Override
		public boolean canContinueToUse() {
			return !this.ocelot.isTrusting() && super.canContinueToUse();
		}
	}

	static class OcelotTemptGoal extends TemptGoal {
		private final Ocelot ocelot;

		public OcelotTemptGoal(Ocelot ocelot, double d, Ingredient ingredient, boolean bl) {
			super(ocelot, d, ingredient, bl);
			this.ocelot = ocelot;
		}

		@Override
		protected boolean canScare() {
			return super.canScare() && !this.ocelot.isTrusting();
		}
	}
}
