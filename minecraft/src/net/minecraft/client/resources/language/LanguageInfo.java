package net.minecraft.client.resources.language;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

@Environment(EnvType.CLIENT)
public record LanguageInfo(String region, String name, boolean bidirectional) {
	public static final Codec<LanguageInfo> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.NON_EMPTY_STRING.fieldOf("region").forGetter(LanguageInfo::region),
					ExtraCodecs.NON_EMPTY_STRING.fieldOf("name").forGetter(LanguageInfo::name),
					Codec.BOOL.optionalFieldOf("bidirectional", Boolean.valueOf(false)).forGetter(LanguageInfo::bidirectional)
				)
				.apply(instance, LanguageInfo::new)
	);

	public Component toComponent() {
		return Component.literal(this.name + " (" + this.region + ")");
	}
}
