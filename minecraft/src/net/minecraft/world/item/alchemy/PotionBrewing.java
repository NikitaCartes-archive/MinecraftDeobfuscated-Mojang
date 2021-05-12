package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;

public class PotionBrewing {
	public static final int BREWING_TIME_SECONDS = 20;
	private static final List<PotionBrewing.Mix<Potion>> POTION_MIXES = Lists.<PotionBrewing.Mix<Potion>>newArrayList();
	private static final List<PotionBrewing.Mix<Item>> CONTAINER_MIXES = Lists.<PotionBrewing.Mix<Item>>newArrayList();
	private static final List<Ingredient> ALLOWED_CONTAINERS = Lists.<Ingredient>newArrayList();
	private static final Predicate<ItemStack> ALLOWED_CONTAINER = itemStack -> {
		for (Ingredient ingredient : ALLOWED_CONTAINERS) {
			if (ingredient.test(itemStack)) {
				return true;
			}
		}

		return false;
	};

	public static boolean isIngredient(ItemStack itemStack) {
		return isContainerIngredient(itemStack) || isPotionIngredient(itemStack);
	}

	protected static boolean isContainerIngredient(ItemStack itemStack) {
		int i = 0;

		for (int j = CONTAINER_MIXES.size(); i < j; i++) {
			if (((PotionBrewing.Mix)CONTAINER_MIXES.get(i)).ingredient.test(itemStack)) {
				return true;
			}
		}

		return false;
	}

