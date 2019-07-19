package net.minecraft.client.resources.metadata.language;

import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.Language;

@Environment(EnvType.CLIENT)
public class LanguageMetadataSection {
	public static final LanguageMetadataSectionSerializer SERIALIZER = new LanguageMetadataSectionSerializer();
	private final Collection<Language> languages;

	public LanguageMetadataSection(Collection<Language> collection) {
		this.languages = collection;
	}

	public Collection<Language> getLanguages() {
		return this.languages;
	}
}
