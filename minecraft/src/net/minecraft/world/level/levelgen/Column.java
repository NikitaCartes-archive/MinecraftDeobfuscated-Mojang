package net.minecraft.world.level.levelgen;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;

public abstract class Column {
	public static Column.Range inside(int i, int j) {
		return new Column.Range(i, j);
	}

	public static Column below(int i) {
		return new Column.Ray(i, false);
	}

	public static Column above(int i) {
		return new Column.Ray(i, true);
	}

	public static Column line() {
		return Column.Line.INSTANCE;
	}

	public static Column create(OptionalInt optionalInt, OptionalInt optionalInt2) {
		if (optionalInt.isPresent() && optionalInt2.isPresent()) {
			return inside(optionalInt.getAsInt(), optionalInt2.getAsInt());
		} else if (optionalInt.isPresent()) {
			return above(optionalInt.getAsInt());
		} else {
			return optionalInt2.isPresent() ? below(optionalInt2.getAsInt()) : line();
		}
	}

	public abstract OptionalInt getCeiling();

	public abstract OptionalInt getFloor();

	public Column withFloor(OptionalInt optionalInt) {
		return create(optionalInt, this.getCeiling());
	}

	public static Optional<Column> scan(
		LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, int i, Predicate<BlockState> predicate, Predicate<BlockState> predicate2
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		if (!levelSimulatedReader.isStateAtPosition(blockPos, predicate)) {
			return Optional.empty();
		} else {
			int j = blockPos.getY();
			mutableBlockPos.setY(j);

			for (int k = 1; k < i && levelSimulatedReader.isStateAtPosition(mutableBlockPos, predicate); k++) {
				mutableBlockPos.move(Direction.UP);
			}

			OptionalInt optionalInt = levelSimulatedReader.isStateAtPosition(mutableBlockPos, predicate2) ? OptionalInt.of(mutableBlockPos.getY()) : OptionalInt.empty();
			mutableBlockPos.setY(j);

			for (int l = 1; l < i && levelSimulatedReader.isStateAtPosition(mutableBlockPos, predicate); l++) {
				mutableBlockPos.move(Direction.DOWN);
			}

			OptionalInt optionalInt2 = levelSimulatedReader.isStateAtPosition(mutableBlockPos, predicate2)
				? OptionalInt.of(mutableBlockPos.getY())
				: OptionalInt.empty();
			return Optional.of(create(optionalInt2, optionalInt));
		}
	}

	public static final class Line extends Column {
		private static final Column.Line INSTANCE = new Column.Line();

		private Line() {
		}

		@Override
		public OptionalInt getCeiling() {
			return OptionalInt.empty();
		}

		@Override
		public OptionalInt getFloor() {
			return OptionalInt.empty();
		}

		public String toString() {
			return "C(-)";
		}
	}

	public static final class Range extends Column {
		private final int floor;
		private final int ceiling;

		protected Range(int i, int j) {
			this.floor = i;
			this.ceiling = j;
			if (this.height() < 0) {
				throw new IllegalArgumentException("Column of negative height: " + this);
			}
		}

		@Override
		public OptionalInt getCeiling() {
			return OptionalInt.of(this.ceiling);
		}

		@Override
		public OptionalInt getFloor() {
			return OptionalInt.of(this.floor);
		}

		public int ceiling() {
			return this.ceiling;
		}

		public int floor() {
			return this.floor;
		}

		public int height() {
			return this.ceiling - this.floor - 1;
		}

		public String toString() {
			return "C(" + this.ceiling + "-" + this.floor + ')';
		}
	}

	public static final class Ray extends Column {
		private final int edge;
		private final boolean pointingUp;

		public Ray(int i, boolean bl) {
			this.edge = i;
			this.pointingUp = bl;
		}

		@Override
		public OptionalInt getCeiling() {
			return this.pointingUp ? OptionalInt.empty() : OptionalInt.of(this.edge);
		}

		@Override
		public OptionalInt getFloor() {
			return this.pointingUp ? OptionalInt.of(this.edge) : OptionalInt.empty();
		}

		public String toString() {
			return this.pointingUp ? "C(" + this.edge + "-)" : "C(-" + this.edge + ")";
		}
	}
}