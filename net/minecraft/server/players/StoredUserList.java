/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class StoredUserList<K, V extends StoredUserEntry<K>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File file;
    private final Map<String, V> map = Maps.newHashMap();

    public StoredUserList(File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    public void add(V storedUserEntry) {
        this.map.put(this.getKeyForUser(((StoredUserEntry)storedUserEntry).getUser()), storedUserEntry);
        try {
            this.save();
        } catch (IOException iOException) {
            LOGGER.warn("Could not save the list after adding a user.", iOException);
        }
    }

    @Nullable
    public V get(K object) {
        this.removeExpired();
        return (V)((StoredUserEntry)this.map.get(this.getKeyForUser(object)));
    }

    public void remove(K object) {
        this.map.remove(this.getKeyForUser(object));
        try {
            this.save();
        } catch (IOException iOException) {
            LOGGER.warn("Could not save the list after removing a user.", iOException);
        }
    }

    public void remove(StoredUserEntry<K> storedUserEntry) {
        this.remove(storedUserEntry.getUser());
    }

    public String[] getUserList() {
        return this.map.keySet().toArray(new String[0]);
    }

    public boolean isEmpty() {
        return this.map.size() < 1;
    }

    protected String getKeyForUser(K object) {
        return object.toString();
    }

    protected boolean contains(K object) {
        return this.map.containsKey(this.getKeyForUser(object));
    }

    private void removeExpired() {
        ArrayList<Object> list = Lists.newArrayList();
        for (StoredUserEntry storedUserEntry : this.map.values()) {
            if (!storedUserEntry.hasExpired()) continue;
            list.add(storedUserEntry.getUser());
        }
        for (Object object : list) {
            this.map.remove(this.getKeyForUser(object));
        }
    }

    protected abstract StoredUserEntry<K> createEntry(JsonObject var1);

    public Collection<V> getEntries() {
        return this.map.values();
    }

    public void save() throws IOException {
        JsonArray jsonArray = new JsonArray();
        this.map.values().stream().map(storedUserEntry -> Util.make(new JsonObject(), storedUserEntry::serialize)).forEach(jsonArray::add);
        try (BufferedWriter bufferedWriter = Files.newWriter(this.file, StandardCharsets.UTF_8);){
            GSON.toJson((JsonElement)jsonArray, (Appendable)bufferedWriter);
        }
    }

    public void load() throws IOException {
        if (!this.file.exists()) {
            return;
        }
        try (BufferedReader bufferedReader = Files.newReader(this.file, StandardCharsets.UTF_8);){
            JsonArray jsonArray = GSON.fromJson((Reader)bufferedReader, JsonArray.class);
            this.map.clear();
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entry");
                StoredUserEntry<K> storedUserEntry = this.createEntry(jsonObject);
                if (storedUserEntry.getUser() == null) continue;
                this.map.put(this.getKeyForUser(storedUserEntry.getUser()), storedUserEntry);
            }
        }
    }
}

