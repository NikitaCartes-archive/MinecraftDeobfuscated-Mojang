package net.minecraft.world.item.alchemy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;

public class PotionBrewing {
	public static final int BREWING_TIME_SECONDS = 20;
	public static final PotionBrewing EMPTY = new PotionBrewing(List.of(), List.of(), List.of());
	private final List<Ingredient> containers;
	private final List<PotionBrewing.Mix<Potion>> potionMixes;
	private final List<PotionBrewing.Mix<Item>> containerMixes;

	PotionBrewing(List<Ingredient> list, List<PotionBrewing.Mix<Potion>> list2, List<PotionBrewing.Mix<Item>> list3) {
		this.containers = list;
		this.potionMixes = list2;
		this.containerMixes = list3;
	}

	public boolean isIngredient(ItemStack itemStack) {
		return this.isContainerIngredient(itemStack) || this.isPotionIngredient(itemStack);
	}

	private boolean isContainer(ItemStack itemStack) {
		for (Ingredient ingredient : this.containers) {
			if (ingredient.test(itemStack)) {
				return true;
			}
		}

		return false;
	}

	public boolean isContainerIngredient(ItemStack itemStack) {
		for (PotionBrewing.Mix<Item> mix : this.containerMixes) {
			if (mix.ingredient.test(itemStack)) {
				return true;
			}
		}

		return false;
	}

	public boolean isPotionIngredient(ItemStack itemStack) {
		for (PotionBrewing.Mix<Potion> mix : this.potionMixes) {
			if (mix.ingredient.test(itemStack)) {
				return true;
			}
		}

		return false;
	}

