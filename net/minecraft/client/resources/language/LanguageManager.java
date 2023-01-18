/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class LanguageManager
implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_LANGUAGE_CODE = "en_us";
    private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("US", "English", false);
    private Map<String, LanguageInfo> languages = ImmutableMap.of("en_us", DEFAULT_LANGUAGE);
    private String currentCode;

    public LanguageManager(String string) {
        this.currentCode = string;
    }

    private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> stream) {
        HashMap map = Maps.newHashMap();
        stream.forEach(packResources -> {
            try {
                LanguageMetadataSection languageMetadataSection = packResources.getMetadataSection(LanguageMetadataSection.TYPE);
                if (languageMetadataSection != null) {
                    languageMetadataSection.languages().forEach(map::putIfAbsent);
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", (Object)packResources.packId(), (Object)exception);
            }
        });
        return ImmutableMap.copyOf(map);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        LanguageInfo languageInfo;
        this.languages = LanguageManager.extractLanguages(resourceManager.listPacks());
        ArrayList<String> list = new ArrayList<String>(2);
        boolean bl = DEFAULT_LANGUAGE.bidirectional();
        list.add(DEFAULT_LANGUAGE_CODE);
        if (!this.currentCode.equals(DEFAULT_LANGUAGE_CODE) && (languageInfo = this.languages.get(this.currentCode)) != null) {
            list.add(this.currentCode);
            bl = languageInfo.bidirectional();
        }
        ClientLanguage clientLanguage = ClientLanguage.loadFrom(resourceManager, list, bl);
        I18n.setLanguage(clientLanguage);
        Language.inject(clientLanguage);
    }

    public void setSelected(String string) {
        this.currentCode = string;
    }

    public String getSelected() {
        return this.currentCode;
    }

    public SortedMap<String, LanguageInfo> getLanguages() {
        return new TreeMap<String, LanguageInfo>(this.languages);
    }

    @Nullable
    public LanguageInfo getLanguage(String string) {
        return this.languages.get(string);
    }
}

