package net.minecraft.world.item;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class AnimalArmorItem extends ArmorItem {
	private final ResourceLocation textureLocation;
	private final AnimalArmorItem.BodyType bodyType;

	public AnimalArmorItem(Holder<ArmorMaterial> holder, AnimalArmorItem.BodyType bodyType, Item.Properties properties) {
		super(holder, ArmorItem.Type.BODY, properties);
		this.bodyType = bodyType;
		this.textureLocation = (ResourceLocation)bodyType.textureLocator.apply(((ResourceKey)holder.unwrapKey().orElseThrow()).location());
	}

	public ResourceLocation getTexture() {
		return this.textureLocation;
	}

	public AnimalArmorItem.BodyType getBodyType() {
		return this.bodyType;
	}

	public static enum BodyType {
		EQUESTRIAN(resourceLocation -> resourceLocation.withPath((UnaryOperator<String>)(string -> "textures/entity/horse/armor/horse_armor_" + string + ".png"))),
		CANINE(resourceLocation -> resourceLocation.withPath("textures/entity/wolf/wolf_armor.png"));

		final Function<ResourceLocation, ResourceLocation> textureLocator;

		private BodyType(Function<ResourceLocation, ResourceLocation> function) {
			this.textureLocator = function;
		}
	}
}
