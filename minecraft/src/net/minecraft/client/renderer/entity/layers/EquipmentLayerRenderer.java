package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentModelSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

@Environment(EnvType.CLIENT)
public class EquipmentLayerRenderer {
	private static final int NO_LAYER_COLOR = 0;
	private final EquipmentModelSet equipmentModels;
	private final Function<EquipmentLayerRenderer.LayerTextureKey, ResourceLocation> layerTextureLookup;
	private final Function<EquipmentLayerRenderer.TrimSpriteKey, TextureAtlasSprite> trimSpriteLookup;

	public EquipmentLayerRenderer(EquipmentModelSet equipmentModelSet, TextureAtlas textureAtlas) {
		this.equipmentModels = equipmentModelSet;
		this.layerTextureLookup = Util.memoize(
			(Function<EquipmentLayerRenderer.LayerTextureKey, ResourceLocation>)(layerTextureKey -> layerTextureKey.layer.getTextureLocation(layerTextureKey.layerType))
		);
		this.trimSpriteLookup = Util.memoize((Function<EquipmentLayerRenderer.TrimSpriteKey, TextureAtlasSprite>)(trimSpriteKey -> {
			ResourceLocation resourceLocation = trimSpriteKey.trim.getTexture(trimSpriteKey.layerType, trimSpriteKey.equipmentModelId);
			return textureAtlas.getSprite(resourceLocation);
		}));
	}

	public void renderLayers(
		EquipmentModel.LayerType layerType,
		ResourceLocation resourceLocation,
		Model model,
		ItemStack itemStack,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i
	) {
		this.renderLayers(layerType, resourceLocation, model, itemStack, poseStack, multiBufferSource, i, null);
	}

	public void renderLayers(
		EquipmentModel.LayerType layerType,
		ResourceLocation resourceLocation,
		Model model,
		ItemStack itemStack,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		@Nullable ResourceLocation resourceLocation2
	) {
		List<EquipmentModel.Layer> list = this.equipmentModels.get(resourceLocation).getLayers(layerType);
		if (!list.isEmpty()) {
			int j = itemStack.is(ItemTags.DYEABLE) ? DyedItemColor.getOrDefault(itemStack, 0) : 0;
			boolean bl = itemStack.hasFoil();

			for (EquipmentModel.Layer layer : list) {
				int k = getColorForLayer(layer, j);
				if (k != 0) {
					ResourceLocation resourceLocation3 = layer.usePlayerTexture() && resourceLocation2 != null
						? resourceLocation2
						: (ResourceLocation)this.layerTextureLookup.apply(new EquipmentLayerRenderer.LayerTextureKey(layerType, layer));
					VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource, RenderType.armorCutoutNoCull(resourceLocation3), bl);
					model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, k);
					bl = false;
				}
			}

			ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
			if (armorTrim != null) {
				TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)this.trimSpriteLookup
					.apply(new EquipmentLayerRenderer.TrimSpriteKey(armorTrim, layerType, resourceLocation));
				VertexConsumer vertexConsumer2 = textureAtlasSprite.wrap(multiBufferSource.getBuffer(Sheets.armorTrimsSheet(armorTrim.pattern().value().decal())));
				model.renderToBuffer(poseStack, vertexConsumer2, i, OverlayTexture.NO_OVERLAY);
			}
		}
	}

	private static int getColorForLayer(EquipmentModel.Layer layer, int i) {
		Optional<EquipmentModel.Dyeable> optional = layer.dyeable();
		if (optional.isPresent()) {
			int j = (Integer)((EquipmentModel.Dyeable)optional.get()).colorWhenUndyed().map(ARGB::opaque).orElse(0);
			return i != 0 ? i : j;
		} else {
			return -1;
		}
	}

	@Environment(EnvType.CLIENT)
	static record LayerTextureKey(EquipmentModel.LayerType layerType, EquipmentModel.Layer layer) {
	}

	@Environment(EnvType.CLIENT)
	static record TrimSpriteKey(ArmorTrim trim, EquipmentModel.LayerType layerType, ResourceLocation equipmentModelId) {
	}
}
