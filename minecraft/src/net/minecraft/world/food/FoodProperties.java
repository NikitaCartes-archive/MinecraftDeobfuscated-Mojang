package net.minecraft.world.food;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;

public class FoodProperties {
	private final int nutrition;
	private final float saturationModifier;
	private final boolean isMeat;
	private final boolean canAlwaysEat;
	private final boolean fastFood;
	private final List<Pair<MobEffectInstance, Float>> effects;

	private FoodProperties(int i, float f, boolean bl, boolean bl2, boolean bl3, List<Pair<MobEffectInstance, Float>> list) {
		this.nutrition = i;
		this.saturationModifier = f;
		this.isMeat = bl;
		this.canAlwaysEat = bl2;
		this.fastFood = bl3;
		this.effects = list;
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

		public FoodProperties build() {
			return new FoodProperties(this.nutrition, this.saturationModifier, this.isMeat, this.canAlwaysEat, this.fastFood, this.effects);
		}
	}
}
