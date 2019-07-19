package net.minecraft.client.searchtree;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface MutableSearchTree<T> extends SearchTree<T> {
	void add(T object);

	void clear();

	void refresh();
}
