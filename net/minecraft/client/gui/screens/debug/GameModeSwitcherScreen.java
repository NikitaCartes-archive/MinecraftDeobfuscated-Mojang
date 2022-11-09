/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class GameModeSwitcherScreen
extends Screen {
    static final ResourceLocation GAMEMODE_SWITCHER_LOCATION = new ResourceLocation("textures/gui/container/gamemode_switcher.png");
    private static final int SPRITE_SHEET_WIDTH = 128;
    private static final int SPRITE_SHEET_HEIGHT = 128;
    private static final int SLOT_AREA = 26;
    private static final int SLOT_PADDING = 5;
    private static final int SLOT_AREA_PADDED = 31;
    private static final int HELP_TIPS_OFFSET_Y = 5;
    private static final int ALL_SLOTS_WIDTH = GameModeIcon.values().length * 31 - 5;
    private static final Component SELECT_KEY = Component.translatable("debug.gamemodes.select_next", Component.translatable("debug.gamemodes.press_f4").withStyle(ChatFormatting.AQUA));
    private final Optional<GameModeIcon> previousHovered;
    private Optional<GameModeIcon> currentlyHovered = Optional.empty();
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;
    private final List<GameModeSlot> slots = Lists.newArrayList();

    public GameModeSwitcherScreen() {
        super(GameNarrator.NO_TITLE);
        this.previousHovered = GameModeIcon.getFromGameType(this.getDefaultSelected());
    }

    private GameType getDefaultSelected() {
        MultiPlayerGameMode multiPlayerGameMode = Minecraft.getInstance().gameMode;
        GameType gameType = multiPlayerGameMode.getPreviousPlayerMode();
        if (gameType != null) {
            return gameType;
        }
        return multiPlayerGameMode.getPlayerMode() == GameType.CREATIVE ? GameType.SURVIVAL : GameType.CREATIVE;
    }

    @Override
    protected void init() {
        super.init();
        this.currentlyHovered = this.previousHovered.isPresent() ? this.previousHovered : GameModeIcon.getFromGameType(this.minecraft.gameMode.getPlayerMode());
        for (int i = 0; i < GameModeIcon.VALUES.length; ++i) {
            GameModeIcon gameModeIcon = GameModeIcon.VALUES[i];
            this.slots.add(new GameModeSlot(gameModeIcon, this.width / 2 - ALL_SLOTS_WIDTH / 2 + i * 31, this.height / 2 - 31));
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (this.checkToClose()) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, GAMEMODE_SWITCHER_LOCATION);
        int k = this.width / 2 - 62;
        int l = this.height / 2 - 31 - 27;
        GameModeSwitcherScreen.blit(poseStack, k, l, 0.0f, 0.0f, 125, 75, 128, 128);
        poseStack.popPose();
        super.render(poseStack, i, j, f);
        this.currentlyHovered.ifPresent(gameModeIcon -> GameModeSwitcherScreen.drawCenteredString(poseStack, this.font, gameModeIcon.getName(), this.width / 2, this.height / 2 - 31 - 20, -1));
        GameModeSwitcherScreen.drawCenteredString(poseStack, this.font, SELECT_KEY, this.width / 2, this.height / 2 + 5, 0xFFFFFF);
        if (!this.setFirstMousePos) {
            this.firstMouseX = i;
            this.firstMouseY = j;
            this.setFirstMousePos = true;
        }
        boolean bl = this.firstMouseX == i && this.firstMouseY == j;
        for (GameModeSlot gameModeSlot : this.slots) {
            gameModeSlot.render(poseStack, i, j, f);
            this.currentlyHovered.ifPresent(gameModeIcon -> gameModeSlot.setSelected(gameModeIcon == gameModeSlot.icon));
            if (bl || !gameModeSlot.isHoveredOrFocused()) continue;
            this.currentlyHovered = Optional.of(gameModeSlot.icon);
        }
    }

    private void switchToHoveredGameMode() {
        GameModeSwitcherScreen.switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
    }

    private static void switchToHoveredGameMode(Minecraft minecraft, Optional<GameModeIcon> optional) {
        if (minecraft.gameMode == null || minecraft.player == null || !optional.isPresent()) {
            return;
        }
        Optional<GameModeIcon> optional2 = GameModeIcon.getFromGameType(minecraft.gameMode.getPlayerMode());
        GameModeIcon gameModeIcon = optional.get();
        if (optional2.isPresent() && minecraft.player.hasPermissions(2) && gameModeIcon != optional2.get()) {
            minecraft.player.connection.sendUnsignedCommand(gameModeIcon.getCommand());
        }
    }

    private boolean checkToClose() {
        if (!InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 292)) {
            this.switchToHoveredGameMode();
            this.minecraft.setScreen(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 293 && this.currentlyHovered.isPresent()) {
            this.setFirstMousePos = false;
            this.currentlyHovered = this.currentlyHovered.get().getNext();
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    static enum GameModeIcon {
        CREATIVE(Component.translatable("gameMode.creative"), "gamemode creative", new ItemStack(Blocks.GRASS_BLOCK)),
        SURVIVAL(Component.translatable("gameMode.survival"), "gamemode survival", new ItemStack(Items.IRON_SWORD)),
        ADVENTURE(Component.translatable("gameMode.adventure"), "gamemode adventure", new ItemStack(Items.MAP)),
        SPECTATOR(Component.translatable("gameMode.spectator"), "gamemode spectator", new ItemStack(Items.ENDER_EYE));

        protected static final GameModeIcon[] VALUES;
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

        void drawIcon(ItemRenderer itemRenderer, int i, int j) {
            itemRenderer.renderAndDecorateItem(this.renderStack, i, j);
        }

        Component getName() {
            return this.name;
        }

        String getCommand() {
            return this.command;
        }

        Optional<GameModeIcon> getNext() {
            switch (this) {
                case CREATIVE: {
                    return Optional.of(SURVIVAL);
                }
                case SURVIVAL: {
                    return Optional.of(ADVENTURE);
                }
                case ADVENTURE: {
                    return Optional.of(SPECTATOR);
                }
            }
            return Optional.of(CREATIVE);
        }

        static Optional<GameModeIcon> getFromGameType(GameType gameType) {
            switch (gameType) {
                case SPECTATOR: {
                    return Optional.of(SPECTATOR);
                }
                case SURVIVAL: {
                    return Optional.of(SURVIVAL);
                }
                case CREATIVE: {
                    return Optional.of(CREATIVE);
                }
                case ADVENTURE: {
                    return Optional.of(ADVENTURE);
                }
            }
            return Optional.empty();
        }

        static {
            VALUES = GameModeIcon.values();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class GameModeSlot
    extends AbstractWidget {
        final GameModeIcon icon;
        private boolean isSelected;

        public GameModeSlot(GameModeIcon gameModeIcon, int i, int j) {
            super(i, j, 26, 26, gameModeIcon.getName());
            this.icon = gameModeIcon;
        }

        @Override
        public void renderButton(PoseStack poseStack, int i, int j, float f) {
            Minecraft minecraft = Minecraft.getInstance();
            this.drawSlot(poseStack, minecraft.getTextureManager());
            this.icon.drawIcon(GameModeSwitcherScreen.this.itemRenderer, this.getX() + 5, this.getY() + 5);
            if (this.isSelected) {
                this.drawSelection(poseStack, minecraft.getTextureManager());
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

        private void drawSlot(PoseStack poseStack, TextureManager textureManager) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GAMEMODE_SWITCHER_LOCATION);
            poseStack.pushPose();
            poseStack.translate(this.getX(), this.getY(), 0.0f);
            GameModeSlot.blit(poseStack, 0, 0, 0.0f, 75.0f, 26, 26, 128, 128);
            poseStack.popPose();
        }

        private void drawSelection(PoseStack poseStack, TextureManager textureManager) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GAMEMODE_SWITCHER_LOCATION);
            poseStack.pushPose();
            poseStack.translate(this.getX(), this.getY(), 0.0f);
            GameModeSlot.blit(poseStack, 0, 0, 26.0f, 75.0f, 26, 26, 128, 128);
            poseStack.popPose();
        }
    }
}

