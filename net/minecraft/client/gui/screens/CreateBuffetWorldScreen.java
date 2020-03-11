/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.datafixers.Dynamic;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CreateBuffetWorldScreen
extends Screen {
    private static final List<ResourceLocation> GENERATORS = Registry.CHUNK_GENERATOR_TYPE.keySet().stream().filter(resourceLocation -> Registry.CHUNK_GENERATOR_TYPE.get((ResourceLocation)resourceLocation).isPublic()).collect(Collectors.toList());
    private final CreateWorldScreen parent;
    private final CompoundTag optionsTag;
    private BiomeList list;
    private int generatorIndex;
    private Button doneButton;

    public CreateBuffetWorldScreen(CreateWorldScreen createWorldScreen, ChunkGeneratorProvider chunkGeneratorProvider) {
        super(new TranslatableComponent("createWorld.customize.buffet.title", new Object[0]));
        this.parent = createWorldScreen;
        this.optionsTag = chunkGeneratorProvider.getType() == LevelType.BUFFET ? (CompoundTag)chunkGeneratorProvider.getSettings().convert(NbtOps.INSTANCE).getValue() : new CompoundTag();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button((this.width - 200) / 2, 40, 200, 20, I18n.get("createWorld.customize.buffet.generatortype", new Object[0]) + " " + I18n.get(Util.makeDescriptionId("generator", GENERATORS.get(this.generatorIndex)), new Object[0]), button -> {
            ++this.generatorIndex;
            if (this.generatorIndex >= GENERATORS.size()) {
                this.generatorIndex = 0;
            }
            button.setMessage(I18n.get("createWorld.customize.buffet.generatortype", new Object[0]) + " " + I18n.get(Util.makeDescriptionId("generator", GENERATORS.get(this.generatorIndex)), new Object[0]));
        }));
        this.list = new BiomeList();
        this.children.add(this.list);
        this.doneButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("gui.done", new Object[0]), button -> {
            this.parent.levelTypeOptions = LevelType.BUFFET.createProvider(new Dynamic<CompoundTag>(NbtOps.INSTANCE, this.saveOptions()));
            this.minecraft.setScreen(this.parent);
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel", new Object[0]), button -> this.minecraft.setScreen(this.parent)));
        this.loadOptions();
        this.updateButtonValidity();
    }

    private void loadOptions() {
        int i;
        if (this.optionsTag.contains("chunk_generator", 10) && this.optionsTag.getCompound("chunk_generator").contains("type", 8)) {
            ResourceLocation resourceLocation = new ResourceLocation(this.optionsTag.getCompound("chunk_generator").getString("type"));
            for (i = 0; i < GENERATORS.size(); ++i) {
                if (!GENERATORS.get(i).equals(resourceLocation)) continue;
                this.generatorIndex = i;
                break;
            }
        }
        if (this.optionsTag.contains("biome_source", 10) && this.optionsTag.getCompound("biome_source").contains("biomes", 9)) {
            ListTag listTag = this.optionsTag.getCompound("biome_source").getList("biomes", 8);
            for (i = 0; i < listTag.size(); ++i) {
                ResourceLocation resourceLocation2 = new ResourceLocation(listTag.getString(i));
                this.list.setSelected((BiomeList.Entry)this.list.children().stream().filter(entry -> Objects.equals(((BiomeList.Entry)entry).key, resourceLocation2)).findFirst().orElse(null));
            }
        }
        this.optionsTag.remove("chunk_generator");
        this.optionsTag.remove("biome_source");
    }

    private CompoundTag saveOptions() {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.putString("type", Registry.BIOME_SOURCE_TYPE.getKey(BiomeSourceType.FIXED).toString());
        CompoundTag compoundTag3 = new CompoundTag();
        ListTag listTag = new ListTag();
        listTag.add(StringTag.valueOf(((BiomeList.Entry)this.list.getSelected()).key.toString()));
        compoundTag3.put("biomes", listTag);
        compoundTag2.put("options", compoundTag3);
        CompoundTag compoundTag4 = new CompoundTag();
        CompoundTag compoundTag5 = new CompoundTag();
        compoundTag4.putString("type", GENERATORS.get(this.generatorIndex).toString());
        compoundTag5.putString("default_block", "minecraft:stone");
        compoundTag5.putString("default_fluid", "minecraft:water");
        compoundTag4.put("options", compoundTag5);
        compoundTag.put("biome_source", compoundTag2);
        compoundTag.put("chunk_generator", compoundTag4);
        return compoundTag;
    }

    public void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderDirtBackground(0);
        this.list.render(i, j, f);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 0xFFFFFF);
        this.drawCenteredString(this.font, I18n.get("createWorld.customize.buffet.generator", new Object[0]), this.width / 2, 30, 0xA0A0A0);
        this.drawCenteredString(this.font, I18n.get("createWorld.customize.buffet.biome", new Object[0]), this.width / 2, 68, 0xA0A0A0);
        super.render(i, j, f);
    }

    @Environment(value=EnvType.CLIENT)
    class BiomeList
    extends ObjectSelectionList<Entry> {
        private BiomeList() {
            super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height, 80, CreateBuffetWorldScreen.this.height - 37, 16);
            Registry.BIOME.keySet().stream().sorted(Comparator.comparing(resourceLocation -> Registry.BIOME.get((ResourceLocation)resourceLocation).getName().getString())).forEach(resourceLocation -> this.addEntry(new Entry((ResourceLocation)resourceLocation)));
        }

        @Override
        protected boolean isFocused() {
            return CreateBuffetWorldScreen.this.getFocused() == this;
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", Registry.BIOME.get(entry.key).getName().getString()).getString());
            }
        }

        @Override
        protected void moveSelection(int i) {
            super.moveSelection(i);
            CreateBuffetWorldScreen.this.updateButtonValidity();
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private final ResourceLocation key;

            public Entry(ResourceLocation resourceLocation) {
                this.key = resourceLocation;
            }

            @Override
            public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                BiomeList.this.drawString(CreateBuffetWorldScreen.this.font, Registry.BIOME.get(this.key).getName().getString(), k + 5, j + 2, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    BiomeList.this.setSelected(this);
                    CreateBuffetWorldScreen.this.updateButtonValidity();
                    return true;
                }
                return false;
            }
        }
    }
}

