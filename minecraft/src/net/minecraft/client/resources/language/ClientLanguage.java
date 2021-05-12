package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientLanguage extends Language {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<String, String> storage;
	private final boolean defaultRightToLeft;

	private ClientLanguage(Map<String, String> map, boolean bl) {
		this.storage = map;
		this.defaultRightToLeft = bl;
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

				try {
					Language.loadFromJson(inputStream, map::put);
				} catch (Throwable var8) {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Throwable var7) {
							var8.addSuppressed(var7);
						}
					}

					throw var8;
				}

				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException var9) {
				LOGGER.warn("Failed to load translations from {}", resource, var9);
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
	public boolean isDefaultRightToLeft() {
		return this.defaultRightToLeft;
	}

	@Override
	public FormattedCharSequence getVisualOrder(FormattedText formattedText) {
		return FormattedBidiReorder.reorder(formattedText, this.defaultRightToLeft);
	}
}
