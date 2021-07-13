package net.minecraft.world.level.biome;

public interface ToFloatFunction<C> {
	float apply(C object);

	default ToFloatFunction<C> combine(ToFloatFunction<C> toFloatFunction, Spline.FloatAdder floatAdder) {
		return object -> floatAdder.combine(this.apply(object), toFloatFunction.apply(object));
	}
}
