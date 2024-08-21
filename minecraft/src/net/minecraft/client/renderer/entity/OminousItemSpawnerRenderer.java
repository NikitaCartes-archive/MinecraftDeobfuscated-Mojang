package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.OminousItemSpawnerRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class OminousItemSpawnerRenderer extends EntityRenderer<OminousItemSpawner, OminousItemSpawnerRenderState> {
	private static final float ROTATION_SPEED = 40.0F;
	private static final int TICKS_SCALING = 50;
	private final ItemRenderer itemRenderer;

	protected OminousItemSpawnerRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
	}

	public ResourceLocation getTextureLocation(OminousItemSpawnerRenderState ominousItemSpawnerRenderState) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	public OminousItemSpawnerRenderState createRenderState() {
		return new OminousItemSpawnerRenderState();
	}

	public void extractRenderState(OminousItemSpawner ominousItemSpawner, OminousItemSpawnerRenderState ominousItemSpawnerRenderState, float f) {
		super.extractRenderState(ominousItemSpawner, ominousItemSpawnerRenderState, f);
		ItemStack itemStack = ominousItemSpawner.getItem();
		ominousItemSpawnerRenderState.item = itemStack.copy();
		ominousItemSpawnerRenderState.itemModel = !itemStack.isEmpty() ? this.itemRenderer.getModel(itemStack, ominousItemSpawner.level(), null, 0) : null;
	}

	public void render(OminousItemSpawnerRenderState ominousItemSpawnerRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		BakedModel bakedModel = ominousItemSpawnerRenderState.itemModel;
		if (bakedModel != null) {
			poseStack.pushPose();
			if (ominousItemSpawnerRenderState.ageInTicks <= 50.0F) {
				float f = Math.min(ominousItemSpawnerRenderState.ageInTicks, 50.0F) / 50.0F;
				poseStack.scale(f, f, f);
			}

			float f = Mth.wrapDegrees(ominousItemSpawnerRenderState.ageInTicks * 40.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees(f));
			ItemEntityRenderer.renderMultipleFromCount(
				this.itemRenderer, poseStack, multiBufferSource, 15728880, ominousItemSpawnerRenderState.item, bakedModel, bakedModel.isGui3d(), RandomSource.create()
			);
			poseStack.popPose();
		}
	}
}
