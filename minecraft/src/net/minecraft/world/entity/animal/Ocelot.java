package net.minecraft.world.entity.animal;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Ocelot extends Animal {
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
				this.setSneaking(true);
				this.setSprinting(false);
			} else if (d == 1.33) {
				this.setSneaking(false);
				this.setSprinting(true);
			} else {
				this.setSneaking(false);
				this.setSprinting(false);
			}
		} else {
			this.setSneaking(false);
			this.setSprinting(false);
		}
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.isTrusting() && this.tickCount > 2400;
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
	}

	@Override
	public void causeFallDamage(float f, float g) {
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

	@Override
	public boolean doHurtTarget(Entity entity) {
		return entity.hurt(DamageSource.mobAttack(this), 3.0F);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		return this.isInvulnerableTo(damageSource) ? false : super.hurt(damageSource, f);
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if ((this.temptGoal == null || this.temptGoal.isRunning()) && !this.isTrusting() && this.isFood(itemStack) && player.distanceToSqr(this) < 9.0) {
			this.usePlayerItem(player, itemStack);
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

			return true;
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Environment(EnvType.CLIENT)
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
			this.level
				.addParticle(
					particleOptions,
					this.x + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
					this.y + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()),
					this.z + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
					d,
					e,
					f
				);
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

	public Ocelot getBreedOffspring(AgableMob agableMob) {
		return EntityType.OCELOT.create(this.level);
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
			BlockPos blockPos = new BlockPos(this.x, this.getBoundingBox().minY, this.z);
			if (blockPos.getY() < levelReader.getSeaLevel()) {
				return false;
			}

			BlockState blockState = levelReader.getBlockState(blockPos.below());
			Block block = blockState.getBlock();
			if (block == Blocks.GRASS_BLOCK || blockState.is(BlockTags.LEAVES)) {
				return true;
			}
		}

		return false;
	}

	protected void addKittensDuringSpawn() {
		for (int i = 0; i < 2; i++) {
			Ocelot ocelot = EntityType.OCELOT.create(this.level);
			ocelot.moveTo(this.x, this.y, this.z, this.yRot, 0.0F);
			ocelot.setAge(-24000);
			this.level.addFreshEntity(ocelot);
		}
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
		spawnGroupData = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
		if (levelAccessor.getRandom().nextInt(7) == 0) {
			this.addKittensDuringSpawn();
		}

		return spawnGroupData;
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
