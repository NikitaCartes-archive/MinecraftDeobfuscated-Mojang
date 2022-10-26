/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsUploadScreen;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsSelectFileToUploadScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Component WORLD_TEXT = Component.translatable("selectWorld.world");
    static final Component HARDCORE_TEXT = Component.translatable("mco.upload.hardcore").withStyle(ChatFormatting.DARK_RED);
    static final Component CHEATS_TEXT = Component.translatable("selectWorld.cheats");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private final RealmsResetWorldScreen lastScreen;
    private final long worldId;
    private final int slotId;
    Button uploadButton;
    List<LevelSummary> levelList = Lists.newArrayList();
    int selectedWorld = -1;
    WorldSelectionList worldSelectionList;
    private final Runnable callback;

    public RealmsSelectFileToUploadScreen(long l, int i, RealmsResetWorldScreen realmsResetWorldScreen, Runnable runnable) {
        super(Component.translatable("mco.upload.select.world.title"));
        this.lastScreen = realmsResetWorldScreen;
        this.worldId = l;
        this.slotId = i;
        this.callback = runnable;
    }

    private void loadLevelList() throws Exception {
        LevelStorageSource.LevelCandidates levelCandidates = this.minecraft.getLevelSource().findLevelCandidates();
        this.levelList = this.minecraft.getLevelSource().loadLevelSummaries(levelCandidates).join().stream().filter(levelSummary -> !levelSummary.requiresManualConversion() && !levelSummary.isLocked()).collect(Collectors.toList());
        for (LevelSummary levelSummary2 : this.levelList) {
            this.worldSelectionList.addEntry(levelSummary2);
        }
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.worldSelectionList = new WorldSelectionList();
        try {
            this.loadLevelList();
        } catch (Exception exception) {
            LOGGER.error("Couldn't load level list", exception);
            this.minecraft.setScreen(new RealmsGenericErrorScreen(Component.literal("Unable to load worlds"), Component.nullToEmpty(exception.getMessage()), this.lastScreen));
            return;
        }
        this.addWidget(this.worldSelectionList);
        this.uploadButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.upload.button.name"), button -> this.upload()).bounds(this.width / 2 - 154, this.height - 32, 153, 20).build());
        this.uploadButton.active = this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 6, this.height - 32, 153, 20).build());
        this.addLabel(new RealmsLabel(Component.translatable("mco.upload.select.world.subtitle"), this.width / 2, RealmsSelectFileToUploadScreen.row(-1), 0xA0A0A0));
        if (this.levelList.isEmpty()) {
            this.addLabel(new RealmsLabel(Component.translatable("mco.upload.select.world.none"), this.width / 2, this.height / 2 - 20, 0xFFFFFF));
        }
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void upload() {
        if (this.selectedWorld != -1 && !this.levelList.get(this.selectedWorld).isHardcore()) {
            LevelSummary levelSummary = this.levelList.get(this.selectedWorld);
            this.minecraft.setScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.lastScreen, levelSummary, this.callback));
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.worldSelectionList.render(poseStack, i, j, f);
        RealmsSelectFileToUploadScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 13, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    static Component gameModeName(LevelSummary levelSummary) {
        return levelSummary.getGameMode().getLongDisplayName();
    }

    static String formatLastPlayed(LevelSummary levelSummary) {
        return DATE_FORMAT.format(new Date(levelSummary.getLastPlayed()));
    }

    @Environment(value=EnvType.CLIENT)
    class WorldSelectionList
    extends RealmsObjectSelectionList<Entry> {
        public WorldSelectionList() {
            super(RealmsSelectFileToUploadScreen.this.width, RealmsSelectFileToUploadScreen.this.height, RealmsSelectFileToUploadScreen.row(0), RealmsSelectFileToUploadScreen.this.height - 40, 36);
        }

        public void addEntry(LevelSummary levelSummary) {
            this.addEntry(new Entry(levelSummary));
        }

        @Override
        public int getMaxPosition() {
            return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
        }

        @Override
        public boolean isFocused() {
            return RealmsSelectFileToUploadScreen.this.getFocused() == this;
        }

        @Override
        public void renderBackground(PoseStack poseStack) {
            RealmsSelectFileToUploadScreen.this.renderBackground(poseStack);
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            RealmsSelectFileToUploadScreen.this.selectedWorld = this.children().indexOf(entry);
            RealmsSelectFileToUploadScreen.this.uploadButton.active = RealmsSelectFileToUploadScreen.this.selectedWorld >= 0 && RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount() && !RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld).isHardcore();
        }
    }

    @Environment(value=EnvType.CLIENT)
    class Entry
    extends ObjectSelectionList.Entry<Entry> {
        private final LevelSummary levelSummary;
        private final String name;
        private final String id;
        private final Component info;

        public Entry(LevelSummary levelSummary) {
            this.levelSummary = levelSummary;
            this.name = levelSummary.getLevelName();
            this.id = levelSummary.getLevelId() + " (" + RealmsSelectFileToUploadScreen.formatLastPlayed(levelSummary) + ")";
            Component component = levelSummary.isHardcore() ? HARDCORE_TEXT : RealmsSelectFileToUploadScreen.gameModeName(levelSummary);
            if (levelSummary.hasCheats()) {
                component = component.copy().append(", ").append(CHEATS_TEXT);
            }
            this.info = component;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.renderItem(poseStack, i, k, j);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            RealmsSelectFileToUploadScreen.this.worldSelectionList.selectItem(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.levelSummary));
            return true;
        }

        protected void renderItem(PoseStack poseStack, int i, int j, int k) {
            Object string = this.name.isEmpty() ? WORLD_TEXT + " " + (i + 1) : this.name;
            RealmsSelectFileToUploadScreen.this.font.draw(poseStack, (String)string, (float)(j + 2), (float)(k + 1), 0xFFFFFF);
            RealmsSelectFileToUploadScreen.this.font.draw(poseStack, this.id, (float)(j + 2), (float)(k + 12), 0x808080);
            RealmsSelectFileToUploadScreen.this.font.draw(poseStack, this.info, (float)(j + 2), (float)(k + 12 + 10), 0x808080);
        }

        @Override
        public Component getNarration() {
            Component component = CommonComponents.joinLines(Component.literal(this.levelSummary.getLevelName()), Component.literal(RealmsSelectFileToUploadScreen.formatLastPlayed(this.levelSummary)), RealmsSelectFileToUploadScreen.gameModeName(this.levelSummary));
            return Component.translatable("narrator.select", component);
        }
    }
}

