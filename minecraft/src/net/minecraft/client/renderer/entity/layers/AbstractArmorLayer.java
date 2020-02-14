package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public abstract class AbstractArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
	protected final A innerModel;
	protected final A outerModel;
	protected static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.<String, ResourceLocation>newHashMap();

	protected AbstractArmorLayer(RenderLayerParent<T, M> renderLayerParent, A humanoidModel, A humanoidModel2) {
		super(renderLayerParent);
		this.innerModel = humanoidModel;
		this.outerModel = humanoidModel2;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, f, g, h, j, k, l, EquipmentSlot.CHEST, i, this.getArmorModel(EquipmentSlot.CHEST));
		this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, f, g, h, j, k, l, EquipmentSlot.LEGS, i, this.getArmorModel(EquipmentSlot.LEGS));
		this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, f, g, h, j, k, l, EquipmentSlot.FEET, i, this.getArmorModel(EquipmentSlot.FEET));
		this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, f, g, h, j, k, l, EquipmentSlot.HEAD, i, this.getArmorModel(EquipmentSlot.HEAD));
	}

	private void renderArmorPiece(
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		T livingEntity,
		float f,
		float g,
		float h,
		float i,
		float j,
		float k,
		EquipmentSlot equipmentSlot,
		int l,
		A humanoidModel
	) {
		ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
		if (itemStack.getItem() instanceof ArmorItem) {
			ArmorItem armorItem = (ArmorItem)itemStack.getItem();
			if (armorItem.getSlot() == equipmentSlot) {
				this.getParentModel().copyPropertiesTo(humanoidModel);
				humanoidModel.prepareMobModel(livingEntity, f, g, h);
				this.setPartVisibility(humanoidModel, equipmentSlot);
				humanoidModel.setupAnim(livingEntity, f, g, i, j, k);
				boolean bl = this.usesInnerModel(equipmentSlot);
				boolean bl2 = itemStack.hasFoil();
				if (armorItem instanceof DyeableArmorItem) {
					int m = ((DyeableArmorItem)armorItem).getColor(itemStack);
					float n = (float)(m >> 16 & 0xFF) / 255.0F;
					float o = (float)(m >> 8 & 0xFF) / 255.0F;
					float p = (float)(m & 0xFF) / 255.0F;
					this.renderModel(equipmentSlot, poseStack, multiBufferSource, l, armorItem, bl2, humanoidModel, bl, n, o, p, null);
					this.renderModel(equipmentSlot, poseStack, multiBufferSource, l, armorItem, bl2, humanoidModel, bl, 1.0F, 1.0F, 1.0F, "overlay");
				} else {
					this.renderModel(equipmentSlot, poseStack, multiBufferSource, l, armorItem, bl2, humanoidModel, bl, 1.0F, 1.0F, 1.0F, null);
				}
			}
		}
	}

	private void renderModel(
		EquipmentSlot equipmentSlot,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		ArmorItem armorItem,
		boolean bl,
		A humanoidModel,
		boolean bl2,
		float f,
		float g,
		float h,
		@Nullable String string
	) {
		VertexConsumer vertexConsumer = ItemRenderer.getFoilBuffer(
			multiBufferSource, RenderType.entityCutoutNoCull(this.getArmorLocation(equipmentSlot, armorItem, bl2, string)), false, bl
		);
		humanoidModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, 1.0F);
	}

	public A getArmorModel(EquipmentSlot equipmentSlot) {
		return this.usesInnerModel(equipmentSlot) ? this.innerModel : this.outerModel;
	}

	private boolean usesInnerModel(EquipmentSlot equipmentSlot) {
		return equipmentSlot == EquipmentSlot.LEGS;
	}

	protected ResourceLocation getArmorLocation(EquipmentSlot equipmentSlot, ArmorItem armorItem, boolean bl, @Nullable String string) {
		String string2 = "textures/models/armor/" + armorItem.getMaterial().getName() + "_layer_" + (bl ? 2 : 1) + (string == null ? "" : "_" + string) + ".png";
		return (ResourceLocation)ARMOR_LOCATION_CACHE.computeIfAbsent(string2, ResourceLocation::new);
	}

	protected abstract void setPartVisibility(A humanoidModel, EquipmentSlot equipmentSlot);

	protected abstract void hideAllArmor(A humanoidModel);
}
