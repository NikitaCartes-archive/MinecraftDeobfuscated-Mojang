package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class EditWorldScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson WORLD_GEN_SETTINGS_GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
	private static final Component NAME_LABEL = new TranslatableComponent("selectWorld.enterName");
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
		Button button = this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.resetIcon"), buttonx -> {
				this.levelAccess.getIconFile().ifPresent(path -> FileUtils.deleteQuietly(path.toFile()));
				buttonx.active = false;
			})
		);
		this.addRenderableWidget(
			new Button(
				this.width / 2 - 100,
				this.height / 4 + 24 + 5,
				200,
				20,
				new TranslatableComponent("selectWorld.edit.openFolder"),
				buttonx -> Util.getPlatform().openFile(this.levelAccess.getLevelPath(LevelResource.ROOT).toFile())
			)
		);
		this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.backup"), buttonx -> {
				boolean bl = makeBackupAndShowToast(this.levelAccess);
				this.callback.accept(!bl);
			})
		);
		this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.backupFolder"), buttonx -> {
				LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
				Path path = levelStorageSource.getBackupPath();

				try {
					Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
				} catch (IOException var5) {
					throw new RuntimeException(var5);
				}

				Util.getPlatform().openFile(path.toFile());
			})
		);
		this.addRenderableWidget(
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

						this.minecraft.setScreen(OptimizeWorldScreen.create(this.minecraft, this.callback, this.minecraft.getFixerUpper(), this.levelAccess, bl2));
					}, new TranslatableComponent("optimizeWorld.confirm.title"), new TranslatableComponent("optimizeWorld.confirm.description"), true))
			)
		);
		this.addRenderableWidget(
			new Button(
				this.width / 2 - 100,
				this.height / 4 + 120 + 5,
				200,
				20,
				new TranslatableComponent("selectWorld.edit.export_worldgen_settings"),
				buttonx -> {
					RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();

					DataResult<String> dataResult2;
					try (Minecraft.ServerStem serverStem = this.minecraft
							.makeServerStem(registryHolder, Minecraft::loadDataPacks, Minecraft::loadWorldData, false, this.levelAccess)) {
						DynamicOps<JsonElement> dynamicOps = RegistryWriteOps.create(JsonOps.INSTANCE, registryHolder);
						DataResult<JsonElement> dataResult = WorldGenSettings.CODEC.encodeStart(dynamicOps, serverStem.worldData().worldGenSettings());
						dataResult2 = dataResult.flatMap(jsonElement -> {
							Path path = this.levelAccess.getLevelPath(LevelResource.ROOT).resolve("worldgen_settings_export.json");

							try {
								JsonWriter jsonWriter = WORLD_GEN_SETTINGS_GSON.newJsonWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8));

								try {
									WORLD_GEN_SETTINGS_GSON.toJson(jsonElement, jsonWriter);
								} catch (Throwable var7) {
									if (jsonWriter != null) {
										try {
											jsonWriter.close();
										} catch (Throwable var6x) {
											var7.addSuppressed(var6x);
										}
									}

									throw var7;
								}

								if (jsonWriter != null) {
									jsonWriter.close();
								}
							} catch (JsonIOException | IOException var8) {
								return DataResult.error("Error writing file: " + var8.getMessage());
							}

							return DataResult.success(path.toString());
						});
					} catch (Exception var9) {
						LOGGER.warn("Could not parse level data", (Throwable)var9);
						dataResult2 = DataResult.error("Could not parse level data: " + var9.getMessage());
					}

					Component component = new TextComponent(dataResult2.get().map(Function.identity(), PartialResult::message));
					Component component2 = new TranslatableComponent(
						dataResult2.result().isPresent() ? "selectWorld.edit.export_worldgen_settings.success" : "selectWorld.edit.export_worldgen_settings.failure"
					);
					dataResult2.error().ifPresent(partialResult -> LOGGER.error("Error exporting world settings: {}", partialResult));
					this.minecraft.getToasts().addToast(SystemToast.multiline(this.minecraft, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component2, component));
				}
			)
		);
		this.renameButton = this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, new TranslatableComponent("selectWorld.edit.save"), buttonx -> this.onRename())
		);
		this.addRenderableWidget(
			new Button(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, CommonComponents.GUI_CANCEL, buttonx -> this.callback.accept(false))
		);
		button.active = this.levelAccess.getIconFile().filter(path -> Files.isRegularFile(path, new LinkOption[0])).isPresent();
		LevelSummary levelSummary = this.levelAccess.getSummary();
		String string = levelSummary == null ? "" : levelSummary.getLevelName();
		this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 38, 200, 20, new TranslatableComponent("selectWorld.enterName"));
		this.nameEdit.setValue(string);
		this.nameEdit.setResponder(stringx -> this.renameButton.active = !stringx.trim().isEmpty());
		this.addWidget(this.nameEdit);
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

	public static void makeBackupAndShowToast(LevelStorageSource levelStorageSource, String string) {
		boolean bl = false;

		try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string)) {
			bl = true;
			makeBackupAndShowToast(levelStorageAccess);
		} catch (IOException var8) {
			if (!bl) {
				SystemToast.onWorldAccessFailure(Minecraft.getInstance(), string);
			}

			LOGGER.warn("Failed to create backup of level {}", string, var8);
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
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);
		drawString(poseStack, this.font, NAME_LABEL, this.width / 2 - 100, 24, 10526880);
		this.nameEdit.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}
}
