package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.Equippable;

@Environment(EnvType.CLIENT)
public class WingsLayer<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private final ElytraModel elytraModel;
	private final ElytraModel elytraBabyModel;
	private final EquipmentLayerRenderer equipmentRenderer;

	public WingsLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, EquipmentLayerRenderer equipmentLayerRenderer) {
		super(renderLayerParent);
		this.elytraModel = new ElytraModel(entityModelSet.bakeLayer(ModelLayers.ELYTRA));
		this.elytraBabyModel = new ElytraModel(entityModelSet.bakeLayer(ModelLayers.ELYTRA_BABY));
		this.equipmentRenderer = equipmentLayerRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S humanoidRenderState, float f, float g) {
		ItemStack itemStack = humanoidRenderState.chestItem;
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		if (equippable != null && !equippable.model().isEmpty()) {
			ResourceLocation resourceLocation = getPlayerElytraTexture(humanoidRenderState);
			ElytraModel elytraModel = humanoidRenderState.isBaby ? this.elytraBabyModel : this.elytraModel;
			ResourceLocation resourceLocation2 = (ResourceLocation)equippable.model().get();
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.0F, 0.125F);
			elytraModel.setupAnim(humanoidRenderState);
			this.equipmentRenderer
				.renderLayers(
					EquipmentModel.LayerType.WINGS,
					resourceLocation2,
					elytraModel,
					itemStack,
					RenderType::armorCutoutNoCull,
					poseStack,
					multiBufferSource,
					i,
					resourceLocation
				);
			poseStack.popPose();
		}
	}

	@Nullable
	private static ResourceLocation getPlayerElytraTexture(HumanoidRenderState humanoidRenderState) {
		if (humanoidRenderState instanceof PlayerRenderState playerRenderState) {
			PlayerSkin playerSkin = playerRenderState.skin;
			if (playerSkin.elytraTexture() != null) {
				return playerSkin.elytraTexture();
			}

			if (playerSkin.capeTexture() != null && playerRenderState.showCape) {
				return playerSkin.capeTexture();
			}
		}

		return null;
	}
}
