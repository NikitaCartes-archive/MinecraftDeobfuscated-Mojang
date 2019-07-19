/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class InsensitiveStringMap<V>
implements Map<String, V> {
    private final Map<String, V> map = Maps.newLinkedHashMap();

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object object) {
        return this.map.containsKey(object.toString().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean containsValue(Object object) {
        return this.map.containsValue(object);
    }

    @Override
    public V get(Object object) {
        return this.map.get(object.toString().toLowerCase(Locale.ROOT));
    }

    @Override
    public V put(String string, V object) {
        return this.map.put(string.toLowerCase(Locale.ROOT), object);
    }

    @Override
    public V remove(Object object) {
        return this.map.remove(object.toString().toLowerCase(Locale.ROOT));
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        for (Map.Entry<String, V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Set<Map.Entry<String, V>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public /* synthetic */ Object put(Object object, Object object2) {
        return this.put((String)object, object2);
    }
}

