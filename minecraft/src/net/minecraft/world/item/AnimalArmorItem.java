package net.minecraft.world.item;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class AnimalArmorItem extends ArmorItem {
	private final ResourceLocation textureLocation;
	@Nullable
	private final ResourceLocation overlayTextureLocation;
	private final AnimalArmorItem.BodyType bodyType;

	public AnimalArmorItem(Holder<ArmorMaterial> holder, AnimalArmorItem.BodyType bodyType, boolean bl, Item.Properties properties) {
		super(holder, ArmorItem.Type.BODY, properties);
		this.bodyType = bodyType;
		ResourceLocation resourceLocation = (ResourceLocation)bodyType.textureLocator.apply(((ResourceKey)holder.unwrapKey().orElseThrow()).location());
		this.textureLocation = resourceLocation.withSuffix(".png");
		if (bl) {
			this.overlayTextureLocation = resourceLocation.withSuffix("_overlay.png");
		} else {
			this.overlayTextureLocation = null;
		}
	}

	public ResourceLocation getTexture() {
		return this.textureLocation;
	}

	@Nullable
	public ResourceLocation getOverlayTexture() {
		return this.overlayTextureLocation;
	}

	public AnimalArmorItem.BodyType getBodyType() {
		return this.bodyType;
	}

	@Override
	public SoundEvent getBreakingSound() {
		return this.bodyType.breakingSound;
	}

	@Override
	public boolean isEnchantable(ItemStack itemStack) {
		return false;
	}

	public static enum BodyType {
		EQUESTRIAN(
			resourceLocation -> resourceLocation.withPath((UnaryOperator<String>)(string -> "textures/entity/horse/armor/horse_armor_" + string)),
			SoundEvents.ITEM_BREAK
		),
		CANINE(resourceLocation -> resourceLocation.withPath("textures/entity/wolf/wolf_armor"), SoundEvents.WOLF_ARMOR_BREAK);

		final Function<ResourceLocation, ResourceLocation> textureLocator;
		final SoundEvent breakingSound;

		private BodyType(final Function<ResourceLocation, ResourceLocation> function, final SoundEvent soundEvent) {
			this.textureLocator = function;
			this.breakingSound = soundEvent;
		}
	}
}
