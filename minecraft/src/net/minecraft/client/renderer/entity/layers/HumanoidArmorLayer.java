package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;

@Environment(EnvType.CLIENT)
public class HumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.<String, ResourceLocation>newHashMap();
	private final A innerModel;
	private final A outerModel;
	private final TextureAtlas armorTrimAtlas;

	public HumanoidArmorLayer(RenderLayerParent<T, M> renderLayerParent, A humanoidModel, A humanoidModel2, ModelManager modelManager) {
		super(renderLayerParent);
		this.innerModel = humanoidModel;
		this.outerModel = humanoidModel2;
		this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, EquipmentSlot.CHEST, i, this.getArmorModel(EquipmentSlot.CHEST));
		this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, EquipmentSlot.LEGS, i, this.getArmorModel(EquipmentSlot.LEGS));
		this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, EquipmentSlot.FEET, i, this.getArmorModel(EquipmentSlot.FEET));
		this.renderArmorPiece(poseStack, multiBufferSource, livingEntity, EquipmentSlot.HEAD, i, this.getArmorModel(EquipmentSlot.HEAD));
	}

	private void renderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int i, A humanoidModel) {
		ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
		if (itemStack.getItem() instanceof ArmorItem armorItem) {
			if (armorItem.getEquipmentSlot() == equipmentSlot) {
				this.getParentModel().copyPropertiesTo(humanoidModel);
				this.setPartVisibility(humanoidModel, equipmentSlot);
				boolean bl = this.usesInnerModel(equipmentSlot);
				ArmorMaterial armorMaterial = armorItem.getMaterial().value();
				int j = itemStack.is(ItemTags.DYEABLE) ? DyedItemColor.getOrDefault(itemStack, -6265536) : -1;

				for (ArmorMaterial.Layer layer : armorMaterial.layers()) {
					float f;
					float g;
					float h;
					if (layer.dyeable() && j != -1) {
						f = (float)FastColor.ARGB32.red(j) / 255.0F;
						g = (float)FastColor.ARGB32.green(j) / 255.0F;
						h = (float)FastColor.ARGB32.blue(j) / 255.0F;
					} else {
						f = 1.0F;
						g = 1.0F;
						h = 1.0F;
					}

					this.renderModel(poseStack, multiBufferSource, i, humanoidModel, f, g, h, layer.texture(bl));
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

	private void renderModel(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, A humanoidModel, float f, float g, float h, ResourceLocation resourceLocation
	) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(resourceLocation));
		humanoidModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, 1.0F);
	}

	private void renderTrim(
		Holder<ArmorMaterial> holder, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorTrim armorTrim, A humanoidModel, boolean bl
	) {
		TextureAtlasSprite textureAtlasSprite = this.armorTrimAtlas.getSprite(bl ? armorTrim.innerTexture(holder) : armorTrim.outerTexture(holder));
		VertexConsumer vertexConsumer = textureAtlasSprite.wrap(multiBufferSource.getBuffer(Sheets.armorTrimsSheet(armorTrim.pattern().value().decal())));
		humanoidModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderGlint(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, A humanoidModel) {
		humanoidModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.armorEntityGlint()), i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	private A getArmorModel(EquipmentSlot equipmentSlot) {
		return this.usesInnerModel(equipmentSlot) ? this.innerModel : this.outerModel;
	}

	private boolean usesInnerModel(EquipmentSlot equipmentSlot) {
		return equipmentSlot == EquipmentSlot.LEGS;
	}
}
