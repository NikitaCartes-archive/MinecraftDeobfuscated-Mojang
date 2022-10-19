package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractCookingRecipe implements Recipe<Container> {
	protected final RecipeType<?> type;
	protected final ResourceLocation id;
	private final CookingBookCategory category;
	protected final String group;
	protected final Ingredient ingredient;
	protected final ItemStack result;
	protected final float experience;
	protected final int cookingTime;

	public AbstractCookingRecipe(
		RecipeType<?> recipeType,
		ResourceLocation resourceLocation,
		String string,
		CookingBookCategory cookingBookCategory,
		Ingredient ingredient,
		ItemStack itemStack,
		float f,
		int i
	) {
		this.type = recipeType;
		this.category = cookingBookCategory;
		this.id = resourceLocation;
		this.group = string;
		this.ingredient = ingredient;
		this.result = itemStack;
		this.experience = f;
		this.cookingTime = i;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.ingredient.test(container.getItem(0));
	}

	@Override
	public ItemStack assemble(Container container) {
		return this.result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return true;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nonNullList = NonNullList.create();
		nonNullList.add(this.ingredient);
		return nonNullList;
	}

	public float getExperience() {
		return this.experience;
	}

	@Override
	public ItemStack getResultItem() {
		return this.result;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	public int getCookingTime() {
		return this.cookingTime;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeType<?> getType() {
		return this.type;
	}

	public CookingBookCategory category() {
		return this.category;
	}
}
