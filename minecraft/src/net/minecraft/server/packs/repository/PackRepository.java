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
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

public class PackRepository {
	private final Set<RepositorySource> sources;
	private Map<String, Pack> available = ImmutableMap.of();
	private List<Pack> selected = ImmutableList.of();
	private final Pack.PackConstructor constructor;

	public PackRepository(Pack.PackConstructor packConstructor, RepositorySource... repositorySources) {
		this.constructor = packConstructor;
		this.sources = ImmutableSet.copyOf(repositorySources);
	}

	public PackRepository(PackType packType, RepositorySource... repositorySources) {
		this(
			(string, component, bl, supplier, packMetadataSection, position, packSource) -> new Pack(
					string, component, bl, supplier, packMetadataSection, packType, position, packSource
				),
			repositorySources
		);
	}

	public void reload() {
		List<String> list = (List<String>)this.selected.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
		this.available = this.discoverAvailable();
		this.selected = this.rebuildSelected(list);
	}

	private Map<String, Pack> discoverAvailable() {
		Map<String, Pack> map = Maps.newTreeMap();

		for (RepositorySource repositorySource : this.sources) {
			repositorySource.loadPacks(pack -> map.put(pack.getId(), pack), this.constructor);
		}

		return ImmutableMap.copyOf(map);
	}

	public void setSelected(Collection<String> collection) {
		this.selected = this.rebuildSelected(collection);
	}

	private List<Pack> rebuildSelected(Collection<String> collection) {
		List<Pack> list = (List<Pack>)this.getAvailablePacks(collection).collect(Collectors.toList());

		for (Pack pack : this.available.values()) {
			if (pack.isRequired() && !list.contains(pack)) {
				pack.getDefaultPosition().insert(list, pack, Functions.identity(), false);
			}
		}

		return ImmutableList.copyOf(list);
	}

	private Stream<Pack> getAvailablePacks(Collection<String> collection) {
		return collection.stream().map(this.available::get).filter(Objects::nonNull);
	}

	public Collection<String> getAvailableIds() {
		return this.available.keySet();
	}

	public Collection<Pack> getAvailablePacks() {
		return this.available.values();
	}

	public Collection<String> getSelectedIds() {
		return (Collection<String>)this.selected.stream().map(Pack::getId).collect(ImmutableSet.toImmutableSet());
	}

	public Collection<Pack> getSelectedPacks() {
		return this.selected;
	}

	@Nullable
	public Pack getPack(String string) {
		return (Pack)this.available.get(string);
	}

	public boolean isAvailable(String string) {
		return this.available.containsKey(string);
	}

	public List<PackResources> openAllSelected() {
		return (List<PackResources>)this.selected.stream().map(Pack::open).collect(ImmutableList.toImmutableList());
	}
}
