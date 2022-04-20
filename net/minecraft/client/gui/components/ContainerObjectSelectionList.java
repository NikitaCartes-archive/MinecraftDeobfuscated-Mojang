/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class ContainerObjectSelectionList<E extends Entry<E>>
extends AbstractSelectionList<E> {
    private boolean hasFocus;

    public ContainerObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }

    @Override
    public boolean changeFocus(boolean bl) {
        this.hasFocus = super.changeFocus(bl);
        if (this.hasFocus) {
            this.ensureVisible((Entry)this.getFocused());
        }
        return this.hasFocus;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.hasFocus) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        return super.narrationPriority();
    }

    @Override
    protected boolean isSelectedItem(int i) {
        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        Entry entry = (Entry)this.getHovered();
        if (entry != null) {
            entry.updateNarration(narrationElementOutput.nest());
            this.narrateListElementPosition(narrationElementOutput, entry);
        } else {
            Entry entry2 = (Entry)this.getFocused();
            if (entry2 != null) {
                entry2.updateNarration(narrationElementOutput.nest());
                this.narrateListElementPosition(narrationElementOutput, entry2);
            }
        }
        narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry<E extends Entry<E>>
    extends AbstractSelectionList.Entry<E>
    implements ContainerEventHandler {
        @Nullable
        private GuiEventListener focused;
        @Nullable
        private NarratableEntry lastNarratable;
        private boolean dragging;

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean bl) {
            this.dragging = bl;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener guiEventListener) {
            this.focused = guiEventListener;
        }

        @Override
        @Nullable
        public GuiEventListener getFocused() {
            return this.focused;
        }

        public abstract List<? extends NarratableEntry> narratables();

        void updateNarration(NarrationElementOutput narrationElementOutput) {
            List<NarratableEntry> list = this.narratables();
            Screen.NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget(list, this.lastNarratable);
            if (narratableSearchResult != null) {
                if (narratableSearchResult.priority.isTerminal()) {
                    this.lastNarratable = narratableSearchResult.entry;
                }
                if (list.size() > 1) {
                    narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.object_list", narratableSearchResult.index + 1, list.size()));
                    if (narratableSearchResult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                        narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
                    }
                }
                narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
            }
        }
    }
}

