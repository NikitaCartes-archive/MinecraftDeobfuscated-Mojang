package net.minecraft.world.food;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FoodProperties {
	private final int nutrition;
	private final float saturationModifier;
	private final boolean isMeat;
	private final boolean canAlwaysEat;
	private final boolean fastFood;
	private final List<Pair<MobEffectInstance, Float>> effects;
	private final BiConsumer<ItemStack, LivingEntity> onEaten;

	FoodProperties(int i, float f, boolean bl, boolean bl2, boolean bl3, List<Pair<MobEffectInstance, Float>> list, BiConsumer<ItemStack, LivingEntity> biConsumer) {
		this.nutrition = i;
		this.saturationModifier = f;
		this.isMeat = bl;
		this.canAlwaysEat = bl2;
		this.fastFood = bl3;
		this.effects = list;
		this.onEaten = biConsumer;
	}

	public BiConsumer<ItemStack, LivingEntity> getOnEaten() {
		return this.onEaten;
	}

	public int getNutrition() {
		return this.nutrition;
	}

	public float getSaturationModifier() {
		return this.saturationModifier;
	}

	public boolean isMeat() {
		return this.isMeat;
	}

	public boolean canAlwaysEat() {
		return this.canAlwaysEat;
	}

	public boolean isFastFood() {
		return this.fastFood;
	}

	public List<Pair<MobEffectInstance, Float>> getEffects() {
		return this.effects;
	}

	public static class Builder {
		private int nutrition;
		private float saturationModifier;
		private boolean isMeat;
		private boolean canAlwaysEat;
		private boolean fastFood;
		private final List<Pair<MobEffectInstance, Float>> effects = Lists.<Pair<MobEffectInstance, Float>>newArrayList();
		private BiConsumer<ItemStack, LivingEntity> onEaten = (itemStack, livingEntity) -> {
		};

		public FoodProperties.Builder nutrition(int i) {
			this.nutrition = i;
			return this;
		}

		public FoodProperties.Builder saturationMod(float f) {
			this.saturationModifier = f;
			return this;
		}

		public FoodProperties.Builder meat() {
			this.isMeat = true;
			return this;
		}

		public FoodProperties.Builder alwaysEat() {
			this.canAlwaysEat = true;
			return this;
		}

		public FoodProperties.Builder fast() {
			this.fastFood = true;
			return this;
		}

		public FoodProperties.Builder effect(MobEffectInstance mobEffectInstance, float f) {
			this.effects.add(Pair.of(mobEffectInstance, f));
			return this;
		}

		public FoodProperties.Builder withMagic(BiConsumer<ItemStack, LivingEntity> biConsumer) {
			this.onEaten = this.onEaten.andThen(biConsumer);
			return this.alwaysEat();
		}

		public FoodProperties build() {
			return new FoodProperties(this.nutrition, this.saturationModifier, this.isMeat, this.canAlwaysEat, this.fastFood, this.effects, this.onEaten);
		}
	}
}
