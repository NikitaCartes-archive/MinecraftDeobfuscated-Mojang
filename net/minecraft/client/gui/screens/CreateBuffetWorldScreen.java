/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CreateBuffetWorldScreen
extends Screen {
    private final Screen parent;
    private final Consumer<Biome> applySettings;
    private BiomeList list;
    private Biome biome;
    private Button doneButton;

    public CreateBuffetWorldScreen(Screen screen, Consumer<Biome> consumer, Biome biome) {
        super(new TranslatableComponent("createWorld.customize.buffet.title"));
        this.parent = screen;
        this.applySettings = consumer;
        this.biome = biome;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.list = new BiomeList();
        this.children.add(this.list);
        this.doneButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, button -> {
            this.applySettings.accept(this.biome);
            this.minecraft.setScreen(this.parent);
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)));
        this.list.setSelected((BiomeList.Entry)this.list.children().stream().filter(entry -> Objects.equals(((BiomeList.Entry)entry).biome, this.biome)).findFirst().orElse(null));
    }

    private void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(0);
        this.list.render(poseStack, i, j, f);
        this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        this.drawCenteredString(poseStack, this.font, I18n.get("createWorld.customize.buffet.biome", new Object[0]), this.width / 2, 28, 0xA0A0A0);
        super.render(poseStack, i, j, f);
    }

    @Environment(value=EnvType.CLIENT)
    class BiomeList
    extends ObjectSelectionList<Entry> {
        private BiomeList() {
            super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height, 40, CreateBuffetWorldScreen.this.height - 37, 16);
            Registry.BIOME.stream().sorted(Comparator.comparing(biome -> biome.getName().getString())).forEach(biome -> this.addEntry(new Entry((Biome)biome)));
        }

        @Override
        protected boolean isFocused() {
            return CreateBuffetWorldScreen.this.getFocused() == this;
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                CreateBuffetWorldScreen.this.biome = entry.biome;
                NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", entry.biome.getName().getString()).getString());
            }
            CreateBuffetWorldScreen.this.updateButtonValidity();
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private final Biome biome;

            public Entry(Biome biome) {
                this.biome = biome;
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                BiomeList.this.drawString(poseStack, CreateBuffetWorldScreen.this.font, this.biome.getName().getString(), k + 5, j + 2, 0xFFFFFF);
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

