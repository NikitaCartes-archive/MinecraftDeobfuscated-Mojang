package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class SignEditScreen extends Screen {
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
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)(this.width / 2), 0.0F, 50.0F);
		float g = 93.75F;
		RenderSystem.scalef(-93.75F, -93.75F, -93.75F);
		RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
		BlockState blockState = this.sign.getBlockState();
		float h;
		if (blockState.getBlock() instanceof StandingSignBlock) {
			h = (float)((Integer)blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F;
		} else {
			h = ((Direction)blockState.getValue(WallSignBlock.FACING)).toYRot();
		}

		RenderSystem.rotatef(h, 0.0F, 1.0F, 0.0F);
		RenderSystem.translatef(0.0F, -1.0625F, 0.0F);
		this.sign.setCursorInfo(this.line, this.signField.getCursorPos(), this.signField.getSelectionPos(), this.frame / 6 % 2 == 0);
		RenderSystem.translatef(-0.5F, -0.75F, -0.5F);
		BlockEntityRenderDispatcher.instance.renderItem(this.sign, new PoseStack());
		this.sign.resetCursorInfo();
		RenderSystem.popMatrix();
		super.render(i, j, f);
	}
}
