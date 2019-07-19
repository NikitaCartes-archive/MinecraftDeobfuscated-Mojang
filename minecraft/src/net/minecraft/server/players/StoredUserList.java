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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StoredUserList<K, V extends StoredUserEntry<K>> {
	protected static final Logger LOGGER = LogManager.getLogger();
	protected final Gson gson;
	private final File file;
	private final Map<String, V> map = Maps.<String, V>newHashMap();
	private boolean enabled = true;
	private static final ParameterizedType USERLIST_ENTRY_TYPE = new ParameterizedType() {
		public Type[] getActualTypeArguments() {
			return new Type[]{StoredUserEntry.class};
		}

		public Type getRawType() {
			return List.class;
		}

		public Type getOwnerType() {
			return null;
		}
	};

	public StoredUserList(File file) {
		this.file = file;
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		gsonBuilder.registerTypeHierarchyAdapter(StoredUserEntry.class, new StoredUserList.Serializer());
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
		this.map.put(this.getKeyForUser(storedUserEntry.getUser()), storedUserEntry);

		try {
			this.save();
		} catch (IOException var3) {
			LOGGER.warn("Could not save the list after adding a user.", (Throwable)var3);
		}
	}

	@Nullable
	public V get(K object) {
		this.removeExpired();
		return (V)this.map.get(this.getKeyForUser(object));
	}

	public void remove(K object) {
		this.map.remove(this.getKeyForUser(object));

		try {
			this.save();
		} catch (IOException var3) {
			LOGGER.warn("Could not save the list after removing a user.", (Throwable)var3);
		}
	}

	public void remove(StoredUserEntry<K> storedUserEntry) {
		this.remove(storedUserEntry.getUser());
	}

	public String[] getUserList() {
		return (String[])this.map.keySet().toArray(new String[this.map.size()]);
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
		List<K> list = Lists.<K>newArrayList();

		for (V storedUserEntry : this.map.values()) {
			if (storedUserEntry.hasExpired()) {
				list.add(storedUserEntry.getUser());
			}
		}

		for (K object : list) {
			this.map.remove(this.getKeyForUser(object));
		}
	}

	protected StoredUserEntry<K> createEntry(JsonObject jsonObject) {
		return new StoredUserEntry<>(null, jsonObject);
	}

	public Collection<V> getEntries() {
		return this.map.values();
	}

	public void save() throws IOException {
		Collection<V> collection = this.map.values();
		String string = this.gson.toJson(collection);
		BufferedWriter bufferedWriter = null;

		try {
			bufferedWriter = Files.newWriter(this.file, StandardCharsets.UTF_8);
			bufferedWriter.write(string);
		} finally {
			IOUtils.closeQuietly(bufferedWriter);
		}
	}

	public void load() throws FileNotFoundException {
		if (this.file.exists()) {
			BufferedReader bufferedReader = null;

			try {
				bufferedReader = Files.newReader(this.file, StandardCharsets.UTF_8);
				Collection<StoredUserEntry<K>> collection = GsonHelper.fromJson(this.gson, bufferedReader, USERLIST_ENTRY_TYPE);
				if (collection != null) {
					this.map.clear();

					for (StoredUserEntry<K> storedUserEntry : collection) {
						if (storedUserEntry.getUser() != null) {
							this.map.put(this.getKeyForUser(storedUserEntry.getUser()), storedUserEntry);
						}
					}
				}
			} finally {
				IOUtils.closeQuietly(bufferedReader);
			}
		}
	}

	class Serializer implements JsonDeserializer<StoredUserEntry<K>>, JsonSerializer<StoredUserEntry<K>> {
		private Serializer() {
		}

		public JsonElement serialize(StoredUserEntry<K> storedUserEntry, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			storedUserEntry.serialize(jsonObject);
			return jsonObject;
		}

		public StoredUserEntry<K> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			if (jsonElement.isJsonObject()) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				return StoredUserList.this.createEntry(jsonObject);
			} else {
				return null;
			}
		}
	}
}
