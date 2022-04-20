package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DirectJoinServerScreen extends Screen {
	private static final Component ENTER_IP_LABEL = Component.translatable("addServer.enterIp");
	private Button selectButton;
	private final ServerData serverData;
	private EditBox ipEdit;
	private final BooleanConsumer callback;
	private final Screen lastScreen;

	public DirectJoinServerScreen(Screen screen, BooleanConsumer booleanConsumer, ServerData serverData) {
		super(Component.translatable("selectServer.direct"));
		this.lastScreen = screen;
		this.serverData = serverData;
		this.callback = booleanConsumer;
	}

	@Override
	public void tick() {
		this.ipEdit.tick();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (!this.selectButton.active || this.getFocused() != this.ipEdit || i != 257 && i != 335) {
			return super.keyPressed(i, j, k);
		} else {
			this.onSelect();
			return true;
		}
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.selectButton = this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20, Component.translatable("selectServer.select"), button -> this.onSelect())
		);
		this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, button -> this.callback.accept(false))
		);
		this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 116, 200, 20, Component.translatable("addServer.enterIp"));
		this.ipEdit.setMaxLength(128);
		this.ipEdit.setFocus(true);
		this.ipEdit.setValue(this.minecraft.options.lastMpIp);
		this.ipEdit.setResponder(string -> this.updateSelectButtonStatus());
		this.addWidget(this.ipEdit);
		this.setInitialFocus(this.ipEdit);
		this.updateSelectButtonStatus();
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.ipEdit.getValue();
		this.init(minecraft, i, j);
		this.ipEdit.setValue(string);
	}

	private void onSelect() {
		this.serverData.ip = this.ipEdit.getValue();
		this.callback.accept(true);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
		this.minecraft.options.lastMpIp = this.ipEdit.getValue();
		this.minecraft.options.save();
	}

	private void updateSelectButtonStatus() {
		this.selectButton.active = ServerAddress.isValidAddress(this.ipEdit.getValue());
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 16777215);
		drawString(poseStack, this.font, ENTER_IP_LABEL, this.width / 2 - 100, 100, 10526880);
		this.ipEdit.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}
}
