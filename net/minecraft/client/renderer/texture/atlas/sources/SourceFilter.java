/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ResourceLocationPattern;

@Environment(value=EnvType.CLIENT)
public class SourceFilter
implements SpriteSource {
    public static final Codec<SourceFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceLocationPattern.CODEC.fieldOf("pattern")).forGetter(sourceFilter -> sourceFilter.filter)).apply((Applicative<SourceFilter, ?>)instance, SourceFilter::new));
    private final ResourceLocationPattern filter;

    public SourceFilter(ResourceLocationPattern resourceLocationPattern) {
        this.filter = resourceLocationPattern;
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        output.removeAll(this.filter.locationPredicate());
    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.FILTER;
    }
}

