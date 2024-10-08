package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;

public class FireworkStarRecipe extends CustomRecipe {
	private static final Map<Item, FireworkExplosion.Shape> SHAPE_BY_ITEM = Map.of(
		Items.FIRE_CHARGE,
		FireworkExplosion.Shape.LARGE_BALL,
		Items.FEATHER,
		FireworkExplosion.Shape.BURST,
		Items.GOLD_NUGGET,
		FireworkExplosion.Shape.STAR,
		Items.SKELETON_SKULL,
		FireworkExplosion.Shape.CREEPER,
		Items.WITHER_SKELETON_SKULL,
		FireworkExplosion.Shape.CREEPER,
		Items.CREEPER_HEAD,
		FireworkExplosion.Shape.CREEPER,
		Items.PLAYER_HEAD,
		FireworkExplosion.Shape.CREEPER,
		Items.DRAGON_HEAD,
		FireworkExplosion.Shape.CREEPER,
		Items.ZOMBIE_HEAD,
		FireworkExplosion.Shape.CREEPER,
		Items.PIGLIN_HEAD,
		FireworkExplosion.Shape.CREEPER
	);
	private static final Ingredient TRAIL_INGREDIENT = Ingredient.of(Items.DIAMOND);
	private static final Ingredient TWINKLE_INGREDIENT = Ingredient.of(Items.GLOWSTONE_DUST);
	private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);

	public FireworkStarRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		if (craftingInput.ingredientCount() < 2) {
			return false;
		} else {
			boolean bl = false;
			boolean bl2 = false;
			boolean bl3 = false;
			boolean bl4 = false;
			boolean bl5 = false;

			for (int i = 0; i < craftingInput.size(); i++) {
				ItemStack itemStack = craftingInput.getItem(i);
				if (!itemStack.isEmpty()) {
					if (SHAPE_BY_ITEM.containsKey(itemStack.getItem())) {
						if (bl3) {
							return false;
						}

						bl3 = true;
					} else if (TWINKLE_INGREDIENT.test(itemStack)) {
						if (bl5) {
							return false;
						}

						bl5 = true;
					} else if (TRAIL_INGREDIENT.test(itemStack)) {
						if (bl4) {
							return false;
						}

						bl4 = true;
					} else if (GUNPOWDER_INGREDIENT.test(itemStack)) {
						if (bl) {
							return false;
						}

						bl = true;
					} else {
						if (!(itemStack.getItem() instanceof DyeItem)) {
							return false;
						}

						bl2 = true;
					}
				}
			}

			return bl && bl2;
		}
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		FireworkExplosion.Shape shape = FireworkExplosion.Shape.SMALL_BALL;
		boolean bl = false;
		boolean bl2 = false;
		IntList intList = new IntArrayList();

		for (int i = 0; i < craftingInput.size(); i++) {
			ItemStack itemStack = craftingInput.getItem(i);
			if (!itemStack.isEmpty()) {
				FireworkExplosion.Shape shape2 = (FireworkExplosion.Shape)SHAPE_BY_ITEM.get(itemStack.getItem());
				if (shape2 != null) {
					shape = shape2;
				} else if (TWINKLE_INGREDIENT.test(itemStack)) {
					bl = true;
				} else if (TRAIL_INGREDIENT.test(itemStack)) {
					bl2 = true;
				} else if (itemStack.getItem() instanceof DyeItem dyeItem) {
					intList.add(dyeItem.getDyeColor().getFireworkColor());
				}
			}
		}

		ItemStack itemStack2 = new ItemStack(Items.FIREWORK_STAR);
		itemStack2.set(DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(shape, intList, IntList.of(), bl2, bl));
		return itemStack2;
	}

	@Override
	public RecipeSerializer<FireworkStarRecipe> getSerializer() {
		return RecipeSerializer.FIREWORK_STAR;
	}
}
