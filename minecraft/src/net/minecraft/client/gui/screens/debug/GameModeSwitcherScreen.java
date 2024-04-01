package net.minecraft.client.gui.screens.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class GameModeSwitcherScreen extends Screen {
	static final ResourceLocation SLOT_SPRITE = new ResourceLocation("gamemode_switcher/slot");
	static final ResourceLocation SELECTION_SPRITE = new ResourceLocation("gamemode_switcher/selection");
	private static final ResourceLocation GAMEMODE_SWITCHER_LOCATION = new ResourceLocation("textures/gui/container/gamemode_switcher.png");
	private static final int SPRITE_SHEET_WIDTH = 128;
	private static final int SPRITE_SHEET_HEIGHT = 128;
	private static final int SLOT_AREA = 26;
	private static final int SLOT_PADDING = 5;
	private static final int SLOT_AREA_PADDED = 31;
	private static final int HELP_TIPS_OFFSET_Y = 5;
	private static final int ALL_SLOTS_WIDTH = GameModeSwitcherScreen.GameModeIcon.values().length * 31 - 5;
	private static final Component SELECT_KEY = Component.translatable(
		"debug.gamemodes.select_next", Component.translatable("debug.gamemodes.press_f4").withStyle(ChatFormatting.AQUA)
	);
	private final GameModeSwitcherScreen.GameModeIcon previousHovered;
	private GameModeSwitcherScreen.GameModeIcon currentlyHovered;
	private int firstMouseX;
	private int firstMouseY;
	private boolean setFirstMousePos;
	private final List<GameModeSwitcherScreen.GameModeSlot> slots = Lists.<GameModeSwitcherScreen.GameModeSlot>newArrayList();

	public GameModeSwitcherScreen() {
		super(GameNarrator.NO_TITLE);
		this.previousHovered = GameModeSwitcherScreen.GameModeIcon.getFromGameType(this.getDefaultSelected());
		this.currentlyHovered = this.previousHovered;
	}

	private GameType getDefaultSelected() {
		MultiPlayerGameMode multiPlayerGameMode = Minecraft.getInstance().gameMode;
		GameType gameType = multiPlayerGameMode.getPreviousPlayerMode();
		if (gameType != null) {
			return gameType;
		} else {
			return multiPlayerGameMode.getPlayerMode() == GameType.CREATIVE ? GameType.SURVIVAL : GameType.CREATIVE;
		}
	}

	@Override
	protected void init() {
		super.init();
		this.currentlyHovered = this.previousHovered;

		for (int i = 0; i < GameModeSwitcherScreen.GameModeIcon.VALUES.length; i++) {
			GameModeSwitcherScreen.GameModeIcon gameModeIcon = GameModeSwitcherScreen.GameModeIcon.VALUES[i];
			this.slots.add(new GameModeSwitcherScreen.GameModeSlot(gameModeIcon, this.width / 2 - ALL_SLOTS_WIDTH / 2 + i * 31, this.height / 2 - 31));
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (!this.checkToClose()) {
			guiGraphics.pose().pushPose();
			RenderSystem.enableBlend();
			int k = this.width / 2 - 62;
			int l = this.height / 2 - 31 - 27;
			guiGraphics.blit(GAMEMODE_SWITCHER_LOCATION, k, l, 0.0F, 0.0F, 125, 75, 128, 128);
			guiGraphics.pose().popPose();
			super.render(guiGraphics, i, j, f);
			guiGraphics.drawCenteredString(this.font, this.currentlyHovered.getName(), this.width / 2, this.height / 2 - 31 - 20, -1);
			guiGraphics.drawCenteredString(this.font, SELECT_KEY, this.width / 2, this.height / 2 + 5, 16777215);
			if (!this.setFirstMousePos) {
				this.firstMouseX = i;
				this.firstMouseY = j;
				this.setFirstMousePos = true;
			}

			boolean bl = this.firstMouseX == i && this.firstMouseY == j;

			for (GameModeSwitcherScreen.GameModeSlot gameModeSlot : this.slots) {
				gameModeSlot.render(guiGraphics, i, j, f);
				gameModeSlot.setSelected(this.currentlyHovered == gameModeSlot.icon);
				if (!bl && gameModeSlot.isHoveredOrFocused()) {
					this.currentlyHovered = gameModeSlot.icon;
				}
			}
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
	}

	private void switchToHoveredGameMode() {
		switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
	}

	private static void switchToHoveredGameMode(Minecraft minecraft, GameModeSwitcherScreen.GameModeIcon gameModeIcon) {
		if (minecraft.gameMode != null && minecraft.player != null) {
			GameModeSwitcherScreen.GameModeIcon gameModeIcon2 = GameModeSwitcherScreen.GameModeIcon.getFromGameType(minecraft.gameMode.getPlayerMode());
			if (minecraft.player.hasPermissions(2) && gameModeIcon != gameModeIcon2) {
				minecraft.player.connection.sendUnsignedCommand(gameModeIcon.getCommand());
			}
		}
	}

	private boolean checkToClose() {
		if (!InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 292)) {
			this.switchToHoveredGameMode();
			this.minecraft.setScreen(null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 293) {
			this.setFirstMousePos = false;
			this.currentlyHovered = this.currentlyHovered.getNext();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	static enum GameModeIcon {
		CREATIVE(Component.translatable("gameMode.creative"), "gamemode creative", new ItemStack(Blocks.PEELGRASS_BLOCK)),
		SURVIVAL(Component.translatable("gameMode.survival"), "gamemode survival", new ItemStack(Items.IRON_SWORD)),
		ADVENTURE(Component.translatable("gameMode.adventure"), "gamemode adventure", new ItemStack(Items.MAP)),
		SPECTATOR(Component.translatable("gameMode.spectator"), "gamemode spectator", new ItemStack(Items.ENDER_EYE));

		protected static final GameModeSwitcherScreen.GameModeIcon[] VALUES = values();
		private static final int ICON_AREA = 16;
		protected static final int ICON_TOP_LEFT = 5;
		final Component name;
		final String command;
		final ItemStack renderStack;

		private GameModeIcon(Component component, String string2, ItemStack itemStack) {
			this.name = component;
			this.command = string2;
			this.renderStack = itemStack;
		}

		void drawIcon(GuiGraphics guiGraphics, int i, int j) {
			guiGraphics.renderItem(this.renderStack, i, j);
		}

		Component getName() {
			return this.name;
		}

		String getCommand() {
			return this.command;
		}

		GameModeSwitcherScreen.GameModeIcon getNext() {
			return switch (this) {
				case CREATIVE -> SURVIVAL;
				case SURVIVAL -> ADVENTURE;
				case ADVENTURE -> SPECTATOR;
				case SPECTATOR -> CREATIVE;
			};
		}

		static GameModeSwitcherScreen.GameModeIcon getFromGameType(GameType gameType) {
			return switch (gameType) {
				case SPECTATOR -> SPECTATOR;
				case SURVIVAL -> SURVIVAL;
				case CREATIVE -> CREATIVE;
				case ADVENTURE -> ADVENTURE;
			};
		}
	}

	@Environment(EnvType.CLIENT)
	public class GameModeSlot extends AbstractWidget {
		final GameModeSwitcherScreen.GameModeIcon icon;
		private boolean isSelected;

		public GameModeSlot(GameModeSwitcherScreen.GameModeIcon gameModeIcon, int i, int j) {
			super(i, j, 26, 26, gameModeIcon.getName());
			this.icon = gameModeIcon;
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			this.drawSlot(guiGraphics);
			this.icon.drawIcon(guiGraphics, this.getX() + 5, this.getY() + 5);
			if (this.isSelected) {
				this.drawSelection(guiGraphics);
			}
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);
		}

		@Override
		public boolean isHoveredOrFocused() {
			return super.isHoveredOrFocused() || this.isSelected;
		}

		public void setSelected(boolean bl) {
			this.isSelected = bl;
		}

		private void drawSlot(GuiGraphics guiGraphics) {
			guiGraphics.blitSprite(GameModeSwitcherScreen.SLOT_SPRITE, this.getX(), this.getY(), 26, 26);
		}

		private void drawSelection(GuiGraphics guiGraphics) {
			guiGraphics.blitSprite(GameModeSwitcherScreen.SELECTION_SPRITE, this.getX(), this.getY(), 26, 26);
		}
	}
}
