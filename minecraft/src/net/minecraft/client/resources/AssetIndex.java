package net.minecraft.client.resources;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class AssetIndex {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, File> rootFiles = Maps.<String, File>newHashMap();
	private final Map<ResourceLocation, File> namespacedFiles = Maps.<ResourceLocation, File>newHashMap();

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
					String string3 = GsonHelper.getAsString(jsonObject3, "hash");
					File file4 = new File(file2, string3.substring(0, 2) + "/" + string3);
					if (strings.length == 1) {
						this.rootFiles.put(strings[0], file4);
					} else {
						this.namespacedFiles.put(new ResourceLocation(strings[0], strings[1]), file4);
					}
				}
			}
		} catch (JsonParseException var19) {
			LOGGER.error("Unable to parse resource index file: {}", file3);
		} catch (FileNotFoundException var20) {
			LOGGER.error("Can't find the resource index file: {}", file3);
		} finally {
			IOUtils.closeQuietly(bufferedReader);
		}
	}

	@Nullable
	public File getFile(ResourceLocation resourceLocation) {
		return (File)this.namespacedFiles.get(resourceLocation);
	}

	@Nullable
	public File getRootFile(String string) {
		return (File)this.rootFiles.get(string);
	}

	public Collection<ResourceLocation> getFiles(String string, String string2, Predicate<ResourceLocation> predicate) {
		return (Collection<ResourceLocation>)this.namespacedFiles
			.keySet()
			.stream()
			.filter(
				resourceLocation -> {
					String string3 = resourceLocation.getPath();
					return resourceLocation.getNamespace().equals(string2)
						&& !string3.endsWith(".mcmeta")
						&& string3.startsWith(string + "/")
						&& predicate.test(resourceLocation);
				}
			)
			.collect(Collectors.toList());
	}
}
