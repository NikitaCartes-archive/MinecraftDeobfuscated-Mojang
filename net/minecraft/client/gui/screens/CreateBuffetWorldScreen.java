/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.ibm.icu.text.Collator;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CreateBuffetWorldScreen
extends Screen {
    private static final Component BIOME_SELECT_INFO = Component.translatable("createWorld.customize.buffet.biome");
    private final Screen parent;
    private final Consumer<Holder<Biome>> applySettings;
    final Registry<Biome> biomes;
    private BiomeList list;
    Holder<Biome> biome;
    private Button doneButton;

    public CreateBuffetWorldScreen(Screen screen, WorldCreationContext worldCreationContext, Consumer<Holder<Biome>> consumer) {
        super(Component.translatable("createWorld.customize.buffet.title"));
        this.parent = screen;
        this.applySettings = consumer;
        this.biomes = worldCreationContext.worldgenLoadContext().registryOrThrow(Registries.BIOME);
        Holder holder = this.biomes.getHolder(Biomes.PLAINS).or(() -> this.biomes.holders().findAny()).orElseThrow();
        this.biome = worldCreationContext.selectedDimensions().overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(holder);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.list = new BiomeList();
        this.addWidget(this.list);
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.applySettings.accept(this.biome);
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
        this.list.setSelected((BiomeList.Entry)this.list.children().stream().filter(entry -> Objects.equals(entry.biome, this.biome)).findFirst().orElse(null));
    }

    void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(poseStack);
        this.list.render(poseStack, i, j, f);
        CreateBuffetWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        CreateBuffetWorldScreen.drawCenteredString(poseStack, this.font, BIOME_SELECT_INFO, this.width / 2, 28, 0xA0A0A0);
        super.render(poseStack, i, j, f);
    }

    @Environment(value=EnvType.CLIENT)
    class BiomeList
    extends ObjectSelectionList<Entry> {
        BiomeList() {
            super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height, 40, CreateBuffetWorldScreen.this.height - 37, 16);
            Collator collator = Collator.getInstance(Locale.getDefault());
            CreateBuffetWorldScreen.this.biomes.holders().map(reference -> new Entry((Holder.Reference<Biome>)reference)).sorted(Comparator.comparing(entry -> entry.name.getString(), collator)).forEach(entry -> this.addEntry(entry));
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                CreateBuffetWorldScreen.this.biome = entry.biome;
            }
            CreateBuffetWorldScreen.this.updateButtonValidity();
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            final Holder.Reference<Biome> biome;
            final Component name;

            public Entry(Holder.Reference<Biome> reference) {
                this.biome = reference;
                ResourceLocation resourceLocation = reference.key().location();
                String string = resourceLocation.toLanguageKey("biome");
                this.name = Language.getInstance().has(string) ? Component.translatable(string) : Component.literal(resourceLocation.toString());
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                GuiComponent.drawString(poseStack, CreateBuffetWorldScreen.this.font, this.name, k + 5, j + 2, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    BiomeList.this.setSelected(this);
                    return true;
                }
                return false;
            }
        }
    }
}

