package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class PresetFlatWorldScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int SLOT_TEX_SIZE = 128;
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
	private static FlatLayerInfo getLayerInfoFromString(String string, int i) {
		String[] strings = string.split("\\*", 2);
		int j;
		if (strings.length == 2) {
			try {
				j = Math.max(Integer.parseInt(strings[0]), 0);
			} catch (NumberFormatException var10) {
				LOGGER.error("Error while parsing flat world string => {}", var10.getMessage());
				return null;
			}
		} else {
			j = 1;
		}

		int k = Math.min(i + j, DimensionType.Y_SIZE);
		int l = k - i;
		String string2 = strings[strings.length - 1];

		Block block;
		try {
			block = (Block)Registry.BLOCK.getOptional(new ResourceLocation(string2)).orElse(null);
		} catch (Exception var9) {
			LOGGER.error("Error while parsing flat world string => {}", var9.getMessage());
			return null;
		}

		if (block == null) {
			LOGGER.error("Error while parsing flat world string => Unknown block, {}", string2);
			return null;
		} else {
			return new FlatLayerInfo(l, block);
		}
	}

	private static List<FlatLayerInfo> getLayersInfoFromString(String string) {
		List<FlatLayerInfo> list = Lists.<FlatLayerInfo>newArrayList();
		String[] strings = string.split(",");
		int i = 0;

		for (String string2 : strings) {
			FlatLayerInfo flatLayerInfo = getLayerInfoFromString(string2, i);
			if (flatLayerInfo == null) {
				return Collections.emptyList();
			}

			list.add(flatLayerInfo);
			i += flatLayerInfo.getHeight();
		}

		return list;
	}

	public static FlatLevelGeneratorSettings fromString(
		Registry<Biome> registry, Registry<StructureSet> registry2, String string, FlatLevelGeneratorSettings flatLevelGeneratorSettings
	) {
		Iterator<String> iterator = Splitter.on(';').split(string).iterator();
		if (!iterator.hasNext()) {
			return FlatLevelGeneratorSettings.getDefault(registry, registry2);
		} else {
			List<FlatLayerInfo> list = getLayersInfoFromString((String)iterator.next());
			if (list.isEmpty()) {
				return FlatLevelGeneratorSettings.getDefault(registry, registry2);
			} else {
				FlatLevelGeneratorSettings flatLevelGeneratorSettings2 = flatLevelGeneratorSettings.withLayers(list, flatLevelGeneratorSettings.structureOverrides());
				ResourceKey<Biome> resourceKey = DEFAULT_BIOME;
				if (iterator.hasNext()) {
					try {
						ResourceLocation resourceLocation = new ResourceLocation((String)iterator.next());
						resourceKey = ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation);
						registry.getOptional(resourceKey).orElseThrow(() -> new IllegalArgumentException("Invalid Biome: " + resourceLocation));
					} catch (Exception var9) {
						LOGGER.error("Error while parsing flat world string => {}", var9.getMessage());
						resourceKey = DEFAULT_BIOME;
					}
				}

				flatLevelGeneratorSettings2.setBiome(registry.getOrCreateHolderOrThrow(resourceKey));
				return flatLevelGeneratorSettings2;
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
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.shareText = Component.translatable("createWorld.customize.presets.share");
		this.listText = Component.translatable("createWorld.customize.presets.list");
		this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
		this.export.setMaxLength(1230);
		RegistryAccess registryAccess = this.parent.parent.worldGenSettingsComponent.registryHolder();
		Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<StructureSet> registry2 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		this.export.setValue(save(this.parent.settings()));
		this.settings = this.parent.settings();
		this.addWidget(this.export);
		this.list = new PresetFlatWorldScreen.PresetsList(this.parent.parent.worldGenSettingsComponent.registryHolder());
		this.addWidget(this.list);
		this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets.select"), button -> {
			FlatLevelGeneratorSettings flatLevelGeneratorSettings = fromString(registry, registry2, this.export.getValue(), this.settings);
			this.parent.setConfig(flatLevelGeneratorSettings);
			this.minecraft.setScreen(this.parent);
		}).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build()
		);
		this.updateButtonValidity(this.list.getSelected() != null);
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		return this.list.mouseScrolled(d, e, f);
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
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.list.render(poseStack, i, j, f);
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.0F, 400.0F);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
		drawString(poseStack, this.font, this.shareText, 50, 30, 10526880);
		drawString(poseStack, this.font, this.listText, 50, 70, 10526880);
		poseStack.popPose();
		this.export.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}

	@Override
	public void tick() {
		this.export.tick();
		super.tick();
	}

	public void updateButtonValidity(boolean bl) {
		this.selectButton.active = bl || this.export.getValue().length() > 1;
	}

	@Environment(EnvType.CLIENT)
	class PresetsList extends ObjectSelectionList<PresetFlatWorldScreen.PresetsList.Entry> {
		public PresetsList(RegistryAccess registryAccess) {
			super(
				PresetFlatWorldScreen.this.minecraft, PresetFlatWorldScreen.this.width, PresetFlatWorldScreen.this.height, 80, PresetFlatWorldScreen.this.height - 37, 24
			);

			for (Holder<FlatLevelGeneratorPreset> holder : registryAccess.registryOrThrow(Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY)
				.getTagOrEmpty(FlatLevelGeneratorPresetTags.VISIBLE)) {
				this.addEntry(new PresetFlatWorldScreen.PresetsList.Entry(holder));
			}
		}

		public void setSelected(@Nullable PresetFlatWorldScreen.PresetsList.Entry entry) {
			super.setSelected(entry);
			PresetFlatWorldScreen.this.updateButtonValidity(entry != null);
		}

		@Override
		protected boolean isFocused() {
			return PresetFlatWorldScreen.this.getFocused() == this;
		}

		@Override
		public boolean keyPressed(int i, int j, int k) {
			if (super.keyPressed(i, j, k)) {
				return true;
			} else {
				if ((i == 257 || i == 335) && this.getSelected() != null) {
					this.getSelected().select();
				}

				return false;
			}
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends ObjectSelectionList.Entry<PresetFlatWorldScreen.PresetsList.Entry> {
			private final FlatLevelGeneratorPreset preset;
			private final Component name;

			public Entry(Holder<FlatLevelGeneratorPreset> holder) {
				this.preset = holder.value();
				this.name = (Component)holder.unwrapKey()
					.map(resourceKey -> Component.translatable(resourceKey.location().toLanguageKey("flat_world_preset")))
					.orElse(PresetFlatWorldScreen.UNKNOWN_PRESET);
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				this.blitSlot(poseStack, k, j, this.preset.displayItem().value());
				PresetFlatWorldScreen.this.font.draw(poseStack, this.name, (float)(k + 18 + 5), (float)(j + 6), 16777215);
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
				PresetFlatWorldScreen.this.export.moveCursorToStart();
			}

			private void blitSlot(PoseStack poseStack, int i, int j, Item item) {
				this.blitSlotBg(poseStack, i + 1, j + 1);
				PresetFlatWorldScreen.this.itemRenderer.renderGuiItem(new ItemStack(item), i + 2, j + 2);
			}

			private void blitSlotBg(PoseStack poseStack, int i, int j) {
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
				GuiComponent.blit(poseStack, i, j, PresetFlatWorldScreen.this.getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", this.name);
			}
		}
	}
}
