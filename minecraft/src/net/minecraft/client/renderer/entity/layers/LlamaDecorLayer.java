package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.EquipmentModels;
import net.minecraft.world.item.equipment.Equippable;

@Environment(EnvType.CLIENT)
public class LlamaDecorLayer extends RenderLayer<LlamaRenderState, LlamaModel> {
	private final LlamaModel adultModel;
	private final LlamaModel babyModel;
	private final EquipmentLayerRenderer equipmentRenderer;

	public LlamaDecorLayer(
		RenderLayerParent<LlamaRenderState, LlamaModel> renderLayerParent, EntityModelSet entityModelSet, EquipmentLayerRenderer equipmentLayerRenderer
	) {
		super(renderLayerParent);
		this.equipmentRenderer = equipmentLayerRenderer;
		this.adultModel = new LlamaModel(entityModelSet.bakeLayer(ModelLayers.LLAMA_DECOR));
		this.babyModel = new LlamaModel(entityModelSet.bakeLayer(ModelLayers.LLAMA_BABY_DECOR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, LlamaRenderState llamaRenderState, float f, float g) {
		ItemStack itemStack = llamaRenderState.bodyItem;
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		if (equippable != null && equippable.model().isPresent()) {
			this.renderEquipment(poseStack, multiBufferSource, llamaRenderState, itemStack, (ResourceLocation)equippable.model().get(), i);
		} else if (llamaRenderState.isTraderLlama) {
			this.renderEquipment(poseStack, multiBufferSource, llamaRenderState, ItemStack.EMPTY, EquipmentModels.TRADER_LLAMA, i);
		}
	}

	private void renderEquipment(
		PoseStack poseStack, MultiBufferSource multiBufferSource, LlamaRenderState llamaRenderState, ItemStack itemStack, ResourceLocation resourceLocation, int i
	) {
		LlamaModel llamaModel = llamaRenderState.isBaby ? this.babyModel : this.adultModel;
		llamaModel.setupAnim(llamaRenderState);
		this.equipmentRenderer
			.renderLayers(EquipmentModel.LayerType.LLAMA_BODY, resourceLocation, llamaModel, itemStack, RenderType::entityCutoutNoCull, poseStack, multiBufferSource, i);
	}
}
