package net.minecraft.data.recipes;

public enum RecipeCategory {
	BUILDING_BLOCKS("building_blocks"),
	DECORATIONS("decorations"),
	REDSTONE("redstone"),
	TRANSPORTATION("transportation"),
	TOOLS("tools"),
	COMBAT("combat"),
	FOOD("food"),
	BREWING("brewing"),
	MISC("misc");

	private final String recipeFolderName;

	private RecipeCategory(final String string2) {
		this.recipeFolderName = string2;
	}

	public String getFolderName() {
		return this.recipeFolderName;
	}
}
