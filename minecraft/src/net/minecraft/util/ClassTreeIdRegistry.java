package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;

public class ClassTreeIdRegistry {
	public static final int NO_ID_VALUE = -1;
	private final Object2IntMap<Class<?>> classToLastIdCache = Util.make(
		new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> object2IntOpenHashMap.defaultReturnValue(-1)
	);

	public int getLastIdFor(Class<?> class_) {
		int i = this.classToLastIdCache.getInt(class_);
		if (i != -1) {
			return i;
		} else {
			Class<?> class2 = class_;

			while ((class2 = class2.getSuperclass()) != Object.class) {
				int j = this.classToLastIdCache.getInt(class2);
				if (j != -1) {
					return j;
				}
			}

			return -1;
		}
	}

	public int getCount(Class<?> class_) {
		return this.getLastIdFor(class_) + 1;
	}

	public int define(Class<?> class_) {
		int i = this.getLastIdFor(class_);
		int j = i == -1 ? 0 : i + 1;
		this.classToLastIdCache.put(class_, j);
		return j;
	}
}
