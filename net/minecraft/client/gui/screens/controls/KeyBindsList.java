/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.ArrayUtils;

@Environment(value=EnvType.CLIENT)
public class KeyBindsList
extends ContainerObjectSelectionList<Entry> {
    final KeyBindsScreen keyBindsScreen;
    int maxNameWidth;

    public KeyBindsList(KeyBindsScreen keyBindsScreen, Minecraft minecraft) {
        super(minecraft, keyBindsScreen.width + 45, keyBindsScreen.height, 20, keyBindsScreen.height - 32, 20);
        this.keyBindsScreen = keyBindsScreen;
        Object[] keyMappings = ArrayUtils.clone(minecraft.options.keyMappings);
        Arrays.sort(keyMappings);
        String string = null;
        for (Object keyMapping : keyMappings) {
            MutableComponent component;
            int i;
            String string2 = ((KeyMapping)keyMapping).getCategory();
            if (!string2.equals(string)) {
                string = string2;
                this.addEntry(new CategoryEntry(Component.translatable(string2)));
            }
            if ((i = minecraft.font.width(component = Component.translatable(((KeyMapping)keyMapping).getName()))) > this.maxNameWidth) {
                this.maxNameWidth = i;
            }
            this.addEntry(new KeyEntry((KeyMapping)keyMapping, component));
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    @Environment(value=EnvType.CLIENT)
    public class CategoryEntry
    extends Entry {
        final Component name;
        private final int width;

        public CategoryEntry(Component component) {
            this.name = component;
            this.width = ((KeyBindsList)KeyBindsList.this).minecraft.font.width(this.name);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            ((KeyBindsList)KeyBindsList.this).minecraft.font.draw(poseStack, this.name, (float)(((KeyBindsList)KeyBindsList.this).minecraft.screen.width / 2 - this.width / 2), (float)(j + m - ((KeyBindsList)KeyBindsList.this).minecraft.font.lineHeight - 1), 0xFFFFFF);
        }

        @Override
        public boolean changeFocus(boolean bl) {
            return false;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry(){

                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput narrationElementOutput) {
                    narrationElementOutput.add(NarratedElementType.TITLE, CategoryEntry.this.name);
                }
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class KeyEntry
    extends Entry {
        private final KeyMapping key;
        private final Component name;
        private final Button changeButton;
        private final Button resetButton;

        KeyEntry(KeyMapping keyMapping, Component component) {
            this.key = keyMapping;
            this.name = component;
            this.changeButton = Button.builder(component, button -> {
                KeyBindsList.this.keyBindsScreen.selectedKey = keyMapping;
            }).bounds(0, 0, 75, 20).createNarration(supplier -> {
                if (keyMapping.isUnbound()) {
                    return Component.translatable("narrator.controls.unbound", component);
                }
                return Component.translatable("narrator.controls.bound", component, supplier.get());
            }).build();
            this.resetButton = Button.builder(Component.translatable("controls.reset"), button -> {
                ((KeyBindsList)KeyBindsList.this).minecraft.options.setKey(keyMapping, keyMapping.getDefaultKey());
                KeyMapping.resetMapping();
            }).bounds(0, 0, 50, 20).createNarration(supplier -> Component.translatable("narrator.controls.reset", component)).build();
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            boolean bl2 = KeyBindsList.this.keyBindsScreen.selectedKey == this.key;
            ((KeyBindsList)KeyBindsList.this).minecraft.font.draw(poseStack, this.name, (float)(k + 90 - KeyBindsList.this.maxNameWidth), (float)(j + m / 2 - ((KeyBindsList)KeyBindsList.this).minecraft.font.lineHeight / 2), 0xFFFFFF);
            this.resetButton.setX(k + 190);
            this.resetButton.setY(j);
            this.resetButton.active = !this.key.isDefault();
            this.resetButton.render(poseStack, n, o, f);
            this.changeButton.setX(k + 105);
            this.changeButton.setY(j);
            this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
            boolean bl3 = false;
            if (!this.key.isUnbound()) {
                for (KeyMapping keyMapping : ((KeyBindsList)KeyBindsList.this).minecraft.options.keyMappings) {
                    if (keyMapping == this.key || !this.key.same(keyMapping)) continue;
                    bl3 = true;
                    break;
                }
            }
            if (bl2) {
                this.changeButton.setMessage(Component.literal("> ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));
            } else if (bl3) {
                this.changeButton.setMessage(this.changeButton.getMessage().copy().withStyle(ChatFormatting.RED));
            }
            this.changeButton.render(poseStack, n, o, f);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.changeButton, this.resetButton);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (this.changeButton.mouseClicked(d, e, i)) {
                return true;
            }
            return this.resetButton.mouseClicked(d, e, i);
        }

        @Override
        public boolean mouseReleased(double d, double e, int i) {
            return this.changeButton.mouseReleased(d, e, i) || this.resetButton.mouseReleased(d, e, i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
    extends ContainerObjectSelectionList.Entry<Entry> {
    }
}

