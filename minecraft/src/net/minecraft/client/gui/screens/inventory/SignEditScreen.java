package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
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

	public SignEditScreen(SignBlockEntity signBlockEntity) {
		super(new TranslatableComponent("sign.edit"));
		this.sign = signBlockEntity;
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, I18n.get("gui.done"), button -> this.onDone()));
		this.sign.setEditable(false);
		this.signField = new TextFieldHelper(
			this.minecraft, () -> this.sign.getMessage(this.line).getString(), string -> this.sign.setMessage(this.line, new TextComponent(string)), 90
		);
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener != null) {
			clientPacketListener.send(
				new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.sign.getMessage(0), this.sign.getMessage(1), this.sign.getMessage(2), this.sign.getMessage(3))
			);
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
			this.signField.setEnd();
			return true;
		} else if (i == 264 || i == 257 || i == 335) {
			this.line = this.line + 1 & 3;
			this.signField.setEnd();
			return true;
		} else {
			return this.signField.keyPressed(i) ? true : super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 40, 16777215);
		PoseStack poseStack = new PoseStack();
		poseStack.pushPose();
		poseStack.translate((double)(this.width / 2), 0.0, 50.0);
		float g = 93.75F;
		poseStack.scale(-93.75F, -93.75F, -93.75F);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
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
		TextureAtlasSprite textureAtlasSprite = this.minecraft.getTextureAtlas().getSprite(SignRenderer.getTexture(blockState.getBlock()));
		VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.cutout());
		this.signModel.sign.render(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, textureAtlasSprite);
		if (bl) {
			this.signModel.stick.render(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, textureAtlasSprite);
		}

		poseStack.popPose();
		float k = 0.010416667F;
		poseStack.translate(0.0, 0.33333334F, 0.046666667F);
		poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
		int l = this.sign.getColor().getTextColor();
		String[] strings = new String[4];

		for (int m = 0; m < strings.length; m++) {
			strings[m] = this.sign.getRenderMessage(m, component -> {
				List<Component> list = ComponentRenderUtils.wrapComponents(component, 90, this.minecraft.font, false, true);
				return list.isEmpty() ? "" : ((Component)list.get(0)).getColoredString();
			});
		}

		Matrix4f matrix4f = poseStack.last().pose();
		int n = this.signField.getCursorPos();
		int o = this.signField.getSelectionPos();
		int p = this.minecraft.font.isBidirectional() ? -1 : 1;
		int q = this.line * 10 - this.sign.messages.length * 5;

		for (int r = 0; r < strings.length; r++) {
			String string = strings[r];
			if (string != null) {
				float s = (float)(-this.minecraft.font.width(string) / 2);
				this.minecraft.font.drawInBatch(string, s, (float)(r * 10 - this.sign.messages.length * 5), l, false, matrix4f, bufferSource, false, 0, 15728880);
				if (r == this.line && n >= 0 && bl2) {
					int t = this.minecraft.font.width(string.substring(0, Math.max(Math.min(n, string.length()), 0)));
					int u = (t - this.minecraft.font.width(string) / 2) * p;
					if (n >= string.length()) {
						this.minecraft.font.drawInBatch("_", (float)u, (float)q, l, false, matrix4f, bufferSource, false, 0, 15728880);
					}
				}
			}
		}

		bufferSource.endBatch();

		for (int rx = 0; rx < strings.length; rx++) {
			String string = strings[rx];
			if (string != null && rx == this.line && n >= 0) {
				int v = this.minecraft.font.width(string.substring(0, Math.max(Math.min(n, string.length()), 0)));
				int t = (v - this.minecraft.font.width(string) / 2) * p;
				if (bl2 && n < string.length()) {
					fill(matrix4f, t, q - 1, t + 1, q + 9, 0xFF000000 | l);
				}

				if (o != n) {
					int u = Math.min(n, o);
					int w = Math.max(n, o);
					int x = (this.minecraft.font.width(string.substring(0, u)) - this.minecraft.font.width(string) / 2) * p;
					int y = (this.minecraft.font.width(string.substring(0, w)) - this.minecraft.font.width(string) / 2) * p;
					int z = Math.min(x, y);
					int aa = Math.max(x, y);
					Tesselator tesselator = Tesselator.getInstance();
					BufferBuilder bufferBuilder = tesselator.getBuilder();
					RenderSystem.disableTexture();
					RenderSystem.enableColorLogicOp();
					RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
					bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
					bufferBuilder.vertex(matrix4f, (float)z, (float)(q + 9), 0.0F).color(0, 0, 255, 255).endVertex();
					bufferBuilder.vertex(matrix4f, (float)aa, (float)(q + 9), 0.0F).color(0, 0, 255, 255).endVertex();
					bufferBuilder.vertex(matrix4f, (float)aa, (float)q, 0.0F).color(0, 0, 255, 255).endVertex();
					bufferBuilder.vertex(matrix4f, (float)z, (float)q, 0.0F).color(0, 0, 255, 255).endVertex();
					bufferBuilder.end();
					BufferUploader.end(bufferBuilder);
					RenderSystem.disableColorLogicOp();
					RenderSystem.enableTexture();
				}
			}
		}

		poseStack.popPose();
		super.render(i, j, f);
	}
}
