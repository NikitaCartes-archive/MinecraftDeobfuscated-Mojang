/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TextureAtlas
extends AbstractTexture
implements Tickable {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Deprecated
    public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
    @Deprecated
    public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
    private List<SpriteContents> sprites = List.of();
    private List<TextureAtlasSprite.Ticker> animatedTextures = List.of();
    private Map<ResourceLocation, TextureAtlasSprite> texturesByName = Map.of();
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;

    public TextureAtlas(ResourceLocation resourceLocation) {
        this.location = resourceLocation;
        this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
    }

    @Override
    public void load(ResourceManager resourceManager) {
    }

    public void upload(SpriteLoader.Preparations preparations) {
        LOGGER.info("Created: {}x{}x{} {}-atlas", preparations.width(), preparations.height(), preparations.mipLevel(), this.location);
        TextureUtil.prepareImage(this.getId(), preparations.mipLevel(), preparations.width(), preparations.height());
        this.clearTextureData();
        this.texturesByName = Map.copyOf(preparations.regions());
        ArrayList<SpriteContents> list = new ArrayList<SpriteContents>();
        ArrayList<TextureAtlasSprite.Ticker> list2 = new ArrayList<TextureAtlasSprite.Ticker>();
        for (TextureAtlasSprite textureAtlasSprite : preparations.regions().values()) {
            list.add(textureAtlasSprite.contents());
            try {
                textureAtlasSprite.uploadFirstFrame();
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
                crashReportCategory.setDetail("Atlas path", this.location);
                crashReportCategory.setDetail("Sprite", textureAtlasSprite);
                throw new ReportedException(crashReport);
            }
            TextureAtlasSprite.Ticker ticker = textureAtlasSprite.createTicker();
            if (ticker == null) continue;
            list2.add(ticker);
        }
        this.sprites = List.copyOf(list);
        this.animatedTextures = List.copyOf(list2);
    }

    private void dumpContents(int i, int j, int k) {
        String string = this.location.toDebugFileName();
        TextureUtil.writeAsPNG(string, this.getId(), i, j, k);
        TextureAtlas.dumpSpriteNames(string, this.texturesByName);
    }

    private static void dumpSpriteNames(String string, Map<ResourceLocation, TextureAtlasSprite> map) {
        Path path = Path.of(string + ".txt", new String[0]);
        try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
            for (Map.Entry entry : map.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)entry.getValue();
                writer.write(String.format("%s\tx=%d\ty=%d\tw=%d\th=%d%n", entry.getKey(), textureAtlasSprite.getX(), textureAtlasSprite.getY(), textureAtlasSprite.contents().width(), textureAtlasSprite.contents().height()));
            }
        } catch (IOException iOException) {
            LOGGER.warn("Failed to write file {}", (Object)path, (Object)iOException);
        }
    }

    public void cycleAnimationFrames() {
        this.bind();
        for (TextureAtlasSprite.Ticker ticker : this.animatedTextures) {
            ticker.tickAndUpload();
        }
    }

    @Override
    public void tick() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::cycleAnimationFrames);
        } else {
            this.cycleAnimationFrames();
        }
    }

    public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        TextureAtlasSprite textureAtlasSprite = this.texturesByName.get(resourceLocation);
        if (textureAtlasSprite == null) {
            return this.texturesByName.get(MissingTextureAtlasSprite.getLocation());
        }
        return textureAtlasSprite;
    }

    public void clearTextureData() {
        this.sprites.forEach(SpriteContents::close);
        this.animatedTextures.forEach(TextureAtlasSprite.Ticker::close);
        this.sprites = List.of();
        this.animatedTextures = List.of();
        this.texturesByName = Map.of();
    }

    public ResourceLocation location() {
        return this.location;
    }

    public int maxSupportedTextureSize() {
        return this.maxSupportedTextureSize;
    }

    public void updateFilter(SpriteLoader.Preparations preparations) {
        this.setFilter(false, preparations.mipLevel() > 0);
    }
}

