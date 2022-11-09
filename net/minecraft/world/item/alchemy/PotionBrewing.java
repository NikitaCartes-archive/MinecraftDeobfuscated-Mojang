/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class PotionBrewing {
    public static final int BREWING_TIME_SECONDS = 20;
    private static final List<Mix<Potion>> POTION_MIXES = Lists.newArrayList();
    private static final List<Mix<Item>> CONTAINER_MIXES = Lists.newArrayList();
    private static final List<Ingredient> ALLOWED_CONTAINERS = Lists.newArrayList();
    private static final Predicate<ItemStack> ALLOWED_CONTAINER = itemStack -> {
        for (Ingredient ingredient : ALLOWED_CONTAINERS) {
            if (!ingredient.test((ItemStack)itemStack)) continue;
            return true;
        }
        return false;
    };

    public static boolean isIngredient(ItemStack itemStack) {
        return PotionBrewing.isContainerIngredient(itemStack) || PotionBrewing.isPotionIngredient(itemStack);
    }

    protected static boolean isContainerIngredient(ItemStack itemStack) {
        int j = CONTAINER_MIXES.size();
        for (int i = 0; i < j; ++i) {
            if (!PotionBrewing.CONTAINER_MIXES.get((int)i).ingredient.test(itemStack)) continue;
            return true;
        }
        return false;
    }

    protected static boolean isPotionIngredient(ItemStack itemStack) {
        int j = POTION_MIXES.size();
        for (int i = 0; i < j; ++i) {
            if (!PotionBrewing.POTION_MIXES.get((int)i).ingredient.test(itemStack)) continue;
            return true;
        }
        return false;
    }

    public static boolean isBrewablePotion(Potion potion) {
        int j = POTION_MIXES.size();
        for (int i = 0; i < j; ++i) {
            if (PotionBrewing.POTION_MIXES.get((int)i).to != potion) continue;
            return true;
        }
        return false;
    }

    public static boolean hasMix(ItemStack itemStack, ItemStack itemStack2) {
        if (!ALLOWED_CONTAINER.test(itemStack)) {
            return false;
        }
        return PotionBrewing.hasContainerMix(itemStack, itemStack2) || PotionBrewing.hasPotionMix(itemStack, itemStack2);
    }

    protected static boolean hasContainerMix(ItemStack itemStack, ItemStack itemStack2) {
        Item item = itemStack.getItem();
        int j = CONTAINER_MIXES.size();
        for (int i = 0; i < j; ++i) {
            Mix<Item> mix = CONTAINER_MIXES.get(i);
            if (mix.from != item || !mix.ingredient.test(itemStack2)) continue;
            return true;
        }
        return false;
    }

    protected static boolean hasPotionMix(ItemStack itemStack, ItemStack itemStack2) {
        Potion potion = PotionUtils.getPotion(itemStack);
        int j = POTION_MIXES.size();
        for (int i = 0; i < j; ++i) {
            Mix<Potion> mix = POTION_MIXES.get(i);
            if (mix.from != potion || !mix.ingredient.test(itemStack2)) continue;
            return true;
        }
        return false;
    }

    public static ItemStack mix(ItemStack itemStack, ItemStack itemStack2) {
        if (!itemStack2.isEmpty()) {
            Mix<Object> mix;
            int i;
            Potion potion = PotionUtils.getPotion(itemStack2);
            Item item = itemStack2.getItem();
            int j = CONTAINER_MIXES.size();
            for (i = 0; i < j; ++i) {
                mix = CONTAINER_MIXES.get(i);
                if (mix.from != item || !mix.ingredient.test(itemStack)) continue;
                return PotionUtils.setPotion(new ItemStack((ItemLike)mix.to), potion);
            }
            j = POTION_MIXES.size();
            for (i = 0; i < j; ++i) {
                mix = POTION_MIXES.get(i);
                if (mix.from != potion || !mix.ingredient.test(itemStack)) continue;
                return PotionUtils.setPotion(new ItemStack(item), (Potion)mix.to);
            }
        }
        return itemStack2;
    }

    public static void bootStrap() {
        PotionBrewing.addContainer(Items.POTION);
        PotionBrewing.addContainer(Items.SPLASH_POTION);
        PotionBrewing.addContainer(Items.LINGERING_POTION);
        PotionBrewing.addContainerRecipe(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
        PotionBrewing.addContainerRecipe(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
        PotionBrewing.addMix(Potions.WATER, Items.GLISTERING_MELON_SLICE, Potions.MUNDANE);
        PotionBrewing.addMix(Potions.WATER, Items.GHAST_TEAR, Potions.MUNDANE);
        PotionBrewing.addMix(Potions.WATER, Items.RABBIT_FOOT, Potions.MUNDANE);
        PotionBrewing.addMix(Potions.WATER, Items.BLAZE_POWDER, Potions.MUNDANE);
        PotionBrewing.addMix(Potions.WATER, Items.SPIDER_EYE, Potions.MUNDANE);
        PotionBrewing.addMix(Potions.WATER, Items.SUGAR, Potions.MUNDANE);
        PotionBrewing.addMix(Potions.WATER, Items.MAGMA_CREAM, Potions.MUNDANE);
        PotionBrewing.addMix(Potions.WATER, Items.GLOWSTONE_DUST, Potions.THICK);
        PotionBrewing.addMix(Potions.WATER, Items.REDSTONE, Potions.MUNDANE);
        PotionBrewing.addMix(Potions.WATER, Items.NETHER_WART, Potions.AWKWARD);
        PotionBrewing.addMix(Potions.AWKWARD, Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
        PotionBrewing.addMix(Potions.NIGHT_VISION, Items.REDSTONE, Potions.LONG_NIGHT_VISION);
        PotionBrewing.addMix(Potions.NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
        PotionBrewing.addMix(Potions.LONG_NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
        PotionBrewing.addMix(Potions.INVISIBILITY, Items.REDSTONE, Potions.LONG_INVISIBILITY);
        PotionBrewing.addMix(Potions.AWKWARD, Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
        PotionBrewing.addMix(Potions.FIRE_RESISTANCE, Items.REDSTONE, Potions.LONG_FIRE_RESISTANCE);
        PotionBrewing.addMix(Potions.AWKWARD, Items.RABBIT_FOOT, Potions.LEAPING);
        PotionBrewing.addMix(Potions.LEAPING, Items.REDSTONE, Potions.LONG_LEAPING);
        PotionBrewing.addMix(Potions.LEAPING, Items.GLOWSTONE_DUST, Potions.STRONG_LEAPING);
        PotionBrewing.addMix(Potions.LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        PotionBrewing.addMix(Potions.LONG_LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        PotionBrewing.addMix(Potions.SLOWNESS, Items.REDSTONE, Potions.LONG_SLOWNESS);
        PotionBrewing.addMix(Potions.SLOWNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SLOWNESS);
        PotionBrewing.addMix(Potions.AWKWARD, Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
        PotionBrewing.addMix(Potions.TURTLE_MASTER, Items.REDSTONE, Potions.LONG_TURTLE_MASTER);
        PotionBrewing.addMix(Potions.TURTLE_MASTER, Items.GLOWSTONE_DUST, Potions.STRONG_TURTLE_MASTER);
        PotionBrewing.addMix(Potions.SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        PotionBrewing.addMix(Potions.LONG_SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        PotionBrewing.addMix(Potions.AWKWARD, Items.SUGAR, Potions.SWIFTNESS);
        PotionBrewing.addMix(Potions.SWIFTNESS, Items.REDSTONE, Potions.LONG_SWIFTNESS);
        PotionBrewing.addMix(Potions.SWIFTNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SWIFTNESS);
        PotionBrewing.addMix(Potions.AWKWARD, Items.PUFFERFISH, Potions.WATER_BREATHING);
        PotionBrewing.addMix(Potions.WATER_BREATHING, Items.REDSTONE, Potions.LONG_WATER_BREATHING);
        PotionBrewing.addMix(Potions.AWKWARD, Items.GLISTERING_MELON_SLICE, Potions.HEALING);
        PotionBrewing.addMix(Potions.HEALING, Items.GLOWSTONE_DUST, Potions.STRONG_HEALING);
        PotionBrewing.addMix(Potions.HEALING, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        PotionBrewing.addMix(Potions.STRONG_HEALING, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        PotionBrewing.addMix(Potions.HARMING, Items.GLOWSTONE_DUST, Potions.STRONG_HARMING);
        PotionBrewing.addMix(Potions.POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        PotionBrewing.addMix(Potions.LONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        PotionBrewing.addMix(Potions.STRONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        PotionBrewing.addMix(Potions.AWKWARD, Items.SPIDER_EYE, Potions.POISON);
        PotionBrewing.addMix(Potions.POISON, Items.REDSTONE, Potions.LONG_POISON);
        PotionBrewing.addMix(Potions.POISON, Items.GLOWSTONE_DUST, Potions.STRONG_POISON);
        PotionBrewing.addMix(Potions.AWKWARD, Items.GHAST_TEAR, Potions.REGENERATION);
        PotionBrewing.addMix(Potions.REGENERATION, Items.REDSTONE, Potions.LONG_REGENERATION);
        PotionBrewing.addMix(Potions.REGENERATION, Items.GLOWSTONE_DUST, Potions.STRONG_REGENERATION);
        PotionBrewing.addMix(Potions.AWKWARD, Items.BLAZE_POWDER, Potions.STRENGTH);
        PotionBrewing.addMix(Potions.STRENGTH, Items.REDSTONE, Potions.LONG_STRENGTH);
        PotionBrewing.addMix(Potions.STRENGTH, Items.GLOWSTONE_DUST, Potions.STRONG_STRENGTH);
        PotionBrewing.addMix(Potions.WATER, Items.FERMENTED_SPIDER_EYE, Potions.WEAKNESS);
        PotionBrewing.addMix(Potions.WEAKNESS, Items.REDSTONE, Potions.LONG_WEAKNESS);
        PotionBrewing.addMix(Potions.AWKWARD, Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
        PotionBrewing.addMix(Potions.SLOW_FALLING, Items.REDSTONE, Potions.LONG_SLOW_FALLING);
    }

    private static void addContainerRecipe(Item item, Item item2, Item item3) {
        if (!(item instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + BuiltInRegistries.ITEM.getKey(item));
        }
        if (!(item3 instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + BuiltInRegistries.ITEM.getKey(item3));
        }
        CONTAINER_MIXES.add(new Mix<Item>(item, Ingredient.of(item2), item3));
    }

    private static void addContainer(Item item) {
        if (!(item instanceof PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + BuiltInRegistries.ITEM.getKey(item));
        }
        ALLOWED_CONTAINERS.add(Ingredient.of(item));
    }

    private static void addMix(Potion potion, Item item, Potion potion2) {
        POTION_MIXES.add(new Mix<Potion>(potion, Ingredient.of(item), potion2));
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

