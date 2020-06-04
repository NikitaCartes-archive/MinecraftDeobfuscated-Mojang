package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StoredUserList<K, V extends StoredUserEntry<K>> {
	protected static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final File file;
	private final Map<String, V> map = Maps.<String, V>newHashMap();

	public StoredUserList(File file) {
		this.file = file;
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

	protected abstract StoredUserEntry<K> createEntry(JsonObject jsonObject);

	public Collection<V> getEntries() {
		return this.map.values();
	}

	public void save() throws IOException {
		JsonArray jsonArray = new JsonArray();
		this.map.values().stream().map(storedUserEntry -> Util.make(new JsonObject(), storedUserEntry::serialize)).forEach(jsonArray::add);
		BufferedWriter bufferedWriter = Files.newWriter(this.file, StandardCharsets.UTF_8);
		Throwable var3 = null;

		try {
			GSON.toJson(jsonArray, bufferedWriter);
		} catch (Throwable var12) {
			var3 = var12;
			throw var12;
		} finally {
			if (bufferedWriter != null) {
				if (var3 != null) {
					try {
						bufferedWriter.close();
					} catch (Throwable var11) {
						var3.addSuppressed(var11);
					}
				} else {
					bufferedWriter.close();
				}
			}
		}
	}

	public void load() throws IOException {
		if (this.file.exists()) {
			BufferedReader bufferedReader = Files.newReader(this.file, StandardCharsets.UTF_8);
			Throwable var2 = null;

			try {
				JsonArray jsonArray = GSON.fromJson(bufferedReader, JsonArray.class);
				this.map.clear();

				for (JsonElement jsonElement : jsonArray) {
					JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entry");
					StoredUserEntry<K> storedUserEntry = this.createEntry(jsonObject);
					if (storedUserEntry.getUser() != null) {
						this.map.put(this.getKeyForUser(storedUserEntry.getUser()), storedUserEntry);
					}
				}
			} catch (Throwable var15) {
				var2 = var15;
				throw var15;
			} finally {
				if (bufferedReader != null) {
					if (var2 != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var14) {
							var2.addSuppressed(var14);
						}
					} else {
						bufferedReader.close();
					}
				}
			}
		}
	}
}
