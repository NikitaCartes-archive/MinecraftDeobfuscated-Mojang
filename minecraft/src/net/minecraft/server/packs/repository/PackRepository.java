package net.minecraft.server.packs.repository;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.server.packs.Pack;

public class PackRepository<T extends UnopenedPack> implements AutoCloseable {
	private final Set<RepositorySource> sources;
	private Map<String, T> available = ImmutableMap.of();
	private List<T> selected = ImmutableList.of();
	private final UnopenedPack.UnopenedPackConstructor<T> constructor;

	public PackRepository(UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor, RepositorySource... repositorySources) {
		this.constructor = unopenedPackConstructor;
		this.sources = ImmutableSet.copyOf(repositorySources);
	}

	public void reload() {
		List<String> list = (List<String>)this.selected.stream().map(UnopenedPack::getId).collect(ImmutableList.toImmutableList());
		this.close();
		this.available = this.discoverAvailable();
		this.selected = this.rebuildSelected(list);
	}

	private Map<String, T> discoverAvailable() {
		Map<String, T> map = Maps.newTreeMap();

		for (RepositorySource repositorySource : this.sources) {
			repositorySource.loadPacks(map, this.constructor);
		}

		return ImmutableMap.copyOf(map);
	}

	public void setSelected(Collection<String> collection) {
		this.selected = this.rebuildSelected(collection);
	}

	private List<T> rebuildSelected(Collection<String> collection) {
		List<T> list = (List<T>)this.getAvailablePacks(collection).collect(Collectors.toList());

		for (T unopenedPack : this.available.values()) {
			if (unopenedPack.isRequired() && !list.contains(unopenedPack)) {
				unopenedPack.getDefaultPosition().insert(list, unopenedPack, Functions.identity(), false);
			}
		}

		return ImmutableList.copyOf(list);
	}

	private Stream<T> getAvailablePacks(Collection<String> collection) {
		return collection.stream().map(this.available::get).filter(Objects::nonNull);
	}

	public Collection<String> getAvailableIds() {
		return this.available.keySet();
	}

	public Collection<T> getAvailablePacks() {
		return this.available.values();
	}

	public Collection<String> getSelectedIds() {
		return (Collection<String>)this.selected.stream().map(UnopenedPack::getId).collect(ImmutableSet.toImmutableSet());
	}

	public Collection<T> getSelectedPacks() {
		return this.selected;
	}

	@Nullable
	public T getPack(String string) {
		return (T)this.available.get(string);
	}

	public void close() {
		this.available.values().forEach(UnopenedPack::close);
	}

	public boolean isAvailable(String string) {
		return this.available.containsKey(string);
	}

	public List<Pack> openAllSelected() {
		return (List<Pack>)this.selected.stream().map(UnopenedPack::open).collect(ImmutableList.toImmutableList());
	}
}
