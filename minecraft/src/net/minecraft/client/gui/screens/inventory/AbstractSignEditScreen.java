package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class AbstractSignEditScreen extends Screen {
	private final SignBlockEntity sign;
	private SignText text;
	private final String[] messages;
	private final boolean isFrontText;
	protected final WoodType woodType;
	private int frame;
	private int line;
	@Nullable
	private TextFieldHelper signField;

	public AbstractSignEditScreen(SignBlockEntity signBlockEntity, boolean bl, boolean bl2) {
		this(signBlockEntity, bl, bl2, Component.translatable("sign.edit"));
	}

	public AbstractSignEditScreen(SignBlockEntity signBlockEntity, boolean bl, boolean bl2, Component component) {
		super(component);
		this.sign = signBlockEntity;
		this.text = signBlockEntity.getText(bl);
		this.isFrontText = bl;
		this.woodType = SignBlock.getWoodType(signBlockEntity.getBlockState().getBlock());
		this.messages = (String[])IntStream.range(0, 4).mapToObj(i -> this.text.getMessage(i, bl2)).map(Component::getString).toArray(String[]::new);
	}

	@Override
	protected void init() {
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build()
		);
		this.signField = new TextFieldHelper(
			() -> this.messages[this.line],
			this::setMessage,
			TextFieldHelper.createClipboardGetter(this.minecraft),
			TextFieldHelper.createClipboardSetter(this.minecraft),
			string -> this.minecraft.font.width(string) <= this.sign.getMaxTextLineWidth()
		);
	}

	@Override
	public void tick() {
		this.frame++;
		if (!this.isValid()) {
			this.onDone();
		}
	}

	private boolean isValid() {
		return this.minecraft != null
			&& this.minecraft.player != null
			&& !this.sign.isRemoved()
			&& !this.sign.playerIsTooFarAwayToEdit(this.minecraft.player.getUUID());
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
	public boolean charTyped(char c, int i) {
		this.signField.charTyped(c);
		return true;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		Lighting.setupForFlatItems();
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
		this.renderSign(guiGraphics);
		Lighting.setupFor3DItems();
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderTransparentBackground(guiGraphics);
	}

	@Override
	public void onClose() {
		this.onDone();
	}

	@Override
	public void removed() {
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener != null) {
			clientPacketListener.send(
				new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.isFrontText, this.messages[0], this.messages[1], this.messages[2], this.messages[3])
			);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected abstract void renderSignBackground(GuiGraphics guiGraphics, BlockState blockState);

	protected abstract Vector3f getSignTextScale();

	protected void offsetSign(GuiGraphics guiGraphics, BlockState blockState) {
		guiGraphics.pose().translate((float)this.width / 2.0F, 90.0F, 50.0F);
	}

	private void renderSign(GuiGraphics guiGraphics) {
		BlockState blockState = this.sign.getBlockState();
		guiGraphics.pose().pushPose();
		this.offsetSign(guiGraphics, blockState);
		guiGraphics.pose().pushPose();
		this.renderSignBackground(guiGraphics, blockState);
		guiGraphics.pose().popPose();
		this.renderSignText(guiGraphics);
		guiGraphics.pose().popPose();
	}

	private void renderSignText(GuiGraphics guiGraphics) {
		guiGraphics.pose().translate(0.0F, 0.0F, 4.0F);
		Vector3f vector3f = this.getSignTextScale();
		guiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());
		int i = this.text.hasGlowingText() ? this.text.getColor().getTextColor() : SignRenderer.getDarkColor(this.text);
		boolean bl = this.frame / 6 % 2 == 0;
		int j = this.signField.getCursorPos();
		int k = this.signField.getSelectionPos();
		int l = 4 * this.sign.getTextLineHeight() / 2;
		int m = this.line * this.sign.getTextLineHeight() - l;

		for (int n = 0; n < this.messages.length; n++) {
			String string = this.messages[n];
			if (string != null) {
				if (this.font.isBidirectional()) {
					string = this.font.bidirectionalShaping(string);
				}

				int o = -this.font.width(string) / 2;
				guiGraphics.drawString(this.font, string, o, n * this.sign.getTextLineHeight() - l, i, false);
				if (n == this.line && j >= 0 && bl) {
					int p = this.font.width(string.substring(0, Math.max(Math.min(j, string.length()), 0)));
					int q = p - this.font.width(string) / 2;
					if (j >= string.length()) {
						guiGraphics.drawString(this.font, "_", q, m, i, false);
					}
				}
			}
		}

		for (int nx = 0; nx < this.messages.length; nx++) {
			String string = this.messages[nx];
			if (string != null && nx == this.line && j >= 0) {
				int o = this.font.width(string.substring(0, Math.max(Math.min(j, string.length()), 0)));
				int p = o - this.font.width(string) / 2;
				if (bl && j < string.length()) {
					guiGraphics.fill(p, m - 1, p + 1, m + this.sign.getTextLineHeight(), 0xFF000000 | i);
				}

				if (k != j) {
					int q = Math.min(j, k);
					int r = Math.max(j, k);
					int s = this.font.width(string.substring(0, q)) - this.font.width(string) / 2;
					int t = this.font.width(string.substring(0, r)) - this.font.width(string) / 2;
					int u = Math.min(s, t);
					int v = Math.max(s, t);
					guiGraphics.fill(RenderType.guiTextHighlight(), u, m, v, m + this.sign.getTextLineHeight(), -16776961);
				}
			}
		}
	}

	private void setMessage(String string) {
		this.messages[this.line] = string;
		this.text = this.text.setMessage(this.line, Component.literal(string));
		this.sign.setText(this.text, this.isFrontText);
	}

	private void onDone() {
		this.minecraft.setScreen(null);
	}
}
