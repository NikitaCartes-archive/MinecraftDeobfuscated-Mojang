package net.minecraft.client.renderer.entity.layers;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;

@Environment(EnvType.CLIENT)
public class PiglinArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A> {
	private final A headModel;

	public PiglinArmorLayer(RenderLayerParent<T, M> renderLayerParent, A humanoidModel, A humanoidModel2, A humanoidModel3) {
		super(renderLayerParent, humanoidModel, humanoidModel2);
		this.headModel = humanoidModel3;
	}

	@Override
	public A getArmorModel(EquipmentSlot equipmentSlot) {
		return equipmentSlot == EquipmentSlot.HEAD ? this.headModel : super.getArmorModel(equipmentSlot);
	}

	@Override
	protected ResourceLocation getArmorLocation(EquipmentSlot equipmentSlot, ArmorItem armorItem, boolean bl, @Nullable String string) {
		if (equipmentSlot == EquipmentSlot.HEAD) {
			String string2 = string == null ? "" : "_" + string;
			String string3 = "textures/models/armor/" + armorItem.getMaterial().getName() + "_piglin_helmet" + string2 + ".png";
			return (ResourceLocation)ARMOR_LOCATION_CACHE.computeIfAbsent(string3, ResourceLocation::new);
		} else {
			return super.getArmorLocation(equipmentSlot, armorItem, bl, string);
		}
	}
}
