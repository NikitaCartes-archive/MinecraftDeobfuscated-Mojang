package net.minecraft.world.level.levelgen;

import java.util.function.Function;
import net.minecraft.world.level.levelgen.blending.Blender;

public interface DensityFunction {
	double compute(DensityFunction.FunctionContext functionContext);

	void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider);

	DensityFunction mapAll(DensityFunction.Visitor visitor);

	double minValue();

	double maxValue();

	default DensityFunction clamp(double d, double e) {
		return new DensityFunctions.Clamp(this, d, e);
	}

	default DensityFunction abs() {
		return DensityFunctions.map(this, DensityFunctions.Mapped.Type.ABS);
	}

	default DensityFunction square() {
		return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUARE);
	}

	default DensityFunction cube() {
		return DensityFunctions.map(this, DensityFunctions.Mapped.Type.CUBE);
	}

	default DensityFunction halfNegative() {
		return DensityFunctions.map(this, DensityFunctions.Mapped.Type.HALF_NEGATIVE);
	}

	default DensityFunction quarterNegative() {
		return DensityFunctions.map(this, DensityFunctions.Mapped.Type.QUARTER_NEGATIVE);
	}

	default DensityFunction squeeze() {
		return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUEEZE);
	}

	public interface ContextProvider {
		DensityFunction.FunctionContext forIndex(int i);

		void fillAllDirectly(double[] ds, DensityFunction densityFunction);
	}

	public interface FunctionContext {
		int blockX();

		int blockY();

		int blockZ();

		default Blender getBlender() {
			return Blender.empty();
		}
	}

	public interface SimpleFunction extends DensityFunction {
		@Override
		default void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		default DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(this);
		}
	}

	public static record SinglePointContext(int blockX, int blockY, int blockZ) implements DensityFunction.FunctionContext {
	}

	public interface Visitor extends Function<DensityFunction, DensityFunction> {
	}
}
