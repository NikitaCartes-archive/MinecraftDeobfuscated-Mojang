package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.Equippable;

@Environment(EnvType.CLIENT)
public class HumanoidArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> {
	private final A innerModel;
	private final A outerModel;
	private final A innerModelBaby;
	private final A outerModelBaby;
	private final EquipmentLayerRenderer equipmentRenderer;

	public HumanoidArmorLayer(RenderLayerParent<S, M> renderLayerParent, A humanoidModel, A humanoidModel2, EquipmentLayerRenderer equipmentLayerRenderer) {
		this(renderLayerParent, humanoidModel, humanoidModel2, humanoidModel, humanoidModel2, equipmentLayerRenderer);
	}

	public HumanoidArmorLayer(
		RenderLayerParent<S, M> renderLayerParent,
		A humanoidModel,
		A humanoidModel2,
		A humanoidModel3,
		A humanoidModel4,
		EquipmentLayerRenderer equipmentLayerRenderer
	) {
		super(renderLayerParent);
		this.innerModel = humanoidModel;
		this.outerModel = humanoidModel2;
		this.innerModelBaby = humanoidModel3;
		this.outerModelBaby = humanoidModel4;
		this.equipmentRenderer = equipmentLayerRenderer;
	}

	public static boolean shouldRender(ItemStack itemStack, EquipmentSlot equipmentSlot) {
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		return equippable != null && shouldRender(equippable, equipmentSlot);
	}

	private static boolean shouldRender(Equippable equippable, EquipmentSlot equipmentSlot) {
		return equippable.model().isPresent() && equippable.slot() == equipmentSlot;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S humanoidRenderState, float f, float g) {
		this.renderArmorPiece(
			poseStack, multiBufferSource, humanoidRenderState.chestItem, EquipmentSlot.CHEST, i, this.getArmorModel(humanoidRenderState, EquipmentSlot.CHEST)
		);
		this.renderArmorPiece(
			poseStack, multiBufferSource, humanoidRenderState.legsItem, EquipmentSlot.LEGS, i, this.getArmorModel(humanoidRenderState, EquipmentSlot.LEGS)
		);
		this.renderArmorPiece(
			poseStack, multiBufferSource, humanoidRenderState.feetItem, EquipmentSlot.FEET, i, this.getArmorModel(humanoidRenderState, EquipmentSlot.FEET)
		);
		this.renderArmorPiece(
			poseStack, multiBufferSource, humanoidRenderState.headItem, EquipmentSlot.HEAD, i, this.getArmorModel(humanoidRenderState, EquipmentSlot.HEAD)
		);
	}

	private void renderArmorPiece(
		PoseStack poseStack, MultiBufferSource multiBufferSource, ItemStack itemStack, EquipmentSlot equipmentSlot, int i, A humanoidModel
	) {
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		if (equippable != null && shouldRender(equippable, equipmentSlot)) {
			this.getParentModel().copyPropertiesTo(humanoidModel);
			this.setPartVisibility(humanoidModel, equipmentSlot);
			ResourceLocation resourceLocation = (ResourceLocation)equippable.model().orElseThrow();
			EquipmentModel.LayerType layerType = this.usesInnerModel(equipmentSlot) ? EquipmentModel.LayerType.HUMANOID_LEGGINGS : EquipmentModel.LayerType.HUMANOID;
			this.equipmentRenderer.renderLayers(layerType, resourceLocation, humanoidModel, itemStack, poseStack, multiBufferSource, i);
		}
	}

	protected void setPartVisibility(A humanoidModel, EquipmentSlot equipmentSlot) {
		humanoidModel.setAllVisible(false);
		switch (equipmentSlot) {
			case HEAD:
				humanoidModel.head.visible = true;
				humanoidModel.hat.visible = true;
				break;
			case CHEST:
				humanoidModel.body.visible = true;
				humanoidModel.rightArm.visible = true;
				humanoidModel.leftArm.visible = true;
				break;
			case LEGS:
				humanoidModel.body.visible = true;
				humanoidModel.rightLeg.visible = true;
				humanoidModel.leftLeg.visible = true;
				break;
			case FEET:
				humanoidModel.rightLeg.visible = true;
				humanoidModel.leftLeg.visible = true;
		}
	}

	private A getArmorModel(S humanoidRenderState, EquipmentSlot equipmentSlot) {
		if (this.usesInnerModel(equipmentSlot)) {
			return humanoidRenderState.isBaby ? this.innerModelBaby : this.innerModel;
		} else {
			return humanoidRenderState.isBaby ? this.outerModelBaby : this.outerModel;
		}
	}

	private boolean usesInnerModel(EquipmentSlot equipmentSlot) {
		return equipmentSlot == EquipmentSlot.LEGS;
	}
}
