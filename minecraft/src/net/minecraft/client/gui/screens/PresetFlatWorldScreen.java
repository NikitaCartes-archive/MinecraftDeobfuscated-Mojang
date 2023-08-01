package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class PresetFlatWorldScreen extends Screen {
	static final ResourceLocation SLOT_SPRITE = new ResourceLocation("container/slot");
	static final Logger LOGGER = LogUtils.getLogger();
	private static final int SLOT_BG_SIZE = 18;
	private static final int SLOT_STAT_HEIGHT = 20;
	private static final int SLOT_BG_X = 1;
	private static final int SLOT_BG_Y = 1;
	private static final int SLOT_FG_X = 2;
	private static final int SLOT_FG_Y = 2;
	private static final ResourceKey<Biome> DEFAULT_BIOME = Biomes.PLAINS;
	public static final Component UNKNOWN_PRESET = Component.translatable("flat_world_preset.unknown");
	private final CreateFlatWorldScreen parent;
	private Component shareText;
	private Component listText;
	private PresetFlatWorldScreen.PresetsList list;
	private Button selectButton;
	EditBox export;
	FlatLevelGeneratorSettings settings;

	public PresetFlatWorldScreen(CreateFlatWorldScreen createFlatWorldScreen) {
		super(Component.translatable("createWorld.customize.presets.title"));
		this.parent = createFlatWorldScreen;
	}

	@Nullable
	private static FlatLayerInfo getLayerInfoFromString(HolderGetter<Block> holderGetter, String string, int i) {
		List<String> list = Splitter.on('*').limit(2).splitToList(string);
		int j;
		String string2;
		if (list.size() == 2) {
			string2 = (String)list.get(1);

			try {
				j = Math.max(Integer.parseInt((String)list.get(0)), 0);
			} catch (NumberFormatException var11) {
				LOGGER.error("Error while parsing flat world string", (Throwable)var11);
				return null;
			}
		} else {
			string2 = (String)list.get(0);
			j = 1;
		}

		int k = Math.min(i + j, DimensionType.Y_SIZE);
		int l = k - i;

		Optional<Holder.Reference<Block>> optional;
		try {
			optional = holderGetter.get(ResourceKey.create(Registries.BLOCK, new ResourceLocation(string2)));
		} catch (Exception var10) {
			LOGGER.error("Error while parsing flat world string", (Throwable)var10);
			return null;
		}

		if (optional.isEmpty()) {
			LOGGER.error("Error while parsing flat world string => Unknown block, {}", string2);
			return null;
		} else {
			return new FlatLayerInfo(l, (Block)((Holder.Reference)optional.get()).value());
		}
	}

	private static List<FlatLayerInfo> getLayersInfoFromString(HolderGetter<Block> holderGetter, String string) {
		List<FlatLayerInfo> list = Lists.<FlatLayerInfo>newArrayList();
		String[] strings = string.split(",");
		int i = 0;

		for (String string2 : strings) {
			FlatLayerInfo flatLayerInfo = getLayerInfoFromString(holderGetter, string2, i);
			if (flatLayerInfo == null) {
				return Collections.emptyList();
			}

			list.add(flatLayerInfo);
			i += flatLayerInfo.getHeight();
		}

		return list;
	}

	public static FlatLevelGeneratorSettings fromString(
		HolderGetter<Block> holderGetter,
		HolderGetter<Biome> holderGetter2,
		HolderGetter<StructureSet> holderGetter3,
		HolderGetter<PlacedFeature> holderGetter4,
		String string,
		FlatLevelGeneratorSettings flatLevelGeneratorSettings
	) {
		Iterator<String> iterator = Splitter.on(';').split(string).iterator();
		if (!iterator.hasNext()) {
			return FlatLevelGeneratorSettings.getDefault(holderGetter2, holderGetter3, holderGetter4);
		} else {
			List<FlatLayerInfo> list = getLayersInfoFromString(holderGetter, (String)iterator.next());
			if (list.isEmpty()) {
				return FlatLevelGeneratorSettings.getDefault(holderGetter2, holderGetter3, holderGetter4);
			} else {
				Holder.Reference<Biome> reference = holderGetter2.getOrThrow(DEFAULT_BIOME);
				Holder<Biome> holder = reference;
				if (iterator.hasNext()) {
					String string2 = (String)iterator.next();
					holder = (Holder<Biome>)Optional.ofNullable(ResourceLocation.tryParse(string2))
						.map(resourceLocation -> ResourceKey.create(Registries.BIOME, resourceLocation))
						.flatMap(holderGetter2::get)
						.orElseGet(() -> {
							LOGGER.warn("Invalid biome: {}", string2);
							return reference;
						});
				}

				return flatLevelGeneratorSettings.withBiomeAndLayers(list, flatLevelGeneratorSettings.structureOverrides(), holder);
			}
		}
	}

	static String save(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < flatLevelGeneratorSettings.getLayersInfo().size(); i++) {
			if (i > 0) {
				stringBuilder.append(",");
			}

			stringBuilder.append(flatLevelGeneratorSettings.getLayersInfo().get(i));
		}

		stringBuilder.append(";");
		stringBuilder.append(
			flatLevelGeneratorSettings.getBiome().unwrapKey().map(ResourceKey::location).orElseThrow(() -> new IllegalStateException("Biome not registered"))
		);
		return stringBuilder.toString();
	}

	@Override
	protected void init() {
		this.shareText = Component.translatable("createWorld.customize.presets.share");
		this.listText = Component.translatable("createWorld.customize.presets.list");
		this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
		this.export.setMaxLength(1230);
		WorldCreationContext worldCreationContext = this.parent.parent.getUiState().getSettings();
		RegistryAccess registryAccess = worldCreationContext.worldgenLoadContext();
		FeatureFlagSet featureFlagSet = worldCreationContext.dataConfiguration().enabledFeatures();
		HolderGetter<Biome> holderGetter = registryAccess.lookupOrThrow(Registries.BIOME);
		HolderGetter<StructureSet> holderGetter2 = registryAccess.lookupOrThrow(Registries.STRUCTURE_SET);
		HolderGetter<PlacedFeature> holderGetter3 = registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);
		HolderGetter<Block> holderGetter4 = registryAccess.lookupOrThrow(Registries.BLOCK).filterFeatures(featureFlagSet);
		this.export.setValue(save(this.parent.settings()));
		this.settings = this.parent.settings();
		this.addWidget(this.export);
		this.list = new PresetFlatWorldScreen.PresetsList(registryAccess, featureFlagSet);
		this.addWidget(this.list);
		this.selectButton = this.addRenderableWidget(
			Button.builder(
					Component.translatable("createWorld.customize.presets.select"),
					button -> {
						FlatLevelGeneratorSettings flatLevelGeneratorSettings = fromString(
							holderGetter4, holderGetter, holderGetter2, holderGetter3, this.export.getValue(), this.settings
						);
						this.parent.setConfig(flatLevelGeneratorSettings);
						this.minecraft.setScreen(this.parent);
					}
				)
				.bounds(this.width / 2 - 155, this.height - 28, 150, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build()
		);
		this.updateButtonValidity(this.list.getSelected() != null);
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f, double g) {
		return this.list.mouseScrolled(d, e, f, g);
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.export.getValue();
		this.init(minecraft, i, j);
		this.export.setValue(string);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.list.render(guiGraphics, i, j, f);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
		guiGraphics.drawString(this.font, this.shareText, 50, 30, 10526880);
		guiGraphics.drawString(this.font, this.listText, 50, 70, 10526880);
		guiGraphics.pose().popPose();
		this.export.render(guiGraphics, i, j, f);
	}

	public void updateButtonValidity(boolean bl) {
		this.selectButton.active = bl || this.export.getValue().length() > 1;
	}

	@Environment(EnvType.CLIENT)
	class PresetsList extends ObjectSelectionList<PresetFlatWorldScreen.PresetsList.Entry> {
		public PresetsList(RegistryAccess registryAccess, FeatureFlagSet featureFlagSet) {
			super(
				PresetFlatWorldScreen.this.minecraft, PresetFlatWorldScreen.this.width, PresetFlatWorldScreen.this.height, 80, PresetFlatWorldScreen.this.height - 37, 24
			);

			for (Holder<FlatLevelGeneratorPreset> holder : registryAccess.registryOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET)
				.getTagOrEmpty(FlatLevelGeneratorPresetTags.VISIBLE)) {
				Set<Block> set = (Set<Block>)holder.value()
					.settings()
					.getLayersInfo()
					.stream()
					.map(flatLayerInfo -> flatLayerInfo.getBlockState().getBlock())
					.filter(block -> !block.isEnabled(featureFlagSet))
					.collect(Collectors.toSet());
				if (!set.isEmpty()) {
					PresetFlatWorldScreen.LOGGER
						.info(
							"Discarding flat world preset {} since it contains experimental blocks {}",
							holder.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("<unknown>"),
							set
						);
				} else {
					this.addEntry(new PresetFlatWorldScreen.PresetsList.Entry(holder));
				}
			}
		}

		public void setSelected(@Nullable PresetFlatWorldScreen.PresetsList.Entry entry) {
			super.setSelected(entry);
			PresetFlatWorldScreen.this.updateButtonValidity(entry != null);
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (super.keyPressed(i, j, k)) {
				return true;
			} else {
				if (CommonInputs.selected(i) && this.getSelected() != null) {
					this.getSelected().select();
				}

				return false;
			}
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends ObjectSelectionList.Entry<PresetFlatWorldScreen.PresetsList.Entry> {
			private static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
			private final FlatLevelGeneratorPreset preset;
			private final Component name;

			public Entry(Holder<FlatLevelGeneratorPreset> holder) {
				this.preset = holder.value();
				this.name = (Component)holder.unwrapKey()
					.map(resourceKey -> Component.translatable(resourceKey.location().toLanguageKey("flat_world_preset")))
					.orElse(PresetFlatWorldScreen.UNKNOWN_PRESET);
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				this.blitSlot(guiGraphics, k, j, this.preset.displayItem().value());
				guiGraphics.drawString(PresetFlatWorldScreen.this.font, this.name, k + 18 + 5, j + 6, 16777215, false);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					this.select();
				}

				return false;
			}

			void select() {
				PresetsList.this.setSelected(this);
				PresetFlatWorldScreen.this.settings = this.preset.settings();
				PresetFlatWorldScreen.this.export.setValue(PresetFlatWorldScreen.save(PresetFlatWorldScreen.this.settings));
				PresetFlatWorldScreen.this.export.moveCursorToStart(false);
			}

			private void blitSlot(GuiGraphics guiGraphics, int i, int j, Item item) {
				this.blitSlotBg(guiGraphics, i + 1, j + 1);
				guiGraphics.renderFakeItem(new ItemStack(item), i + 2, j + 2);
			}

			private void blitSlotBg(GuiGraphics guiGraphics, int i, int j) {
				guiGraphics.blitSprite(PresetFlatWorldScreen.SLOT_SPRITE, i, j, 0, 18, 18);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", this.name);
			}
		}
	}
}
