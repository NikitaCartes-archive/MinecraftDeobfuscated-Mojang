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
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
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
implements Dumpable,
Tickable {
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
    private int width;
    private int height;
    private int mipLevel;

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
        this.width = preparations.width();
        this.height = preparations.height();
        this.mipLevel = preparations.mipLevel();
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

    @Override
    public void dumpContents(ResourceLocation resourceLocation, Path path) throws IOException {
        String string = resourceLocation.toDebugFileName();
        TextureUtil.writeAsPNG(path, string, this.getId(), this.mipLevel, this.width, this.height);
        TextureAtlas.dumpSpriteNames(path, string, this.texturesByName);
    }

    private static void dumpSpriteNames(Path path, String string, Map<ResourceLocation, TextureAtlasSprite> map) {
        Path path2 = path.resolve(string + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path2, new OpenOption[0]);){
            for (Map.Entry entry : map.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)entry.getValue();
                writer.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", entry.getKey(), textureAtlasSprite.getX(), textureAtlasSprite.getY(), textureAtlasSprite.contents().width(), textureAtlasSprite.contents().height()));
            }
        } catch (IOException iOException) {
            LOGGER.warn("Failed to write file {}", (Object)path2, (Object)iOException);
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

    int getWidth() {
        return this.width;
    }

    int getHeight() {
        return this.height;
    }

    public void updateFilter(SpriteLoader.Preparations preparations) {
        this.setFilter(false, preparations.mipLevel() > 0);
    }
}

