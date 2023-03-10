/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PresetFlatWorldScreen
extends Screen {
    static final Logger LOGGER = LogUtils.getLogger();
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
    private PresetsList list;
    private Button selectButton;
    EditBox export;
    FlatLevelGeneratorSettings settings;

    public PresetFlatWorldScreen(CreateFlatWorldScreen createFlatWorldScreen) {
        super(Component.translatable("createWorld.customize.presets.title"));
        this.parent = createFlatWorldScreen;
    }

    @Nullable
    private static FlatLayerInfo getLayerInfoFromString(HolderGetter<Block> holderGetter, String string, int i) {
        Optional<Holder.Reference<Block>> optional;
        int j;
        String string2;
        List<String> list = Splitter.on('*').limit(2).splitToList(string);
        if (list.size() == 2) {
            string2 = list.get(1);
            try {
                j = Math.max(Integer.parseInt(list.get(0)), 0);
            } catch (NumberFormatException numberFormatException) {
                LOGGER.error("Error while parsing flat world string", numberFormatException);
                return null;
            }
        } else {
            string2 = list.get(0);
            j = 1;
        }
        int k = Math.min(i + j, DimensionType.Y_SIZE);
        int l = k - i;
        try {
            optional = holderGetter.get(ResourceKey.create(Registries.BLOCK, new ResourceLocation(string2)));
        } catch (Exception exception) {
            LOGGER.error("Error while parsing flat world string", exception);
            return null;
        }
        if (optional.isEmpty()) {
            LOGGER.error("Error while parsing flat world string => Unknown block, {}", (Object)string2);
            return null;
        }
        return new FlatLayerInfo(l, optional.get().value());
    }

    private static List<FlatLayerInfo> getLayersInfoFromString(HolderGetter<Block> holderGetter, String string) {
        ArrayList<FlatLayerInfo> list = Lists.newArrayList();
        String[] strings = string.split(",");
        int i = 0;
        for (String string2 : strings) {
            FlatLayerInfo flatLayerInfo = PresetFlatWorldScreen.getLayerInfoFromString(holderGetter, string2, i);
            if (flatLayerInfo == null) {
                return Collections.emptyList();
            }
            list.add(flatLayerInfo);
            i += flatLayerInfo.getHeight();
        }
        return list;
    }

    public static FlatLevelGeneratorSettings fromString(HolderGetter<Block> holderGetter, HolderGetter<Biome> holderGetter2, HolderGetter<StructureSet> holderGetter3, HolderGetter<PlacedFeature> holderGetter4, String string, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        Holder.Reference<Biome> reference;
        Iterator<String> iterator = Splitter.on(';').split(string).iterator();
        if (!iterator.hasNext()) {
            return FlatLevelGeneratorSettings.getDefault(holderGetter2, holderGetter3, holderGetter4);
        }
        List<FlatLayerInfo> list = PresetFlatWorldScreen.getLayersInfoFromString(holderGetter, iterator.next());
        if (list.isEmpty()) {
            return FlatLevelGeneratorSettings.getDefault(holderGetter2, holderGetter3, holderGetter4);
        }
        Holder<Biome> holder = reference = holderGetter2.getOrThrow(DEFAULT_BIOME);
        if (iterator.hasNext()) {
            String string2 = iterator.next();
            holder = Optional.ofNullable(ResourceLocation.tryParse(string2)).map(resourceLocation -> ResourceKey.create(Registries.BIOME, resourceLocation)).flatMap(holderGetter2::get).orElseGet(() -> {
                LOGGER.warn("Invalid biome: {}", (Object)string2);
                return reference;
            });
        }
        return flatLevelGeneratorSettings.withBiomeAndLayers(list, flatLevelGeneratorSettings.structureOverrides(), holder);
    }

    static String save(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < flatLevelGeneratorSettings.getLayersInfo().size(); ++i) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(flatLevelGeneratorSettings.getLayersInfo().get(i));
        }
        stringBuilder.append(";");
        stringBuilder.append(flatLevelGeneratorSettings.getBiome().unwrapKey().map(ResourceKey::location).orElseThrow(() -> new IllegalStateException("Biome not registered")));
        return stringBuilder.toString();
    }

    @Override
    protected void init() {
        this.shareText = Component.translatable("createWorld.customize.presets.share");
        this.listText = Component.translatable("createWorld.customize.presets.list");
        this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
        this.export.setMaxLength(1230);
        WorldCreationContext worldCreationContext = this.parent.parent.getUiState().getSettings();
        RegistryAccess.Frozen registryAccess = worldCreationContext.worldgenLoadContext();
        FeatureFlagSet featureFlagSet = worldCreationContext.dataConfiguration().enabledFeatures();
        HolderLookup.RegistryLookup<Biome> holderGetter = registryAccess.lookupOrThrow(Registries.BIOME);
        HolderLookup.RegistryLookup<StructureSet> holderGetter2 = registryAccess.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderLookup.RegistryLookup<PlacedFeature> holderGetter3 = registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);
        HolderLookup<Block> holderGetter4 = registryAccess.lookupOrThrow(Registries.BLOCK).filterFeatures(featureFlagSet);
        this.export.setValue(PresetFlatWorldScreen.save(this.parent.settings()));
        this.settings = this.parent.settings();
        this.addWidget(this.export);
        this.list = new PresetsList(registryAccess, featureFlagSet);
        this.addWidget(this.list);
        this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets.select"), button -> {
            FlatLevelGeneratorSettings flatLevelGeneratorSettings = PresetFlatWorldScreen.fromString(holderGetter4, holderGetter, holderGetter2, holderGetter3, this.export.getValue(), this.settings);
            this.parent.setConfig(flatLevelGeneratorSettings);
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
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
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.list.render(poseStack, i, j, f);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 400.0f);
        PresetFlatWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        PresetFlatWorldScreen.drawString(poseStack, this.font, this.shareText, 50, 30, 0xA0A0A0);
        PresetFlatWorldScreen.drawString(poseStack, this.font, this.listText, 50, 70, 0xA0A0A0);
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

    @Environment(value=EnvType.CLIENT)
    class PresetsList
    extends ObjectSelectionList<Entry> {
        public PresetsList(RegistryAccess registryAccess, FeatureFlagSet featureFlagSet) {
            super(PresetFlatWorldScreen.this.minecraft, PresetFlatWorldScreen.this.width, PresetFlatWorldScreen.this.height, 80, PresetFlatWorldScreen.this.height - 37, 24);
            for (Holder<FlatLevelGeneratorPreset> holder : registryAccess.registryOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET).getTagOrEmpty(FlatLevelGeneratorPresetTags.VISIBLE)) {
                Set set = holder.value().settings().getLayersInfo().stream().map(flatLayerInfo -> flatLayerInfo.getBlockState().getBlock()).filter(block -> !block.isEnabled(featureFlagSet)).collect(Collectors.toSet());
                if (!set.isEmpty()) {
                    LOGGER.info("Discarding flat world preset {} since it contains experimental blocks {}", (Object)holder.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("<unknown>"), (Object)set);
                    continue;
                }
                this.addEntry(new Entry(holder));
            }
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            PresetFlatWorldScreen.this.updateButtonValidity(entry != null);
        }

        @Override
        public boolean keyPressed(int i, int j, int k) {
            if (super.keyPressed(i, j, k)) {
                return true;
            }
            if ((i == 257 || i == 335) && this.getSelected() != null) {
                ((Entry)this.getSelected()).select();
            }
            return false;
        }

        @Environment(value=EnvType.CLIENT)
        public class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private final FlatLevelGeneratorPreset preset;
            private final Component name;

            public Entry(Holder<FlatLevelGeneratorPreset> holder) {
                this.preset = holder.value();
                this.name = holder.unwrapKey().map(resourceKey -> Component.translatable(resourceKey.location().toLanguageKey("flat_world_preset"))).orElse(UNKNOWN_PRESET);
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                this.blitSlot(poseStack, k, j, this.preset.displayItem().value());
                PresetFlatWorldScreen.this.font.draw(poseStack, this.name, (float)(k + 18 + 5), (float)(j + 6), 0xFFFFFF);
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
                PresetFlatWorldScreen.this.itemRenderer.renderGuiItem(poseStack, new ItemStack(item), i + 2, j + 2);
            }

            private void blitSlotBg(PoseStack poseStack, int i, int j) {
                RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(poseStack, i, j, 0, 0.0f, 0.0f, 18, 18, 128, 128);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }
        }
    }
}

