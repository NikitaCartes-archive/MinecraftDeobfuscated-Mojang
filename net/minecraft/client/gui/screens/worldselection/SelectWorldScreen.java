/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SelectWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final WorldOptions TEST_OPTIONS = new WorldOptions("test1".hashCode(), true, false);
    protected final Screen lastScreen;
    private Button deleteButton;
    private Button selectButton;
    private Button renameButton;
    private Button copyButton;
    protected EditBox searchBox;
    private WorldSelectionList list;

    public SelectWorldScreen(Screen screen) {
        super(Component.translatable("selectWorld.title"));
        this.lastScreen = screen;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        return super.mouseScrolled(d, e, f);
    }

    @Override
    public void tick() {
        this.searchBox.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, Component.translatable("selectWorld.search"));
        this.searchBox.setResponder(string -> this.list.updateFilter((String)string));
        this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, this.searchBox.getValue(), this.list);
        this.addWidget(this.searchBox);
        this.addWidget(this.list);
        this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.select"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld)).bounds(this.width / 2 - 154, this.height - 52, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"), button -> CreateWorldScreen.openFresh(this.minecraft, this)).bounds(this.width / 2 + 4, this.height - 52, 150, 20).build());
        this.renameButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld)).bounds(this.width / 2 - 154, this.height - 28, 72, 20).build());
        this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.delete"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld)).bounds(this.width / 2 - 76, this.height - 28, 72, 20).build());
        this.copyButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.recreate"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)).bounds(this.width / 2 + 4, this.height - 28, 72, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 82, this.height - 28, 72, 20).build());
        this.updateButtonStatus(false);
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        return this.searchBox.keyPressed(i, j, k);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public boolean charTyped(char c, int i) {
        return this.searchBox.charTyped(c, i);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.list.render(poseStack, i, j, f);
        this.searchBox.render(poseStack, i, j, f);
        SelectWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    public void updateButtonStatus(boolean bl) {
        this.selectButton.active = bl;
        this.deleteButton.active = bl;
        this.renameButton.active = bl;
        this.copyButton.active = bl;
    }

    @Override
    public void removed() {
        if (this.list != null) {
            this.list.children().forEach(WorldSelectionList.Entry::close);
        }
    }

    private /* synthetic */ void method_35739(Button button) {
        try {
            WorldSelectionList.WorldListEntry worldListEntry;
            WorldSelectionList.Entry entry;
            String string = "DEBUG world";
            if (!this.list.children().isEmpty() && (entry = (WorldSelectionList.Entry)this.list.children().get(0)) instanceof WorldSelectionList.WorldListEntry && (worldListEntry = (WorldSelectionList.WorldListEntry)entry).getLevelName().equals("DEBUG world")) {
                worldListEntry.doDeleteWorld();
            }
            LevelSettings levelSettings = new LevelSettings("DEBUG world", GameType.SPECTATOR, false, Difficulty.NORMAL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
            String string2 = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), "DEBUG world", "");
            this.minecraft.createWorldOpenFlows().createFreshLevel(string2, levelSettings, TEST_OPTIONS, WorldPresets::createNormalWorldDimensions);
        } catch (IOException iOException) {
            LOGGER.error("Failed to recreate the debug world", iOException);
        }
    }
}

