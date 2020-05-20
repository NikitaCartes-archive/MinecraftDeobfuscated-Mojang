package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class EditWorldScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private Button renameButton;
	private final BooleanConsumer callback;
	private EditBox nameEdit;
	private final LevelStorageSource.LevelStorageAccess levelAccess;

	public EditWorldScreen(BooleanConsumer booleanConsumer, LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		super(new TranslatableComponent("selectWorld.edit.title"));
		this.callback = booleanConsumer;
		this.levelAccess = levelStorageAccess;
	}

	@Override
	public void tick() {
		this.nameEdit.tick();
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		Button button = this.addButton(
			new Button(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.resetIcon"), buttonx -> {
				FileUtils.deleteQuietly(this.levelAccess.getIconFile());
				buttonx.active = false;
			})
		);
		this.addButton(
			new Button(
				this.width / 2 - 100,
				this.height / 4 + 24 + 5,
				200,
				20,
				new TranslatableComponent("selectWorld.edit.openFolder"),
				buttonx -> Util.getPlatform().openFile(this.levelAccess.getLevelPath(LevelResource.ROOT).toFile())
			)
		);
		this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.backup"), buttonx -> {
			boolean bl = makeBackupAndShowToast(this.levelAccess);
			this.callback.accept(!bl);
		}));
		this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.backupFolder"), buttonx -> {
			LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
			Path path = levelStorageSource.getBackupPath();

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
				this.height / 4 + 96 + 5,
				200,
				20,
				new TranslatableComponent("selectWorld.edit.optimize"),
				buttonx -> this.minecraft.setScreen(new BackupConfirmScreen(this, (bl, bl2) -> {
						if (bl) {
							makeBackupAndShowToast(this.levelAccess);
						}

						this.minecraft.setScreen(OptimizeWorldScreen.create(this.callback, this.minecraft.getFixerUpper(), this.levelAccess, bl2));
					}, new TranslatableComponent("optimizeWorld.confirm.title"), new TranslatableComponent("optimizeWorld.confirm.description"), true))
			)
		);
		this.addButton(
			new Button(
				this.width / 2 - 100,
				this.height / 4 + 120 + 5,
				200,
				20,
				new TranslatableComponent("selectWorld.edit.export_worldgen_settings"),
				buttonx -> {
					DataResult<String> dataResult = this.levelAccess.exportWorldGenSettings();
					Component component = new TextComponent(dataResult.get().map(Function.identity(), PartialResult::message));
					Component component2 = new TranslatableComponent(
						dataResult.result().isPresent() ? "selectWorld.edit.export_worldgen_settings.success" : "selectWorld.edit.export_worldgen_settings.failure"
					);
					dataResult.error().ifPresent(partialResult -> LOGGER.error("Error exporting world settings: {}", partialResult));
					this.minecraft.getToasts().addToast(SystemToast.multiline(SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component2, component));
				}
			)
		);
		this.renameButton = this.addButton(
			new Button(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, new TranslatableComponent("selectWorld.edit.save"), buttonx -> this.onRename())
		);
		this.addButton(new Button(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, CommonComponents.GUI_CANCEL, buttonx -> this.callback.accept(false)));
		button.active = this.levelAccess.getIconFile().isFile();
		WorldData worldData = this.levelAccess.getDataTag();
		String string = worldData == null ? "" : worldData.getLevelName();
		this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 38, 200, 20, new TranslatableComponent("selectWorld.enterName"));
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
	public void onClose() {
		this.callback.accept(false);
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	private void onRename() {
		try {
			this.levelAccess.renameLevel(this.nameEdit.getValue().trim());
			this.callback.accept(true);
		} catch (IOException var2) {
			LOGGER.error("Failed to access world '{}'", this.levelAccess.getLevelId(), var2);
			SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
			this.callback.accept(true);
		}
	}

	public static boolean makeBackupAndShowToast(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		long l = 0L;
		IOException iOException = null;

		try {
			l = levelStorageAccess.makeWorldBackup();
		} catch (IOException var6) {
			iOException = var6;
		}

		if (iOException != null) {
			Component component = new TranslatableComponent("selectWorld.edit.backupFailed");
			Component component2 = new TextComponent(iOException.getMessage());
			Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component, component2));
			return false;
		} else {
			Component component = new TranslatableComponent("selectWorld.edit.backupCreated", levelStorageAccess.getLevelId());
			Component component2 = new TranslatableComponent("selectWorld.edit.backupSize", Mth.ceil((double)l / 1048576.0));
			Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component, component2));
			return true;
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);
		this.drawString(poseStack, this.font, I18n.get("selectWorld.enterName"), this.width / 2 - 100, 24, 10526880);
		this.nameEdit.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}
}
