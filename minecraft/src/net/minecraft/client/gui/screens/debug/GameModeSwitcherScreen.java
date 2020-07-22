package net.minecraft.client.gui.screens.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class GameModeSwitcherScreen extends Screen {
	private static final ResourceLocation GAMEMODE_SWITCHER_LOCATION = new ResourceLocation("textures/gui/container/gamemode_switcher.png");
	private static final int ALL_SLOTS_WIDTH = GameModeSwitcherScreen.GameModeIcon.values().length * 30 - 5;
	private static final Component SELECT_KEY = new TranslatableComponent(
		"debug.gamemodes.select_next", new TranslatableComponent("debug.gamemodes.press_f4").withStyle(ChatFormatting.AQUA)
	);
	private final Optional<GameModeSwitcherScreen.GameModeIcon> previousHovered;
	private Optional<GameModeSwitcherScreen.GameModeIcon> currentlyHovered = Optional.empty();
	private int firstMouseX;
	private int firstMouseY;
	private boolean setFirstMousePos;
	private final List<GameModeSwitcherScreen.GameModeSlot> slots = Lists.<GameModeSwitcherScreen.GameModeSlot>newArrayList();

	public GameModeSwitcherScreen() {
		super(NarratorChatListener.NO_TITLE);
		this.previousHovered = GameModeSwitcherScreen.GameModeIcon.getFromGameType(this.getDefaultSelected());
	}

	private GameType getDefaultSelected() {
		GameType gameType = Minecraft.getInstance().gameMode.getPlayerMode();
		GameType gameType2 = Minecraft.getInstance().gameMode.getPreviousPlayerMode();
		if (gameType2 == GameType.NOT_SET) {
			if (gameType == GameType.CREATIVE) {
				gameType2 = GameType.SURVIVAL;
			} else {
				gameType2 = GameType.CREATIVE;
			}
		}

		return gameType2;
	}

	@Override
	protected void init() {
		super.init();
		this.currentlyHovered = this.previousHovered.isPresent()
			? this.previousHovered
			: GameModeSwitcherScreen.GameModeIcon.getFromGameType(this.minecraft.gameMode.getPlayerMode());

		for (int i = 0; i < GameModeSwitcherScreen.GameModeIcon.VALUES.length; i++) {
			GameModeSwitcherScreen.GameModeIcon gameModeIcon = GameModeSwitcherScreen.GameModeIcon.VALUES[i];
			this.slots.add(new GameModeSwitcherScreen.GameModeSlot(gameModeIcon, this.width / 2 - ALL_SLOTS_WIDTH / 2 + i * 30, this.height / 2 - 30));
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (!this.checkToClose()) {
			poseStack.pushPose();
			RenderSystem.enableBlend();
			this.minecraft.getTextureManager().bind(GAMEMODE_SWITCHER_LOCATION);
			int k = this.width / 2 - 62;
			int l = this.height / 2 - 30 - 27;
			blit(poseStack, k, l, 0.0F, 0.0F, 125, 75, 128, 128);
			poseStack.popPose();
			super.render(poseStack, i, j, f);
			this.currentlyHovered
				.ifPresent(gameModeIcon -> drawCenteredString(poseStack, this.font, gameModeIcon.getName(), this.width / 2, this.height / 2 - 30 - 20, -1));
			drawCenteredString(poseStack, this.font, SELECT_KEY, this.width / 2, this.height / 2 + 5, 16777215);
			if (!this.setFirstMousePos) {
				this.firstMouseX = i;
				this.firstMouseY = j;
				this.setFirstMousePos = true;
			}

			boolean bl = this.firstMouseX == i && this.firstMouseY == j;

			for (GameModeSwitcherScreen.GameModeSlot gameModeSlot : this.slots) {
				gameModeSlot.render(poseStack, i, j, f);
				this.currentlyHovered.ifPresent(gameModeIcon -> gameModeSlot.setSelected(gameModeIcon == gameModeSlot.icon));
				if (!bl && gameModeSlot.isHovered()) {
					this.currentlyHovered = Optional.of(gameModeSlot.icon);
				}
			}
		}
	}

	private void switchToHoveredGameMode() {
		switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
	}

	private static void switchToHoveredGameMode(Minecraft minecraft, Optional<GameModeSwitcherScreen.GameModeIcon> optional) {
		if (minecraft.gameMode != null && minecraft.player != null && optional.isPresent()) {
			Optional<GameModeSwitcherScreen.GameModeIcon> optional2 = GameModeSwitcherScreen.GameModeIcon.getFromGameType(minecraft.gameMode.getPlayerMode());
			GameModeSwitcherScreen.GameModeIcon gameModeIcon = (GameModeSwitcherScreen.GameModeIcon)optional.get();
			if (optional2.isPresent() && minecraft.player.hasPermissions(2) && gameModeIcon != optional2.get()) {
				minecraft.player.chat(gameModeIcon.getCommand());
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
		if (i == 293 && this.currentlyHovered.isPresent()) {
			this.setFirstMousePos = false;
			this.currentlyHovered = ((GameModeSwitcherScreen.GameModeIcon)this.currentlyHovered.get()).getNext();
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
		CREATIVE(new TranslatableComponent("gameMode.creative"), "/gamemode creative", new ItemStack(Blocks.GRASS_BLOCK)),
		SURVIVAL(new TranslatableComponent("gameMode.survival"), "/gamemode survival", new ItemStack(Items.IRON_SWORD)),
		ADVENTURE(new TranslatableComponent("gameMode.adventure"), "/gamemode adventure", new ItemStack(Items.MAP)),
		SPECTATOR(new TranslatableComponent("gameMode.spectator"), "/gamemode spectator", new ItemStack(Items.ENDER_EYE));

		protected static final GameModeSwitcherScreen.GameModeIcon[] VALUES = values();
		final Component name;
		final String command;
		final ItemStack renderStack;

		private GameModeIcon(Component component, String string2, ItemStack itemStack) {
			this.name = component;
			this.command = string2;
			this.renderStack = itemStack;
		}

		private void drawIcon(ItemRenderer itemRenderer, int i, int j) {
			itemRenderer.renderAndDecorateItem(this.renderStack, i, j);
		}

		private Component getName() {
			return this.name;
		}

		private String getCommand() {
			return this.command;
		}

		private Optional<GameModeSwitcherScreen.GameModeIcon> getNext() {
			switch (this) {
				case CREATIVE:
					return Optional.of(SURVIVAL);
				case SURVIVAL:
					return Optional.of(ADVENTURE);
				case ADVENTURE:
					return Optional.of(SPECTATOR);
				default:
					return Optional.of(CREATIVE);
			}
		}

		private static Optional<GameModeSwitcherScreen.GameModeIcon> getFromGameType(GameType gameType) {
			switch (gameType) {
				case SPECTATOR:
					return Optional.of(SPECTATOR);
				case SURVIVAL:
					return Optional.of(SURVIVAL);
				case CREATIVE:
					return Optional.of(CREATIVE);
				case ADVENTURE:
					return Optional.of(ADVENTURE);
				default:
					return Optional.empty();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public class GameModeSlot extends AbstractWidget {
		private final GameModeSwitcherScreen.GameModeIcon icon;
		private boolean isSelected;

		public GameModeSlot(GameModeSwitcherScreen.GameModeIcon gameModeIcon, int i, int j) {
			super(i, j, 25, 25, gameModeIcon.getName());
			this.icon = gameModeIcon;
		}

		@Override
		public void renderButton(PoseStack poseStack, int i, int j, float f) {
			Minecraft minecraft = Minecraft.getInstance();
			this.drawSlot(poseStack, minecraft.getTextureManager());
			this.icon.drawIcon(GameModeSwitcherScreen.this.itemRenderer, this.x + 5, this.y + 5);
			if (this.isSelected) {
				this.drawSelection(poseStack, minecraft.getTextureManager());
			}
		}

		@Override
		public boolean isHovered() {
			return super.isHovered() || this.isSelected;
		}

		public void setSelected(boolean bl) {
			this.isSelected = bl;
			this.narrate();
		}

		private void drawSlot(PoseStack poseStack, TextureManager textureManager) {
			textureManager.bind(GameModeSwitcherScreen.GAMEMODE_SWITCHER_LOCATION);
			poseStack.pushPose();
			poseStack.translate((double)this.x, (double)this.y, 0.0);
			blit(poseStack, 0, 0, 0.0F, 75.0F, 25, 25, 128, 128);
			poseStack.popPose();
		}

		private void drawSelection(PoseStack poseStack, TextureManager textureManager) {
			textureManager.bind(GameModeSwitcherScreen.GAMEMODE_SWITCHER_LOCATION);
			poseStack.pushPose();
			poseStack.translate((double)this.x, (double)this.y, 0.0);
			blit(poseStack, 0, 0, 25.0F, 75.0F, 25, 25, 128, 128);
			poseStack.popPose();
		}
	}
}
