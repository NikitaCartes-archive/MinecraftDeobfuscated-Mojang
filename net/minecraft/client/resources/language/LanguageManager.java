/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class LanguageManager
implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("en_us", "US", "English", false);
    private Map<String, LanguageInfo> languages = ImmutableMap.of("en_us", DEFAULT_LANGUAGE);
    private String currentCode;
    private LanguageInfo currentLanguage = DEFAULT_LANGUAGE;

    public LanguageManager(String string) {
        this.currentCode = string;
    }

    private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> stream) {
        HashMap map = Maps.newHashMap();
        stream.forEach(packResources -> {
            try {
                LanguageMetadataSection languageMetadataSection = packResources.getMetadataSection(LanguageMetadataSection.SERIALIZER);
                if (languageMetadataSection != null) {
                    for (LanguageInfo languageInfo : languageMetadataSection.getLanguages()) {
                        map.putIfAbsent(languageInfo.getCode(), languageInfo);
                    }
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", (Object)packResources.getName(), (Object)exception);
            }
        });
        return ImmutableMap.copyOf(map);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.languages = LanguageManager.extractLanguages(resourceManager.listPacks());
        LanguageInfo languageInfo = this.languages.getOrDefault("en_us", DEFAULT_LANGUAGE);
        this.currentLanguage = this.languages.getOrDefault(this.currentCode, languageInfo);
        ArrayList<LanguageInfo> list = Lists.newArrayList(languageInfo);
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
        return Sets.newTreeSet(this.languages.values());
    }

    public LanguageInfo getLanguage(String string) {
        return this.languages.get(string);
    }
}

