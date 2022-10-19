/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class IndexedAssetSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Splitter PATH_SPLITTER = Splitter.on('/');

    public static Path createIndexFs(Path path, String string) {
        Path path2 = path.resolve("objects");
        LinkFileSystem.Builder builder = LinkFileSystem.builder();
        Path path3 = path.resolve("indexes/" + string + ".json");
        try (BufferedReader bufferedReader = Files.newBufferedReader(path3, StandardCharsets.UTF_8);){
            JsonObject jsonObject = GsonHelper.parse(bufferedReader);
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "objects", null);
            if (jsonObject2 != null) {
                for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                    JsonObject jsonObject3 = (JsonObject)entry.getValue();
                    String string2 = entry.getKey();
                    List<String> list = PATH_SPLITTER.splitToList(string2);
                    String string3 = GsonHelper.getAsString(jsonObject3, "hash");
                    Path path4 = path2.resolve(string3.substring(0, 2) + "/" + string3);
                    builder.put(list, path4);
                }
            }
        } catch (JsonParseException jsonParseException) {
            LOGGER.error("Unable to parse resource index file: {}", (Object)path3);
        } catch (IOException iOException) {
            LOGGER.error("Can't open the resource index file: {}", (Object)path3);
        }
        return builder.build("index-" + string).getPath("/", new String[0]);
    }
}

