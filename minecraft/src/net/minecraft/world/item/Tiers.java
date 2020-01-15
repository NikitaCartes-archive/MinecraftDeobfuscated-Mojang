package net.minecraft.world.item;

import java.util.function.Supplier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public enum Tiers implements Tier {
	WOOD(0, 59, 2.0F, 0.0F, 15, () -> Ingredient.of(ItemTags.PLANKS)),
	STONE(1, 131, 4.0F, 0.0F, 5, () -> Ingredient.of(Blocks.COBBLESTONE)),
	IRON(2, 250, 6.0F, 1.0F, 14, () -> Ingredient.of(Items.IRON_INGOT)),
	DIAMOND(3, 1561, 8.0F, 2.0F, 10, () -> Ingredient.of(Items.DIAMOND)),
	GOLD(0, 32, 12.0F, 0.0F, 22, () -> Ingredient.of(Items.GOLD_INGOT));

	private final int level;
	private final int uses;
	private final float speed;
	private final float damage;
	private final int enchantmentValue;
	private final LazyLoadedValue<Ingredient> repairIngredient;

	private Tiers(int j, int k, float f, float g, int l, Supplier<Ingredient> supplier) {
		this.level = j;
		this.uses = k;
		this.speed = f;
		this.damage = g;
		this.enchantmentValue = l;
		this.repairIngredient = new LazyLoadedValue<>(supplier);
	}

	@Override
	public int getUses() {
		return this.uses;
	}

	@Override
	public float getSpeed() {
		return this.speed;
	}

	@Override
	public float getAttackDamageBonus() {
		return this.damage;
	}

	@Override
	public int getLevel() {
		return this.level;
	}

	@Override
	public int getEnchantmentValue() {
		return this.enchantmentValue;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairIngredient.get();
	}
}
