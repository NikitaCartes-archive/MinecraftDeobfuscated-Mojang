package net.minecraft.client.searchtree;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface RefreshableSearchTree<T> extends SearchTree<T> {
	static <T> RefreshableSearchTree<T> empty() {
		return string -> List.of();
	}

	default void refresh() {
	}
}
