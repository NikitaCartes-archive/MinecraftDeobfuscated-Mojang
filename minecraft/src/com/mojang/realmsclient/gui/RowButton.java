package com.mojang.realmsclient.gui;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;

@Environment(EnvType.CLIENT)
public abstract class RowButton {
	public final int width;
	public final int height;
	public final int xOffset;
	public final int yOffset;

	public RowButton(int i, int j, int k, int l) {
		this.width = i;
		this.height = j;
		this.xOffset = k;
		this.yOffset = l;
	}

	public void drawForRowAt(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		int m = i + this.xOffset;
		int n = j + this.yOffset;
		boolean bl = k >= m && k <= m + this.width && l >= n && l <= n + this.height;
		this.draw(guiGraphics, m, n, bl);
	}

	protected abstract void draw(GuiGraphics guiGraphics, int i, int j, boolean bl);

	public int getRight() {
		return this.xOffset + this.width;
	}

	public int getBottom() {
		return this.yOffset + this.height;
	}

	public abstract void onClick(int i);

	public static void drawButtonsInRow(GuiGraphics guiGraphics, List<RowButton> list, AbstractSelectionList<?> abstractSelectionList, int i, int j, int k, int l) {
		for (RowButton rowButton : list) {
			if (abstractSelectionList.getRowWidth() > rowButton.getRight()) {
				rowButton.drawForRowAt(guiGraphics, i, j, k, l);
			}
		}
	}

	public static void rowButtonMouseClicked(
		AbstractSelectionList<?> abstractSelectionList, ObjectSelectionList.Entry<?> entry, List<RowButton> list, int i, double d, double e
	) {
		int j = abstractSelectionList.children().indexOf(entry);
		if (j > -1) {
			abstractSelectionList.setSelectedIndex(j);
			int k = abstractSelectionList.getRowLeft();
			int l = abstractSelectionList.getRowTop(j);
			int m = (int)(d - (double)k);
			int n = (int)(e - (double)l);

			for (RowButton rowButton : list) {
				if (m >= rowButton.xOffset && m <= rowButton.getRight() && n >= rowButton.yOffset && n <= rowButton.getBottom()) {
					rowButton.onClick(j);
				}
			}
		}
	}
}
