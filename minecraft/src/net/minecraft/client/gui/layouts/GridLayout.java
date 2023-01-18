package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class GridLayout extends AbstractLayout {
	private final List<LayoutElement> children = new ArrayList();
	private final List<GridLayout.CellInhabitant> cellInhabitants = new ArrayList();
	private final LayoutSettings defaultCellSettings = LayoutSettings.defaults();

	public GridLayout() {
		this(0, 0);
	}

	public GridLayout(int i, int j) {
		super(i, j, 0, 0);
	}

	@Override
	public void arrangeElements() {
		super.arrangeElements();
		int i = 0;
		int j = 0;

		for (GridLayout.CellInhabitant cellInhabitant : this.cellInhabitants) {
			i = Math.max(cellInhabitant.getLastOccupiedRow(), i);
			j = Math.max(cellInhabitant.getLastOccupiedColumn(), j);
		}

		int[] is = new int[j + 1];
		int[] js = new int[i + 1];

		for (GridLayout.CellInhabitant cellInhabitant2 : this.cellInhabitants) {
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

		for (GridLayout.CellInhabitant cellInhabitant3 : this.cellInhabitants) {
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

	public <T extends LayoutElement> T addChild(T layoutElement, int i, int j) {
		return this.addChild(layoutElement, i, j, this.newCellSettings());
	}

	public <T extends LayoutElement> T addChild(T layoutElement, int i, int j, LayoutSettings layoutSettings) {
		return this.addChild(layoutElement, i, j, 1, 1, layoutSettings);
	}

	public <T extends LayoutElement> T addChild(T layoutElement, int i, int j, int k, int l) {
		return this.addChild(layoutElement, i, j, k, l, this.newCellSettings());
	}

	public <T extends LayoutElement> T addChild(T layoutElement, int i, int j, int k, int l, LayoutSettings layoutSettings) {
		if (k < 1) {
			throw new IllegalArgumentException("Occupied rows must be at least 1");
		} else if (l < 1) {
			throw new IllegalArgumentException("Occupied columns must be at least 1");
		} else {
			this.cellInhabitants.add(new GridLayout.CellInhabitant(layoutElement, i, j, k, l, layoutSettings));
			this.children.add(layoutElement);
			return layoutElement;
		}
	}

	@Override
	protected void visitChildren(Consumer<LayoutElement> consumer) {
		this.children.forEach(consumer);
	}

	public LayoutSettings newCellSettings() {
		return this.defaultCellSettings.copy();
	}

	public LayoutSettings defaultCellSetting() {
		return this.defaultCellSettings;
	}

	public GridLayout.RowHelper createRowHelper(int i) {
		return new GridLayout.RowHelper(i);
	}

	@Environment(EnvType.CLIENT)
	static class CellInhabitant extends AbstractLayout.AbstractChildWrapper {
		final int row;
		final int column;
		final int occupiedRows;
		final int occupiedColumns;

		CellInhabitant(LayoutElement layoutElement, int i, int j, int k, int l, LayoutSettings layoutSettings) {
			super(layoutElement, layoutSettings.getExposed());
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

	@Environment(EnvType.CLIENT)
	public final class RowHelper {
		private final int columns;
		private int index;

		RowHelper(int i) {
			this.columns = i;
		}

		public <T extends LayoutElement> T addChild(T layoutElement) {
			return this.addChild(layoutElement, 1);
		}

		public <T extends LayoutElement> T addChild(T layoutElement, int i) {
			return this.addChild(layoutElement, i, this.defaultCellSetting());
		}

		public <T extends LayoutElement> T addChild(T layoutElement, LayoutSettings layoutSettings) {
			return this.addChild(layoutElement, 1, layoutSettings);
		}

		public <T extends LayoutElement> T addChild(T layoutElement, int i, LayoutSettings layoutSettings) {
			int j = this.index / this.columns;
			int k = this.index % this.columns;
			if (k + i > this.columns) {
				j++;
				k = 0;
				this.index = Mth.roundToward(this.index, this.columns);
			}

			this.index += i;
			return GridLayout.this.addChild(layoutElement, j, k, 1, i, layoutSettings);
		}

		public LayoutSettings newCellSettings() {
			return GridLayout.this.newCellSettings();
		}

		public LayoutSettings defaultCellSetting() {
			return GridLayout.this.defaultCellSetting();
		}
	}
}
