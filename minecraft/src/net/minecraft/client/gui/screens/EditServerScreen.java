package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class EditServerScreen extends Screen {
	private static final Component NAME_LABEL = new TranslatableComponent("addServer.enterName");
	private static final Component IP_LABEL = new TranslatableComponent("addServer.enterIp");
	private Button addButton;
	private final BooleanConsumer callback;
	private final ServerData serverData;
	private EditBox ipEdit;
	private EditBox nameEdit;
	private final Screen lastScreen;

	public EditServerScreen(Screen screen, BooleanConsumer booleanConsumer, ServerData serverData) {
		super(new TranslatableComponent("addServer.title"));
		this.lastScreen = screen;
		this.callback = booleanConsumer;
		this.serverData = serverData;
	}

	@Override
	public void tick() {
		this.nameEdit.tick();
		this.ipEdit.tick();
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 66, 200, 20, new TranslatableComponent("addServer.enterName"));
		this.nameEdit.setFocus(true);
		this.nameEdit.setValue(this.serverData.name);
		this.nameEdit.setResponder(string -> this.updateAddButtonStatus());
		this.addWidget(this.nameEdit);
		this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 106, 200, 20, new TranslatableComponent("addServer.enterIp"));
		this.ipEdit.setMaxLength(128);
		this.ipEdit.setValue(this.serverData.ip);
		this.ipEdit.setResponder(string -> this.updateAddButtonStatus());
		this.addWidget(this.ipEdit);
		this.addRenderableWidget(
			CycleButton.<ServerData.ServerPackStatus>builder(ServerData.ServerPackStatus::getName)
				.withValues(ServerData.ServerPackStatus.values())
				.withInitialValue(this.serverData.getResourcePackStatus())
				.create(
					this.width / 2 - 100,
					this.height / 4 + 72,
					200,
					20,
					new TranslatableComponent("addServer.resourcePack"),
					(cycleButton, serverPackStatus) -> this.serverData.setResourcePackStatus(serverPackStatus)
				)
		);
		this.addButton = this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20, new TranslatableComponent("addServer.add"), button -> this.onAdd())
		);
		this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20, CommonComponents.GUI_CANCEL, button -> this.callback.accept(false))
		);
		this.updateAddButtonStatus();
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.ipEdit.getValue();
		String string2 = this.nameEdit.getValue();
		this.init(minecraft, i, j);
		this.ipEdit.setValue(string);
		this.nameEdit.setValue(string2);
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	private void onAdd() {
		this.serverData.name = this.nameEdit.getValue();
		this.serverData.ip = this.ipEdit.getValue();
		this.callback.accept(true);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	private void updateAddButtonStatus() {
		this.addButton.active = ServerAddress.isValidAddress(this.ipEdit.getValue()) && !this.nameEdit.getValue().isEmpty();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 16777215);
		drawString(poseStack, this.font, NAME_LABEL, this.width / 2 - 100, 53, 10526880);
		drawString(poseStack, this.font, IP_LABEL, this.width / 2 - 100, 94, 10526880);
		this.nameEdit.render(poseStack, i, j, f);
		this.ipEdit.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}
}
