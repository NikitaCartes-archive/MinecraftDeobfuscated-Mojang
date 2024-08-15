package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;

@Environment(EnvType.CLIENT)
public class HumanoidArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> {
	private final A innerModel;
	private final A outerModel;
	private final A innerModelBaby;
	private final A outerModelBaby;
	private final TextureAtlas armorTrimAtlas;

	public HumanoidArmorLayer(RenderLayerParent<S, M> renderLayerParent, A humanoidModel, A humanoidModel2, ModelManager modelManager) {
		this(renderLayerParent, humanoidModel, humanoidModel2, humanoidModel, humanoidModel2, modelManager);
	}

	public HumanoidArmorLayer(
		RenderLayerParent<S, M> renderLayerParent, A humanoidModel, A humanoidModel2, A humanoidModel3, A humanoidModel4, ModelManager modelManager
	) {
		super(renderLayerParent);
		this.innerModel = humanoidModel;
		this.outerModel = humanoidModel2;
		this.innerModelBaby = humanoidModel3;
		this.outerModelBaby = humanoidModel4;
		this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
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
		if (itemStack.getItem() instanceof ArmorItem armorItem) {
			if (armorItem.getEquipmentSlot() == equipmentSlot) {
				this.getParentModel().copyPropertiesTo(humanoidModel);
				this.setPartVisibility(humanoidModel, equipmentSlot);
				boolean bl = this.usesInnerModel(equipmentSlot);
				ArmorMaterial armorMaterial = armorItem.getMaterial().value();
				int j = itemStack.is(ItemTags.DYEABLE) ? ARGB.opaque(DyedItemColor.getOrDefault(itemStack, -6265536)) : -1;

				for (ArmorMaterial.Layer layer : armorMaterial.layers()) {
					int k = layer.dyeable() ? j : -1;
					this.renderModel(poseStack, multiBufferSource, i, humanoidModel, k, layer.texture(bl));
				}

				ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
				if (armorTrim != null) {
					this.renderTrim(armorItem.getMaterial(), poseStack, multiBufferSource, i, armorTrim, humanoidModel, bl);
				}

				if (itemStack.hasFoil()) {
					this.renderGlint(poseStack, multiBufferSource, i, humanoidModel);
				}
			}
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

	private void renderModel(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, A humanoidModel, int j, ResourceLocation resourceLocation) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(resourceLocation));
		humanoidModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, j);
	}

	private void renderTrim(
		Holder<ArmorMaterial> holder, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorTrim armorTrim, A humanoidModel, boolean bl
	) {
		TextureAtlasSprite textureAtlasSprite = this.armorTrimAtlas.getSprite(bl ? armorTrim.innerTexture(holder) : armorTrim.outerTexture(holder));
		VertexConsumer vertexConsumer = textureAtlasSprite.wrap(multiBufferSource.getBuffer(Sheets.armorTrimsSheet(armorTrim.pattern().value().decal())));
		humanoidModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
	}

	private void renderGlint(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, A humanoidModel) {
		humanoidModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.armorEntityGlint()), i, OverlayTexture.NO_OVERLAY);
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
