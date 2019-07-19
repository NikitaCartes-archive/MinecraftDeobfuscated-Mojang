/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;

@Environment(value=EnvType.CLIENT)
public class EditWorldScreen
extends Screen {
    private Button renameButton;
    private final BooleanConsumer callback;
    private EditBox nameEdit;
    private final String levelId;

    public EditWorldScreen(BooleanConsumer booleanConsumer, String string) {
        super(new TranslatableComponent("selectWorld.edit.title", new Object[0]));
        this.callback = booleanConsumer;
        this.levelId = string;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        Button button2 = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20, I18n.get("selectWorld.edit.resetIcon", new Object[0]), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            FileUtils.deleteQuietly(levelStorageSource.getFile(this.levelId, "icon.png"));
            button.active = false;
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, I18n.get("selectWorld.edit.openFolder", new Object[0]), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            Util.getPlatform().openFile(levelStorageSource.getFile(this.levelId, "icon.png").getParentFile());
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, I18n.get("selectWorld.edit.backup", new Object[0]), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            EditWorldScreen.makeBackupAndShowToast(levelStorageSource, this.levelId);
            this.callback.accept(false);
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20, I18n.get("selectWorld.edit.backupFolder", new Object[0]), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            Path path = levelStorageSource.getBackupPath();
            try {
                Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath(new LinkOption[0]) : path, new FileAttribute[0]);
            } catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            Util.getPlatform().openFile(path.toFile());
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20, I18n.get("selectWorld.edit.optimize", new Object[0]), button -> this.minecraft.setScreen(new BackupConfirmScreen(this, (bl, bl2) -> {
            if (bl) {
                EditWorldScreen.makeBackupAndShowToast(this.minecraft.getLevelSource(), this.levelId);
            }
            this.minecraft.setScreen(new OptimizeWorldScreen(this.callback, this.levelId, this.minecraft.getLevelSource(), bl2));
        }, new TranslatableComponent("optimizeWorld.confirm.title", new Object[0]), new TranslatableComponent("optimizeWorld.confirm.description", new Object[0]), true))));
        this.renameButton = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, I18n.get("selectWorld.edit.save", new Object[0]), button -> this.onRename()));
        this.addButton(new Button(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, I18n.get("gui.cancel", new Object[0]), button -> this.callback.accept(false)));
        button2.active = this.minecraft.getLevelSource().getFile(this.levelId, "icon.png").isFile();
        LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
        LevelData levelData = levelStorageSource.getDataTagFor(this.levelId);
        String string2 = levelData == null ? "" : levelData.getLevelName();
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 53, 200, 20, I18n.get("selectWorld.enterName", new Object[0]));
        this.nameEdit.setValue(string2);
        this.nameEdit.setResponder(string -> {
            this.renameButton.active = !string.trim().isEmpty();
        });
        this.children.add(this.nameEdit);
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.nameEdit.getValue();
        this.init(minecraft, i, j);
        this.nameEdit.setValue(string);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onRename() {
        LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
        levelStorageSource.renameLevel(this.levelId, this.nameEdit.getValue().trim());
        this.callback.accept(true);
    }

    public static void makeBackupAndShowToast(LevelStorageSource levelStorageSource, String string) {
        BaseComponent component2;
        TranslatableComponent component;
        ToastComponent toastComponent = Minecraft.getInstance().getToasts();
        long l = 0L;
        IOException iOException = null;
        try {
            l = levelStorageSource.makeWorldBackup(string);
        } catch (IOException iOException2) {
            iOException = iOException2;
        }
        if (iOException != null) {
            component = new TranslatableComponent("selectWorld.edit.backupFailed", new Object[0]);
            component2 = new TextComponent(iOException.getMessage());
        } else {
            component = new TranslatableComponent("selectWorld.edit.backupCreated", string);
            component2 = new TranslatableComponent("selectWorld.edit.backupSize", Mth.ceil((double)l / 1048576.0));
        }
        toastComponent.addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component, component2));
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, 0xFFFFFF);
        this.drawString(this.font, I18n.get("selectWorld.enterName", new Object[0]), this.width / 2 - 100, 40, 0xA0A0A0);
        this.nameEdit.render(i, j, f);
        super.render(i, j, f);
    }
}

