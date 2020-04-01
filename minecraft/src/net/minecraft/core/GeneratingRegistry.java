package net.minecraft.core;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class GeneratingRegistry<T> extends MappedRegistry<T> {
	private final IntFunction<T> generator;

	public GeneratingRegistry(IntFunction<T> intFunction) {
		this.generator = intFunction;
	}

	@Nullable
	@Override
	public T get(@Nullable ResourceLocation resourceLocation) {
		T object = super.get(resourceLocation);
		if (object != null) {
			return object;
		} else if (resourceLocation.getNamespace().equals("_generated")) {
			int i = Integer.parseInt(resourceLocation.getPath());
			if (i < 0) {
				return null;
			} else {
				T object2 = (T)this.generator.apply(i);
				this.registerMapping(i, resourceLocation, object2);
				return object2;
			}
		} else {
			return null;
		}
	}

	@Override
	public T byId(int i) {
		T object = super.byId(i);
		if (object != null) {
			return object;
		} else if (i < 0) {
			return null;
		} else {
			T object2 = (T)this.generator.apply(i);
			this.registerMapping(i, new ResourceLocation("_generated", Integer.toString(i)), object2);
			return object2;
		}
	}
}
