package net.minecraft.world.item;

import net.minecraft.resources.ResourceLocation;

public class HorseArmorItem extends Item {
	private static final String TEX_FOLDER = "textures/entity/horse/";
	private final int protection;
	private final String texture;

	public HorseArmorItem(int i, String string, Item.Properties properties) {
		super(properties);
		this.protection = i;
		this.texture = "textures/entity/horse/armor/horse_armor_" + string + ".png";
	}

	public ResourceLocation getTexture() {
		return new ResourceLocation(this.texture);
	}

	public int getProtection() {
		return this.protection;
	}
}
