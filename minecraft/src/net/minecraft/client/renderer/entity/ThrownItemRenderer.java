package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ThrownItemRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T, ThrownItemRenderState> {
	private final ItemRenderer itemRenderer;
	private final float scale;
	private final boolean fullBright;

	public ThrownItemRenderer(EntityRendererProvider.Context context, float f, boolean bl) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.scale = f;
		this.fullBright = bl;
	}

	public ThrownItemRenderer(EntityRendererProvider.Context context) {
		this(context, 1.0F, false);
	}

	@Override
	protected int getBlockLightLevel(T entity, BlockPos blockPos) {
		return this.fullBright ? 15 : super.getBlockLightLevel(entity, blockPos);
	}

	public void render(ThrownItemRenderState thrownItemRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.scale(this.scale, this.scale, this.scale);
		poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		if (thrownItemRenderState.itemModel != null) {
			this.itemRenderer
				.render(
					thrownItemRenderState.item, ItemDisplayContext.GROUND, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, thrownItemRenderState.itemModel
				);
		}

		poseStack.popPose();
		super.render(thrownItemRenderState, poseStack, multiBufferSource, i);
	}

	public ThrownItemRenderState createRenderState() {
		return new ThrownItemRenderState();
	}

	public void extractRenderState(T entity, ThrownItemRenderState thrownItemRenderState, float f) {
		super.extractRenderState(entity, thrownItemRenderState, f);
		ItemStack itemStack = entity.getItem();
		thrownItemRenderState.itemModel = !itemStack.isEmpty() ? this.itemRenderer.getModel(itemStack, entity.level(), null, entity.getId()) : null;
		thrownItemRenderState.item = itemStack.copy();
	}

	public ResourceLocation getTextureLocation(ThrownItemRenderState thrownItemRenderState) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
