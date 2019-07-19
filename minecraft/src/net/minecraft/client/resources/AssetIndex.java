package net.minecraft.client.resources;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class AssetIndex {
	protected static final Logger LOGGER = LogManager.getLogger();
	private final Map<String, File> mapping = Maps.<String, File>newHashMap();

	protected AssetIndex() {
	}

	public AssetIndex(File file, String string) {
		File file2 = new File(file, "objects");
		File file3 = new File(file, "indexes/" + string + ".json");
		BufferedReader bufferedReader = null;

		try {
			bufferedReader = Files.newReader(file3, StandardCharsets.UTF_8);
			JsonObject jsonObject = GsonHelper.parse(bufferedReader);
			JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "objects", null);
			if (jsonObject2 != null) {
				for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
					JsonObject jsonObject3 = (JsonObject)entry.getValue();
					String string2 = (String)entry.getKey();
					String[] strings = string2.split("/", 2);
					String string3 = strings.length == 1 ? strings[0] : strings[0] + ":" + strings[1];
					String string4 = GsonHelper.getAsString(jsonObject3, "hash");
					File file4 = new File(file2, string4.substring(0, 2) + "/" + string4);
					this.mapping.put(string3, file4);
				}
			}
		} catch (JsonParseException var20) {
			LOGGER.error("Unable to parse resource index file: {}", file3);
		} catch (FileNotFoundException var21) {
			LOGGER.error("Can't find the resource index file: {}", file3);
		} finally {
			IOUtils.closeQuietly(bufferedReader);
		}
	}

	@Nullable
	public File getFile(ResourceLocation resourceLocation) {
		return this.getFile(resourceLocation.toString());
	}

	@Nullable
	public File getFile(String string) {
		return (File)this.mapping.get(string);
	}

	public Collection<String> getFiles(String string, int i, Predicate<String> predicate) {
		return (Collection<String>)this.mapping
			.keySet()
			.stream()
			.filter(stringx -> !stringx.endsWith(".mcmeta"))
			.map(ResourceLocation::new)
			.map(ResourceLocation::getPath)
			.filter(string2 -> string2.startsWith(string + "/"))
			.filter(predicate)
			.collect(Collectors.toList());
	}
}
