package net.minecraft.world.item.crafting;

import net.minecraft.util.StringRepresentable;

public enum CookingBookCategory implements StringRepresentable {
	FOOD("food"),
	BLOCKS("blocks"),
	MISC("misc");

	public static final StringRepresentable.EnumCodec<CookingBookCategory> CODEC = StringRepresentable.fromEnum(CookingBookCategory::values);
	private final String name;

	private CookingBookCategory(String string2) {
		this.name = string2;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
