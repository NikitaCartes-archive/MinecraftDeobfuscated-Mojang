package net.minecraft.world.entity.monster;

import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestHealableRaiderTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Witch extends Raider implements RangedAttackMob {
	private static final UUID SPEED_MODIFIER_DRINKING_UUID = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
	private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(
		SPEED_MODIFIER_DRINKING_UUID, "Drinking speed penalty", -0.25, AttributeModifier.Operation.ADDITION
	);
	private static final EntityDataAccessor<Boolean> DATA_USING_ITEM = SynchedEntityData.defineId(Witch.class, EntityDataSerializers.BOOLEAN);
	private int usingTime;
	private NearestHealableRaiderTargetGoal<Raider> healRaidersGoal;
	private NearestAttackableWitchTargetGoal<Player> attackPlayersGoal;

	public Witch(EntityType<? extends Witch> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.healRaidersGoal = new NearestHealableRaiderTargetGoal<>(
			this, Raider.class, true, livingEntity -> livingEntity != null && this.hasActiveRaid() && livingEntity.getType() != EntityType.WITCH
		);
		this.attackPlayersGoal = new NearestAttackableWitchTargetGoal<>(this, Player.class, 10, true, false, null);
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 60, 10.0F));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class));
		this.targetSelector.addGoal(2, this.healRaidersGoal);
		this.targetSelector.addGoal(3, this.attackPlayersGoal);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.getEntityData().define(DATA_USING_ITEM, false);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WITCH_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.WITCH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITCH_DEATH;
	}

	public void setUsingItem(boolean bl) {
		this.getEntityData().set(DATA_USING_ITEM, bl);
	}

	public boolean isDrinkingPotion() {
		return this.getEntityData().get(DATA_USING_ITEM);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 26.0).add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@Override
	public void aiStep() {
		if (!this.level.isClientSide && this.isAlive()) {
			this.healRaidersGoal.decrementCooldown();
			if (this.healRaidersGoal.getCooldown() <= 0) {
				this.attackPlayersGoal.setCanAttack(true);
			} else {
				this.attackPlayersGoal.setCanAttack(false);
			}

			if (this.isDrinkingPotion()) {
				if (this.usingTime-- <= 0) {
					this.setUsingItem(false);
					ItemStack itemStack = this.getMainHandItem();
					this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
					if (itemStack.getItem() == Items.POTION) {
						List<MobEffectInstance> list = PotionUtils.getMobEffects(itemStack);
						if (list != null) {
							for (MobEffectInstance mobEffectInstance : list) {
								this.addEffect(new MobEffectInstance(mobEffectInstance));
							}
						}
					}

					this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING);
				}
			} else {
				Potion potion = null;
				if (this.random.nextFloat() < 0.15F && this.isEyeInFluid(FluidTags.WATER) && !this.hasEffect(MobEffects.WATER_BREATHING)) {
					potion = Potions.WATER_BREATHING;
				} else if (this.random.nextFloat() < 0.15F
					&& (this.isOnFire() || this.getLastDamageSource() != null && this.getLastDamageSource().isFire())
					&& !this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
					potion = Potions.FIRE_RESISTANCE;
				} else if (this.random.nextFloat() < 0.05F && this.getHealth() < this.getMaxHealth()) {
					potion = Potions.HEALING;
				} else if (this.random.nextFloat() < 0.5F
					&& this.getTarget() != null
					&& !this.hasEffect(MobEffects.MOVEMENT_SPEED)
					&& this.getTarget().distanceToSqr(this) > 121.0) {
					potion = Potions.SWIFTNESS;
				}

				if (potion != null) {
					this.setItemSlot(EquipmentSlot.MAINHAND, PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
					this.usingTime = this.getMainHandItem().getUseDuration();
					this.setUsingItem(true);
					if (!this.isSilent()) {
						this.level
							.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
					}

					AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
					attributeInstance.removeModifier(SPEED_MODIFIER_DRINKING);
					attributeInstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
				}
			}

			if (this.random.nextFloat() < 7.5E-4F) {
				this.level.broadcastEntityEvent(this, (byte)15);
			}
		}

		super.aiStep();
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return SoundEvents.WITCH_CELEBRATE;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 15) {
			for (int i = 0; i < this.random.nextInt(35) + 10; i++) {
				this.level
					.addParticle(
						ParticleTypes.WITCH,
						this.getX() + this.random.nextGaussian() * 0.13F,
						this.getBoundingBox().maxY + 0.5 + this.random.nextGaussian() * 0.13F,
						this.getZ() + this.random.nextGaussian() * 0.13F,
						0.0,
						0.0,
						0.0
					);
			}
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Override
	protected float getDamageAfterMagicAbsorb(DamageSource damageSource, float f) {
		f = super.getDamageAfterMagicAbsorb(damageSource, f);
		if (damageSource.getEntity() == this) {
			f = 0.0F;
		}

		if (damageSource.isMagic()) {
			f = (float)((double)f * 0.15);
		}

		return f;
	}

	@Override
	public void performRangedAttack(LivingEntity livingEntity, float f) {
		if (!this.isDrinkingPotion()) {
			Vec3 vec3 = livingEntity.getDeltaMovement();
			double d = livingEntity.getX() + vec3.x - this.getX();
			double e = livingEntity.getEyeY() - 1.1F - this.getY();
			double g = livingEntity.getZ() + vec3.z - this.getZ();
			float h = Mth.sqrt(d * d + g * g);
			Potion potion = Potions.HARMING;
			if (livingEntity instanceof Raider) {
				if (livingEntity.getHealth() <= 4.0F) {
					potion = Potions.HEALING;
				} else {
					potion = Potions.REGENERATION;
				}

				this.setTarget(null);
			} else if (h >= 8.0F && !livingEntity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
				potion = Potions.SLOWNESS;
			} else if (livingEntity.getHealth() >= 8.0F && !livingEntity.hasEffect(MobEffects.POISON)) {
				potion = Potions.POISON;
			} else if (h <= 3.0F && !livingEntity.hasEffect(MobEffects.WEAKNESS) && this.random.nextFloat() < 0.25F) {
				potion = Potions.WEAKNESS;
			}

			ThrownPotion thrownPotion = new ThrownPotion(this.level, this);
			thrownPotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
			thrownPotion.xRot -= -20.0F;
			thrownPotion.shoot(d, e + (double)(h * 0.2F), g, 0.75F, 8.0F);
			if (!this.isSilent()) {
				this.level
					.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
			}

			this.level.addFreshEntity(thrownPotion);
		}
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 1.62F;
	}

	@Override
	public void applyRaidBuffs(int i, boolean bl) {
	}

	@Override
	public boolean canBeLeader() {
		return false;
	}
}
