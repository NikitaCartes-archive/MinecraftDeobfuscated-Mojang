package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SnowGolem extends AbstractGolem implements RangedAttackMob {
	private static final EntityDataAccessor<Byte> DATA_PUMPKIN_ID = SynchedEntityData.defineId(SnowGolem.class, EntityDataSerializers.BYTE);

	public SnowGolem(EntityType<? extends SnowGolem> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 20, 10.0F));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0, 1.0000001E-5F));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Mob.class, 10, true, false, livingEntity -> livingEntity instanceof Enemy));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_PUMPKIN_ID, (byte)16);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("Pumpkin", this.hasPumpkin());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("Pumpkin")) {
			this.setPumpkin(compoundTag.getBoolean("Pumpkin"));
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide) {
			int i = Mth.floor(this.getX());
			int j = Mth.floor(this.getY());
			int k = Mth.floor(this.getZ());
			if (this.isInWaterRainOrBubble()) {
				this.hurt(DamageSource.DROWN, 1.0F);
			}

			if (this.level.getBiome(new BlockPos(i, 0, k)).getTemperature(new BlockPos(i, j, k)) > 1.0F) {
				this.hurt(DamageSource.ON_FIRE, 1.0F);
			}

			if (!this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
				return;
			}

			BlockState blockState = Blocks.SNOW.defaultBlockState();

			for (int l = 0; l < 4; l++) {
				i = Mth.floor(this.getX() + (double)((float)(l % 2 * 2 - 1) * 0.25F));
				j = Mth.floor(this.getY());
				k = Mth.floor(this.getZ() + (double)((float)(l / 2 % 2 * 2 - 1) * 0.25F));
				BlockPos blockPos = new BlockPos(i, j, k);
				if (this.level.getBlockState(blockPos).isAir()
					&& this.level.getBiome(blockPos).getTemperature(blockPos) < 0.8F
					&& blockState.canSurvive(this.level, blockPos)) {
					this.level.setBlockAndUpdate(blockPos, blockState);
				}
			}
		}
	}

	@Override
	public void performRangedAttack(LivingEntity livingEntity, float f) {
		Snowball snowball = new Snowball(this.level, this);
		double d = livingEntity.getEyeY() - 1.1F;
		double e = livingEntity.getX() - this.getX();
		double g = d - snowball.getY();
		double h = livingEntity.getZ() - this.getZ();
		float i = Mth.sqrt(e * e + h * h) * 0.2F;
		snowball.shoot(e, g + (double)i, h, 1.6F, 12.0F);
		this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
		this.level.addFreshEntity(snowball);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 1.7F;
	}

	@Override
	protected boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.getItem() == Items.SHEARS && this.hasPumpkin()) {
			if (!this.level.isClientSide) {
				this.setPumpkin(false);
				itemStack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
				this.spawnAtLocation(new ItemStack(Items.CARVED_PUMPKIN), 1.7F);
				this.playSound(SoundEvents.SNOW_GOLEM_SHEAR, 1.0F, 1.0F);
			}

			return true;
		} else {
			return false;
		}
	}

	public boolean hasPumpkin() {
		return (this.entityData.get(DATA_PUMPKIN_ID) & 16) != 0;
	}

	public void setPumpkin(boolean bl) {
		byte b = this.entityData.get(DATA_PUMPKIN_ID);
		if (bl) {
			this.entityData.set(DATA_PUMPKIN_ID, (byte)(b | 16));
		} else {
			this.entityData.set(DATA_PUMPKIN_ID, (byte)(b & -17));
		}
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SNOW_GOLEM_AMBIENT;
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.SNOW_GOLEM_HURT;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SNOW_GOLEM_DEATH;
	}
}
