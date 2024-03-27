package net.minecraft.server.packs.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.server.packs.PackResources;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackRepository {
	private final Set<RepositorySource> sources;
	private Map<String, Pack> available = ImmutableMap.of();
	private List<Pack> selected = ImmutableList.of();

	public PackRepository(RepositorySource... repositorySources) {
		this.sources = ImmutableSet.copyOf(repositorySources);
	}

	public void reload() {
		List<String> list = (List<String>)this.selected.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
		this.available = this.discoverAvailable();
		this.selected = this.rebuildSelected(list);
	}

	private Map<String, Pack> discoverAvailable() {
		Map<String, Pack> map = Maps.newTreeMap();

		for (RepositorySource repositorySource : this.sources) {
			repositorySource.loadPacks(pack -> map.put(pack.getId(), pack));
		}

		return ImmutableMap.copyOf(map);
	}

	public void setSelected(Collection<String> collection) {
		this.selected = this.rebuildSelected(collection);
	}

	public boolean addPack(String string) {
		Pack pack = (Pack)this.available.get(string);
		if (pack != null && !this.selected.contains(pack)) {
			List<Pack> list = Lists.<Pack>newArrayList(this.selected);
			list.add(pack);
			this.selected = list;
			return true;
		} else {
			return false;
		}
	}

	public boolean removePack(String string) {
		Pack pack = (Pack)this.available.get(string);
		if (pack != null && this.selected.contains(pack)) {
			List<Pack> list = Lists.<Pack>newArrayList(this.selected);
			list.remove(pack);
			this.selected = list;
			return true;
		} else {
			return false;
		}
	}

	private List<Pack> rebuildSelected(Collection<String> collection) {
		List<Pack> list = (List<Pack>)this.getAvailablePacks(collection).collect(Util.toMutableList());

		for (Pack pack : this.available.values()) {
			if (pack.isRequired() && !list.contains(pack)) {
				pack.getDefaultPosition().insert(list, pack, Pack::selectionConfig, false);
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

	public FeatureFlagSet getRequestedFeatureFlags() {
		return (FeatureFlagSet)this.getSelectedPacks().stream().map(Pack::getRequestedFeatures).reduce(FeatureFlagSet::join).orElse(FeatureFlagSet.of());
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
