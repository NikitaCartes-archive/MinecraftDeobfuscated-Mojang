package net.minecraft.core;

public enum AxisCycle {
	NONE {
		@Override
		public int cycle(int i, int j, int k, Direction.Axis axis) {
			return axis.choose(i, j, k);
		}

		@Override
		public double cycle(double d, double e, double f, Direction.Axis axis) {
			return axis.choose(d, e, f);
		}

		@Override
		public Direction.Axis cycle(Direction.Axis axis) {
			return axis;
		}

		@Override
		public AxisCycle inverse() {
			return this;
		}
	},
	FORWARD {
		@Override
		public int cycle(int i, int j, int k, Direction.Axis axis) {
			return axis.choose(k, i, j);
		}

		@Override
		public double cycle(double d, double e, double f, Direction.Axis axis) {
			return axis.choose(f, d, e);
		}

		@Override
		public Direction.Axis cycle(Direction.Axis axis) {
			return AXIS_VALUES[Math.floorMod(axis.ordinal() + 1, 3)];
		}

		@Override
		public AxisCycle inverse() {
			return BACKWARD;
		}
	},
	BACKWARD {
		@Override
		public int cycle(int i, int j, int k, Direction.Axis axis) {
			return axis.choose(j, k, i);
		}

		@Override
		public double cycle(double d, double e, double f, Direction.Axis axis) {
			return axis.choose(e, f, d);
		}

		@Override
		public Direction.Axis cycle(Direction.Axis axis) {
			return AXIS_VALUES[Math.floorMod(axis.ordinal() - 1, 3)];
		}

		@Override
		public AxisCycle inverse() {
			return FORWARD;
		}
	};

	public static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
	public static final AxisCycle[] VALUES = values();

	public abstract int cycle(int i, int j, int k, Direction.Axis axis);

	public abstract double cycle(double d, double e, double f, Direction.Axis axis);

	public abstract Direction.Axis cycle(Direction.Axis axis);

	public abstract AxisCycle inverse();

	public static AxisCycle between(Direction.Axis axis, Direction.Axis axis2) {
		return VALUES[Math.floorMod(axis2.ordinal() - axis.ordinal(), 3)];
	}
}
