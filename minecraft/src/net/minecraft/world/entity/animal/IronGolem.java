package net.minecraft.world.entity.animal;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveBackToVillage;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class IronGolem extends AbstractGolem {
	protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(IronGolem.class, EntityDataSerializers.BYTE);
	private int attackAnimationTick;
	private int offerFlowerTick;

	public IronGolem(EntityType<? extends IronGolem> entityType, Level level) {
		super(entityType, level);
		this.maxUpStep = 1.0F;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
		this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9, 32.0F));
		this.goalSelector.addGoal(2, new MoveBackToVillage(this, 0.6));
		this.goalSelector.addGoal(3, new MoveThroughVillageGoal(this, 0.6, false, 4, () -> false));
		this.goalSelector.addGoal(5, new OfferFlowerGoal(this));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new DefendVillageTargetGoal(this));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.targetSelector
			.addGoal(
				3, new NearestAttackableTargetGoal(this, Mob.class, 5, false, false, livingEntity -> livingEntity instanceof Enemy && !(livingEntity instanceof Creeper))
			);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_FLAGS_ID, (byte)0);
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0);
	}

	@Override
	protected int decreaseAirSupply(int i) {
		return i;
	}

	@Override
	protected void doPush(Entity entity) {
		if (entity instanceof Enemy && !(entity instanceof Creeper) && this.getRandom().nextInt(20) == 0) {
			this.setTarget((LivingEntity)entity);
		}

		super.doPush(entity);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.attackAnimationTick > 0) {
			this.attackAnimationTick--;
		}

		if (this.offerFlowerTick > 0) {
			this.offerFlowerTick--;
		}

		if (getHorizontalDistanceSqr(this.getDeltaMovement()) > 2.5000003E-7F && this.random.nextInt(5) == 0) {
			int i = Mth.floor(this.getX());
			int j = Mth.floor(this.getY() - 0.2F);
			int k = Mth.floor(this.getZ());
			BlockState blockState = this.level.getBlockState(new BlockPos(i, j, k));
			if (!blockState.isAir()) {
				this.level
					.addParticle(
						new BlockParticleOption(ParticleTypes.BLOCK, blockState),
						this.getX() + ((double)this.random.nextFloat() - 0.5) * (double)this.getBbWidth(),
						this.getY() + 0.1,
						this.getZ() + ((double)this.random.nextFloat() - 0.5) * (double)this.getBbWidth(),
						4.0 * ((double)this.random.nextFloat() - 0.5),
						0.5,
						((double)this.random.nextFloat() - 0.5) * 4.0
					);
			}
		}
	}

	@Override
	public boolean canAttackType(EntityType<?> entityType) {
		if (this.isPlayerCreated() && entityType == EntityType.PLAYER) {
			return false;
		} else {
			return entityType == EntityType.CREEPER ? false : super.canAttackType(entityType);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("PlayerCreated", this.isPlayerCreated());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setPlayerCreated(compoundTag.getBoolean("PlayerCreated"));
	}

	private float getAttackDamage() {
		return (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		this.attackAnimationTick = 10;
		this.level.broadcastEntityEvent(this, (byte)4);
		float f = this.getAttackDamage();
		float g = f > 0.0F ? f / 2.0F + (float)this.random.nextInt((int)f) : 0.0F;
		boolean bl = entity.hurt(DamageSource.mobAttack(this), g);
		if (bl) {
			entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, 0.4F, 0.0));
			this.doEnchantDamageEffects(this, entity);
		}

		this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
		return bl;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		IronGolem.Crackiness crackiness = this.getCrackiness();
		boolean bl = super.hurt(damageSource, f);
		if (bl && this.getCrackiness() != crackiness) {
			this.playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
		}

		return bl;
	}

	public IronGolem.Crackiness getCrackiness() {
		return IronGolem.Crackiness.byFraction(this.getHealth() / this.getMaxHealth());
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 4) {
			this.attackAnimationTick = 10;
			this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
		} else if (b == 11) {
			this.offerFlowerTick = 400;
		} else if (b == 34) {
			this.offerFlowerTick = 0;
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Environment(EnvType.CLIENT)
	public int getAttackAnimationTick() {
		return this.attackAnimationTick;
	}

	public void offerFlower(boolean bl) {
		if (bl) {
			this.offerFlowerTick = 400;
			this.level.broadcastEntityEvent(this, (byte)11);
		} else {
			this.offerFlowerTick = 0;
			this.level.broadcastEntityEvent(this, (byte)34);
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.IRON_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.IRON_GOLEM_DEATH;
	}

	@Override
	protected boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Item item = itemStack.getItem();
		if (item != Items.IRON_INGOT) {
			return false;
		} else {
			float f = this.getHealth();
			this.heal(25.0F);
			if (this.getHealth() == f) {
				return false;
			} else {
				float g = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
				this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, g);
				if (!player.abilities.instabuild) {
					itemStack.shrink(1);
				}

				return true;
			}
		}
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
	}

	@Environment(EnvType.CLIENT)
	public int getOfferFlowerTick() {
		return this.offerFlowerTick;
	}

	public boolean isPlayerCreated() {
		return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
	}

	public void setPlayerCreated(boolean bl) {
		byte b = this.entityData.get(DATA_FLAGS_ID);
		if (bl) {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b | 1));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b & -2));
		}
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		BlockPos blockPos = this.blockPosition();
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = levelReader.getBlockState(blockPos2);
		if (!blockState.entityCanStandOn(levelReader, blockPos2, this)) {
			return false;
		} else {
			for (int i = 1; i < 3; i++) {
				BlockPos blockPos3 = blockPos.above(i);
				BlockState blockState2 = levelReader.getBlockState(blockPos3);
				if (!NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPos3, blockState2, blockState2.getFluidState())) {
					return false;
				}
			}

			return NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPos, levelReader.getBlockState(blockPos), Fluids.EMPTY.defaultFluidState())
				&& levelReader.isUnobstructed(this);
		}
	}

	public static enum Crackiness {
		NONE(1.0F),
		LOW(0.75F),
		MEDIUM(0.5F),
		HIGH(0.25F);

		private static final List<IronGolem.Crackiness> BY_DAMAGE = (List<IronGolem.Crackiness>)Stream.of(values())
			.sorted(Comparator.comparingDouble(crackiness -> (double)crackiness.fraction))
			.collect(ImmutableList.toImmutableList());
		private final float fraction;

		private Crackiness(float f) {
			this.fraction = f;
		}

		public static IronGolem.Crackiness byFraction(float f) {
			for (IronGolem.Crackiness crackiness : BY_DAMAGE) {
				if (f < crackiness.fraction) {
					return crackiness;
				}
			}

			return NONE;
		}
	}
}