	public boolean isBrewablePotion(Holder<Potion> holder) {
		for (PotionBrewing.Mix<Potion> mix : this.potionMixes) {
			if (mix.to.is(holder)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasMix(ItemStack itemStack, ItemStack itemStack2) {
		return !this.isContainer(itemStack) ? false : this.hasContainerMix(itemStack, itemStack2) || this.hasPotionMix(itemStack, itemStack2);
	}

	public boolean hasContainerMix(ItemStack itemStack, ItemStack itemStack2) {
		for (PotionBrewing.Mix<Item> mix : this.containerMixes) {
			if (itemStack.is(mix.from) && mix.ingredient.test(itemStack2)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasPotionMix(ItemStack itemStack, ItemStack itemStack2) {
		Optional<Holder<Potion>> optional = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
		if (optional.isEmpty()) {
			return false;
		} else {
			for (PotionBrewing.Mix<Potion> mix : this.potionMixes) {
				if (mix.from.is((Holder<Potion>)optional.get()) && mix.ingredient.test(itemStack2)) {
					return true;
				}
			}

			return false;
		}
	}

	public ItemStack mix(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack2.isEmpty()) {
			return itemStack2;
		} else {
			Optional<Holder<Potion>> optional = itemStack2.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
			if (optional.isEmpty()) {
				return itemStack2;
			} else {
				for (PotionBrewing.Mix<Item> mix : this.containerMixes) {
					if (itemStack2.is(mix.from) && mix.ingredient.test(itemStack)) {
						return PotionContents.createItemStack(mix.to.value(), (Holder<Potion>)optional.get());
					}
				}

				for (PotionBrewing.Mix<Potion> mixx : this.potionMixes) {
					if (mixx.from.is((Holder<Potion>)optional.get()) && mixx.ingredient.test(itemStack)) {
						return PotionContents.createItemStack(itemStack2.getItem(), mixx.to);
					}
				}

				return itemStack2;
			}
		}
	}

	public static PotionBrewing bootstrap(FeatureFlagSet featureFlagSet) {
		PotionBrewing.Builder builder = new PotionBrewing.Builder(featureFlagSet);
		addVanillaMixes(builder);
		return builder.build();
	}

	public static void addVanillaMixes(PotionBrewing.Builder builder) {
		builder.addContainer(Items.POTION);
		builder.addContainer(Items.SPLASH_POTION);
		builder.addContainer(Items.LINGERING_POTION);
		builder.addContainerRecipe(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
		builder.addContainerRecipe(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
		builder.addMix(Potions.WATER, Items.GLOWSTONE_DUST, Potions.THICK);
		builder.addMix(Potions.WATER, Items.REDSTONE, Potions.MUNDANE);
		builder.addMix(Potions.WATER, Items.NETHER_WART, Potions.AWKWARD);
		builder.addStartMix(Items.BREEZE_ROD, Potions.WIND_CHARGED);
		builder.addStartMix(Items.SLIME_BLOCK, Potions.OOZING);
		builder.addStartMix(Items.STONE, Potions.INFESTED);
		builder.addStartMix(Items.COBWEB, Potions.WEAVING);
		builder.addMix(Potions.AWKWARD, Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
		builder.addMix(Potions.NIGHT_VISION, Items.REDSTONE, Potions.LONG_NIGHT_VISION);
		builder.addMix(Potions.NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
		builder.addMix(Potions.LONG_NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
		builder.addMix(Potions.INVISIBILITY, Items.REDSTONE, Potions.LONG_INVISIBILITY);
		builder.addStartMix(Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
		builder.addMix(Potions.FIRE_RESISTANCE, Items.REDSTONE, Potions.LONG_FIRE_RESISTANCE);
		builder.addStartMix(Items.RABBIT_FOOT, Potions.LEAPING);
		builder.addMix(Potions.LEAPING, Items.REDSTONE, Potions.LONG_LEAPING);
		builder.addMix(Potions.LEAPING, Items.GLOWSTONE_DUST, Potions.STRONG_LEAPING);
		builder.addMix(Potions.LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
		builder.addMix(Potions.LONG_LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
		builder.addMix(Potions.SLOWNESS, Items.REDSTONE, Potions.LONG_SLOWNESS);
		builder.addMix(Potions.SLOWNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SLOWNESS);
		builder.addMix(Potions.AWKWARD, Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
		builder.addMix(Potions.TURTLE_MASTER, Items.REDSTONE, Potions.LONG_TURTLE_MASTER);
		builder.addMix(Potions.TURTLE_MASTER, Items.GLOWSTONE_DUST, Potions.STRONG_TURTLE_MASTER);
		builder.addMix(Potions.SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
		builder.addMix(Potions.LONG_SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
		builder.addStartMix(Items.SUGAR, Potions.SWIFTNESS);
		builder.addMix(Potions.SWIFTNESS, Items.REDSTONE, Potions.LONG_SWIFTNESS);
		builder.addMix(Potions.SWIFTNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SWIFTNESS);
		builder.addMix(Potions.AWKWARD, Items.PUFFERFISH, Potions.WATER_BREATHING);
		builder.addMix(Potions.WATER_BREATHING, Items.REDSTONE, Potions.LONG_WATER_BREATHING);
		builder.addStartMix(Items.GLISTERING_MELON_SLICE, Potions.HEALING);
		builder.addMix(Potions.HEALING, Items.GLOWSTONE_DUST, Potions.STRONG_HEALING);
		builder.addMix(Potions.HEALING, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
		builder.addMix(Potions.STRONG_HEALING, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
		builder.addMix(Potions.HARMING, Items.GLOWSTONE_DUST, Potions.STRONG_HARMING);
		builder.addMix(Potions.POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
		builder.addMix(Potions.LONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
		builder.addMix(Potions.STRONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
		builder.addStartMix(Items.SPIDER_EYE, Potions.POISON);
		builder.addMix(Potions.POISON, Items.REDSTONE, Potions.LONG_POISON);
		builder.addMix(Potions.POISON, Items.GLOWSTONE_DUST, Potions.STRONG_POISON);
		builder.addStartMix(Items.GHAST_TEAR, Potions.REGENERATION);
		builder.addMix(Potions.REGENERATION, Items.REDSTONE, Potions.LONG_REGENERATION);
		builder.addMix(Potions.REGENERATION, Items.GLOWSTONE_DUST, Potions.STRONG_REGENERATION);
		builder.addStartMix(Items.BLAZE_POWDER, Potions.STRENGTH);
		builder.addMix(Potions.STRENGTH, Items.REDSTONE, Potions.LONG_STRENGTH);
		builder.addMix(Potions.STRENGTH, Items.GLOWSTONE_DUST, Potions.STRONG_STRENGTH);
		builder.addMix(Potions.WATER, Items.FERMENTED_SPIDER_EYE, Potions.WEAKNESS);
		builder.addMix(Potions.WEAKNESS, Items.REDSTONE, Potions.LONG_WEAKNESS);
		builder.addMix(Potions.AWKWARD, Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
		builder.addMix(Potions.SLOW_FALLING, Items.REDSTONE, Potions.LONG_SLOW_FALLING);
	}

	public static class Builder {
		private final List<Ingredient> containers = new ArrayList();
		private final List<PotionBrewing.Mix<Potion>> potionMixes = new ArrayList();
		private final List<PotionBrewing.Mix<Item>> containerMixes = new ArrayList();
		private final FeatureFlagSet enabledFeatures;

		public Builder(FeatureFlagSet featureFlagSet) {
			this.enabledFeatures = featureFlagSet;
		}

		private static void expectPotion(Item item) {
			if (!(item instanceof PotionItem)) {
				throw new IllegalArgumentException("Expected a potion, got: " + BuiltInRegistries.ITEM.getKey(item));
			}
		}

		public void addContainerRecipe(Item item, Item item2, Item item3) {
			if (item.isEnabled(this.enabledFeatures) && item2.isEnabled(this.enabledFeatures) && item3.isEnabled(this.enabledFeatures)) {
				expectPotion(item);
				expectPotion(item3);
				this.containerMixes.add(new PotionBrewing.Mix<>(item.builtInRegistryHolder(), Ingredient.of(item2), item3.builtInRegistryHolder()));
			}
		}

		public void addContainer(Item item) {
			if (item.isEnabled(this.enabledFeatures)) {
				expectPotion(item);
				this.containers.add(Ingredient.of(item));
			}
		}

		public void addMix(Holder<Potion> holder, Item item, Holder<Potion> holder2) {
			if (holder.value().isEnabled(this.enabledFeatures) && item.isEnabled(this.enabledFeatures) && holder2.value().isEnabled(this.enabledFeatures)) {
				this.potionMixes.add(new PotionBrewing.Mix<>(holder, Ingredient.of(item), holder2));
			}
		}

		public void addStartMix(Item item, Holder<Potion> holder) {
			if (holder.value().isEnabled(this.enabledFeatures)) {
				this.addMix(Potions.WATER, item, Potions.MUNDANE);
				this.addMix(Potions.AWKWARD, item, holder);
			}
		}

		public PotionBrewing build() {
			return new PotionBrewing(List.copyOf(this.containers), List.copyOf(this.potionMixes), List.copyOf(this.containerMixes));
		}
	}

	static record Mix<T>(Holder<T> from, Ingredient ingredient, Holder<T> to) {
	}
}
