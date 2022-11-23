package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class TropicalFish extends AbstractSchoolingFish implements VariantHolder<TropicalFish.Pattern> {
	public static final String BUCKET_VARIANT_TAG = "BucketVariantTag";
	private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(TropicalFish.class, EntityDataSerializers.INT);
	public static final List<TropicalFish.Variant> COMMON_VARIANTS = List.of(
		new TropicalFish.Variant(TropicalFish.Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY),
		new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY),
		new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE),
		new TropicalFish.Variant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY),
		new TropicalFish.Variant(TropicalFish.Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY),
		new TropicalFish.Variant(TropicalFish.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE),
		new TropicalFish.Variant(TropicalFish.Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE),
		new TropicalFish.Variant(TropicalFish.Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW),
		new TropicalFish.Variant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED),
		new TropicalFish.Variant(TropicalFish.Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW),
		new TropicalFish.Variant(TropicalFish.Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY),
		new TropicalFish.Variant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE),
		new TropicalFish.Variant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK),
		new TropicalFish.Variant(TropicalFish.Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE),
		new TropicalFish.Variant(TropicalFish.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE),
		new TropicalFish.Variant(TropicalFish.Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED),
		new TropicalFish.Variant(TropicalFish.Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE),
		new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW),
		new TropicalFish.Variant(TropicalFish.Pattern.KOB, DyeColor.RED, DyeColor.WHITE),
		new TropicalFish.Variant(TropicalFish.Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE),
		new TropicalFish.Variant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW),
		new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW)
	);
	private boolean isSchool = true;

	public TropicalFish(EntityType<? extends TropicalFish> entityType, Level level) {
		super(entityType, level);
	}

	public static String getPredefinedName(int i) {
		return "entity.minecraft.tropical_fish.predefined." + i;
	}

	static int packVariant(TropicalFish.Pattern pattern, DyeColor dyeColor, DyeColor dyeColor2) {
		return pattern.getPackedId() & 65535 | (dyeColor.getId() & 0xFF) << 16 | (dyeColor2.getId() & 0xFF) << 24;
	}

	public static DyeColor getBaseColor(int i) {
		return DyeColor.byId(i >> 16 & 0xFF);
	}

	public static DyeColor getPatternColor(int i) {
		return DyeColor.byId(i >> 24 & 0xFF);
	}

	public static TropicalFish.Pattern getPattern(int i) {
		return TropicalFish.Pattern.byId(i & 65535);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Variant", this.getPackedVariant());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setPackedVariant(compoundTag.getInt("Variant"));
	}

	private void setPackedVariant(int i) {
		this.entityData.set(DATA_ID_TYPE_VARIANT, i);
	}

	@Override
	public boolean isMaxGroupSizeReached(int i) {
		return !this.isSchool;
	}

	private int getPackedVariant() {
		return this.entityData.get(DATA_ID_TYPE_VARIANT);
	}

	public DyeColor getBaseColor() {
		return getBaseColor(this.getPackedVariant());
	}

	public DyeColor getPatternColor() {
		return getPatternColor(this.getPackedVariant());
	}

	public TropicalFish.Pattern getVariant() {
		return getPattern(this.getPackedVariant());
	}

	public void setVariant(TropicalFish.Pattern pattern) {
		int i = this.getPackedVariant();
		DyeColor dyeColor = getBaseColor(i);
		DyeColor dyeColor2 = getPatternColor(i);
		this.setPackedVariant(packVariant(pattern, dyeColor, dyeColor2));
	}

	@Override
	public void saveToBucketTag(ItemStack itemStack) {
		super.saveToBucketTag(itemStack);
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		compoundTag.putInt("BucketVariantTag", this.getPackedVariant());
	}

	@Override
	public ItemStack getBucketItemStack() {
		return new ItemStack(Items.TROPICAL_FISH_BUCKET);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.TROPICAL_FISH_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.TROPICAL_FISH_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.TROPICAL_FISH_HURT;
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.TROPICAL_FISH_FLOP;
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
		spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
		if (mobSpawnType == MobSpawnType.BUCKET && compoundTag != null && compoundTag.contains("BucketVariantTag", 3)) {
			this.setPackedVariant(compoundTag.getInt("BucketVariantTag"));
			return spawnGroupData;
		} else {
			RandomSource randomSource = serverLevelAccessor.getRandom();
			TropicalFish.Variant variant;
			if (spawnGroupData instanceof TropicalFish.TropicalFishGroupData tropicalFishGroupData) {
				variant = tropicalFishGroupData.variant;
			} else if ((double)randomSource.nextFloat() < 0.9) {
				variant = Util.getRandom(COMMON_VARIANTS, randomSource);
				spawnGroupData = new TropicalFish.TropicalFishGroupData(this, variant);
			} else {
				this.isSchool = false;
				TropicalFish.Pattern[] patterns = TropicalFish.Pattern.values();
				DyeColor[] dyeColors = DyeColor.values();
				TropicalFish.Pattern pattern = Util.getRandom(patterns, randomSource);
				DyeColor dyeColor = Util.getRandom(dyeColors, randomSource);
				DyeColor dyeColor2 = Util.getRandom(dyeColors, randomSource);
				variant = new TropicalFish.Variant(pattern, dyeColor, dyeColor2);
			}

			this.setPackedVariant(variant.getPackedId());
			return spawnGroupData;
		}
	}

	public static boolean checkTropicalFishSpawnRules(
		EntityType<TropicalFish> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return levelAccessor.getFluidState(blockPos.below()).is(FluidTags.WATER)
			&& levelAccessor.getBlockState(blockPos.above()).is(Blocks.WATER)
			&& (
				levelAccessor.getBiome(blockPos).is(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT)
					|| WaterAnimal.checkSurfaceWaterAnimalSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource)
			);
	}

	public static enum Base {
		SMALL(0),
		LARGE(1);

		final int id;

		private Base(int j) {
			this.id = j;
		}
	}

	public static enum Pattern implements StringRepresentable {
		KOB("kob", TropicalFish.Base.SMALL, 0),
		SUNSTREAK("sunstreak", TropicalFish.Base.SMALL, 1),
		SNOOPER("snooper", TropicalFish.Base.SMALL, 2),
		DASHER("dasher", TropicalFish.Base.SMALL, 3),
		BRINELY("brinely", TropicalFish.Base.SMALL, 4),
		SPOTTY("spotty", TropicalFish.Base.SMALL, 5),
		FLOPPER("flopper", TropicalFish.Base.LARGE, 0),
		STRIPEY("stripey", TropicalFish.Base.LARGE, 1),
		GLITTER("glitter", TropicalFish.Base.LARGE, 2),
		BLOCKFISH("blockfish", TropicalFish.Base.LARGE, 3),
		BETTY("betty", TropicalFish.Base.LARGE, 4),
		CLAYFISH("clayfish", TropicalFish.Base.LARGE, 5);

		public static final Codec<TropicalFish.Pattern> CODEC = StringRepresentable.fromEnum(TropicalFish.Pattern::values);
		private static final IntFunction<TropicalFish.Pattern> BY_ID = ByIdMap.sparse(TropicalFish.Pattern::getPackedId, values(), KOB);
		private final String name;
		private final Component displayName;
		private final TropicalFish.Base base;
		private final int packedId;

		private Pattern(String string2, TropicalFish.Base base, int j) {
			this.name = string2;
			this.base = base;
			this.packedId = base.id | j << 8;
			this.displayName = Component.translatable("entity.minecraft.tropical_fish.type." + this.name);
		}

		public static TropicalFish.Pattern byId(int i) {
			return (TropicalFish.Pattern)BY_ID.apply(i);
		}

		public TropicalFish.Base base() {
			return this.base;
		}

		public int getPackedId() {
			return this.packedId;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public Component displayName() {
			return this.displayName;
		}
	}

	static class TropicalFishGroupData extends AbstractSchoolingFish.SchoolSpawnGroupData {
		final TropicalFish.Variant variant;

		TropicalFishGroupData(TropicalFish tropicalFish, TropicalFish.Variant variant) {
			super(tropicalFish);
			this.variant = variant;
		}
	}

	public static record Variant(TropicalFish.Pattern pattern, DyeColor baseColor, DyeColor patternColor) {
		public int getPackedId() {
			return TropicalFish.packVariant(this.pattern, this.baseColor, this.patternColor);
		}
	}
}
