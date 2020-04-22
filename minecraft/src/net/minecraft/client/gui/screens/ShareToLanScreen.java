package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public class ShareToLanScreen extends Screen {
	private final Screen lastScreen;
	private Button commandsButton;
	private Button modeButton;
	private String gameModeName = "survival";
	private boolean commands;

	public ShareToLanScreen(Screen screen) {
		super(new TranslatableComponent("lanServer.title"));
		this.lastScreen = screen;
	}

	@Override
	protected void init() {
		this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("lanServer.start"), button -> {
			this.minecraft.setScreen(null);
			int i = HttpUtil.getAvailablePort();
			Component component;
			if (this.minecraft.getSingleplayerServer().publishServer(GameType.byName(this.gameModeName), this.commands, i)) {
				component = new TranslatableComponent("commands.publish.started", i);
			} else {
				component = new TranslatableComponent("commands.publish.failed");
			}

			this.minecraft.gui.getChat().addMessage(component);
			this.minecraft.updateTitle();
		}));
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen)));
		this.modeButton = this.addButton(new Button(this.width / 2 - 155, 100, 150, 20, new TranslatableComponent("selectWorld.gameMode"), button -> {
			if ("spectator".equals(this.gameModeName)) {
				this.gameModeName = "creative";
			} else if ("creative".equals(this.gameModeName)) {
				this.gameModeName = "adventure";
			} else if ("adventure".equals(this.gameModeName)) {
				this.gameModeName = "survival";
			} else {
				this.gameModeName = "spectator";
			}

			this.updateSelectionStrings();
		}));
		this.commandsButton = this.addButton(new Button(this.width / 2 + 5, 100, 150, 20, new TranslatableComponent("selectWorld.allowCommands"), button -> {
			this.commands = !this.commands;
			this.updateSelectionStrings();
		}));
		this.updateSelectionStrings();
	}

	private void updateSelectionStrings() {
		this.modeButton
			.setMessage(new TranslatableComponent("selectWorld.gameMode").append(": ").append(new TranslatableComponent("selectWorld.gameMode." + this.gameModeName)));
		this.commandsButton.setMessage(new TranslatableComponent("selectWorld.allowCommands").append(" ").append(CommonComponents.optionStatus(this.commands)));
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
		this.drawCenteredString(poseStack, this.font, I18n.get("lanServer.otherPlayers"), this.width / 2, 82, 16777215);
		super.render(poseStack, i, j, f);
	}
}
