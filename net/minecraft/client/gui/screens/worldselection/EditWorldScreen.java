/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class EditWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
    private Button renameButton;
    private final BooleanConsumer callback;
    private EditBox nameEdit;
    private final LevelStorageSource.LevelStorageAccess levelAccess;

    public EditWorldScreen(BooleanConsumer booleanConsumer, LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        super(Component.translatable("selectWorld.edit.title"));
        this.callback = booleanConsumer;
        this.levelAccess = levelStorageAccess;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    protected void init() {
        this.renameButton = Button.builder(Component.translatable("selectWorld.edit.save"), button -> this.onRename()).bounds(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20).build();
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 38, 200, 20, Component.translatable("selectWorld.enterName"));
        LevelSummary levelSummary = this.levelAccess.getSummary();
        String string2 = levelSummary == null ? "" : levelSummary.getLevelName();
        this.nameEdit.setValue(string2);
        this.nameEdit.setResponder(string -> {
            this.renameButton.active = !string.trim().isEmpty();
        });
        this.addWidget(this.nameEdit);
        Button button2 = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.resetIcon"), button -> {
            this.levelAccess.getIconFile().ifPresent(path -> FileUtils.deleteQuietly(path.toFile()));
            button.active = false;
        }).bounds(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.openFolder"), button -> Util.getPlatform().openFile(this.levelAccess.getLevelPath(LevelResource.ROOT).toFile())).bounds(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.backup"), button -> {
            boolean bl = EditWorldScreen.makeBackupAndShowToast(this.levelAccess);
            this.callback.accept(!bl);
        }).bounds(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.backupFolder"), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            Path path = levelStorageSource.getBackupPath();
            try {
                FileUtil.createDirectoriesSafe(path);
            } catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            Util.getPlatform().openFile(path.toFile());
        }).bounds(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.optimize"), button -> this.minecraft.setScreen(new BackupConfirmScreen(this, (bl, bl2) -> {
            if (bl) {
                EditWorldScreen.makeBackupAndShowToast(this.levelAccess);
            }
            this.minecraft.setScreen(OptimizeWorldScreen.create(this.minecraft, this.callback, this.minecraft.getFixerUpper(), this.levelAccess, bl2));
        }, Component.translatable("optimizeWorld.confirm.title"), Component.translatable("optimizeWorld.confirm.description"), true))).bounds(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20).build());
        this.addRenderableWidget(this.renameButton);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.callback.accept(false)).bounds(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20).build());
        button2.active = this.levelAccess.getIconFile().filter(path -> Files.isRegularFile(path, new LinkOption[0])).isPresent();
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.nameEdit.getValue();
        this.init(minecraft, i, j);
        this.nameEdit.setValue(string);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    private void onRename() {
        try {
            this.levelAccess.renameLevel(this.nameEdit.getValue().trim());
            this.callback.accept(true);
        } catch (IOException iOException) {
            LOGGER.error("Failed to access world '{}'", (Object)this.levelAccess.getLevelId(), (Object)iOException);
            SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
            this.callback.accept(true);
        }
    }

    public static void makeBackupAndShowToast(LevelStorageSource levelStorageSource, String string) {
        boolean bl = false;
        try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);){
            bl = true;
            EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
        } catch (IOException iOException) {
            if (!bl) {
                SystemToast.onWorldAccessFailure(Minecraft.getInstance(), string);
            }
            LOGGER.warn("Failed to create backup of level {}", (Object)string, (Object)iOException);
        }
    }

    public static boolean makeBackupAndShowToast(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        long l = 0L;
        IOException iOException = null;
        try {
            l = levelStorageAccess.makeWorldBackup();
        } catch (IOException iOException2) {
            iOException = iOException2;
        }
        if (iOException != null) {
            MutableComponent component = Component.translatable("selectWorld.edit.backupFailed");
            MutableComponent component2 = Component.literal(iOException.getMessage());
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component, component2));
            return false;
        }
        MutableComponent component = Component.translatable("selectWorld.edit.backupCreated", levelStorageAccess.getLevelId());
        MutableComponent component2 = Component.translatable("selectWorld.edit.backupSize", Mth.ceil((double)l / 1048576.0));
        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component, component2));
        return true;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        EditWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        EditWorldScreen.drawString(poseStack, this.font, NAME_LABEL, this.width / 2 - 100, 24, 0xA0A0A0);
        this.nameEdit.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
    }
}

