package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringDecomposer;
import org.slf4j.Logger;

public abstract class Language {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new Gson();
	private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
	public static final String DEFAULT = "en_us";
	private static volatile Language instance = loadDefault();

	private static Language loadDefault() {
		DeprecatedTranslationsInfo deprecatedTranslationsInfo = DeprecatedTranslationsInfo.loadFromDefaultResource();
		Map<String, String> map = new HashMap();
		BiConsumer<String, String> biConsumer = map::put;
		parseTranslations(biConsumer, "/assets/minecraft/lang/en_us.json");
		deprecatedTranslationsInfo.applyToMap(map);
		final Map<String, String> map2 = Map.copyOf(map);
		return new Language() {
			@Override
			public String getOrDefault(String string, String string2) {
				return (String)map2.getOrDefault(string, string2);
			}

			@Override
			public boolean has(String string) {
				return map2.containsKey(string);
			}

			@Override
			public boolean isDefaultRightToLeft() {
				return false;
			}

			@Override
			public FormattedCharSequence getVisualOrder(FormattedText formattedText) {
				return formattedCharSink -> formattedText.visit(
							(style, string) -> StringDecomposer.iterateFormatted(string, style, formattedCharSink) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY
						)
						.isPresent();
			}
		};
	}

	private static void parseTranslations(BiConsumer<String, String> biConsumer, String string) {
		try {
			InputStream inputStream = Language.class.getResourceAsStream(string);

			try {
				loadFromJson(inputStream, biConsumer);
			} catch (Throwable var6) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}
				}

				throw var6;
			}

			if (inputStream != null) {
				inputStream.close();
			}
		} catch (JsonParseException | IOException var7) {
			LOGGER.error("Couldn't read strings from {}", string, var7);
		}
	}

	public static void loadFromJson(InputStream inputStream, BiConsumer<String, String> biConsumer) {
		JsonObject jsonObject = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);

		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String string = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString((JsonElement)entry.getValue(), (String)entry.getKey())).replaceAll("%$1s");
			biConsumer.accept((String)entry.getKey(), string);
		}
	}

	public static Language getInstance() {
		return instance;
	}

	public static void inject(Language language) {
		instance = language;
	}

	public String getOrDefault(String string) {
		return this.getOrDefault(string, string);
	}

	public abstract String getOrDefault(String string, String string2);

	public abstract boolean has(String string);

	public abstract boolean isDefaultRightToLeft();

	public abstract FormattedCharSequence getVisualOrder(FormattedText formattedText);

	public List<FormattedCharSequence> getVisualOrder(List<FormattedText> list) {
		return (List<FormattedCharSequence>)list.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
	}
}
