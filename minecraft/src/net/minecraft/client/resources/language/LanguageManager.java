package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class LanguageManager implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("US", "English", false);
	private Map<String, LanguageInfo> languages = ImmutableMap.of("en_us", DEFAULT_LANGUAGE);
	private String currentCode;
	private final Consumer<ClientLanguage> reloadCallback;

	public LanguageManager(String string, Consumer<ClientLanguage> consumer) {
		this.currentCode = string;
		this.reloadCallback = consumer;
	}

	private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> stream) {
		Map<String, LanguageInfo> map = Maps.<String, LanguageInfo>newHashMap();
		stream.forEach(packResources -> {
			try {
				LanguageMetadataSection languageMetadataSection = packResources.getMetadataSection(LanguageMetadataSection.TYPE);
				if (languageMetadataSection != null) {
					languageMetadataSection.languages().forEach(map::putIfAbsent);
				}
			} catch (IOException | RuntimeException var3) {
				LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", packResources.packId(), var3);
			}
		});
		return ImmutableMap.copyOf(map);
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.languages = extractLanguages(resourceManager.listPacks());
		List<String> list = new ArrayList(2);
		boolean bl = DEFAULT_LANGUAGE.bidirectional();
		list.add("en_us");
		if (!this.currentCode.equals("en_us")) {
			LanguageInfo languageInfo = (LanguageInfo)this.languages.get(this.currentCode);
			if (languageInfo != null) {
				list.add(this.currentCode);
				bl = languageInfo.bidirectional();
			}
		}

		ClientLanguage clientLanguage = ClientLanguage.loadFrom(resourceManager, list, bl);
		I18n.setLanguage(clientLanguage);
		Language.inject(clientLanguage);
		this.reloadCallback.accept(clientLanguage);
	}

	public void setSelected(String string) {
		this.currentCode = string;
	}

	public String getSelected() {
		return this.currentCode;
	}

	public SortedMap<String, LanguageInfo> getLanguages() {
		return new TreeMap(this.languages);
	}

	@Nullable
	public LanguageInfo getLanguage(String string) {
		return (LanguageInfo)this.languages.get(string);
	}
}
