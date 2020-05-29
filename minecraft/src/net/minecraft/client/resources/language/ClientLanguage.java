package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientLanguage extends Language {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z])");
	private final Map<String, String> storage;
	private final boolean requiresReordering;

	private ClientLanguage(Map<String, String> map, boolean bl) {
		this.storage = map;
		this.requiresReordering = bl;
	}

	public static ClientLanguage loadFrom(ResourceManager resourceManager, List<LanguageInfo> list) {
		Map<String, String> map = Maps.<String, String>newHashMap();
		boolean bl = false;

		for (LanguageInfo languageInfo : list) {
			bl |= languageInfo.isBidirectional();
			String string = String.format("lang/%s.json", languageInfo.getCode());

			for (String string2 : resourceManager.getNamespaces()) {
				try {
					ResourceLocation resourceLocation = new ResourceLocation(string2, string);
					appendFrom(resourceManager.getResources(resourceLocation), map);
				} catch (FileNotFoundException var10) {
				} catch (Exception var11) {
					LOGGER.warn("Skipped language file: {}:{} ({})", string2, string, var11.toString());
				}
			}
		}

		return new ClientLanguage(ImmutableMap.copyOf(map), bl);
	}

	private static void appendFrom(List<Resource> list, Map<String, String> map) {
		for (Resource resource : list) {
			try {
				InputStream inputStream = resource.getInputStream();
				Throwable var5 = null;

				try {
					Language.loadFromJson(inputStream, map::put);
				} catch (Throwable var15) {
					var5 = var15;
					throw var15;
				} finally {
					if (inputStream != null) {
						if (var5 != null) {
							try {
								inputStream.close();
							} catch (Throwable var14) {
								var5.addSuppressed(var14);
							}
						} else {
							inputStream.close();
						}
					}
				}
			} catch (IOException var17) {
				LOGGER.warn("Failed to load translations from {}", resource, var17);
			}
		}
	}

	@Override
	public String getOrDefault(String string) {
		return (String)this.storage.getOrDefault(string, string);
	}

	@Override
	public boolean has(String string) {
		return this.storage.containsKey(string);
	}

	@Override
	public boolean requiresReordering() {
		return this.requiresReordering;
	}

	@Override
	public String reorder(String string, boolean bl) {
		if (!this.requiresReordering) {
			return string;
		} else {
			if (bl && string.indexOf(37) != -1) {
				string = wrapFormatCodes(string);
			}

			return this.reorder(string);
		}
	}

	public static String wrapFormatCodes(String string) {
		Matcher matcher = FORMAT_PATTERN.matcher(string);
		StringBuffer stringBuffer = new StringBuffer();
		int i = 1;

		while (matcher.find()) {
			String string2 = matcher.group(1);
			String string3 = string2 != null ? string2 : Integer.toString(i++);
			String string4 = matcher.group(2);
			String string5 = Matcher.quoteReplacement("\u2066%" + string3 + "$" + string4 + "\u2069");
			matcher.appendReplacement(stringBuffer, string5);
		}

		matcher.appendTail(stringBuffer);
		return stringBuffer.toString();
	}

	private String reorder(String string) {
		try {
			Bidi bidi = new Bidi(new ArabicShaping(8).shape(string), 127);
			bidi.setReorderingMode(0);
			return bidi.writeReordered(10);
		} catch (ArabicShapingException var3) {
			return string;
		}
	}
}
