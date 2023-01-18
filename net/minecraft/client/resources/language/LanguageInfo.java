/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.language;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

@Environment(value=EnvType.CLIENT)
public record LanguageInfo(String region, String name, boolean bidirectional) {
    public static final Codec<LanguageInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.NON_EMPTY_STRING.fieldOf("region")).forGetter(LanguageInfo::region), ((MapCodec)ExtraCodecs.NON_EMPTY_STRING.fieldOf("name")).forGetter(LanguageInfo::name), Codec.BOOL.optionalFieldOf("bidirectional", false).forGetter(LanguageInfo::bidirectional)).apply((Applicative<LanguageInfo, ?>)instance, LanguageInfo::new));

    public Component toComponent() {
        return Component.literal(this.name + " (" + this.region + ")");
    }
}

