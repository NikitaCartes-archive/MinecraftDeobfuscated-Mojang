package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

@Environment(EnvType.CLIENT)
public class WolfArmorLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
	private final WolfModel<Wolf> model;
	private static final Map<Crackiness.Level, ResourceLocation> ARMOR_CRACK_LOCATIONS = Map.of(
		Crackiness.Level.LOW,
		new ResourceLocation("textures/entity/wolf/wolf_armor_crackiness_low.png"),
		Crackiness.Level.MEDIUM,
		new ResourceLocation("textures/entity/wolf/wolf_armor_crackiness_medium.png"),
		Crackiness.Level.HIGH,
		new ResourceLocation("textures/entity/wolf/wolf_armor_crackiness_high.png")
	);

	public WolfArmorLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new WolfModel<>(entityModelSet.bakeLayer(ModelLayers.WOLF_ARMOR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Wolf wolf, float f, float g, float h, float j, float k, float l) {
		if (wolf.hasArmor()) {
			ItemStack itemStack = wolf.getBodyArmorItem();
			if (itemStack.getItem() instanceof AnimalArmorItem animalArmorItem && animalArmorItem.getBodyType() == AnimalArmorItem.BodyType.CANINE) {
				this.getParentModel().copyPropertiesTo(this.model);
				this.model.prepareMobModel(wolf, f, g, h);
				this.model.setupAnim(wolf, f, g, j, k, l);
				VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(animalArmorItem.getTexture()));
				this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
				this.maybeRenderColoredLayer(poseStack, multiBufferSource, i, itemStack, animalArmorItem);
				this.maybeRenderCracks(poseStack, multiBufferSource, i, itemStack);
				return;
			}
		}
	}

	private void maybeRenderColoredLayer(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ItemStack itemStack, AnimalArmorItem animalArmorItem) {
		if (itemStack.is(ItemTags.DYEABLE)) {
			int j = DyedItemColor.getOrDefault(itemStack, 0);
			if (FastColor.ARGB32.alpha(j) == 0) {
				return;
			}

			ResourceLocation resourceLocation = animalArmorItem.getOverlayTexture();
			if (resourceLocation == null) {
				return;
			}

			float f = (float)FastColor.ARGB32.red(j) / 255.0F;
			float g = (float)FastColor.ARGB32.green(j) / 255.0F;
			float h = (float)FastColor.ARGB32.blue(j) / 255.0F;
			this.model
				.renderToBuffer(poseStack, multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(resourceLocation)), i, OverlayTexture.NO_OVERLAY, f, g, h, 1.0F);
		}
	}

	private void maybeRenderCracks(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ItemStack itemStack) {
		Crackiness.Level level = Crackiness.WOLF_ARMOR.byDamage(itemStack);
		if (level != Crackiness.Level.NONE) {
			ResourceLocation resourceLocation = (ResourceLocation)ARMOR_CRACK_LOCATIONS.get(level);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(resourceLocation));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
