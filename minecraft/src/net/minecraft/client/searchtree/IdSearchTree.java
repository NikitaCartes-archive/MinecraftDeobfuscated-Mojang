package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class IdSearchTree<T> implements SearchTree<T> {
	protected final Comparator<T> additionOrder;
	protected final ResourceLocationSearchTree<T> resourceLocationSearchTree;

	public IdSearchTree(Function<T, Stream<ResourceLocation>> function, List<T> list) {
		ToIntFunction<T> toIntFunction = Util.createIndexLookup(list);
		this.additionOrder = Comparator.comparingInt(toIntFunction);
		this.resourceLocationSearchTree = ResourceLocationSearchTree.create(list, function);
	}

	@Override
	public List<T> search(String string) {
		int i = string.indexOf(58);
		return i == -1 ? this.searchPlainText(string) : this.searchResourceLocation(string.substring(0, i).trim(), string.substring(i + 1).trim());
	}

	protected List<T> searchPlainText(String string) {
		return this.resourceLocationSearchTree.searchPath(string);
	}

	protected List<T> searchResourceLocation(String string, String string2) {
		List<T> list = this.resourceLocationSearchTree.searchNamespace(string);
		List<T> list2 = this.resourceLocationSearchTree.searchPath(string2);
		return ImmutableList.copyOf(new IntersectionIterator<>(list.iterator(), list2.iterator(), this.additionOrder));
	}
}
