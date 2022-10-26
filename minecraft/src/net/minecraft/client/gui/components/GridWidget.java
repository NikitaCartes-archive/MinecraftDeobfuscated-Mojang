package net.minecraft.client.gui.components;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class GridWidget extends AbstractContainerWidget {
	private final List<AbstractWidget> children = new ArrayList();
	private final List<GridWidget.CellInhabitant> cellInhabitants = new ArrayList();
	private final LayoutSettings defaultCellSettings = LayoutSettings.defaults();

	public GridWidget() {
		this(0, 0);
	}

	public GridWidget(int i, int j) {
		this(i, j, Component.empty());
	}

	public GridWidget(int i, int j, Component component) {
		super(i, j, 0, 0, component);
	}

	public void pack() {
		int i = 0;
		int j = 0;

		for (GridWidget.CellInhabitant cellInhabitant : this.cellInhabitants) {
			i = Math.max(cellInhabitant.getLastOccupiedRow(), i);
			j = Math.max(cellInhabitant.getLastOccupiedColumn(), j);
		}

		int[] is = new int[j + 1];
		int[] js = new int[i + 1];

		for (GridWidget.CellInhabitant cellInhabitant2 : this.cellInhabitants) {
			Divisor divisor = new Divisor(cellInhabitant2.getHeight(), cellInhabitant2.occupiedRows);

			for (int k = cellInhabitant2.row; k <= cellInhabitant2.getLastOccupiedRow(); k++) {
				js[k] = Math.max(js[k], divisor.nextInt());
			}

			Divisor divisor2 = new Divisor(cellInhabitant2.getWidth(), cellInhabitant2.occupiedColumns);

			for (int l = cellInhabitant2.column; l <= cellInhabitant2.getLastOccupiedColumn(); l++) {
				is[l] = Math.max(is[l], divisor2.nextInt());
			}
		}

		int[] ks = new int[j + 1];
		int[] ls = new int[i + 1];
		ks[0] = 0;

		for (int m = 1; m <= j; m++) {
			ks[m] = ks[m - 1] + is[m - 1];
		}

		ls[0] = 0;

		for (int m = 1; m <= i; m++) {
			ls[m] = ls[m - 1] + js[m - 1];
		}

		for (GridWidget.CellInhabitant cellInhabitant3 : this.cellInhabitants) {
			int l = 0;

			for (int n = cellInhabitant3.column; n <= cellInhabitant3.getLastOccupiedColumn(); n++) {
				l += is[n];
			}

			cellInhabitant3.setX(this.getX() + ks[cellInhabitant3.column], l);
			int n = 0;

			for (int o = cellInhabitant3.row; o <= cellInhabitant3.getLastOccupiedRow(); o++) {
				n += js[o];
			}

			cellInhabitant3.setY(this.getY() + ls[cellInhabitant3.row], n);
		}

		this.width = ks[j] + is[j];
		this.height = ls[i] + js[i];
	}

	public <T extends AbstractWidget> T addChild(T abstractWidget, int i, int j) {
		return this.addChild(abstractWidget, i, j, this.newCellSettings());
	}

	public <T extends AbstractWidget> T addChild(T abstractWidget, int i, int j, LayoutSettings layoutSettings) {
		return this.addChild(abstractWidget, i, j, 1, 1, layoutSettings);
	}

	public <T extends AbstractWidget> T addChild(T abstractWidget, int i, int j, int k, int l) {
		return this.addChild(abstractWidget, i, j, k, l, this.newCellSettings());
	}

	public <T extends AbstractWidget> T addChild(T abstractWidget, int i, int j, int k, int l, LayoutSettings layoutSettings) {
		if (k < 1) {
			throw new IllegalArgumentException("Occupied rows must be at least 1");
		} else if (l < 1) {
			throw new IllegalArgumentException("Occupied columns must be at least 1");
		} else {
			this.cellInhabitants.add(new GridWidget.CellInhabitant(abstractWidget, i, j, k, l, layoutSettings));
			this.children.add(abstractWidget);
			return abstractWidget;
		}
	}

	@Override
	protected List<? extends AbstractWidget> getContainedChildren() {
		return this.children;
	}

	public LayoutSettings newCellSettings() {
		return this.defaultCellSettings.copy();
	}

	public LayoutSettings defaultCellSetting() {
		return this.defaultCellSettings;
	}

	@Environment(EnvType.CLIENT)
	static class CellInhabitant extends AbstractContainerWidget.AbstractChildWrapper {
		final int row;
		final int column;
		final int occupiedRows;
		final int occupiedColumns;

		CellInhabitant(AbstractWidget abstractWidget, int i, int j, int k, int l, LayoutSettings layoutSettings) {
			super(abstractWidget, layoutSettings.getExposed());
			this.row = i;
			this.column = j;
			this.occupiedRows = k;
			this.occupiedColumns = l;
		}

		public int getLastOccupiedRow() {
			return this.row + this.occupiedRows - 1;
		}

		public int getLastOccupiedColumn() {
			return this.column + this.occupiedColumns - 1;
		}
	}
}
