package net.minecraft.client.resources.language;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Locale {
	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
	protected final Map<String, String> storage = Maps.<String, String>newHashMap();

	public synchronized void loadFrom(ResourceManager resourceManager, List<String> list) {
		this.storage.clear();

		for (String string : list) {
			String string2 = String.format("lang/%s.json", string);

			for (String string3 : resourceManager.getNamespaces()) {
				try {
					ResourceLocation resourceLocation = new ResourceLocation(string3, string2);
					this.appendFrom(resourceManager.getResources(resourceLocation));
				} catch (FileNotFoundException var9) {
				} catch (Exception var10) {
					LOGGER.warn("Skipped language file: {}:{} ({})", string3, string2, var10.toString());
				}
			}
		}
	}

	private void appendFrom(List<Resource> list) {
		for (Resource resource : list) {
			InputStream inputStream = resource.getInputStream();

			try {
				this.appendFrom(inputStream);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
	}

	private void appendFrom(InputStream inputStream) {
		JsonElement jsonElement = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonElement.class);
		JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "strings");

		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String string = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString((JsonElement)entry.getValue(), (String)entry.getKey())).replaceAll("%$1s");
			this.storage.put(entry.getKey(), string);
		}
	}

	private String getOrDefault(String string) {
		String string2 = (String)this.storage.get(string);
		return string2 == null ? string : string2;
	}

	public String get(String string, Object[] objects) {
		String string2 = this.getOrDefault(string);

		try {
			return String.format(string2, objects);
		} catch (IllegalFormatException var5) {
			return "Format error: " + string2;
		}
	}

	public boolean has(String string) {
		return this.storage.containsKey(string);
	}
}
