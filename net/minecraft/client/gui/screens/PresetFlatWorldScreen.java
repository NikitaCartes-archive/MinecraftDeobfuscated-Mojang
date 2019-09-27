/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PresetFlatWorldScreen
extends Screen {
    private static final List<PresetInfo> PRESETS = Lists.newArrayList();
    private final CreateFlatWorldScreen parent;
    private String shareText;
    private String listText;
    private PresetsList list;
    private Button selectButton;
    private EditBox export;

    public PresetFlatWorldScreen(CreateFlatWorldScreen createFlatWorldScreen) {
        super(new TranslatableComponent("createWorld.customize.presets.title", new Object[0]));
        this.parent = createFlatWorldScreen;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.shareText = I18n.get("createWorld.customize.presets.share", new Object[0]);
        this.listText = I18n.get("createWorld.customize.presets.list", new Object[0]);
        this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
        this.export.setMaxLength(1230);
        this.export.setValue(this.parent.saveLayerString());
        this.children.add(this.export);
        this.list = new PresetsList();
        this.children.add(this.list);
        this.selectButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("createWorld.customize.presets.select", new Object[0]), button -> {
            this.parent.loadLayers(this.export.getValue());
            this.minecraft.setScreen(this.parent);
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel", new Object[0]), button -> this.minecraft.setScreen(this.parent)));
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
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.list.render(i, j, f);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 0xFFFFFF);
        this.drawString(this.font, this.shareText, 50, 30, 0xA0A0A0);
        this.drawString(this.font, this.listText, 50, 70, 0xA0A0A0);
        this.export.render(i, j, f);
        super.render(i, j, f);
    }

    @Override
    public void tick() {
        this.export.tick();
        super.tick();
    }

    public void updateButtonValidity(boolean bl) {
        this.selectButton.active = bl || this.export.getValue().length() > 1;
    }

    private static void preset(String string, ItemLike itemLike, Biome biome, List<String> list, FlatLayerInfo ... flatLayerInfos) {
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = ChunkGeneratorType.FLAT.createSettings();
        for (int i = flatLayerInfos.length - 1; i >= 0; --i) {
            flatLevelGeneratorSettings.getLayersInfo().add(flatLayerInfos[i]);
        }
        flatLevelGeneratorSettings.setBiome(biome);
        flatLevelGeneratorSettings.updateLayers();
        for (String string2 : list) {
            flatLevelGeneratorSettings.getStructuresOptions().put(string2, Maps.newHashMap());
        }
        PRESETS.add(new PresetInfo(itemLike.asItem(), string, flatLevelGeneratorSettings.toString()));
    }

    static {
        PresetFlatWorldScreen.preset(I18n.get("createWorld.customize.preset.classic_flat", new Object[0]), Blocks.GRASS_BLOCK, Biomes.PLAINS, Arrays.asList("village"), new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(2, Blocks.DIRT), new FlatLayerInfo(1, Blocks.BEDROCK));
        PresetFlatWorldScreen.preset(I18n.get("createWorld.customize.preset.tunnelers_dream", new Object[0]), Blocks.STONE, Biomes.MOUNTAINS, Arrays.asList("biome_1", "dungeon", "decoration", "stronghold", "mineshaft"), new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(230, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        PresetFlatWorldScreen.preset(I18n.get("createWorld.customize.preset.water_world", new Object[0]), Items.WATER_BUCKET, Biomes.DEEP_OCEAN, Arrays.asList("biome_1", "oceanmonument"), new FlatLayerInfo(90, Blocks.WATER), new FlatLayerInfo(5, Blocks.SAND), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(5, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        PresetFlatWorldScreen.preset(I18n.get("createWorld.customize.preset.overworld", new Object[0]), Blocks.GRASS, Biomes.PLAINS, Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon", "lake", "lava_lake", "pillager_outpost"), new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        PresetFlatWorldScreen.preset(I18n.get("createWorld.customize.preset.snowy_kingdom", new Object[0]), Blocks.SNOW, Biomes.SNOWY_TUNDRA, Arrays.asList("village", "biome_1"), new FlatLayerInfo(1, Blocks.SNOW), new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        PresetFlatWorldScreen.preset(I18n.get("createWorld.customize.preset.bottomless_pit", new Object[0]), Items.FEATHER, Biomes.PLAINS, Arrays.asList("village", "biome_1"), new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(2, Blocks.COBBLESTONE));
        PresetFlatWorldScreen.preset(I18n.get("createWorld.customize.preset.desert", new Object[0]), Blocks.SAND, Biomes.DESERT, Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon"), new FlatLayerInfo(8, Blocks.SAND), new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        PresetFlatWorldScreen.preset(I18n.get("createWorld.customize.preset.redstone_ready", new Object[0]), Items.REDSTONE, Biomes.DESERT, Collections.emptyList(), new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
        PresetFlatWorldScreen.preset(I18n.get("createWorld.customize.preset.the_void", new Object[0]), Blocks.BARRIER, Biomes.THE_VOID, Arrays.asList("decoration"), new FlatLayerInfo(1, Blocks.AIR));
    }

    @Environment(value=EnvType.CLIENT)
    static class PresetInfo {
        public final Item icon;
        public final String name;
        public final String value;

        public PresetInfo(Item item, String string, String string2) {
            this.icon = item;
            this.name = string;
            this.value = string2;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class PresetsList
    extends ObjectSelectionList<Entry> {
        public PresetsList() {
            super(PresetFlatWorldScreen.this.minecraft, PresetFlatWorldScreen.this.width, PresetFlatWorldScreen.this.height, 80, PresetFlatWorldScreen.this.height - 37, 24);
            for (int i = 0; i < PRESETS.size(); ++i) {
                this.addEntry(new Entry());
            }
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", ((PresetInfo)PRESETS.get((int)this.children().indexOf((Object)entry))).name).getString());
            }
        }

        @Override
        protected void moveSelection(int i) {
            super.moveSelection(i);
            PresetFlatWorldScreen.this.updateButtonValidity(true);
        }

        @Override
        protected boolean isFocused() {
            return PresetFlatWorldScreen.this.getFocused() == this;
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

        @Override
        public /* synthetic */ void setSelected(@Nullable AbstractSelectionList.Entry entry) {
            this.setSelected((Entry)entry);
        }

        @Environment(value=EnvType.CLIENT)
        public class Entry
        extends ObjectSelectionList.Entry<Entry> {
            @Override
            public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                PresetInfo presetInfo = (PresetInfo)PRESETS.get(i);
                this.blitSlot(k, j, presetInfo.icon);
                PresetFlatWorldScreen.this.font.draw(presetInfo.name, k + 18 + 5, j + 6, 0xFFFFFF);
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
                PresetFlatWorldScreen.this.updateButtonValidity(true);
                PresetFlatWorldScreen.this.export.setValue(((PresetInfo)PRESETS.get((int)PresetsList.this.children().indexOf((Object)this))).value);
                PresetFlatWorldScreen.this.export.moveCursorToStart();
            }

            private void blitSlot(int i, int j, Item item) {
                this.blitSlotBg(i + 1, j + 1);
                RenderSystem.enableRescaleNormal();
                PresetFlatWorldScreen.this.itemRenderer.renderGuiItem(new ItemStack(item), i + 2, j + 2);
                RenderSystem.disableRescaleNormal();
            }

            private void blitSlotBg(int i, int j) {
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                PresetsList.this.minecraft.getTextureManager().bind(GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(i, j, PresetFlatWorldScreen.this.getBlitOffset(), 0.0f, 0.0f, 18, 18, 128, 128);
            }
        }
    }
}

