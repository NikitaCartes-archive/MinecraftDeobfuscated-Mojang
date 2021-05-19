package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public abstract class ObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
	private static final Component USAGE_NARRATION = new TranslatableComponent("narration.selection.usage");
	private boolean inFocus;

	public ObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
	}

	@Override
	public boolean changeFocus(boolean bl) {
		if (!this.inFocus && this.getItemCount() == 0) {
			return false;
		} else {
			this.inFocus = !this.inFocus;
			if (this.inFocus && this.getSelected() == null && this.getItemCount() > 0) {
				this.moveSelection(AbstractSelectionList.SelectionDirection.DOWN);
			} else if (this.inFocus && this.getSelected() != null) {
				this.refreshSelection();
			}

			return this.inFocus;
		}
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		E entry = this.getHovered();
		if (entry != null) {
			this.narrateListElementPosition(narrationElementOutput.nest(), entry);
			entry.updateNarration(narrationElementOutput);
		} else {
			E entry2 = this.getSelected();
			if (entry2 != null) {
				this.narrateListElementPosition(narrationElementOutput.nest(), entry2);
				entry2.updateNarration(narrationElementOutput);
			}
		}

		if (this.isFocused()) {
			narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements NarrationSupplier {
		@Override
		public boolean changeFocus(boolean bl) {
			return false;
		}

		public abstract Component getNarration();

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {
			narrationElementOutput.add(NarratedElementType.TITLE, this.getNarration());
		}
	}
}
