package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class SignEditScreen extends AbstractSignEditScreen {
	public static final float MAGIC_SCALE_NUMBER = 62.500004F;
	public static final float MAGIC_TEXT_SCALE = 0.9765628F;
	private static final Vector3f TEXT_SCALE = new Vector3f(0.9765628F, 0.9765628F, 0.9765628F);
	@Nullable
	private SignRenderer.SignModel signModel;

	public SignEditScreen(SignBlockEntity signBlockEntity, boolean bl) {
		super(signBlockEntity, bl);
	}

	@Override
	protected void init() {
		super.init();
		this.signModel = SignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType);
	}

	@Override
	protected void offsetSign(PoseStack poseStack, BlockState blockState) {
		super.offsetSign(poseStack, blockState);
		boolean bl = blockState.getBlock() instanceof StandingSignBlock;
		if (!bl) {
			poseStack.translate(0.0F, 35.0F, 0.0F);
		}
	}

	@Override
	protected void renderSignBackground(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockState blockState) {
		if (this.signModel != null) {
			boolean bl = blockState.getBlock() instanceof StandingSignBlock;
			poseStack.translate(0.0F, 31.0F, 0.0F);
			poseStack.scale(62.500004F, 62.500004F, -62.500004F);
			Material material = Sheets.getSignMaterial(this.woodType);
			VertexConsumer vertexConsumer = material.buffer(bufferSource, this.signModel::renderType);
			this.signModel.stick.visible = bl;
			this.signModel.root.render(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY);
		}
	}

	@Override
	protected Vector3f getSignTextScale() {
		return TEXT_SCALE;
	}
}
