/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.locale;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Language {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    private static final Language SINGLETON = new Language();
    private final Map<String, String> storage = Maps.newHashMap();
    private long lastUpdateTime;

    public Language() {
        try (InputStream inputStream = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json");){
            JsonElement jsonElement = new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonElement.class);
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "strings");
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String string = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
                this.storage.put(entry.getKey(), string);
            }
            this.lastUpdateTime = Util.getMillis();
        } catch (JsonParseException | IOException exception) {
            LOGGER.error("Couldn't read strings from /assets/minecraft/lang/en_us.json", (Throwable)exception);
        }
    }

    public static Language getInstance() {
        return SINGLETON;
    }

    @Environment(value=EnvType.CLIENT)
    public static synchronized void forceData(Map<String, String> map) {
        Language.SINGLETON.storage.clear();
        Language.SINGLETON.storage.putAll(map);
        Language.SINGLETON.lastUpdateTime = Util.getMillis();
    }

    public synchronized String getElement(String string) {
        return this.getProperty(string);
    }

    private String getProperty(String string) {
        String string2 = this.storage.get(string);
        return string2 == null ? string : string2;
    }

    public synchronized boolean exists(String string) {
        return this.storage.containsKey(string);
    }

    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }
}

