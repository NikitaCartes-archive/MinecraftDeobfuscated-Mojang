package net.minecraft.world.item.component;

import java.util.List;
import net.minecraft.server.network.Filterable;

public interface BookContent<T, C> {
	List<Filterable<T>> pages();

	C withReplacedPages(List<Filterable<T>> list);
}
