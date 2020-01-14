package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
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
		this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("lanServer.start"), button -> {
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
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel"), button -> this.minecraft.setScreen(this.lastScreen)));
		this.modeButton = this.addButton(new Button(this.width / 2 - 155, 100, 150, 20, I18n.get("selectWorld.gameMode"), button -> {
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
		this.commandsButton = this.addButton(new Button(this.width / 2 + 5, 100, 150, 20, I18n.get("selectWorld.allowCommands"), button -> {
			this.commands = !this.commands;
			this.updateSelectionStrings();
		}));
		this.updateSelectionStrings();
	}

	private void updateSelectionStrings() {
		this.modeButton.setMessage(I18n.get("selectWorld.gameMode") + ": " + I18n.get("selectWorld.gameMode." + this.gameModeName));
		this.commandsButton.setMessage(I18n.get("selectWorld.allowCommands") + ' ' + I18n.get(this.commands ? "options.on" : "options.off"));
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 50, 16777215);
		this.drawCenteredString(this.font, I18n.get("lanServer.otherPlayers"), this.width / 2, 82, 16777215);
		super.render(i, j, f);
	}
}
