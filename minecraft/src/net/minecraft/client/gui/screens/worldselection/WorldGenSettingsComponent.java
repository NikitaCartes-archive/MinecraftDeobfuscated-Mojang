package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

@Environment(EnvType.CLIENT)
public class WorldGenSettingsComponent implements Widget {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
	private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
	private static final Component MAP_FEATURES_INFO = new TranslatableComponent("selectWorld.mapFeatures.info");
	private static final Component SELECT_FILE_PROMPT = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
	private MultiLineLabel amplifiedWorldInfo = MultiLineLabel.EMPTY;
	private Font font;
	private int width;
	private EditBox seedEdit;
	private CycleButton<Boolean> featuresButton;
	private CycleButton<Boolean> bonusItemsButton;
	private CycleButton<WorldPreset> typeButton;
	private Button customWorldDummyButton;
	private Button customizeTypeButton;
	private Button importSettingsButton;
	private RegistryAccess.RegistryHolder registryHolder;
	private WorldGenSettings settings;
	private Optional<WorldPreset> preset;
	private OptionalLong seed;

	public WorldGenSettingsComponent(
		RegistryAccess.RegistryHolder registryHolder, WorldGenSettings worldGenSettings, Optional<WorldPreset> optional, OptionalLong optionalLong
	) {
		this.registryHolder = registryHolder;
		this.settings = worldGenSettings;
		this.preset = optional;
		this.seed = optionalLong;
	}