	protected static boolean isPotionIngredient(ItemStack itemStack) {
		int i = 0;

		for (int j = POTION_MIXES.size(); i < j; i++) {
			if (((PotionBrewing.Mix)POTION_MIXES.get(i)).ingredient.test(itemStack)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isBrewablePotion(Potion potion) {
		int i = 0;

		for (int j = POTION_MIXES.size(); i < j; i++) {
			if (((PotionBrewing.Mix)POTION_MIXES.get(i)).to == potion) {
				return true;
			}
		}

		return false;
	}

	public static boolean hasMix(ItemStack itemStack, ItemStack itemStack2) {
		return !ALLOWED_CONTAINER.test(itemStack) ? false : hasContainerMix(itemStack, itemStack2) || hasPotionMix(itemStack, itemStack2);
	}

	protected static boolean hasContainerMix(ItemStack itemStack, ItemStack itemStack2) {
		Item item = itemStack.getItem();
		int i = 0;

		for (int j = CONTAINER_MIXES.size(); i < j; i++) {
			PotionBrewing.Mix<Item> mix = (PotionBrewing.Mix<Item>)CONTAINER_MIXES.get(i);
			if (mix.from == item && mix.ingredient.test(itemStack2)) {
				return true;
			}
		}

		return false;
	}

	protected static boolean hasPotionMix(ItemStack itemStack, ItemStack itemStack2) {
		Potion potion = PotionUtils.getPotion(itemStack);
		int i = 0;

		for (int j = POTION_MIXES.size(); i < j; i++) {
			PotionBrewing.Mix<Potion> mix = (PotionBrewing.Mix<Potion>)POTION_MIXES.get(i);
			if (mix.from == potion && mix.ingredient.test(itemStack2)) {
				return true;
			}
		}

		return false;
	}

	public static ItemStack mix(ItemStack itemStack, ItemStack itemStack2) {
		if (!itemStack2.isEmpty()) {
			Potion potion = PotionUtils.getPotion(itemStack2);
			Item item = itemStack2.getItem();
			int i = 0;

			for (int j = CONTAINER_MIXES.size(); i < j; i++) {
				PotionBrewing.Mix<Item> mix = (PotionBrewing.Mix<Item>)CONTAINER_MIXES.get(i);
				if (mix.from == item && mix.ingredient.test(itemStack)) {
					return PotionUtils.setPotion(new ItemStack(mix.to), potion);
				}
			}

			i = 0;

			for (int jx = POTION_MIXES.size(); i < jx; i++) {
				PotionBrewing.Mix<Potion> mix = (PotionBrewing.Mix<Potion>)POTION_MIXES.get(i);
				if (mix.from == potion && mix.ingredient.test(itemStack)) {
					return PotionUtils.setPotion(new ItemStack(item), mix.to);
				}
			}
		}

		return itemStack2;
	}

	public static void bootStrap() {
		addContainer(Items.POTION);
		addContainer(Items.SPLASH_POTION);
		addContainer(Items.LINGERING_POTION);
		addContainerRecipe(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
		addContainerRecipe(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
		addMix(Potions.WATER, Items.GLISTERING_MELON_SLICE, Potions.MUNDANE);
		addMix(Potions.WATER, Items.GHAST_TEAR, Potions.MUNDANE);
		addMix(Potions.WATER, Items.RABBIT_FOOT, Potions.MUNDANE);
		addMix(Potions.WATER, Items.BLAZE_POWDER, Potions.MUNDANE);
		addMix(Potions.WATER, Items.SPIDER_EYE, Potions.MUNDANE);
		addMix(Potions.WATER, Items.SUGAR, Potions.MUNDANE);
		addMix(Potions.WATER, Items.MAGMA_CREAM, Potions.MUNDANE);
		addMix(Potions.WATER, Items.GLOWSTONE_DUST, Potions.THICK);
		addMix(Potions.WATER, Items.REDSTONE, Potions.MUNDANE);
		addMix(Potions.WATER, Items.NETHER_WART, Potions.AWKWARD);
		addMix(Potions.AWKWARD, Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
		addMix(Potions.NIGHT_VISION, Items.REDSTONE, Potions.LONG_NIGHT_VISION);
		addMix(Potions.NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
		addMix(Potions.LONG_NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
		addMix(Potions.INVISIBILITY, Items.REDSTONE, Potions.LONG_INVISIBILITY);
		addMix(Potions.AWKWARD, Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
		addMix(Potions.FIRE_RESISTANCE, Items.REDSTONE, Potions.LONG_FIRE_RESISTANCE);
		addMix(Potions.AWKWARD, Items.RABBIT_FOOT, Potions.LEAPING);
		addMix(Potions.LEAPING, Items.REDSTONE, Potions.LONG_LEAPING);
		addMix(Potions.LEAPING, Items.GLOWSTONE_DUST, Potions.STRONG_LEAPING);
		addMix(Potions.LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
		addMix(Potions.LONG_LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
		addMix(Potions.SLOWNESS, Items.REDSTONE, Potions.LONG_SLOWNESS);
		addMix(Potions.SLOWNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SLOWNESS);
		addMix(Potions.AWKWARD, Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
		addMix(Potions.TURTLE_MASTER, Items.REDSTONE, Potions.LONG_TURTLE_MASTER);
		addMix(Potions.TURTLE_MASTER, Items.GLOWSTONE_DUST, Potions.STRONG_TURTLE_MASTER);
		addMix(Potions.SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
		addMix(Potions.LONG_SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
		addMix(Potions.AWKWARD, Items.SUGAR, Potions.SWIFTNESS);
		addMix(Potions.SWIFTNESS, Items.REDSTONE, Potions.LONG_SWIFTNESS);
		addMix(Potions.SWIFTNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SWIFTNESS);
		addMix(Potions.AWKWARD, Items.PUFFERFISH, Potions.WATER_BREATHING);
		addMix(Potions.WATER_BREATHING, Items.REDSTONE, Potions.LONG_WATER_BREATHING);
		addMix(Potions.AWKWARD, Items.GLISTERING_MELON_SLICE, Potions.HEALING);
		addMix(Potions.HEALING, Items.GLOWSTONE_DUST, Potions.STRONG_HEALING);
		addMix(Potions.HEALING, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
		addMix(Potions.STRONG_HEALING, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
		addMix(Potions.HARMING, Items.GLOWSTONE_DUST, Potions.STRONG_HARMING);
		addMix(Potions.POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
		addMix(Potions.LONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
		addMix(Potions.STRONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
		addMix(Potions.AWKWARD, Items.SPIDER_EYE, Potions.POISON);
		addMix(Potions.POISON, Items.REDSTONE, Potions.LONG_POISON);
		addMix(Potions.POISON, Items.GLOWSTONE_DUST, Potions.STRONG_POISON);
		addMix(Potions.AWKWARD, Items.GHAST_TEAR, Potions.REGENERATION);
		addMix(Potions.REGENERATION, Items.REDSTONE, Potions.LONG_REGENERATION);
		addMix(Potions.REGENERATION, Items.GLOWSTONE_DUST, Potions.STRONG_REGENERATION);
		addMix(Potions.AWKWARD, Items.BLAZE_POWDER, Potions.STRENGTH);
		addMix(Potions.STRENGTH, Items.REDSTONE, Potions.LONG_STRENGTH);
		addMix(Potions.STRENGTH, Items.GLOWSTONE_DUST, Potions.STRONG_STRENGTH);
		addMix(Potions.WATER, Items.FERMENTED_SPIDER_EYE, Potions.WEAKNESS);
		addMix(Potions.WEAKNESS, Items.REDSTONE, Potions.LONG_WEAKNESS);
		addMix(Potions.AWKWARD, Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
		addMix(Potions.SLOW_FALLING, Items.REDSTONE, Potions.LONG_SLOW_FALLING);
	}

	private static void addContainerRecipe(Item item, Item item2, Item item3) {
		if (!(item instanceof PotionItem)) {
			throw new IllegalArgumentException("Expected a potion, got: " + Registry.ITEM.getKey(item));
		} else if (!(item3 instanceof PotionItem)) {
			throw new IllegalArgumentException("Expected a potion, got: " + Registry.ITEM.getKey(item3));
		} else {
			CONTAINER_MIXES.add(new PotionBrewing.Mix<>(item, Ingredient.of(item2), item3));
		}
	}

	private static void addContainer(Item item) {
		if (!(item instanceof PotionItem)) {
			throw new IllegalArgumentException("Expected a potion, got: " + Registry.ITEM.getKey(item));
		} else {
			ALLOWED_CONTAINERS.add(Ingredient.of(item));
		}
	}

	private static void addMix(Potion potion, Item item, Potion potion2) {
		POTION_MIXES.add(new PotionBrewing.Mix<>(potion, Ingredient.of(item), potion2));
	}

	static class Mix<T> {
		final T from;
		final Ingredient ingredient;
		final T to;

		public Mix(T object, Ingredient ingredient, T object2) {
			this.from = object;
			this.ingredient = ingredient;
			this.to = object2;
		}
	}
}
