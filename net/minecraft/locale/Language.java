/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringDecomposer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Language {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT = "en_us";
    private static volatile Language instance = Language.loadDefault();

    private static Language loadDefault() {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        BiConsumer<String, String> biConsumer = builder::put;
        String string = "/assets/minecraft/lang/en_us.json";
        try (InputStream inputStream = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json");){
            Language.loadFromJson(inputStream, biConsumer);
        } catch (JsonParseException | IOException exception) {
            LOGGER.error("Couldn't read strings from {}", (Object)"/assets/minecraft/lang/en_us.json", (Object)exception);
        }
        final ImmutableMap map = builder.build();
        return new Language(){

            @Override
            public String getOrDefault(String string) {
                return map.getOrDefault(string, string);
            }

            @Override
            public boolean has(String string) {
                return map.containsKey(string);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public FormattedCharSequence getVisualOrder(FormattedText formattedText) {
                return formattedCharSink -> formattedText.visit((style, string) -> StringDecomposer.iterateFormatted(string, style, formattedCharSink) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY).isPresent();
            }
        };
    }

    public static void loadFromJson(InputStream inputStream, BiConsumer<String, String> biConsumer) {
        JsonObject jsonObject = GSON.fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String string = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
            biConsumer.accept(entry.getKey(), string);
        }
    }

    public static Language getInstance() {
        return instance;
    }

    public static void inject(Language language) {
        instance = language;
    }

    public abstract String getOrDefault(String var1);

    public abstract boolean has(String var1);

    public abstract boolean isDefaultRightToLeft();

    public abstract FormattedCharSequence getVisualOrder(FormattedText var1);

    public List<FormattedCharSequence> getVisualOrder(List<FormattedText> list) {
        return list.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
    }
}

