/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import org.jetbrains.annotations.Nullable;

public class StatType<T>
implements Iterable<Stat<T>> {
    private final Registry<T> registry;
    private final Map<T, Stat<T>> map = new IdentityHashMap<T, Stat<T>>();
    @Nullable
    private Component displayName;

    public StatType(Registry<T> registry) {
        this.registry = registry;
    }

    public boolean contains(T object) {
        return this.map.containsKey(object);
    }

    public Stat<T> get(T object2, StatFormatter statFormatter) {
        return this.map.computeIfAbsent(object2, object -> new Stat<Object>(this, object, statFormatter));
    }

    public Registry<T> getRegistry() {
        return this.registry;
    }

    @Override
    public Iterator<Stat<T>> iterator() {
        return this.map.values().iterator();
    }

    public Stat<T> get(T object) {
        return this.get(object, StatFormatter.DEFAULT);
    }

    public String getTranslationKey() {
        return "stat_type." + BuiltInRegistries.STAT_TYPE.getKey(this).toString().replace(':', '.');
    }

    public Component getDisplayName() {
        if (this.displayName == null) {
            this.displayName = Component.translatable(this.getTranslationKey());
        }
        return this.displayName;
    }
}

