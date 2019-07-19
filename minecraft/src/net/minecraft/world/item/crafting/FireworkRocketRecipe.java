package net.minecraft.world.item.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FireworkRocketRecipe extends CustomRecipe {
	private static final Ingredient PAPER_INGREDIENT = Ingredient.of(Items.PAPER);
	private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);
	private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

	public FireworkRocketRecipe(ResourceLocation resourceLocation) {
		super(resourceLocation);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		boolean bl = false;
		int i = 0;

		for (int j = 0; j < craftingContainer.getContainerSize(); j++) {
			ItemStack itemStack = craftingContainer.getItem(j);
			if (!itemStack.isEmpty()) {
				if (PAPER_INGREDIENT.test(itemStack)) {
					if (bl) {
						return false;
					}

					bl = true;
				} else if (GUNPOWDER_INGREDIENT.test(itemStack)) {
					if (++i > 3) {
						return false;
					}
				} else if (!STAR_INGREDIENT.test(itemStack)) {
					return false;
				}
			}
		}

		return bl && i >= 1;
	}

	public ItemStack assemble(CraftingContainer craftingContainer) {
		ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET, 3);
		CompoundTag compoundTag = itemStack.getOrCreateTagElement("Fireworks");
		ListTag listTag = new ListTag();
		int i = 0;

		for (int j = 0; j < craftingContainer.getContainerSize(); j++) {
			ItemStack itemStack2 = craftingContainer.getItem(j);
			if (!itemStack2.isEmpty()) {
				if (GUNPOWDER_INGREDIENT.test(itemStack2)) {
					i++;
				} else if (STAR_INGREDIENT.test(itemStack2)) {
					CompoundTag compoundTag2 = itemStack2.getTagElement("Explosion");
					if (compoundTag2 != null) {
						listTag.add(compoundTag2);
					}
				}
			}
		}

		compoundTag.putByte("Flight", (byte)i);
		if (!listTag.isEmpty()) {
			compoundTag.put("Explosions", listTag);
		}

		return itemStack;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}

	@Override
	public ItemStack getResultItem() {
		return new ItemStack(Items.FIREWORK_ROCKET);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.FIREWORK_ROCKET;
	}
}
