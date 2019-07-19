/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.language;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.Language;
import net.minecraft.client.resources.language.Locale;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class LanguageManager
implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    protected static final Locale LOCALE = new Locale();
    private String currentCode;
    private final Map<String, Language> languages = Maps.newHashMap();

    public LanguageManager(String string) {
        this.currentCode = string;
        I18n.setLocale(LOCALE);
    }

    public void reload(List<Pack> list) {
        this.languages.clear();
        for (Pack pack : list) {
            try {
                LanguageMetadataSection languageMetadataSection = pack.getMetadataSection(LanguageMetadataSection.SERIALIZER);
                if (languageMetadataSection == null) continue;
                for (Language language : languageMetadataSection.getLanguages()) {
                    if (this.languages.containsKey(language.getCode())) continue;
                    this.languages.put(language.getCode(), language);
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", (Object)pack.getName(), (Object)exception);
            }
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ArrayList<String> list = Lists.newArrayList("en_us");
        if (!"en_us".equals(this.currentCode)) {
            list.add(this.currentCode);
        }
        LOCALE.loadFrom(resourceManager, list);
        net.minecraft.locale.Language.forceData(LanguageManager.LOCALE.storage);
    }

    public boolean isBidirectional() {
        return this.getSelected() != null && this.getSelected().isBidirectional();
    }

    public void setSelected(Language language) {
        this.currentCode = language.getCode();
    }

    public Language getSelected() {
        String string = this.languages.containsKey(this.currentCode) ? this.currentCode : "en_us";
        return this.languages.get(string);
    }

    public SortedSet<Language> getLanguages() {
        return Sets.newTreeSet(this.languages.values());
    }

    public Language getLanguage(String string) {
        return this.languages.get(string);
    }
}

