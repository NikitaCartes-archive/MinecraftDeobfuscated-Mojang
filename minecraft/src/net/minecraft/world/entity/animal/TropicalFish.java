package net.minecraft.world.entity.animal;

import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class TropicalFish extends AbstractSchoolingFish {
	private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(TropicalFish.class, EntityDataSerializers.INT);
	private static final ResourceLocation[] BASE_TEXTURE_LOCATIONS = new ResourceLocation[]{
		new ResourceLocation("textures/entity/fish/tropical_a.png"), new ResourceLocation("textures/entity/fish/tropical_b.png")
	};
	private static final ResourceLocation[] PATTERN_A_TEXTURE_LOCATIONS = new ResourceLocation[]{
		new ResourceLocation("textures/entity/fish/tropical_a_pattern_1.png"),
		new ResourceLocation("textures/entity/fish/tropical_a_pattern_2.png"),
		new ResourceLocation("textures/entity/fish/tropical_a_pattern_3.png"),
		new ResourceLocation("textures/entity/fish/tropical_a_pattern_4.png"),
		new ResourceLocation("textures/entity/fish/tropical_a_pattern_5.png"),
		new ResourceLocation("textures/entity/fish/tropical_a_pattern_6.png")
	};
	private static final ResourceLocation[] PATTERN_B_TEXTURE_LOCATIONS = new ResourceLocation[]{
		new ResourceLocation("textures/entity/fish/tropical_b_pattern_1.png"),
		new ResourceLocation("textures/entity/fish/tropical_b_pattern_2.png"),
		new ResourceLocation("textures/entity/fish/tropical_b_pattern_3.png"),
		new ResourceLocation("textures/entity/fish/tropical_b_pattern_4.png"),
		new ResourceLocation("textures/entity/fish/tropical_b_pattern_5.png"),
		new ResourceLocation("textures/entity/fish/tropical_b_pattern_6.png")
	};
	public static final int[] COMMON_VARIANTS = new int[]{
		calculateVariant(TropicalFish.Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY),
		calculateVariant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY),
		calculateVariant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE),
		calculateVariant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY),
		calculateVariant(TropicalFish.Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY),
		calculateVariant(TropicalFish.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE),
		calculateVariant(TropicalFish.Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE),
		calculateVariant(TropicalFish.Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW),
		calculateVariant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED),
		calculateVariant(TropicalFish.Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW),
		calculateVariant(TropicalFish.Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY),
		calculateVariant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE),
		calculateVariant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK),
		calculateVariant(TropicalFish.Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE),
		calculateVariant(TropicalFish.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE),
		calculateVariant(TropicalFish.Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED),
		calculateVariant(TropicalFish.Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE),
		calculateVariant(TropicalFish.Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW),
		calculateVariant(TropicalFish.Pattern.KOB, DyeColor.RED, DyeColor.WHITE),
		calculateVariant(TropicalFish.Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE),
		calculateVariant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW),
		calculateVariant(TropicalFish.Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW)
	};
	private boolean isSchool = true;

	private static int calculateVariant(TropicalFish.Pattern pattern, DyeColor dyeColor, DyeColor dyeColor2) {
		return pattern.getBase() & 0xFF | (pattern.getIndex() & 0xFF) << 8 | (dyeColor.getId() & 0xFF) << 16 | (dyeColor2.getId() & 0xFF) << 24;
	}

	public TropicalFish(EntityType<? extends TropicalFish> entityType, Level level) {
		super(entityType, level);
	}

	@Environment(EnvType.CLIENT)
	public static String getPredefinedName(int i) {
		return "entity.minecraft.tropical_fish.predefined." + i;
	}

	@Environment(EnvType.CLIENT)
	public static DyeColor getBaseColor(int i) {
		return DyeColor.byId(getBaseColorIdx(i));
	}

	@Environment(EnvType.CLIENT)
	public static DyeColor getPatternColor(int i) {
		return DyeColor.byId(getPatternColorIdx(i));
	}

	@Environment(EnvType.CLIENT)
	public static String getFishTypeName(int i) {
		int j = getBaseVariant(i);
		int k = getPatternVariant(i);
		return "entity.minecraft.tropical_fish.type." + TropicalFish.Pattern.getPatternName(j, k);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
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

	public void setVariant(int i) {
		this.entityData.set(DATA_ID_TYPE_VARIANT, i);
	}

	@Override
	public boolean isMaxGroupSizeReached(int i) {
		return !this.isSchool;
	}

	public int getVariant() {
		return this.entityData.get(DATA_ID_TYPE_VARIANT);
	}

	@Override
	protected void saveToBucketTag(ItemStack itemStack) {
		super.saveToBucketTag(itemStack);
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		compoundTag.putInt("BucketVariantTag", this.getVariant());
	}

	@Override
	protected ItemStack getBucketItemStack() {
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

	@Environment(EnvType.CLIENT)
	private static int getBaseColorIdx(int i) {
		return (i & 0xFF0000) >> 16;
	}

	@Environment(EnvType.CLIENT)
	public float[] getBaseColor() {
		return DyeColor.byId(getBaseColorIdx(this.getVariant())).getTextureDiffuseColors();
	}

	@Environment(EnvType.CLIENT)
	private static int getPatternColorIdx(int i) {
		return (i & 0xFF000000) >> 24;
	}

	@Environment(EnvType.CLIENT)
	public float[] getPatternColor() {
		return DyeColor.byId(getPatternColorIdx(this.getVariant())).getTextureDiffuseColors();
	}

	@Environment(EnvType.CLIENT)
	public static int getBaseVariant(int i) {
		return Math.min(i & 0xFF, 1);
	}

	@Environment(EnvType.CLIENT)
	public int getBaseVariant() {
		return getBaseVariant(this.getVariant());
	}

	@Environment(EnvType.CLIENT)
	private static int getPatternVariant(int i) {
		return Math.min((i & 0xFF00) >> 8, 5);
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation getPatternTextureLocation() {
		return getBaseVariant(this.getVariant()) == 0
			? PATTERN_A_TEXTURE_LOCATIONS[getPatternVariant(this.getVariant())]
			: PATTERN_B_TEXTURE_LOCATIONS[getPatternVariant(this.getVariant())];
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation getBaseTextureLocation() {
		return BASE_TEXTURE_LOCATIONS[getBaseVariant(this.getVariant())];
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
		if (compoundTag != null && compoundTag.contains("BucketVariantTag", 3)) {
			this.setVariant(compoundTag.getInt("BucketVariantTag"));
			return spawnGroupData;
		} else {
			int i;
			int j;
			int k;
			int l;
			if (spawnGroupData instanceof TropicalFish.TropicalFishGroupData) {
				TropicalFish.TropicalFishGroupData tropicalFishGroupData = (TropicalFish.TropicalFishGroupData)spawnGroupData;
				i = tropicalFishGroupData.base;
				j = tropicalFishGroupData.pattern;
				k = tropicalFishGroupData.baseColor;
				l = tropicalFishGroupData.patternColor;
			} else if ((double)this.random.nextFloat() < 0.9) {
				int m = Util.getRandom(COMMON_VARIANTS, this.random);
				i = m & 0xFF;
				j = (m & 0xFF00) >> 8;
				k = (m & 0xFF0000) >> 16;
				l = (m & 0xFF000000) >> 24;
				spawnGroupData = new TropicalFish.TropicalFishGroupData(this, i, j, k, l);
			} else {
				this.isSchool = false;
				i = this.random.nextInt(2);
				j = this.random.nextInt(6);
				k = this.random.nextInt(15);
				l = this.random.nextInt(15);
			}

			this.setVariant(i | j << 8 | k << 16 | l << 24);
			return spawnGroupData;
		}
	}

	static enum Pattern {
		KOB(0, 0),
		SUNSTREAK(0, 1),
		SNOOPER(0, 2),
		DASHER(0, 3),
		BRINELY(0, 4),
		SPOTTY(0, 5),
		FLOPPER(1, 0),
		STRIPEY(1, 1),
		GLITTER(1, 2),
		BLOCKFISH(1, 3),
		BETTY(1, 4),
		CLAYFISH(1, 5);

		private final int base;
		private final int index;
		private static final TropicalFish.Pattern[] VALUES = values();

		private Pattern(int j, int k) {
			this.base = j;
			this.index = k;
		}

		public int getBase() {
			return this.base;
		}

		public int getIndex() {
			return this.index;
		}

		@Environment(EnvType.CLIENT)
		public static String getPatternName(int i, int j) {
			return VALUES[j + 6 * i].getName();
		}

		@Environment(EnvType.CLIENT)
		public String getName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

	static class TropicalFishGroupData extends AbstractSchoolingFish.SchoolSpawnGroupData {
		private final int base;
		private final int pattern;
		private final int baseColor;
		private final int patternColor;

		private TropicalFishGroupData(TropicalFish tropicalFish, int i, int j, int k, int l) {
			super(tropicalFish);
			this.base = i;
			this.pattern = j;
			this.baseColor = k;
			this.patternColor = l;
		}
	}
}
