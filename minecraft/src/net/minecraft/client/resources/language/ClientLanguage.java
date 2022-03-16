package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
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
import net.minecraft.server.packs.resources.ResourceThunk;
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

	public static ClientLanguage loadFrom(ResourceManager resourceManager, List<LanguageInfo> list) {
		Map<String, String> map = Maps.<String, String>newHashMap();
		boolean bl = false;

		for (LanguageInfo languageInfo : list) {
			bl |= languageInfo.isBidirectional();
			String string = languageInfo.getCode();
			String string2 = String.format("lang/%s.json", string);

			for (String string3 : resourceManager.getNamespaces()) {
				try {
					ResourceLocation resourceLocation = new ResourceLocation(string3, string2);
					appendFrom(string, resourceManager.getResourceStack(resourceLocation), map);
				} catch (FileNotFoundException var11) {
				} catch (Exception var12) {
					LOGGER.warn("Skipped language file: {}:{} ({})", string3, string2, var12.toString());
				}
			}
		}

		return new ClientLanguage(ImmutableMap.copyOf(map), bl);
	}

	private static void appendFrom(String string, List<ResourceThunk> list, Map<String, String> map) {
		for (ResourceThunk resourceThunk : list) {
			try {
				Resource resource = resourceThunk.open();

				try {
					InputStream inputStream = resource.getInputStream();

					try {
						Language.loadFromJson(inputStream, map::put);
					} catch (Throwable var11) {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (Throwable var10) {
								var11.addSuppressed(var10);
							}
						}

						throw var11;
					}

					if (inputStream != null) {
						inputStream.close();
					}
				} catch (Throwable var12) {
					if (resource != null) {
						try {
							resource.close();
						} catch (Throwable var9) {
							var12.addSuppressed(var9);
						}
					}

					throw var12;
				}

				if (resource != null) {
					resource.close();
				}
			} catch (IOException var13) {
				LOGGER.warn("Failed to load translations for {} from pack {}", string, resourceThunk.sourcePackId(), var13);
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
