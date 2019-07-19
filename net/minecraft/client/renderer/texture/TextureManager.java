/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.PreloadedTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.renderer.texture.TickableTextureObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TextureManager
implements Tickable,
PreparableReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
    private final Map<ResourceLocation, TextureObject> byPath = Maps.newHashMap();
    private final List<Tickable> tickableTextures = Lists.newArrayList();
    private final Map<String, Integer> prefixRegister = Maps.newHashMap();
    private final ResourceManager resourceManager;

    public TextureManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void bind(ResourceLocation resourceLocation) {
        TextureObject textureObject = this.byPath.get(resourceLocation);
        if (textureObject == null) {
            textureObject = new SimpleTexture(resourceLocation);
            this.register(resourceLocation, textureObject);
        }
        textureObject.bind();
    }

    public boolean register(ResourceLocation resourceLocation, TickableTextureObject tickableTextureObject) {
        if (this.register(resourceLocation, (TextureObject)tickableTextureObject)) {
            this.tickableTextures.add(tickableTextureObject);
            return true;
        }
        return false;
    }

    public boolean register(ResourceLocation resourceLocation, TextureObject textureObject) {
        boolean bl = true;
        try {
            textureObject.load(this.resourceManager);
        } catch (IOException iOException) {
            if (resourceLocation != INTENTIONAL_MISSING_TEXTURE) {
                LOGGER.warn("Failed to load texture: {}", (Object)resourceLocation, (Object)iOException);
            }
            textureObject = MissingTextureAtlasSprite.getTexture();
            this.byPath.put(resourceLocation, textureObject);
            bl = false;
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Registering texture");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Resource location being registered");
            TextureObject textureObject2 = textureObject;
            crashReportCategory.setDetail("Resource location", resourceLocation);
            crashReportCategory.setDetail("Texture object class", () -> textureObject2.getClass().getName());
            throw new ReportedException(crashReport);
        }
        this.byPath.put(resourceLocation, textureObject);
        return bl;
    }

    public TextureObject getTexture(ResourceLocation resourceLocation) {
        return this.byPath.get(resourceLocation);
    }

    public ResourceLocation register(String string, DynamicTexture dynamicTexture) {
        Integer integer = this.prefixRegister.get(string);
        if (integer == null) {
            integer = 1;
        } else {
            Integer n = integer;
            Integer n2 = integer = Integer.valueOf(integer + 1);
        }
        this.prefixRegister.put(string, integer);
        ResourceLocation resourceLocation = new ResourceLocation(String.format("dynamic/%s_%d", string, integer));
        this.register(resourceLocation, (TextureObject)dynamicTexture);
        return resourceLocation;
    }

    public CompletableFuture<Void> preload(ResourceLocation resourceLocation, Executor executor) {
        if (!this.byPath.containsKey(resourceLocation)) {
            PreloadedTexture preloadedTexture = new PreloadedTexture(this.resourceManager, resourceLocation, executor);
            this.byPath.put(resourceLocation, preloadedTexture);
            return preloadedTexture.getFuture().thenRunAsync(() -> this.register(resourceLocation, preloadedTexture), Minecraft.getInstance());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void tick() {
        for (Tickable tickable : this.tickableTextures) {
            tickable.tick();
        }
    }

    public void release(ResourceLocation resourceLocation) {
        TextureObject textureObject = this.getTexture(resourceLocation);
        if (textureObject != null) {
            TextureUtil.releaseTextureId(textureObject.getId());
        }
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        return ((CompletableFuture)CompletableFuture.allOf(TitleScreen.preloadResources(this, executor), this.preload(AbstractWidget.WIDGETS_LOCATION, executor)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(void_ -> {
            MissingTextureAtlasSprite.getTexture();
            Iterator<Map.Entry<ResourceLocation, TextureObject>> iterator = this.byPath.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, TextureObject> entry = iterator.next();
                ResourceLocation resourceLocation = entry.getKey();
                TextureObject textureObject = entry.getValue();
                if (textureObject == MissingTextureAtlasSprite.getTexture() && !resourceLocation.equals(MissingTextureAtlasSprite.getLocation())) {
                    iterator.remove();
                    continue;
                }
                textureObject.reset(this, resourceManager, resourceLocation, executor2);
            }
        }, executor2);
    }
}

