package net.minecraft.server.packs.repository;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class PackRepository<T extends UnopenedPack> implements AutoCloseable {
	private final Set<RepositorySource> sources = Sets.<RepositorySource>newHashSet();
	private final Map<String, T> available = Maps.<String, T>newLinkedHashMap();
	private final List<T> selected = Lists.<T>newLinkedList();
	private final UnopenedPack.UnopenedPackConstructor<T> constructor;

	public PackRepository(UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor) {
		this.constructor = unopenedPackConstructor;
	}

	public void reload() {
		this.close();
		Set<String> set = (Set<String>)this.selected.stream().map(UnopenedPack::getId).collect(Collectors.toCollection(LinkedHashSet::new));
		this.available.clear();
		this.selected.clear();

		for (RepositorySource repositorySource : this.sources) {
			repositorySource.loadPacks(this.available, this.constructor);
		}

		this.sortAvailable();
		this.selected.addAll((Collection)set.stream().map(this.available::get).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));

		for (T unopenedPack : this.available.values()) {
			if (unopenedPack.isRequired() && !this.selected.contains(unopenedPack)) {
				unopenedPack.getDefaultPosition().insert(this.selected, unopenedPack, Functions.identity(), false);
			}
		}
	}

	private void sortAvailable() {
		List<Entry<String, T>> list = Lists.<Entry<String, T>>newArrayList(this.available.entrySet());
		this.available.clear();
		list.stream().sorted(Entry.comparingByKey()).forEachOrdered(entry -> {
			UnopenedPack var10000 = (UnopenedPack)this.available.put(entry.getKey(), entry.getValue());
		});
	}

	public void setSelected(Collection<T> collection) {
		this.selected.clear();
		this.selected.addAll(collection);

		for (T unopenedPack : this.available.values()) {
			if (unopenedPack.isRequired() && !this.selected.contains(unopenedPack)) {
				unopenedPack.getDefaultPosition().insert(this.selected, unopenedPack, Functions.identity(), false);
			}
		}
	}

	public Collection<T> getAvailable() {
		return this.available.values();
	}

	public Collection<T> getUnselected() {
		Collection<T> collection = Lists.<T>newArrayList(this.available.values());
		collection.removeAll(this.selected);
		return collection;
	}

	public Collection<T> getSelected() {
		return this.selected;
	}

	@Nullable
	public T getPack(String string) {
		return (T)this.available.get(string);
	}

	public void addSource(RepositorySource repositorySource) {
		this.sources.add(repositorySource);
	}

	public void close() {
		this.available.values().forEach(UnopenedPack::close);
	}
}
