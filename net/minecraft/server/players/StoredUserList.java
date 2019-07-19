/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class StoredUserList<K, V extends StoredUserEntry<K>> {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final Gson gson;
    private final File file;
    private final Map<String, V> map = Maps.newHashMap();
    private boolean enabled = true;
    private static final ParameterizedType USERLIST_ENTRY_TYPE = new ParameterizedType(){

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{StoredUserEntry.class};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    };

    public StoredUserList(File file) {
        this.file = file;
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
        gsonBuilder.registerTypeHierarchyAdapter(StoredUserEntry.class, new Serializer());
        this.gson = gsonBuilder.create();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean bl) {
        this.enabled = bl;
    }

    public File getFile() {
        return this.file;
    }

    public void add(V storedUserEntry) {
        this.map.put(this.getKeyForUser(((StoredUserEntry)storedUserEntry).getUser()), storedUserEntry);
        try {
            this.save();
        } catch (IOException iOException) {
            LOGGER.warn("Could not save the list after adding a user.", (Throwable)iOException);
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
            LOGGER.warn("Could not save the list after removing a user.", (Throwable)iOException);
        }
    }

    public void remove(StoredUserEntry<K> storedUserEntry) {
        this.remove(storedUserEntry.getUser());
    }

    public String[] getUserList() {
        return this.map.keySet().toArray(new String[this.map.size()]);
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

    protected StoredUserEntry<K> createEntry(JsonObject jsonObject) {
        return new StoredUserEntry<Object>(null, jsonObject);
    }

    public Collection<V> getEntries() {
        return this.map.values();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void save() throws IOException {
        Collection<V> collection = this.map.values();
        String string = this.gson.toJson(collection);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = Files.newWriter(this.file, StandardCharsets.UTF_8);
            bufferedWriter.write(string);
        } catch (Throwable throwable) {
            IOUtils.closeQuietly(bufferedWriter);
            throw throwable;
        }
        IOUtils.closeQuietly(bufferedWriter);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void load() throws FileNotFoundException {
        if (!this.file.exists()) {
            return;
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = Files.newReader(this.file, StandardCharsets.UTF_8);
            Collection collection = (Collection)GsonHelper.fromJson(this.gson, (Reader)bufferedReader, (Type)USERLIST_ENTRY_TYPE);
            if (collection != null) {
                this.map.clear();
                for (StoredUserEntry storedUserEntry : collection) {
                    if (storedUserEntry.getUser() == null) continue;
                    this.map.put(this.getKeyForUser(storedUserEntry.getUser()), storedUserEntry);
                }
            }
        } catch (Throwable throwable) {
            IOUtils.closeQuietly(bufferedReader);
            throw throwable;
        }
        IOUtils.closeQuietly(bufferedReader);
    }

    class Serializer
    implements JsonDeserializer<StoredUserEntry<K>>,
    JsonSerializer<StoredUserEntry<K>> {
        private Serializer() {
        }

        @Override
        public JsonElement serialize(StoredUserEntry<K> storedUserEntry, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            storedUserEntry.serialize(jsonObject);
            return jsonObject;
        }

        @Override
        public StoredUserEntry<K> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                return StoredUserList.this.createEntry(jsonObject);
            }
            return null;
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((StoredUserEntry)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

