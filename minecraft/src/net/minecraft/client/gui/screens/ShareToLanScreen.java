package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public class ShareToLanScreen extends Screen {
	private static final Component ALLOW_COMMANDS_LABEL = new TranslatableComponent("selectWorld.allowCommands");
	private static final Component GAME_MODE_LABEL = new TranslatableComponent("selectWorld.gameMode");
	private static final Component INFO_TEXT = new TranslatableComponent("lanServer.otherPlayers");
	private final Screen lastScreen;
	private GameType gameMode = GameType.SURVIVAL;
	private boolean commands;

	public ShareToLanScreen(Screen screen) {
		super(new TranslatableComponent("lanServer.title"));
		this.lastScreen = screen;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(
			CycleButton.<GameType>builder(GameType::getShortDisplayName)
				.withValues(GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE)
				.withInitialValue(this.gameMode)
				.create(this.width / 2 - 155, 100, 150, 20, GAME_MODE_LABEL, (cycleButton, gameType) -> this.gameMode = gameType)
		);
		this.addRenderableWidget(
			CycleButton.onOffBuilder(this.commands).create(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_LABEL, (cycleButton, boolean_) -> this.commands = boolean_)
		);
		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("lanServer.start"), button -> {
			this.minecraft.setScreen(null);
			int i = HttpUtil.getAvailablePort();
			Component component;
			if (this.minecraft.getSingleplayerServer().publishServer(this.gameMode, this.commands, i)) {
				component = new TranslatableComponent("commands.publish.started", i);
			} else {
				component = new TranslatableComponent("commands.publish.failed");
			}

			this.minecraft.gui.getChat().addMessage(component);
			this.minecraft.updateTitle();
		}));
		this.addRenderableWidget(
			new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen))
		);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
		drawCenteredString(poseStack, this.font, INFO_TEXT, this.width / 2, 82, 16777215);
		super.render(poseStack, i, j, f);
	}
}
