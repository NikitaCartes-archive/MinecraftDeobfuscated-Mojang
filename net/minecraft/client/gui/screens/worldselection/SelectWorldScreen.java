/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SelectWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final Screen lastScreen;
    @Nullable
    private List<FormattedCharSequence> toolTip;
    private Button deleteButton;
    private Button selectButton;
    private Button renameButton;
    private Button copyButton;
    protected EditBox searchBox;
    private WorldSelectionList list;

    public SelectWorldScreen(Screen screen) {
        super(new TranslatableComponent("selectWorld.title"));
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
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, new TranslatableComponent("selectWorld.search"));
        this.searchBox.setResponder(string -> this.list.refreshList(() -> string, false));
        this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, () -> this.searchBox.getValue(), this.list);
        this.addWidget(this.searchBox);
        this.addWidget(this.list);
        this.selectButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 52, 150, 20, new TranslatableComponent("selectWorld.select"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld)));
        this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 52, 150, 20, new TranslatableComponent("selectWorld.create"), button -> this.minecraft.setScreen(CreateWorldScreen.createFresh(this))));
        this.renameButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 28, 72, 20, new TranslatableComponent("selectWorld.edit"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld)));
        this.deleteButton = this.addRenderableWidget(new Button(this.width / 2 - 76, this.height - 28, 72, 20, new TranslatableComponent("selectWorld.delete"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld)));
        this.copyButton = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 28, 72, 20, new TranslatableComponent("selectWorld.recreate"), button -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)));
        this.addRenderableWidget(new Button(this.width / 2 + 82, this.height - 28, 72, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen)));
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
        this.toolTip = null;
        this.list.render(poseStack, i, j, f);
        this.searchBox.render(poseStack, i, j, f);
        SelectWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, i, j, f);
        if (this.toolTip != null) {
            this.renderTooltip(poseStack, this.toolTip, i, j);
        }
    }

    public void setToolTip(List<FormattedCharSequence> list) {
        this.toolTip = list;
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
            this.list.children().forEach(WorldSelectionList.WorldListEntry::close);
        }
    }

    private /* synthetic */ void method_35739(Button button) {
        try {
            WorldSelectionList.WorldListEntry worldListEntry;
            String string = "DEBUG world";
            if (!this.list.children().isEmpty() && (worldListEntry = (WorldSelectionList.WorldListEntry)this.list.children().get(0)).getLevelName().equals("DEBUG world")) {
                worldListEntry.doDeleteWorld();
            }
            RegistryAccess registryAccess = RegistryAccess.BUILTIN.get();
            long l = "test1".hashCode();
            WorldGenSettings worldGenSettings = WorldPreset.NORMAL.create(registryAccess, l, true, false);
            LevelSettings levelSettings = new LevelSettings("DEBUG world", GameType.SPECTATOR, false, Difficulty.NORMAL, true, new GameRules(), DataPackConfig.DEFAULT);
            String string2 = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), "DEBUG world", "");
            this.minecraft.createLevel(string2, levelSettings, registryAccess, worldGenSettings);
        } catch (IOException iOException) {
            LOGGER.error("Failed to recreate the debug world", iOException);
        }
    }
}

