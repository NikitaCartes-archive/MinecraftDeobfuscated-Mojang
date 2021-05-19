package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public abstract class ContainerObjectSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
	private boolean hasFocus;

	public ContainerObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
	}

	@Override
	public boolean changeFocus(boolean bl) {
		this.hasFocus = super.changeFocus(bl);
		if (this.hasFocus) {
			this.ensureVisible(this.getFocused());
		}

		return this.hasFocus;
	}

	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		return this.hasFocus ? NarratableEntry.NarrationPriority.FOCUSED : super.narrationPriority();
	}

	@Override
	protected boolean isSelectedItem(int i) {
		return false;
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		E entry = this.getHovered();
		if (entry != null) {
			entry.updateNarration(narrationElementOutput.nest());
			this.narrateListElementPosition(narrationElementOutput, entry);
		} else {
			E entry2 = this.getFocused();
			if (entry2 != null) {
				entry2.updateNarration(narrationElementOutput.nest());
				this.narrateListElementPosition(narrationElementOutput, entry2);
			}
		}

		narrationElementOutput.add(NarratedElementType.USAGE, new TranslatableComponent("narration.component_list.usage"));
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements ContainerEventHandler {
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

		@Nullable
		@Override
		public GuiEventListener getFocused() {
			return this.focused;
		}

		public abstract List<? extends NarratableEntry> narratables();

		void updateNarration(NarrationElementOutput narrationElementOutput) {
			List<? extends NarratableEntry> list = this.narratables();
			Screen.NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget(list, this.lastNarratable);
			if (narratableSearchResult != null) {
				if (narratableSearchResult.priority.isTerminal()) {
					this.lastNarratable = narratableSearchResult.entry;
				}

				if (list.size() > 1) {
					narrationElementOutput.add(
						NarratedElementType.POSITION, new TranslatableComponent("narrator.position.object_list", narratableSearchResult.index + 1, list.size())
					);
					if (narratableSearchResult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
						narrationElementOutput.add(NarratedElementType.USAGE, new TranslatableComponent("narration.component_list.usage"));
					}
				}

				narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
			}
		}
	}
}
