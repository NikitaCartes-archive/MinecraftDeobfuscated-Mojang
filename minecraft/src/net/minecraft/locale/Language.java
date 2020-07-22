package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
	private static volatile Language instance = loadDefault();

	private static Language loadDefault() {
		Builder<String, String> builder = ImmutableMap.builder();
		BiConsumer<String, String> biConsumer = builder::put;

		try {
			InputStream inputStream = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json");
			Throwable var3 = null;

			try {
				loadFromJson(inputStream, biConsumer);
			} catch (Throwable var13) {
				var3 = var13;
				throw var13;
			} finally {
				if (inputStream != null) {
					if (var3 != null) {
						try {
							inputStream.close();
						} catch (Throwable var12) {
							var3.addSuppressed(var12);
						}
					} else {
						inputStream.close();
					}
				}
			}
		} catch (JsonParseException | IOException var15) {
			LOGGER.error("Couldn't read strings from /assets/minecraft/lang/en_us.json", (Throwable)var15);
		}

		final Map<String, String> map = builder.build();
		return new Language() {
			@Override
			public String getOrDefault(String string) {
				return (String)map.getOrDefault(string, string);
			}

			@Override
			public boolean has(String string) {
				return map.containsKey(string);
			}

			@Environment(EnvType.CLIENT)
			@Override
			public boolean isDefaultRightToLeft() {
				return false;
			}

			@Environment(EnvType.CLIENT)
			@Override
			public FormattedCharSequence getVisualOrder(FormattedText formattedText) {
				return formattedCharSink -> formattedText.visit(
							(style, string) -> StringDecomposer.iterateFormatted(string, style, formattedCharSink) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY
						)
						.isPresent();
			}
		};
	}

	public static void loadFromJson(InputStream inputStream, BiConsumer<String, String> biConsumer) {
		JsonObject jsonObject = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);

		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String string = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString((JsonElement)entry.getValue(), (String)entry.getKey())).replaceAll("%$1s");
			biConsumer.accept(entry.getKey(), string);
		}
	}

	public static Language getInstance() {
		return instance;
	}

	@Environment(EnvType.CLIENT)
	public static void inject(Language language) {
		instance = language;
	}

	public abstract String getOrDefault(String string);

	public abstract boolean has(String string);

	@Environment(EnvType.CLIENT)
	public abstract boolean isDefaultRightToLeft();

	@Environment(EnvType.CLIENT)
	public abstract FormattedCharSequence getVisualOrder(FormattedText formattedText);

	@Environment(EnvType.CLIENT)
	public List<FormattedCharSequence> getVisualOrder(List<FormattedText> list) {
		return (List<FormattedCharSequence>)list.stream().map(getInstance()::getVisualOrder).collect(ImmutableList.toImmutableList());
	}
}
