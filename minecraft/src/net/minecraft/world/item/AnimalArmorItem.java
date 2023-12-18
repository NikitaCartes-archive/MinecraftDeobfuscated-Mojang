package net.minecraft.world.item;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class AnimalArmorItem extends Item {
	private final int protection;
	private final ResourceLocation textureLocation;
	private final AnimalArmorItem.Type type;

	public AnimalArmorItem(int i, AnimalArmorItem.Type type, @Nullable String string, Item.Properties properties) {
		super(properties);
		this.protection = i;
		this.type = type;
		this.textureLocation = (ResourceLocation)type.textureLocator.apply(string);
	}

	public ResourceLocation getTexture() {
		return this.textureLocation;
	}

	public int getProtection() {
		return this.protection;
	}

	public AnimalArmorItem.Type getType() {
		return this.type;
	}

	public static enum Type {
		EQUESTRIAN(string -> new ResourceLocation("textures/entity/horse/armor/horse_armor_" + string + ".png")),
		CANINE(string -> new ResourceLocation("textures/entity/wolf/wolf_armor.png"));

		final Function<String, ResourceLocation> textureLocator;

		private Type(Function<String, ResourceLocation> function) {
			this.textureLocator = function;
		}
	}
}