	public void init(CreateWorldScreen createWorldScreen, Minecraft minecraft, Font font) {
		this.font = font;
		this.width = createWorldScreen.width;
		this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
		this.seedEdit.setValue(toString(this.seed));
		this.seedEdit.setResponder(string -> this.seed = this.parseSeed());
		createWorldScreen.addWidget(this.seedEdit);
		int i = this.width / 2 - 155;
		int j = this.width / 2 + 5;
		this.featuresButton = createWorldScreen.addRenderableWidget(
			CycleButton.onOffBuilder(this.settings.generateFeatures())
				.withCustomNarration(
					cycleButton -> CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), new TranslatableComponent("selectWorld.mapFeatures.info"))
				)
				.create(
					i, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), (cycleButton, boolean_) -> this.settings = this.settings.withFeaturesToggled()
				)
		);
		this.featuresButton.visible = false;
		this.typeButton = createWorldScreen.addRenderableWidget(
			CycleButton.<WorldPreset>builder(WorldPreset::description)
				.withValues((List<WorldPreset>)WorldPreset.PRESETS.stream().filter(WorldPreset::isVisibleByDefault).collect(Collectors.toList()), WorldPreset.PRESETS)
				.withCustomNarration(
					cycleButton -> cycleButton.getValue() == WorldPreset.AMPLIFIED
							? CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT)
							: cycleButton.createDefaultNarrationMessage()
				)
				.create(j, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), (cycleButton, worldPreset) -> {
					this.preset = Optional.of(worldPreset);
					this.settings = worldPreset.create(this.registryHolder, this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest());
					createWorldScreen.refreshWorldGenSettingsVisibility();
				})
		);
		this.preset.ifPresent(this.typeButton::setValue);
		this.typeButton.visible = false;
		this.customWorldDummyButton = createWorldScreen.addRenderableWidget(
			new Button(j, 100, 150, 20, CommonComponents.optionNameValue(new TranslatableComponent("selectWorld.mapType"), CUSTOM_WORLD_DESCRIPTION), button -> {
			})
		);
		this.customWorldDummyButton.active = false;
		this.customWorldDummyButton.visible = false;
		this.customizeTypeButton = createWorldScreen.addRenderableWidget(
			new Button(j, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), button -> {
				WorldPreset.PresetEditor presetEditor = (WorldPreset.PresetEditor)WorldPreset.EDITORS.get(this.preset);
				if (presetEditor != null) {
					minecraft.setScreen(presetEditor.createEditScreen(createWorldScreen, this.settings));
				}
			})
		);
		this.customizeTypeButton.visible = false;
		this.bonusItemsButton = createWorldScreen.addRenderableWidget(
			CycleButton.onOffBuilder(this.settings.generateBonusChest() && !createWorldScreen.hardCore)
				.create(
					i, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), (cycleButton, boolean_) -> this.settings = this.settings.withBonusChestToggled()
				)
		);
		this.bonusItemsButton.visible = false;
		this.importSettingsButton = createWorldScreen.addRenderableWidget(
			new Button(
				i,
				185,
				150,
				20,
				new TranslatableComponent("selectWorld.import_worldgen_settings"),
				button -> {
					String string = TinyFileDialogs.tinyfd_openFileDialog(SELECT_FILE_PROMPT.getString(), null, null, null, false);
					if (string != null) {
						RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();
						PackRepository packRepository = new PackRepository(
							PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(createWorldScreen.getTempDataPackDir().toFile(), PackSource.WORLD)
						);

						ServerResources serverResources;
						try {
							MinecraftServer.configurePackRepository(packRepository, createWorldScreen.dataPacks, false);
							CompletableFuture<ServerResources> completableFuture = ServerResources.loadResources(
								packRepository.openAllSelected(), registryHolder, Commands.CommandSelection.INTEGRATED, 2, Util.backgroundExecutor(), minecraft
							);
							minecraft.managedBlock(completableFuture::isDone);
							serverResources = (ServerResources)completableFuture.get();
						} catch (ExecutionException | InterruptedException var15) {
							LOGGER.error("Error loading data packs when importing world settings", (Throwable)var15);
							Component component = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
							Component component2 = new TextComponent(var15.getMessage());
							minecraft.getToasts().addToast(SystemToast.multiline(minecraft, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component, component2));
							packRepository.close();
							return;
						}

						RegistryReadOps<JsonElement> registryReadOps = RegistryReadOps.createAndLoad(JsonOps.INSTANCE, serverResources.getResourceManager(), registryHolder);
						JsonParser jsonParser = new JsonParser();

						DataResult<WorldGenSettings> dataResult;
						try {
							BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(string));

							try {
								JsonElement jsonElement = jsonParser.parse(bufferedReader);
								dataResult = WorldGenSettings.CODEC.parse(registryReadOps, jsonElement);
							} catch (Throwable var16) {
								if (bufferedReader != null) {
									try {
										bufferedReader.close();
									} catch (Throwable var14) {
										var16.addSuppressed(var14);
									}
								}

								throw var16;
							}

							if (bufferedReader != null) {
								bufferedReader.close();
							}
						} catch (JsonIOException | JsonSyntaxException | IOException var17) {
							dataResult = DataResult.error("Failed to parse file: " + var17.getMessage());
						}

						if (dataResult.error().isPresent()) {
							Component component3 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
							String string2 = ((PartialResult)dataResult.error().get()).message();
							LOGGER.error("Error parsing world settings: {}", string2);
							Component component4 = new TextComponent(string2);
							minecraft.getToasts().addToast(SystemToast.multiline(minecraft, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component3, component4));
						}

						serverResources.close();
						Lifecycle lifecycle = dataResult.lifecycle();
						dataResult.resultOrPartial(LOGGER::error)
							.ifPresent(
								worldGenSettings -> {
									BooleanConsumer booleanConsumer = bl -> {
										minecraft.setScreen(createWorldScreen);
										if (bl) {
											this.importSettings(registryHolder, worldGenSettings);
										}
									};
									if (lifecycle == Lifecycle.stable()) {
										this.importSettings(registryHolder, worldGenSettings);
									} else if (lifecycle == Lifecycle.experimental()) {
										minecraft.setScreen(
											new ConfirmScreen(
												booleanConsumer,
												new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.title"),
												new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.question")
											)
										);
									} else {
										minecraft.setScreen(
											new ConfirmScreen(
												booleanConsumer,
												new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.title"),
												new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.question")
											)
										);
									}
								}
							);
					}
				}
			)
		);
		this.importSettingsButton.visible = false;
		this.amplifiedWorldInfo = MultiLineLabel.create(font, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
	}

	private void importSettings(RegistryAccess.RegistryHolder registryHolder, WorldGenSettings worldGenSettings) {
		this.registryHolder = registryHolder;
		this.settings = worldGenSettings;
		this.preset = WorldPreset.of(worldGenSettings);
		this.selectWorldTypeButton(true);
		this.seed = OptionalLong.of(worldGenSettings.seed());
		this.seedEdit.setValue(toString(this.seed));
	}

	public void tick() {
		this.seedEdit.tick();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.featuresButton.visible) {
			this.font.drawShadow(poseStack, MAP_FEATURES_INFO, (float)(this.width / 2 - 150), 122.0F, -6250336);
		}

		this.seedEdit.render(poseStack, i, j, f);
		if (this.preset.equals(Optional.of(WorldPreset.AMPLIFIED))) {
			this.amplifiedWorldInfo.renderLeftAligned(poseStack, this.typeButton.x + 2, this.typeButton.y + 22, 9, 10526880);
		}
	}

	protected void updateSettings(WorldGenSettings worldGenSettings) {
		this.settings = worldGenSettings;
	}

	private static String toString(OptionalLong optionalLong) {
		return optionalLong.isPresent() ? Long.toString(optionalLong.getAsLong()) : "";
	}

	private static OptionalLong parseLong(String string) {
		try {
			return OptionalLong.of(Long.parseLong(string));
		} catch (NumberFormatException var2) {
			return OptionalLong.empty();
		}
	}

	public WorldGenSettings makeSettings(boolean bl) {
		OptionalLong optionalLong = this.parseSeed();
		return this.settings.withSeed(bl, optionalLong);
	}

	private OptionalLong parseSeed() {
		String string = this.seedEdit.getValue();
		OptionalLong optionalLong;
		if (StringUtils.isEmpty(string)) {
			optionalLong = OptionalLong.empty();
		} else {
			OptionalLong optionalLong2 = parseLong(string);
			if (optionalLong2.isPresent() && optionalLong2.getAsLong() != 0L) {
				optionalLong = optionalLong2;
			} else {
				optionalLong = OptionalLong.of((long)string.hashCode());
			}
		}

		return optionalLong;
	}

	public boolean isDebug() {
		return this.settings.isDebug();
	}

	public void setVisibility(boolean bl) {
		this.selectWorldTypeButton(bl);
		if (this.settings.isDebug()) {
			this.featuresButton.visible = false;
			this.bonusItemsButton.visible = false;
			this.customizeTypeButton.visible = false;
			this.importSettingsButton.visible = false;
		} else {
			this.featuresButton.visible = bl;
			this.bonusItemsButton.visible = bl;
			this.customizeTypeButton.visible = bl && WorldPreset.EDITORS.containsKey(this.preset);
			this.importSettingsButton.visible = bl;
		}

		this.seedEdit.setVisible(bl);
	}

	private void selectWorldTypeButton(boolean bl) {
		if (this.preset.isPresent()) {
			this.typeButton.visible = bl;
			this.customWorldDummyButton.visible = false;
		} else {
			this.typeButton.visible = false;
			this.customWorldDummyButton.visible = bl;
		}
	}

	public RegistryAccess.RegistryHolder registryHolder() {
		return this.registryHolder;
	}

	void updateDataPacks(ServerResources serverResources) {
		RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();
		RegistryWriteOps<JsonElement> registryWriteOps = RegistryWriteOps.create(JsonOps.INSTANCE, this.registryHolder);
		RegistryReadOps<JsonElement> registryReadOps = RegistryReadOps.createAndLoad(JsonOps.INSTANCE, serverResources.getResourceManager(), registryHolder);
		DataResult<WorldGenSettings> dataResult = WorldGenSettings.CODEC
			.encodeStart(registryWriteOps, this.settings)
			.flatMap(jsonElement -> WorldGenSettings.CODEC.parse(registryReadOps, jsonElement));
		dataResult.resultOrPartial(Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error)).ifPresent(worldGenSettings -> {
			this.settings = worldGenSettings;
			this.registryHolder = registryHolder;
		});
	}

	public void switchToHardcore() {
		this.bonusItemsButton.active = false;
		this.bonusItemsButton.setValue(false);
	}

	public void switchOutOfHardcode() {
		this.bonusItemsButton.active = true;
		this.bonusItemsButton.setValue(this.settings.generateBonusChest());
	}
}
