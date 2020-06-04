/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.Nullable;

public class PackRepository<T extends Pack>
implements AutoCloseable {
    private final Set<RepositorySource> sources;
    private Map<String, T> available = ImmutableMap.of();
    private List<T> selected = ImmutableList.of();
    private final Pack.PackConstructor<T> constructor;

    public PackRepository(Pack.PackConstructor<T> packConstructor, RepositorySource ... repositorySources) {
        this.constructor = packConstructor;
        this.sources = ImmutableSet.copyOf(repositorySources);
    }

    public void reload() {
        List list = this.selected.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
        this.close();
        this.available = this.discoverAvailable();
        this.selected = this.rebuildSelected(list);
    }

    private Map<String, T> discoverAvailable() {
        TreeMap map = Maps.newTreeMap();
        for (RepositorySource repositorySource : this.sources) {
            repositorySource.loadPacks(pack -> map.put(pack.getId(), pack), this.constructor);
        }
        return ImmutableMap.copyOf(map);
    }

    public void setSelected(Collection<String> collection) {
        this.selected = this.rebuildSelected(collection);
    }

    private List<T> rebuildSelected(Collection<String> collection) {
        List list = this.getAvailablePacks(collection).collect(Collectors.toList());
        for (Pack pack : this.available.values()) {
            if (!pack.isRequired() || list.contains(pack)) continue;
            pack.getDefaultPosition().insert(list, pack, Functions.identity(), false);
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
        return this.selected.stream().map(Pack::getId).collect(ImmutableSet.toImmutableSet());
    }

    public Collection<T> getSelectedPacks() {
        return this.selected;
    }

    @Nullable
    public T getPack(String string) {
        return (T)((Pack)this.available.get(string));
    }

    @Override
    public void close() {
        this.available.values().forEach(Pack::close);
    }

    public boolean isAvailable(String string) {
        return this.available.containsKey(string);
    }

    public List<PackResources> openAllSelected() {
        return this.selected.stream().map(Pack::open).collect(ImmutableList.toImmutableList());
    }
}

