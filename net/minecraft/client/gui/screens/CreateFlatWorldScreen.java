/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CreateFlatWorldScreen
extends Screen {
    private static final int SLOT_TEX_SIZE = 128;
    private static final int SLOT_BG_SIZE = 18;
    private static final int SLOT_STAT_HEIGHT = 20;
    private static final int SLOT_BG_X = 1;
    private static final int SLOT_BG_Y = 1;
    private static final int SLOT_FG_X = 2;
    private static final int SLOT_FG_Y = 2;
    protected final CreateWorldScreen parent;
    private final Consumer<FlatLevelGeneratorSettings> applySettings;
    FlatLevelGeneratorSettings generator;
    private Component columnType;
    private Component columnHeight;
    private DetailsList list;
    private Button deleteLayerButton;

    public CreateFlatWorldScreen(CreateWorldScreen createWorldScreen, Consumer<FlatLevelGeneratorSettings> consumer, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        super(Component.translatable("createWorld.customize.flat.title"));
        this.parent = createWorldScreen;
        this.applySettings = consumer;
        this.generator = flatLevelGeneratorSettings;
    }

    public FlatLevelGeneratorSettings settings() {
        return this.generator;
    }

    public void setConfig(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        this.generator = flatLevelGeneratorSettings;
    }

    @Override
    protected void init() {
        this.columnType = Component.translatable("createWorld.customize.flat.tile");
        this.columnHeight = Component.translatable("createWorld.customize.flat.height");
        this.list = new DetailsList();
        this.addWidget(this.list);
        this.deleteLayerButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.flat.removeLayer"), button -> {
            if (!this.hasValidSelection()) {
                return;
            }
            List<FlatLayerInfo> list = this.generator.getLayersInfo();
            int i = this.list.children().indexOf(this.list.getSelected());
            int j = list.size() - i - 1;
            list.remove(j);
            this.list.setSelected(list.isEmpty() ? null : (DetailsList.Entry)this.list.children().get(Math.min(i, list.size() - 1)));
            this.generator.updateLayers();
            this.list.resetRows();
            this.updateButtonValidity();
        }).bounds(this.width / 2 - 155, this.height - 52, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets"), button -> {
            this.minecraft.setScreen(new PresetFlatWorldScreen(this));
            this.generator.updateLayers();
            this.updateButtonValidity();
        }).bounds(this.width / 2 + 5, this.height - 52, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.applySettings.accept(this.generator);
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
        }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
        this.generator.updateLayers();
        this.updateButtonValidity();
    }

    void updateButtonValidity() {
        this.deleteLayerButton.active = this.hasValidSelection();
    }

    private boolean hasValidSelection() {
        return this.list.getSelected() != null;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.list.render(poseStack, i, j, f);
        CreateFlatWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        int k = this.width / 2 - 92 - 16;
        CreateFlatWorldScreen.drawString(poseStack, this.font, this.columnType, k, 32, 0xFFFFFF);
        CreateFlatWorldScreen.drawString(poseStack, this.font, this.columnHeight, k + 2 + 213 - this.font.width(this.columnHeight), 32, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    @Environment(value=EnvType.CLIENT)
    class DetailsList
    extends ObjectSelectionList<Entry> {
        public DetailsList() {
            super(CreateFlatWorldScreen.this.minecraft, CreateFlatWorldScreen.this.width, CreateFlatWorldScreen.this.height, 43, CreateFlatWorldScreen.this.height - 60, 24);
            for (int i = 0; i < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++i) {
                this.addEntry(new Entry());
            }
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            CreateFlatWorldScreen.this.updateButtonValidity();
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 70;
        }

        public void resetRows() {
            int i = this.children().indexOf(this.getSelected());
            this.clearEntries();
            for (int j = 0; j < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++j) {
                this.addEntry(new Entry());
            }
            List list = this.children();
            if (i >= 0 && i < list.size()) {
                this.setSelected((Entry)list.get(i));
            }
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            Entry() {
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                FlatLayerInfo flatLayerInfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - i - 1);
                BlockState blockState = flatLayerInfo.getBlockState();
                ItemStack itemStack = this.getDisplayItem(blockState);
                this.blitSlot(poseStack, k, j, itemStack);
                CreateFlatWorldScreen.this.font.draw(poseStack, itemStack.getHoverName(), (float)(k + 18 + 5), (float)(j + 3), 0xFFFFFF);
                MutableComponent component = i == 0 ? Component.translatable("createWorld.customize.flat.layer.top", flatLayerInfo.getHeight()) : (i == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1 ? Component.translatable("createWorld.customize.flat.layer.bottom", flatLayerInfo.getHeight()) : Component.translatable("createWorld.customize.flat.layer", flatLayerInfo.getHeight()));
                CreateFlatWorldScreen.this.font.draw(poseStack, component, (float)(k + 2 + 213 - CreateFlatWorldScreen.this.font.width(component)), (float)(j + 3), 0xFFFFFF);
            }

            private ItemStack getDisplayItem(BlockState blockState) {
                Item item = blockState.getBlock().asItem();
                if (item == Items.AIR) {
                    if (blockState.is(Blocks.WATER)) {
                        item = Items.WATER_BUCKET;
                    } else if (blockState.is(Blocks.LAVA)) {
                        item = Items.LAVA_BUCKET;
                    }
                }
                return new ItemStack(item);
            }

            @Override
            public Component getNarration() {
                FlatLayerInfo flatLayerInfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - DetailsList.this.children().indexOf(this) - 1);
                ItemStack itemStack = this.getDisplayItem(flatLayerInfo.getBlockState());
                if (!itemStack.isEmpty()) {
                    return Component.translatable("narrator.select", itemStack.getHoverName());
                }
                return CommonComponents.EMPTY;
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    DetailsList.this.setSelected(this);
                    return true;
                }
                return false;
            }

            private void blitSlot(PoseStack poseStack, int i, int j, ItemStack itemStack) {
                this.blitSlotBg(poseStack, i + 1, j + 1);
                if (!itemStack.isEmpty()) {
                    CreateFlatWorldScreen.this.itemRenderer.renderGuiItem(poseStack, itemStack, i + 2, j + 2);
                }
            }

            private void blitSlotBg(PoseStack poseStack, int i, int j) {
                RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(poseStack, i, j, 0, 0.0f, 0.0f, 18, 18, 128, 128);
            }
        }
    }
}

