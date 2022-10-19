package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class HangingSignEditScreen extends AbstractSignEditScreen {
	public static final float MAGIC_BACKGROUND_SCALE = 4.0F;
	private static final Vector3f TEXT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
	private static final int TEXTURE_WIDTH = 16;
	private static final int TEXTURE_HEIGHT = 16;
	private final ResourceLocation texture = new ResourceLocation("textures/gui/hanging_signs/" + this.woodType.name() + ".png");

	public HangingSignEditScreen(SignBlockEntity signBlockEntity, boolean bl) {
		super(signBlockEntity, bl, Component.translatable("hanging_sign.edit"));
	}

	@Override
	protected void offsetSign(PoseStack poseStack, BlockState blockState) {
		poseStack.translate((double)((float)this.width / 2.0F), 125.0, 50.0);
	}

	@Override
	protected void renderSignBackground(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockState blockState) {
		poseStack.translate(0.0, -13.0, 0.0);
		RenderSystem.setShaderTexture(0, this.texture);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.scale(4.0F, 4.0F, 1.0F);
		blit(poseStack, -8, -8, 0.0F, 0.0F, 16, 16, 16, 16);
	}

	@Override
	protected Vector3f getSignTextScale() {
		return TEXT_SCALE;
	}
}
