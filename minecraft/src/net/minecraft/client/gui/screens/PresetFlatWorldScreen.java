package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class PresetFlatWorldScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final List<PresetFlatWorldScreen.PresetInfo> PRESETS = Lists.<PresetFlatWorldScreen.PresetInfo>newArrayList();
	private static final ResourceKey<Biome> DEFAULT_BIOME = Biomes.PLAINS;
	private final CreateFlatWorldScreen parent;
	private Component shareText;
	private Component listText;
	private PresetFlatWorldScreen.PresetsList list;
	private Button selectButton;
	private EditBox export;
	private FlatLevelGeneratorSettings settings;

	public PresetFlatWorldScreen(CreateFlatWorldScreen createFlatWorldScreen) {
		super(new TranslatableComponent("createWorld.customize.presets.title"));
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

		int k = Math.min(i + j, 256);
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
			FlatLayerInfo flatLayerInfo = new FlatLayerInfo(l, block);
			flatLayerInfo.setStart(i);
			return flatLayerInfo;
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

	public static FlatLevelGeneratorSettings fromString(Registry<Biome> registry, String string, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		Iterator<String> iterator = Splitter.on(';').split(string).iterator();
		if (!iterator.hasNext()) {
			return FlatLevelGeneratorSettings.getDefault(registry);
		} else {
			List<FlatLayerInfo> list = getLayersInfoFromString((String)iterator.next());
			if (list.isEmpty()) {
				return FlatLevelGeneratorSettings.getDefault(registry);
			} else {
				FlatLevelGeneratorSettings flatLevelGeneratorSettings2 = flatLevelGeneratorSettings.withLayers(list, flatLevelGeneratorSettings.structureSettings());
				ResourceKey<Biome> resourceKey = DEFAULT_BIOME;
				if (iterator.hasNext()) {
					try {
						ResourceLocation resourceLocation = new ResourceLocation((String)iterator.next());
						resourceKey = ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation);
						registry.getOptional(resourceKey).orElseThrow(() -> new IllegalArgumentException("Invalid Biome: " + resourceLocation));
					} catch (Exception var8) {
						LOGGER.error("Error while parsing flat world string => {}", var8.getMessage());
						resourceKey = DEFAULT_BIOME;
					}
				}

				ResourceKey<Biome> resourceKey2 = resourceKey;
				flatLevelGeneratorSettings2.setBiome(() -> registry.getOrThrow(resourceKey2));
				return flatLevelGeneratorSettings2;
			}
		}
	}

	private static String save(Registry<Biome> registry, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < flatLevelGeneratorSettings.getLayersInfo().size(); i++) {
			if (i > 0) {
				stringBuilder.append(",");
			}

			stringBuilder.append(flatLevelGeneratorSettings.getLayersInfo().get(i));
		}

		stringBuilder.append(";");
		stringBuilder.append(registry.getKey(flatLevelGeneratorSettings.getBiome()));
		return stringBuilder.toString();
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.shareText = new TranslatableComponent("createWorld.customize.presets.share");
		this.listText = new TranslatableComponent("createWorld.customize.presets.list");
		this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
		this.export.setMaxLength(1230);
		Registry<Biome> registry = this.parent.parent.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY);
		this.export.setValue(save(registry, this.parent.settings()));
		this.settings = this.parent.settings();
		this.children.add(this.export);
		this.list = new PresetFlatWorldScreen.PresetsList();
		this.children.add(this.list);
		this.selectButton = this.addButton(
			new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("createWorld.customize.presets.select"), button -> {
				FlatLevelGeneratorSettings flatLevelGeneratorSettings = fromString(registry, this.export.getValue(), this.settings);
				this.parent.setConfig(flatLevelGeneratorSettings);
				this.minecraft.setScreen(this.parent);
			})
		);
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)));
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
		poseStack.translate(0.0, 0.0, 400.0);
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

	private static void preset(
		Component component,
		ItemLike itemLike,
		ResourceKey<Biome> resourceKey,
		List<StructureFeature<?>> list,
		boolean bl,
		boolean bl2,
		boolean bl3,
		FlatLayerInfo... flatLayerInfos
	) {
		PRESETS.add(new PresetFlatWorldScreen.PresetInfo(itemLike.asItem(), component, registry -> {
			Map<StructureFeature<?>, StructureFeatureConfiguration> map = Maps.<StructureFeature<?>, StructureFeatureConfiguration>newHashMap();

			for (StructureFeature<?> structureFeature : list) {
				map.put(structureFeature, StructureSettings.DEFAULTS.get(structureFeature));
			}

			StructureSettings structureSettings = new StructureSettings(bl ? Optional.of(StructureSettings.DEFAULT_STRONGHOLD) : Optional.empty(), map);
			FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(structureSettings, registry);
			if (bl2) {
				flatLevelGeneratorSettings.setDecoration();
			}

			if (bl3) {
				flatLevelGeneratorSettings.setAddLakes();
			}

			for (int i = flatLayerInfos.length - 1; i >= 0; i--) {
				flatLevelGeneratorSettings.getLayersInfo().add(flatLayerInfos[i]);
			}

			flatLevelGeneratorSettings.setBiome(() -> registry.getOrThrow(resourceKey));
			flatLevelGeneratorSettings.updateLayers();
			return flatLevelGeneratorSettings.withStructureSettings(structureSettings);
		}));
	}

	static {
		preset(
			new TranslatableComponent("createWorld.customize.preset.classic_flat"),
			Blocks.GRASS_BLOCK,
			Biomes.PLAINS,
			Arrays.asList(StructureFeature.VILLAGE),
			false,
			false,
			false,
			new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
			new FlatLayerInfo(2, Blocks.DIRT),
			new FlatLayerInfo(1, Blocks.BEDROCK)
		);
		preset(
			new TranslatableComponent("createWorld.customize.preset.tunnelers_dream"),
			Blocks.STONE,
			Biomes.MOUNTAINS,
			Arrays.asList(StructureFeature.MINESHAFT),
			true,
			true,
			false,
			new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
			new FlatLayerInfo(5, Blocks.DIRT),
			new FlatLayerInfo(230, Blocks.STONE),
			new FlatLayerInfo(1, Blocks.BEDROCK)
		);
		preset(
			new TranslatableComponent("createWorld.customize.preset.water_world"),
			Items.WATER_BUCKET,
			Biomes.DEEP_OCEAN,
			Arrays.asList(StructureFeature.OCEAN_RUIN, StructureFeature.SHIPWRECK, StructureFeature.OCEAN_MONUMENT),
			false,
			false,
			false,
			new FlatLayerInfo(90, Blocks.WATER),
			new FlatLayerInfo(5, Blocks.SAND),
			new FlatLayerInfo(5, Blocks.DIRT),
			new FlatLayerInfo(5, Blocks.STONE),
			new FlatLayerInfo(1, Blocks.BEDROCK)
		);
		preset(
			new TranslatableComponent("createWorld.customize.preset.overworld"),
			Blocks.GRASS,
			Biomes.PLAINS,
			Arrays.asList(StructureFeature.VILLAGE, StructureFeature.MINESHAFT, StructureFeature.PILLAGER_OUTPOST, StructureFeature.RUINED_PORTAL),
			true,
			true,
			true,
			new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
			new FlatLayerInfo(3, Blocks.DIRT),
			new FlatLayerInfo(59, Blocks.STONE),
			new FlatLayerInfo(1, Blocks.BEDROCK)
		);
		preset(
			new TranslatableComponent("createWorld.customize.preset.snowy_kingdom"),
			Blocks.SNOW,
			Biomes.SNOWY_TUNDRA,
			Arrays.asList(StructureFeature.VILLAGE, StructureFeature.IGLOO),
			false,
			false,
			false,
			new FlatLayerInfo(1, Blocks.SNOW),
			new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
			new FlatLayerInfo(3, Blocks.DIRT),
			new FlatLayerInfo(59, Blocks.STONE),
			new FlatLayerInfo(1, Blocks.BEDROCK)
		);
		preset(
			new TranslatableComponent("createWorld.customize.preset.bottomless_pit"),
			Items.FEATHER,
			Biomes.PLAINS,
			Arrays.asList(StructureFeature.VILLAGE),
			false,
			false,
			false,
			new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
			new FlatLayerInfo(3, Blocks.DIRT),
			new FlatLayerInfo(2, Blocks.COBBLESTONE)
		);
		preset(
			new TranslatableComponent("createWorld.customize.preset.desert"),
			Blocks.SAND,
			Biomes.DESERT,
			Arrays.asList(StructureFeature.VILLAGE, StructureFeature.DESERT_PYRAMID, StructureFeature.MINESHAFT),
			true,
			true,
			false,
			new FlatLayerInfo(8, Blocks.SAND),
			new FlatLayerInfo(52, Blocks.SANDSTONE),
			new FlatLayerInfo(3, Blocks.STONE),
			new FlatLayerInfo(1, Blocks.BEDROCK)
		);
		preset(
			new TranslatableComponent("createWorld.customize.preset.redstone_ready"),
			Items.REDSTONE,
			Biomes.DESERT,
			Collections.emptyList(),
			false,
			false,
			false,
			new FlatLayerInfo(52, Blocks.SANDSTONE),
			new FlatLayerInfo(3, Blocks.STONE),
			new FlatLayerInfo(1, Blocks.BEDROCK)
		);
		preset(
			new TranslatableComponent("createWorld.customize.preset.the_void"),
			Blocks.BARRIER,
			Biomes.THE_VOID,
			Collections.emptyList(),
			false,
			true,
			false,
			new FlatLayerInfo(1, Blocks.AIR)
		);
	}

	@Environment(EnvType.CLIENT)
	static class PresetInfo {
		public final Item icon;
		public final Component name;
		public final Function<Registry<Biome>, FlatLevelGeneratorSettings> settings;

		public PresetInfo(Item item, Component component, Function<Registry<Biome>, FlatLevelGeneratorSettings> function) {
			this.icon = item;
			this.name = component;
			this.settings = function;
		}

		public Component getName() {
			return this.name;
		}
	}

	@Environment(EnvType.CLIENT)
	class PresetsList extends ObjectSelectionList<PresetFlatWorldScreen.PresetsList.Entry> {
		public PresetsList() {
			super(
				PresetFlatWorldScreen.this.minecraft, PresetFlatWorldScreen.this.width, PresetFlatWorldScreen.this.height, 80, PresetFlatWorldScreen.this.height - 37, 24
			);

			for (int i = 0; i < PresetFlatWorldScreen.PRESETS.size(); i++) {
				this.addEntry(new PresetFlatWorldScreen.PresetsList.Entry());
			}
		}

		public void setSelected(@Nullable PresetFlatWorldScreen.PresetsList.Entry entry) {
			super.setSelected(entry);
			if (entry != null) {
				NarratorChatListener.INSTANCE
					.sayNow(
						new TranslatableComponent(
								"narrator.select", ((PresetFlatWorldScreen.PresetInfo)PresetFlatWorldScreen.PRESETS.get(this.children().indexOf(entry))).getName()
							)
							.getString()
					);
			}

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
			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				PresetFlatWorldScreen.PresetInfo presetInfo = (PresetFlatWorldScreen.PresetInfo)PresetFlatWorldScreen.PRESETS.get(i);
				this.blitSlot(poseStack, k, j, presetInfo.icon);
				PresetFlatWorldScreen.this.font.draw(poseStack, presetInfo.name, (float)(k + 18 + 5), (float)(j + 6), 16777215);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					this.select();
				}

				return false;
			}

			private void select() {
				PresetsList.this.setSelected(this);
				PresetFlatWorldScreen.PresetInfo presetInfo = (PresetFlatWorldScreen.PresetInfo)PresetFlatWorldScreen.PRESETS
					.get(PresetsList.this.children().indexOf(this));
				Registry<Biome> registry = PresetFlatWorldScreen.this.parent.parent.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY);
				PresetFlatWorldScreen.this.settings = (FlatLevelGeneratorSettings)presetInfo.settings.apply(registry);
				PresetFlatWorldScreen.this.export.setValue(PresetFlatWorldScreen.save(registry, PresetFlatWorldScreen.this.settings));
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
		}
	}
}
