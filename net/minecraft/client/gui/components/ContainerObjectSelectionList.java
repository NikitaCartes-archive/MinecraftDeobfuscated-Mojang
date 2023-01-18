/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class ContainerObjectSelectionList<E extends Entry<E>>
extends AbstractSelectionList<E> {
    public ContainerObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (this.getItemCount() == 0) {
            return null;
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
            ComponentPath componentPath;
            int i;
            FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)focusNavigationEvent;
            Entry entry2 = (Entry)this.getFocused();
            if (arrowNavigation.direction().getAxis() == ScreenAxis.HORIZONTAL && entry2 != null) {
                return ComponentPath.path(this, entry2.nextFocusPath(focusNavigationEvent));
            }
            ScreenDirection screenDirection = arrowNavigation.direction();
            if (entry2 == null) {
                switch (screenDirection) {
                    case LEFT: {
                        i = Integer.MAX_VALUE;
                        screenDirection = ScreenDirection.DOWN;
                        break;
                    }
                    case RIGHT: {
                        i = 0;
                        screenDirection = ScreenDirection.DOWN;
                        break;
                    }
                    default: {
                        i = 0;
                        break;
                    }
                }
            } else {
                i = entry2.children().indexOf(entry2.getFocused());
            }
            Entry entry22 = entry2;
            do {
                if ((entry22 = this.nextEntry(screenDirection, entry -> !entry.children().isEmpty(), entry22)) != null) continue;
                return null;
            } while ((componentPath = entry22.focusPathAtIndex(arrowNavigation, i)) == null);
            return ComponentPath.path(this, componentPath);
        }
        return super.nextFocusPath(focusNavigationEvent);
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        super.setFocused(guiEventListener);
        if (guiEventListener == null) {
            this.setSelected(null);
        }
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
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
        public boolean mouseClicked(double d, double e, int i) {
            return ContainerEventHandler.super.mouseClicked(d, e, i);
        }

        @Override
        public void setFocused(@Nullable GuiEventListener guiEventListener) {
            if (this.focused != null) {
                this.focused.setFocused(false);
            }
            if (guiEventListener != null) {
                guiEventListener.setFocused(true);
            }
            this.focused = guiEventListener;
        }

        @Override
        @Nullable
        public GuiEventListener getFocused() {
            return this.focused;
        }

        @Nullable
        public ComponentPath focusPathAtIndex(FocusNavigationEvent focusNavigationEvent, int i) {
            if (this.children().isEmpty()) {
                return null;
            }
            ComponentPath componentPath = this.children().get(Math.min(i, this.children().size() - 1)).nextFocusPath(focusNavigationEvent);
            return ComponentPath.path(this, componentPath);
        }

        @Override
        @Nullable
        public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
            if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
                int j;
                int i;
                FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)focusNavigationEvent;
                switch (arrowNavigation.direction()) {
                    default: {
                        throw new IncompatibleClassChangeError();
                    }
                    case UP: 
                    case DOWN: {
                        int n = 0;
                        break;
                    }
                    case LEFT: {
                        int n = -1;
                        break;
                    }
                    case RIGHT: {
                        int n = i = 1;
                    }
                }
                if (i == 0) {
                    return null;
                }
                for (int k = j = Mth.clamp(i + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1); k >= 0 && k < this.children().size(); k += i) {
                    GuiEventListener guiEventListener = this.children().get(k);
                    ComponentPath componentPath = guiEventListener.nextFocusPath(focusNavigationEvent);
                    if (componentPath == null) continue;
                    return ComponentPath.path(this, componentPath);
                }
            }
            return ContainerEventHandler.super.nextFocusPath(focusNavigationEvent);
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

