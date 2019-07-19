package net.minecraft.client.gui.screens.worldselection;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
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
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;

@Environment(EnvType.CLIENT)
public class EditWorldScreen extends Screen {
	private Button renameButton;
	private final BooleanConsumer callback;
	private EditBox nameEdit;
	private final String levelId;

	public EditWorldScreen(BooleanConsumer booleanConsumer, String string) {
		super(new TranslatableComponent("selectWorld.edit.title"));
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
		Button button = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20, I18n.get("selectWorld.edit.resetIcon"), buttonx -> {
			LevelStorageSource levelStorageSourcex = this.minecraft.getLevelSource();
			FileUtils.deleteQuietly(levelStorageSourcex.getFile(this.levelId, "icon.png"));
			buttonx.active = false;
		}));
		this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, I18n.get("selectWorld.edit.openFolder"), buttonx -> {
			LevelStorageSource levelStorageSourcex = this.minecraft.getLevelSource();
			Util.getPlatform().openFile(levelStorageSourcex.getFile(this.levelId, "icon.png").getParentFile());
		}));
		this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, I18n.get("selectWorld.edit.backup"), buttonx -> {
			LevelStorageSource levelStorageSourcex = this.minecraft.getLevelSource();
			makeBackupAndShowToast(levelStorageSourcex, this.levelId);
			this.callback.accept(false);
		}));
		this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20, I18n.get("selectWorld.edit.backupFolder"), buttonx -> {
			LevelStorageSource levelStorageSourcex = this.minecraft.getLevelSource();
			Path path = levelStorageSourcex.getBackupPath();

			try {
				Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
			} catch (IOException var5) {
				throw new RuntimeException(var5);
			}

			Util.getPlatform().openFile(path.toFile());
		}));
		this.addButton(
			new Button(
				this.width / 2 - 100,
				this.height / 4 + 120 + 5,
				200,
				20,
				I18n.get("selectWorld.edit.optimize"),
				buttonx -> this.minecraft.setScreen(new BackupConfirmScreen(this, (bl, bl2) -> {
						if (bl) {
							makeBackupAndShowToast(this.minecraft.getLevelSource(), this.levelId);
						}

						this.minecraft.setScreen(new OptimizeWorldScreen(this.callback, this.levelId, this.minecraft.getLevelSource(), bl2));
					}, new TranslatableComponent("optimizeWorld.confirm.title"), new TranslatableComponent("optimizeWorld.confirm.description"), true))
			)
		);
		this.renameButton = this.addButton(
			new Button(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, I18n.get("selectWorld.edit.save"), buttonx -> this.onRename())
		);
		this.addButton(new Button(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, I18n.get("gui.cancel"), buttonx -> this.callback.accept(false)));
		button.active = this.minecraft.getLevelSource().getFile(this.levelId, "icon.png").isFile();
		LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
		LevelData levelData = levelStorageSource.getDataTagFor(this.levelId);
		String string = levelData == null ? "" : levelData.getLevelName();
		this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 53, 200, 20, I18n.get("selectWorld.enterName"));
		this.nameEdit.setValue(string);
		this.nameEdit.setResponder(stringx -> this.renameButton.active = !stringx.trim().isEmpty());
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
		ToastComponent toastComponent = Minecraft.getInstance().getToasts();
		long l = 0L;
		IOException iOException = null;

		try {
			l = levelStorageSource.makeWorldBackup(string);
		} catch (IOException var8) {
			iOException = var8;
		}

		Component component;
		Component component2;
		if (iOException != null) {
			component = new TranslatableComponent("selectWorld.edit.backupFailed");
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
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, 16777215);
		this.drawString(this.font, I18n.get("selectWorld.enterName"), this.width / 2 - 100, 40, 10526880);
		this.nameEdit.render(i, j, f);
		super.render(i, j, f);
	}
}
