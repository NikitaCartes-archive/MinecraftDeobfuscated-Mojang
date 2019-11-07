package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

@Environment(EnvType.CLIENT)
public abstract class BlockEntityRenderer<T extends BlockEntity> {
	protected final BlockEntityRenderDispatcher renderer;

	public BlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		this.renderer = blockEntityRenderDispatcher;
	}

	public abstract void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j);

	protected TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
		return Minecraft.getInstance().getTextureAtlas().getSprite(resourceLocation);
	}

	public boolean shouldRenderOffScreen(T blockEntity) {
		return false;
	}
}
