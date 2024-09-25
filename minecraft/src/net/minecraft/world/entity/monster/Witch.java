package net.minecraft.world.entity.monster;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class Witch extends Raider implements RangedAttackMob {
	private static final ResourceLocation SPEED_MODIFIER_DRINKING_ID = ResourceLocation.withDefaultNamespace("drinking");
	private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(
		SPEED_MODIFIER_DRINKING_ID, -0.25, AttributeModifier.Operation.ADD_VALUE
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
			this, Raider.class, true, (livingEntity, serverLevel) -> this.hasActiveRaid() && livingEntity.getType() != EntityType.WITCH
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
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_USING_ITEM, false);
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
		if (!this.level().isClientSide && this.isAlive()) {
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
					PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
					if (itemStack.is(Items.POTION) && potionContents != null) {
						potionContents.forEachEffect(this::addEffect);
					}

					this.gameEvent(GameEvent.DRINK);
					this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING.id());
				}
			} else {
				Holder<Potion> holder = null;
				if (this.random.nextFloat() < 0.15F && this.isEyeInFluid(FluidTags.WATER) && !this.hasEffect(MobEffects.WATER_BREATHING)) {
					holder = Potions.WATER_BREATHING;
				} else if (this.random.nextFloat() < 0.15F
					&& (this.isOnFire() || this.getLastDamageSource() != null && this.getLastDamageSource().is(DamageTypeTags.IS_FIRE))
					&& !this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
					holder = Potions.FIRE_RESISTANCE;
				} else if (this.random.nextFloat() < 0.05F && this.getHealth() < this.getMaxHealth()) {
					holder = Potions.HEALING;
				} else if (this.random.nextFloat() < 0.5F
					&& this.getTarget() != null
					&& !this.hasEffect(MobEffects.MOVEMENT_SPEED)
					&& this.getTarget().distanceToSqr(this) > 121.0) {
					holder = Potions.SWIFTNESS;
				}

				if (holder != null) {
					this.setItemSlot(EquipmentSlot.MAINHAND, PotionContents.createItemStack(Items.POTION, holder));
					this.usingTime = this.getMainHandItem().getUseDuration(this);
					this.setUsingItem(true);
					if (!this.isSilent()) {
						this.level()
							.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
					}

					AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
					attributeInstance.removeModifier(SPEED_MODIFIER_DRINKING_ID);
					attributeInstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
				}
			}

			if (this.random.nextFloat() < 7.5E-4F) {
				this.level().broadcastEntityEvent(this, (byte)15);
			}
		}

		super.aiStep();
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return SoundEvents.WITCH_CELEBRATE;
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 15) {
			for (int i = 0; i < this.random.nextInt(35) + 10; i++) {
				this.level()
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

		if (damageSource.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
			f *= 0.15F;
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
			double h = Math.sqrt(d * d + g * g);
			Holder<Potion> holder = Potions.HARMING;
			if (livingEntity instanceof Raider) {
				if (livingEntity.getHealth() <= 4.0F) {
					holder = Potions.HEALING;
				} else {
					holder = Potions.REGENERATION;
				}

				this.setTarget(null);
			} else if (h >= 8.0 && !livingEntity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
				holder = Potions.SLOWNESS;
			} else if (livingEntity.getHealth() >= 8.0F && !livingEntity.hasEffect(MobEffects.POISON)) {
				holder = Potions.POISON;
			} else if (h <= 3.0 && !livingEntity.hasEffect(MobEffects.WEAKNESS) && this.random.nextFloat() < 0.25F) {
				holder = Potions.WEAKNESS;
			}

			if (this.level() instanceof ServerLevel serverLevel) {
				ItemStack itemStack = PotionContents.createItemStack(Items.SPLASH_POTION, holder);
				Projectile.spawnProjectileUsingShoot(ThrownPotion::new, serverLevel, itemStack, this, d, e + h * 0.2, g, 0.75F, 8.0F);
			}

			if (!this.isSilent()) {
				this.level()
					.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
			}
		}
	}

	@Override
	public void applyRaidBuffs(ServerLevel serverLevel, int i, boolean bl) {
	}

	@Override
	public boolean canBeLeader() {
		return false;
	}
}
