package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class SignEditScreen extends Screen {
	private final SignRenderer.SignModel signModel = new SignRenderer.SignModel();
	private final SignBlockEntity sign;
	private int frame;
	private int line;
	private TextFieldHelper signField;
	private final String[] messages;

	public SignEditScreen(SignBlockEntity signBlockEntity) {
		super(new TranslatableComponent("sign.edit"));
		this.messages = (String[])IntStream.range(0, 4).mapToObj(signBlockEntity::getMessage).map(Component::getString).toArray(String[]::new);
		this.sign = signBlockEntity;
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, button -> this.onDone()));
		this.sign.setEditable(false);
		this.signField = new TextFieldHelper(
			() -> this.messages[this.line],
			string -> {
				this.messages[this.line] = string;
				this.sign.setMessage(this.line, new TextComponent(string));
			},
			TextFieldHelper.createClipboardGetter(this.minecraft),
			TextFieldHelper.createClipboardSetter(this.minecraft),
			string -> this.minecraft.font.width(string) <= 90
		);
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener != null) {
			clientPacketListener.send(new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.messages[0], this.messages[1], this.messages[2], this.messages[3]));
		}

		this.sign.setEditable(true);
	}

	@Override
	public void tick() {
		this.frame++;
		if (!this.sign.getType().isValid(this.sign.getBlockState().getBlock())) {
			this.onDone();
		}
	}

	private void onDone() {
		this.sign.setChanged();
		this.minecraft.setScreen(null);
	}

	@Override
	public boolean charTyped(char c, int i) {
		this.signField.charTyped(c);
		return true;
	}

	@Override
	public void onClose() {
		this.onDone();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 265) {
			this.line = this.line - 1 & 3;
			this.signField.setCursorToEnd();
			return true;
		} else if (i == 264 || i == 257 || i == 335) {
			this.line = this.line + 1 & 3;
			this.signField.setCursorToEnd();
			return true;
		} else {
			return this.signField.keyPressed(i) ? true : super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		Lighting.setupForFlatItems();
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 40, 16777215);
		poseStack.pushPose();
		poseStack.translate((double)(this.width / 2), 0.0, 50.0);
		float g = 93.75F;
		poseStack.scale(93.75F, -93.75F, 93.75F);
		poseStack.translate(0.0, -1.3125, 0.0);
		BlockState blockState = this.sign.getBlockState();
		boolean bl = blockState.getBlock() instanceof StandingSignBlock;
		if (!bl) {
			poseStack.translate(0.0, -0.3125, 0.0);
		}

		boolean bl2 = this.frame / 6 % 2 == 0;
		float h = 0.6666667F;
		poseStack.pushPose();
		poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
		MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
		Material material = SignRenderer.getMaterial(blockState.getBlock());
		VertexConsumer vertexConsumer = material.buffer(bufferSource, this.signModel::renderType);
		this.signModel.sign.render(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY);
		if (bl) {
			this.signModel.stick.render(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY);
		}

		poseStack.popPose();
		float k = 0.010416667F;
		poseStack.translate(0.0, 0.33333334F, 0.046666667F);
		poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
		int l = this.sign.getColor().getTextColor();
		int m = this.signField.getCursorPos();
		int n = this.signField.getSelectionPos();
		int o = this.line * 10 - this.messages.length * 5;
		Matrix4f matrix4f = poseStack.last().pose();

		for (int p = 0; p < this.messages.length; p++) {
			String string = this.messages[p];
			if (string != null) {
				if (this.font.isBidirectional()) {
					string = this.font.bidirectionalShaping(string);
				}

				float q = (float)(-this.minecraft.font.width(string) / 2);
				this.minecraft.font.drawInBatch(string, q, (float)(p * 10 - this.messages.length * 5), l, false, matrix4f, bufferSource, false, 0, 15728880, false);
				if (p == this.line && m >= 0 && bl2) {
					int r = this.minecraft.font.width(string.substring(0, Math.max(Math.min(m, string.length()), 0)));
					int s = r - this.minecraft.font.width(string) / 2;
					if (m >= string.length()) {
						this.minecraft.font.drawInBatch("_", (float)s, (float)o, l, false, matrix4f, bufferSource, false, 0, 15728880, false);
					}
				}
			}
		}

		bufferSource.endBatch();

		for (int px = 0; px < this.messages.length; px++) {
			String string = this.messages[px];
			if (string != null && px == this.line && m >= 0) {
				int t = this.minecraft.font.width(string.substring(0, Math.max(Math.min(m, string.length()), 0)));
				int r = t - this.minecraft.font.width(string) / 2;
				if (bl2 && m < string.length()) {
					fill(poseStack, r, o - 1, r + 1, o + 9, 0xFF000000 | l);
				}

				if (n != m) {
					int s = Math.min(m, n);
					int u = Math.max(m, n);
					int v = this.minecraft.font.width(string.substring(0, s)) - this.minecraft.font.width(string) / 2;
					int w = this.minecraft.font.width(string.substring(0, u)) - this.minecraft.font.width(string) / 2;
					int x = Math.min(v, w);
					int y = Math.max(v, w);
					Tesselator tesselator = Tesselator.getInstance();
					BufferBuilder bufferBuilder = tesselator.getBuilder();
					RenderSystem.disableTexture();
					RenderSystem.enableColorLogicOp();
					RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
					bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
					bufferBuilder.vertex(matrix4f, (float)x, (float)(o + 9), 0.0F).color(0, 0, 255, 255).endVertex();
					bufferBuilder.vertex(matrix4f, (float)y, (float)(o + 9), 0.0F).color(0, 0, 255, 255).endVertex();
					bufferBuilder.vertex(matrix4f, (float)y, (float)o, 0.0F).color(0, 0, 255, 255).endVertex();
					bufferBuilder.vertex(matrix4f, (float)x, (float)o, 0.0F).color(0, 0, 255, 255).endVertex();
					bufferBuilder.end();
					BufferUploader.end(bufferBuilder);
					RenderSystem.disableColorLogicOp();
					RenderSystem.enableTexture();
				}
			}
		}

		poseStack.popPose();
		Lighting.setupFor3DItems();
		super.render(poseStack, i, j, f);
	}
}
