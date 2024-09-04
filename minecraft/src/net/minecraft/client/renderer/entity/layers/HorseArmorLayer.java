package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.Equippable;

@Environment(EnvType.CLIENT)
public class HorseArmorLayer extends RenderLayer<HorseRenderState, HorseModel> {
	private final HorseModel adultModel;
	private final HorseModel babyModel;
	private final EquipmentLayerRenderer equipmentRenderer;

	public HorseArmorLayer(
		RenderLayerParent<HorseRenderState, HorseModel> renderLayerParent, EntityModelSet entityModelSet, EquipmentLayerRenderer equipmentLayerRenderer
	) {
		super(renderLayerParent);
		this.equipmentRenderer = equipmentLayerRenderer;
		this.adultModel = new HorseModel(entityModelSet.bakeLayer(ModelLayers.HORSE_ARMOR));
		this.babyModel = new HorseModel(entityModelSet.bakeLayer(ModelLayers.HORSE_BABY_ARMOR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, HorseRenderState horseRenderState, float f, float g) {
		ItemStack itemStack = horseRenderState.bodyArmorItem;
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		if (equippable != null && !equippable.model().isEmpty()) {
			HorseModel horseModel = horseRenderState.isBaby ? this.babyModel : this.adultModel;
			ResourceLocation resourceLocation = (ResourceLocation)equippable.model().get();
			horseModel.setupAnim(horseRenderState);
			this.equipmentRenderer
				.renderLayers(EquipmentModel.LayerType.HORSE_BODY, resourceLocation, horseModel, itemStack, RenderType::armorCutoutNoCull, poseStack, multiBufferSource, i);
		}
	}
}
