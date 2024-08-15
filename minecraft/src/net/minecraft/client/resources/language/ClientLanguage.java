package net.minecraft.client.resources.language;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.locale.DeprecatedTranslationsInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientLanguage extends Language {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, String> storage;
	private final boolean defaultRightToLeft;

	private ClientLanguage(Map<String, String> map, boolean bl) {
		this.storage = map;
		this.defaultRightToLeft = bl;
	}

	public static ClientLanguage loadFrom(ResourceManager resourceManager, List<String> list, boolean bl) {
		Map<String, String> map = new HashMap();

		for (String string : list) {
			String string2 = String.format(Locale.ROOT, "lang/%s.json", string);

			for (String string3 : resourceManager.getNamespaces()) {
				try {
					ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(string3, string2);
					appendFrom(string, resourceManager.getResourceStack(resourceLocation), map);
				} catch (Exception var10) {
					LOGGER.warn("Skipped language file: {}:{} ({})", string3, string2, var10.toString());
				}
			}
		}

		DeprecatedTranslationsInfo.loadFromDefaultResource().applyToMap(map);
		return new ClientLanguage(Map.copyOf(map), bl);
	}

	private static void appendFrom(String string, List<Resource> list, Map<String, String> map) {
		for (Resource resource : list) {
			try {
				InputStream inputStream = resource.open();

				try {
					Language.loadFromJson(inputStream, map::put);
				} catch (Throwable var9) {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Throwable var8) {
							var9.addSuppressed(var8);
						}
					}

					throw var9;
				}

				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException var10) {
				LOGGER.warn("Failed to load translations for {} from pack {}", string, resource.sourcePackId(), var10);
			}
		}
	}

	@Override
	public String getOrDefault(String string, String string2) {
		return (String)this.storage.getOrDefault(string, string2);
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
