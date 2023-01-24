/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture.atlas;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(value=EnvType.CLIENT)
public interface SpriteSource {
    public static final FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");

    public void run(ResourceManager var1, Output var2);

    public SpriteSourceType type();

    @Environment(value=EnvType.CLIENT)
    public static interface SpriteSupplier
    extends Supplier<SpriteContents> {
        default public void discard() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Output {
        default public void add(ResourceLocation resourceLocation, Resource resource) {
            this.add(resourceLocation, () -> SpriteLoader.loadSprite(resourceLocation, resource));
        }

        public void add(ResourceLocation var1, SpriteSupplier var2);

        public void removeAll(Predicate<ResourceLocation> var1);
    }
}

