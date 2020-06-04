package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class LanguageManager implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("en_us", "US", "English", false);
	private Map<String, LanguageInfo> languages = ImmutableMap.of("en_us", DEFAULT_LANGUAGE);
	private String currentCode;
	private LanguageInfo currentLanguage = DEFAULT_LANGUAGE;

	public LanguageManager(String string) {
		this.currentCode = string;
	}

	private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> stream) {
		Map<String, LanguageInfo> map = Maps.<String, LanguageInfo>newHashMap();
		stream.forEach(packResources -> {
			try {
				LanguageMetadataSection languageMetadataSection = packResources.getMetadataSection(LanguageMetadataSection.SERIALIZER);
				if (languageMetadataSection != null) {
					for (LanguageInfo languageInfo : languageMetadataSection.getLanguages()) {
						map.putIfAbsent(languageInfo.getCode(), languageInfo);
					}
				}
			} catch (IOException | RuntimeException var5) {
				LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", packResources.getName(), var5);
			}
		});
		return ImmutableMap.copyOf(map);
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.languages = extractLanguages(resourceManager.listPacks());
		LanguageInfo languageInfo = (LanguageInfo)this.languages.getOrDefault("en_us", DEFAULT_LANGUAGE);
		this.currentLanguage = (LanguageInfo)this.languages.getOrDefault(this.currentCode, languageInfo);
		List<LanguageInfo> list = Lists.<LanguageInfo>newArrayList(languageInfo);
		if (this.currentLanguage != languageInfo) {
			list.add(this.currentLanguage);
		}

		ClientLanguage clientLanguage = ClientLanguage.loadFrom(resourceManager, list);
		I18n.setLanguage(clientLanguage);
		Language.inject(clientLanguage);
	}

	public void setSelected(LanguageInfo languageInfo) {
		this.currentCode = languageInfo.getCode();
		this.currentLanguage = languageInfo;
	}

	public LanguageInfo getSelected() {
		return this.currentLanguage;
	}

	public SortedSet<LanguageInfo> getLanguages() {
		return Sets.<LanguageInfo>newTreeSet(this.languages.values());
	}

	public LanguageInfo getLanguage(String string) {
		return (LanguageInfo)this.languages.get(string);
	}
}
