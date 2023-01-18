package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class AbstractSignEditScreen extends Screen {
	protected final SignBlockEntity sign;
	protected final String[] messages;
	protected final WoodType woodType;
	private int frame;
	private int line;
	private TextFieldHelper signField;

	public AbstractSignEditScreen(SignBlockEntity signBlockEntity, boolean bl) {
		this(signBlockEntity, bl, Component.translatable("sign.edit"));
	}

	public AbstractSignEditScreen(SignBlockEntity signBlockEntity, boolean bl, Component component) {
		super(component);
		this.woodType = SignBlock.getWoodType(signBlockEntity.getBlockState().getBlock());
		this.messages = (String[])IntStream.range(0, 4).mapToObj(i -> signBlockEntity.getMessage(i, bl)).map(Component::getString).toArray(String[]::new);
		this.sign = signBlockEntity;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 120, 200, 20).build()
		);
		this.sign.setEditable(false);
		this.signField = new TextFieldHelper(
			() -> this.messages[this.line],
			string -> {
				this.messages[this.line] = string;
				this.sign.setMessage(this.line, Component.literal(string));
			},
			TextFieldHelper.createClipboardGetter(this.minecraft),
			TextFieldHelper.createClipboardSetter(this.minecraft),
			string -> this.minecraft.font.width(string) <= this.sign.getMaxTextLineWidth()
		);
	}

	@Override
	public void removed() {
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener != null) {
			clientPacketListener.send(new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.messages[0], this.messages[1], this.messages[2], this.messages[3]));
		}

		this.sign.setEditable(true);
	}

	@Override
	public void tick() {
		this.frame++;
		if (!this.sign.getType().isValid(this.sign.getBlockState())) {
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
		this.renderSign(poseStack);
		Lighting.setupFor3DItems();
		super.render(poseStack, i, j, f);
	}

	protected abstract void renderSignBackground(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockState blockState);

	protected abstract Vector3f getSignTextScale();

	protected void offsetSign(PoseStack poseStack, BlockState blockState) {
		poseStack.translate((float)this.width / 2.0F, 90.0F, 50.0F);
	}

	private void renderSign(PoseStack poseStack) {
		MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
		BlockState blockState = this.sign.getBlockState();
		poseStack.pushPose();
		this.offsetSign(poseStack, blockState);
		poseStack.pushPose();
		this.renderSignBackground(poseStack, bufferSource, blockState);
		poseStack.popPose();
		this.renderSignText(poseStack, bufferSource);
		poseStack.popPose();
	}

	private void renderSignText(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
		poseStack.translate(0.0F, 0.0F, 4.0F);
		Vector3f vector3f = this.getSignTextScale();
		poseStack.scale(vector3f.x(), vector3f.y(), vector3f.z());
		int i = this.sign.getColor().getTextColor();
		boolean bl = this.frame / 6 % 2 == 0;
		int j = this.signField.getCursorPos();
		int k = this.signField.getSelectionPos();
		int l = 4 * this.sign.getTextLineHeight() / 2;
		int m = this.line * this.sign.getTextLineHeight() - l;
		Matrix4f matrix4f = poseStack.last().pose();

		for (int n = 0; n < this.messages.length; n++) {
			String string = this.messages[n];
			if (string != null) {
				if (this.font.isBidirectional()) {
					string = this.font.bidirectionalShaping(string);
				}

				float f = (float)(-this.minecraft.font.width(string) / 2);
				this.minecraft.font.drawInBatch(string, f, (float)(n * this.sign.getTextLineHeight() - l), i, false, matrix4f, bufferSource, false, 0, 15728880, false);
				if (n == this.line && j >= 0 && bl) {
					int o = this.minecraft.font.width(string.substring(0, Math.max(Math.min(j, string.length()), 0)));
					int p = o - this.minecraft.font.width(string) / 2;
					if (j >= string.length()) {
						this.minecraft.font.drawInBatch("_", (float)p, (float)m, i, false, matrix4f, bufferSource, false, 0, 15728880, false);
					}
				}
			}
		}

		bufferSource.endBatch();

		for (int nx = 0; nx < this.messages.length; nx++) {
			String string = this.messages[nx];
			if (string != null && nx == this.line && j >= 0) {
				int q = this.minecraft.font.width(string.substring(0, Math.max(Math.min(j, string.length()), 0)));
				int o = q - this.minecraft.font.width(string) / 2;
				if (bl && j < string.length()) {
					fill(poseStack, o, m - 1, o + 1, m + this.sign.getTextLineHeight(), 0xFF000000 | i);
				}

				if (k != j) {
					int p = Math.min(j, k);
					int r = Math.max(j, k);
					int s = this.minecraft.font.width(string.substring(0, p)) - this.minecraft.font.width(string) / 2;
					int t = this.minecraft.font.width(string.substring(0, r)) - this.minecraft.font.width(string) / 2;
					int u = Math.min(s, t);
					int v = Math.max(s, t);
					RenderSystem.enableColorLogicOp();
					RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
					fill(poseStack, u, m, v, m + this.sign.getTextLineHeight(), -16776961);
					RenderSystem.disableColorLogicOp();
				}
			}
		}
	}
}
