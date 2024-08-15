package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class Salmon extends AbstractSchoolingFish implements VariantHolder<Salmon.Variant> {
	private static final EntityDataAccessor<String> DATA_TYPE = SynchedEntityData.defineId(Salmon.class, EntityDataSerializers.STRING);

	public Salmon(EntityType<? extends Salmon> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public int getMaxSchoolSize() {
		return 5;
	}

	@Override
	public ItemStack getBucketItemStack() {
		return new ItemStack(Items.SALMON_BUCKET);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SALMON_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SALMON_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.SALMON_HURT;
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.SALMON_FLOP;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_TYPE, Salmon.Variant.MEDIUM.type);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putString("type", this.getVariant().getSerializedName());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setVariant(Salmon.Variant.byName(compoundTag.getString("type")));
	}

	public void setVariant(Salmon.Variant variant) {
		this.entityData.set(DATA_TYPE, variant.type);
	}

	public Salmon.Variant getVariant() {
		return Salmon.Variant.byName(this.entityData.get(DATA_TYPE));
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
	) {
		SimpleWeightedRandomList.Builder<Salmon.Variant> builder = SimpleWeightedRandomList.builder();
		builder.add(Salmon.Variant.SMALL, 30);
		builder.add(Salmon.Variant.MEDIUM, 50);
		builder.add(Salmon.Variant.LARGE, 15);
		builder.build().getRandomValue(this.random).ifPresent(this::setVariant);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
	}

	public static enum Variant implements StringRepresentable {
		SMALL("small"),
		MEDIUM("medium"),
		LARGE("large");

		public static final StringRepresentable.EnumCodec<Salmon.Variant> CODEC = StringRepresentable.fromEnum(Salmon.Variant::values);
		final String type;

		private Variant(final String string2) {
			this.type = string2;
		}

		@Override
		public String getSerializedName() {
			return this.type;
		}

		static Salmon.Variant byName(String string) {
			return (Salmon.Variant)CODEC.byName(string, SMALL);
		}
	}
}
