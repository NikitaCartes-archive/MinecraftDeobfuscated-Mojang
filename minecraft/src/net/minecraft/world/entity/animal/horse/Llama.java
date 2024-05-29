package net.minecraft.world.entity.animal.horse;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Llama extends AbstractChestedHorse implements VariantHolder<Llama.Variant>, RangedAttackMob {
	private static final int MAX_STRENGTH = 5;
	private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
	private static final EntityDimensions BABY_DIMENSIONS = EntityType.LLAMA
		.getDimensions()
		.withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, EntityType.LLAMA.getHeight() - 0.8125F, -0.3F))
		.scale(0.5F);
	boolean didSpit;
	@Nullable
	private Llama caravanHead;
	@Nullable
	private Llama caravanTail;

	public Llama(EntityType<? extends Llama> entityType, Level level) {
		super(entityType, level);
	}

	public boolean isTraderLlama() {
		return false;
	}

	private void setStrength(int i) {
		this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, i)));
	}

	private void setRandomStrength(RandomSource randomSource) {
		int i = randomSource.nextFloat() < 0.04F ? 5 : 3;
		this.setStrength(1 + randomSource.nextInt(i));
	}

	public int getStrength() {
		return this.entityData.get(DATA_STRENGTH_ID);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Variant", this.getVariant().id);
		compoundTag.putInt("Strength", this.getStrength());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		this.setStrength(compoundTag.getInt("Strength"));
		super.readAdditionalSaveData(compoundTag);
		this.setVariant(Llama.Variant.byId(compoundTag.getInt("Variant")));
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
		this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, 2.1F));
		this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25, 40, 20.0F));
		this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
		this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(5, new TemptGoal(this, 1.25, itemStack -> itemStack.is(ItemTags.LLAMA_TEMPT_ITEMS), false));
		this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.0));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new Llama.LlamaHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new Llama.LlamaAttackWolfGoal(this));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return createBaseChestedHorseAttributes().add(Attributes.FOLLOW_RANGE, 40.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_STRENGTH_ID, 0);
		builder.define(DATA_VARIANT_ID, 0);
	}

	public Llama.Variant getVariant() {
		return Llama.Variant.byId(this.entityData.get(DATA_VARIANT_ID));
	}

	public void setVariant(Llama.Variant variant) {
		this.entityData.set(DATA_VARIANT_ID, variant.id);
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.is(ItemTags.LLAMA_FOOD);
	}

	@Override
	protected boolean handleEating(Player player, ItemStack itemStack) {
		int i = 0;
		int j = 0;
		float f = 0.0F;
		boolean bl = false;
		if (itemStack.is(Items.WHEAT)) {
			i = 10;
			j = 3;
			f = 2.0F;
		} else if (itemStack.is(Blocks.HAY_BLOCK.asItem())) {
			i = 90;
			j = 6;
			f = 10.0F;
			if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
				bl = true;
				this.setInLove(player);
			}
		}

		if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
			this.heal(f);
			bl = true;
		}

		if (this.isBaby() && i > 0) {
			this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
			if (!this.level().isClientSide) {
				this.ageUp(i);
			}

			bl = true;
		}

		if (j > 0 && (bl || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
			bl = true;
			if (!this.level().isClientSide) {
				this.modifyTemper(j);
			}
		}

		if (bl && !this.isSilent()) {
			SoundEvent soundEvent = this.getEatingSound();
			if (soundEvent != null) {
				this.level()
					.playSound(
						null,
						this.getX(),
						this.getY(),
						this.getZ(),
						this.getEatingSound(),
						this.getSoundSource(),
						1.0F,
						1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
					);
			}
		}

		return bl;
	}

	@Override
	public boolean isImmobile() {
		return this.isDeadOrDying() || this.isEating();
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		RandomSource randomSource = serverLevelAccessor.getRandom();
		this.setRandomStrength(randomSource);
		Llama.Variant variant;
		if (spawnGroupData instanceof Llama.LlamaGroupData) {
			variant = ((Llama.LlamaGroupData)spawnGroupData).variant;
		} else {
			variant = Util.getRandom(Llama.Variant.values(), randomSource);
			spawnGroupData = new Llama.LlamaGroupData(variant);
		}

		this.setVariant(variant);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
	}

	@Override
	protected boolean canPerformRearing() {
		return false;
	}

	@Override
	protected SoundEvent getAngrySound() {
		return SoundEvents.LLAMA_ANGRY;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.LLAMA_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.LLAMA_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.LLAMA_DEATH;
	}

	@Nullable
	@Override
	protected SoundEvent getEatingSound() {
		return SoundEvents.LLAMA_EAT;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.LLAMA_STEP, 0.15F, 1.0F);
	}

	@Override
	protected void playChestEquipsSound() {
		this.playSound(SoundEvents.LLAMA_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
	}

	@Override
	public int getInventoryColumns() {
		return this.hasChest() ? this.getStrength() : 0;
	}

	@Override
	public boolean canUseSlot(EquipmentSlot equipmentSlot) {
		return true;
	}

	@Override
	public boolean isBodyArmorItem(ItemStack itemStack) {
		return itemStack.is(ItemTags.WOOL_CARPETS);
	}

	@Override
	public boolean isSaddleable() {
		return false;
	}

	@Nullable
	private static DyeColor getDyeColor(ItemStack itemStack) {
		Block block = Block.byItem(itemStack.getItem());
		return block instanceof WoolCarpetBlock ? ((WoolCarpetBlock)block).getColor() : null;
	}

	@Nullable
	public DyeColor getSwag() {
		return getDyeColor(this.getItemBySlot(EquipmentSlot.BODY));
	}

	@Override
	public int getMaxTemper() {
		return 30;
	}

	@Override
	public boolean canMate(Animal animal) {
		return animal != this && animal instanceof Llama && this.canParent() && ((Llama)animal).canParent();
	}

	@Nullable
	public Llama getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Llama llama = this.makeNewLlama();
		if (llama != null) {
			this.setOffspringAttributes(ageableMob, llama);
			Llama llama2 = (Llama)ageableMob;
			int i = this.random.nextInt(Math.max(this.getStrength(), llama2.getStrength())) + 1;
			if (this.random.nextFloat() < 0.03F) {
				i++;
			}

			llama.setStrength(i);
			llama.setVariant(this.random.nextBoolean() ? this.getVariant() : llama2.getVariant());
		}

		return llama;
	}

	@Nullable
	protected Llama makeNewLlama() {
		return EntityType.LLAMA.create(this.level());
	}

	private void spit(LivingEntity livingEntity) {
		LlamaSpit llamaSpit = new LlamaSpit(this.level(), this);
		double d = livingEntity.getX() - this.getX();
		double e = livingEntity.getY(0.3333333333333333) - llamaSpit.getY();
		double f = livingEntity.getZ() - this.getZ();
		double g = Math.sqrt(d * d + f * f) * 0.2F;
		llamaSpit.shoot(d, e + g, f, 1.5F, 10.0F);
		if (!this.isSilent()) {
			this.level()
				.playSound(
					null,
					this.getX(),
					this.getY(),
					this.getZ(),
					SoundEvents.LLAMA_SPIT,
					this.getSoundSource(),
					1.0F,
					1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
				);
		}

		this.level().addFreshEntity(llamaSpit);
		this.didSpit = true;
	}

	void setDidSpit(boolean bl) {
		this.didSpit = bl;
	}

	@Override
	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		int i = this.calculateFallDamage(f, g);
		if (i <= 0) {
			return false;
		} else {
			if (f >= 6.0F) {
				this.hurt(damageSource, (float)i);
				if (this.isVehicle()) {
					for (Entity entity : this.getIndirectPassengers()) {
						entity.hurt(damageSource, (float)i);
					}
				}
			}

			this.playBlockFallSound();
			return true;
		}
	}

	public void leaveCaravan() {
		if (this.caravanHead != null) {
			this.caravanHead.caravanTail = null;
		}

		this.caravanHead = null;
	}

	public void joinCaravan(Llama llama) {
		this.caravanHead = llama;
		this.caravanHead.caravanTail = this;
	}

	public boolean hasCaravanTail() {
		return this.caravanTail != null;
	}

	public boolean inCaravan() {
		return this.caravanHead != null;
	}

	@Nullable
	public Llama getCaravanHead() {
		return this.caravanHead;
	}

	@Override
	protected double followLeashSpeed() {
		return 2.0;
	}

	@Override
	protected void followMommy() {
		if (!this.inCaravan() && this.isBaby()) {
			super.followMommy();
		}
	}

	@Override
	public boolean canEatGrass() {
		return false;
	}

	@Override
	public void performRangedAttack(LivingEntity livingEntity, float f) {
		this.spit(livingEntity);
	}

	@Override
	public Vec3 getLeashOffset() {
		return new Vec3(0.0, 0.75 * (double)this.getEyeHeight(), (double)this.getBbWidth() * 0.5);
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
		return getDefaultPassengerAttachmentPoint(this, entity, entityDimensions.attachments());
	}

	static class LlamaAttackWolfGoal extends NearestAttackableTargetGoal<Wolf> {
		public LlamaAttackWolfGoal(Llama llama) {
			super(llama, Wolf.class, 16, false, true, livingEntity -> !((Wolf)livingEntity).isTame());
		}

		@Override
		protected double getFollowDistance() {
			return super.getFollowDistance() * 0.25;
		}
	}

	static class LlamaGroupData extends AgeableMob.AgeableMobGroupData {
		public final Llama.Variant variant;

		LlamaGroupData(Llama.Variant variant) {
			super(true);
			this.variant = variant;
		}
	}

	static class LlamaHurtByTargetGoal extends HurtByTargetGoal {
		public LlamaHurtByTargetGoal(Llama llama) {
			super(llama);
		}

		@Override
		public boolean canContinueToUse() {
			if (this.mob instanceof Llama llama && llama.didSpit) {
				llama.setDidSpit(false);
				return false;
			}

			return super.canContinueToUse();
		}
	}

	public static enum Variant implements StringRepresentable {
		CREAMY(0, "creamy"),
		WHITE(1, "white"),
		BROWN(2, "brown"),
		GRAY(3, "gray");

		public static final Codec<Llama.Variant> CODEC = StringRepresentable.fromEnum(Llama.Variant::values);
		private static final IntFunction<Llama.Variant> BY_ID = ByIdMap.continuous(Llama.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
		final int id;
		private final String name;

		private Variant(final int j, final String string2) {
			this.id = j;
			this.name = string2;
		}

		public int getId() {
			return this.id;
		}

		public static Llama.Variant byId(int i) {
			return (Llama.Variant)BY_ID.apply(i);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
