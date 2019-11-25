/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CreateFlatWorldScreen
extends Screen {
    private final CreateWorldScreen parent;
    private FlatLevelGeneratorSettings generator = FlatLevelGeneratorSettings.getDefault();
    private String columnType;
    private String columnHeight;
    private DetailsList list;
    private Button deleteLayerButton;

    public CreateFlatWorldScreen(CreateWorldScreen createWorldScreen, CompoundTag compoundTag) {
        super(new TranslatableComponent("createWorld.customize.flat.title", new Object[0]));
        this.parent = createWorldScreen;
        this.loadLayers(compoundTag);
    }

    public String saveLayerString() {
        return this.generator.toString();
    }

    public CompoundTag saveLayers() {
        return (CompoundTag)this.generator.toObject(NbtOps.INSTANCE).getValue();
    }

    public void loadLayers(String string) {
        this.generator = FlatLevelGeneratorSettings.fromString(string);
    }

    public void loadLayers(CompoundTag compoundTag) {
        this.generator = FlatLevelGeneratorSettings.fromObject(new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag));
    }

    @Override
    protected void init() {
        this.columnType = I18n.get("createWorld.customize.flat.tile", new Object[0]);
        this.columnHeight = I18n.get("createWorld.customize.flat.height", new Object[0]);
        this.list = new DetailsList();
        this.children.add(this.list);
        this.deleteLayerButton = this.addButton(new Button(this.width / 2 - 155, this.height - 52, 150, 20, I18n.get("createWorld.customize.flat.removeLayer", new Object[0]), button -> {
            if (!this.hasValidSelection()) {
                return;
            }
            List<FlatLayerInfo> list = this.generator.getLayersInfo();
            int i = this.list.children().indexOf(this.list.getSelected());
            int j = list.size() - i - 1;
            list.remove(j);
            this.list.setSelected(list.isEmpty() ? null : (DetailsList.Entry)this.list.children().get(Math.min(i, list.size() - 1)));
            this.generator.updateLayers();
            this.updateButtonValidity();
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 52, 150, 20, I18n.get("createWorld.customize.presets", new Object[0]), button -> {
            this.minecraft.setScreen(new PresetFlatWorldScreen(this));
            this.generator.updateLayers();
            this.updateButtonValidity();
        }));
        this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("gui.done", new Object[0]), button -> {
            this.parent.levelTypeOptions = this.saveLayers();
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
            this.updateButtonValidity();
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel", new Object[0]), button -> {
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
            this.updateButtonValidity();
        }));
        this.generator.updateLayers();
        this.updateButtonValidity();
    }

    public void updateButtonValidity() {
        this.deleteLayerButton.active = this.hasValidSelection();
        this.list.resetRows();
    }

    private boolean hasValidSelection() {
        return this.list.getSelected() != null;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.list.render(i, j, f);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 0xFFFFFF);
        int k = this.width / 2 - 92 - 16;
        this.drawString(this.font, this.columnType, k, 32, 0xFFFFFF);
        this.drawString(this.font, this.columnHeight, k + 2 + 213 - this.font.width(this.columnHeight), 32, 0xFFFFFF);
        super.render(i, j, f);
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
            FlatLayerInfo flatLayerInfo;
            Item item;
            super.setSelected(entry);
            if (entry != null && (item = (flatLayerInfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - this.children().indexOf(entry) - 1)).getBlockState().getBlock().asItem()) != Items.AIR) {
                NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", item.getName(new ItemStack(item))).getString());
            }
        }

        @Override
        protected void moveSelection(int i) {
            super.moveSelection(i);
            CreateFlatWorldScreen.this.updateButtonValidity();
        }

        @Override
        protected boolean isFocused() {
            return CreateFlatWorldScreen.this.getFocused() == this;
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

        @Override
        public /* synthetic */ void setSelected(@Nullable AbstractSelectionList.Entry entry) {
            this.setSelected((Entry)entry);
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private Entry() {
            }

            @Override
            public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                FlatLayerInfo flatLayerInfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - i - 1);
                BlockState blockState = flatLayerInfo.getBlockState();
                Block block = blockState.getBlock();
                Item item = block.asItem();
                if (item == Items.AIR) {
                    if (block == Blocks.WATER) {
                        item = Items.WATER_BUCKET;
                    } else if (block == Blocks.LAVA) {
                        item = Items.LAVA_BUCKET;
                    }
                }
                ItemStack itemStack = new ItemStack(item);
                String string = item.getName(itemStack).getColoredString();
                this.blitSlot(k, j, itemStack);
                CreateFlatWorldScreen.this.font.draw(string, k + 18 + 5, j + 3, 0xFFFFFF);
                String string2 = i == 0 ? I18n.get("createWorld.customize.flat.layer.top", flatLayerInfo.getHeight()) : (i == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1 ? I18n.get("createWorld.customize.flat.layer.bottom", flatLayerInfo.getHeight()) : I18n.get("createWorld.customize.flat.layer", flatLayerInfo.getHeight()));
                CreateFlatWorldScreen.this.font.draw(string2, k + 2 + 213 - CreateFlatWorldScreen.this.font.width(string2), j + 3, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    DetailsList.this.setSelected(this);
                    CreateFlatWorldScreen.this.updateButtonValidity();
                    return true;
                }
                return false;
            }

            private void blitSlot(int i, int j, ItemStack itemStack) {
                this.blitSlotBg(i + 1, j + 1);
                RenderSystem.enableRescaleNormal();
                if (!itemStack.isEmpty()) {
                    CreateFlatWorldScreen.this.itemRenderer.renderGuiItem(itemStack, i + 2, j + 2);
                }
                RenderSystem.disableRescaleNormal();
            }

            private void blitSlotBg(int i, int j) {
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                DetailsList.this.minecraft.getTextureManager().bind(GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(i, j, CreateFlatWorldScreen.this.getBlitOffset(), 0.0f, 0.0f, 18, 18, 128, 128);
            }
        }
    }
}

