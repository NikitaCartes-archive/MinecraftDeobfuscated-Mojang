/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.PreloadedTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TextureManager
implements PreparableReloadListener,
Tickable,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
    private final Map<ResourceLocation, AbstractTexture> byPath = Maps.newHashMap();
    private final Set<Tickable> tickableTextures = Sets.newHashSet();
    private final Map<String, Integer> prefixRegister = Maps.newHashMap();
    private final ResourceManager resourceManager;

    public TextureManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void bindForSetup(ResourceLocation resourceLocation) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._bind(resourceLocation));
        } else {
            this._bind(resourceLocation);
        }
    }

    private void _bind(ResourceLocation resourceLocation) {
        AbstractTexture abstractTexture = this.byPath.get(resourceLocation);
        if (abstractTexture == null) {
            abstractTexture = new SimpleTexture(resourceLocation);
            this.register(resourceLocation, abstractTexture);
        }
        abstractTexture.bind();
    }

    public void register(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
        AbstractTexture abstractTexture2 = this.byPath.put(resourceLocation, abstractTexture = this.loadTexture(resourceLocation, abstractTexture));
        if (abstractTexture2 != abstractTexture) {
            if (abstractTexture2 != null && abstractTexture2 != MissingTextureAtlasSprite.getTexture()) {
                this.tickableTextures.remove(abstractTexture2);
                this.safeClose(resourceLocation, abstractTexture2);
            }
            if (abstractTexture instanceof Tickable) {
                this.tickableTextures.add((Tickable)((Object)abstractTexture));
            }
        }
    }

    private void safeClose(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
        if (abstractTexture != MissingTextureAtlasSprite.getTexture()) {
            try {
                abstractTexture.close();
            } catch (Exception exception) {
                LOGGER.warn("Failed to close texture {}", (Object)resourceLocation, (Object)exception);
            }
        }
        abstractTexture.releaseId();
    }

    private AbstractTexture loadTexture(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
        try {
            abstractTexture.load(this.resourceManager);
            return abstractTexture;
        } catch (IOException iOException) {
            if (resourceLocation != INTENTIONAL_MISSING_TEXTURE) {
                LOGGER.warn("Failed to load texture: {}", (Object)resourceLocation, (Object)iOException);
            }
            return MissingTextureAtlasSprite.getTexture();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Registering texture");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Resource location being registered");
            crashReportCategory.setDetail("Resource location", resourceLocation);
            crashReportCategory.setDetail("Texture object class", () -> abstractTexture.getClass().getName());
            throw new ReportedException(crashReport);
        }
    }

    public AbstractTexture getTexture(ResourceLocation resourceLocation) {
        AbstractTexture abstractTexture = this.byPath.get(resourceLocation);
        if (abstractTexture == null) {
            abstractTexture = new SimpleTexture(resourceLocation);
            this.register(resourceLocation, abstractTexture);
        }
        return abstractTexture;
    }

    public AbstractTexture getTexture(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
        return this.byPath.getOrDefault(resourceLocation, abstractTexture);
    }

    public ResourceLocation register(String string, DynamicTexture dynamicTexture) {
        Integer integer = this.prefixRegister.get(string);
        if (integer == null) {
            integer = 1;
        } else {
            Integer n = integer;
            integer = integer + 1;
        }
        this.prefixRegister.put(string, integer);
        ResourceLocation resourceLocation = new ResourceLocation(String.format(Locale.ROOT, "dynamic/%s_%d", string, integer));
        this.register(resourceLocation, (AbstractTexture)dynamicTexture);
        return resourceLocation;
    }

    public CompletableFuture<Void> preload(ResourceLocation resourceLocation, Executor executor) {
        if (!this.byPath.containsKey(resourceLocation)) {
            PreloadedTexture preloadedTexture = new PreloadedTexture(this.resourceManager, resourceLocation, executor);
            this.byPath.put(resourceLocation, preloadedTexture);
            return preloadedTexture.getFuture().thenRunAsync(() -> this.register(resourceLocation, preloadedTexture), TextureManager::execute);
        }
        return CompletableFuture.completedFuture(null);
    }

    private static void execute(Runnable runnable) {
        Minecraft.getInstance().execute(() -> RenderSystem.recordRenderCall(runnable::run));
    }

    @Override
    public void tick() {
        for (Tickable tickable : this.tickableTextures) {
            tickable.tick();
        }
    }

    public void release(ResourceLocation resourceLocation) {
        AbstractTexture abstractTexture = this.getTexture(resourceLocation, MissingTextureAtlasSprite.getTexture());
        if (abstractTexture != MissingTextureAtlasSprite.getTexture()) {
            TextureUtil.releaseTextureId(abstractTexture.getId());
        }
    }

    @Override
    public void close() {
        this.byPath.forEach(this::safeClose);
        this.byPath.clear();
        this.tickableTextures.clear();
        this.prefixRegister.clear();
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
        ((CompletableFuture)CompletableFuture.allOf(TitleScreen.preloadResources(this, executor), this.preload(AbstractWidget.WIDGETS_LOCATION, executor)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(void_ -> {
            MissingTextureAtlasSprite.getTexture();
            RealmsMainScreen.updateTeaserImages(this.resourceManager);
            Iterator<Map.Entry<ResourceLocation, AbstractTexture>> iterator = this.byPath.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, AbstractTexture> entry = iterator.next();
                ResourceLocation resourceLocation = entry.getKey();
                AbstractTexture abstractTexture = entry.getValue();
                if (abstractTexture == MissingTextureAtlasSprite.getTexture() && !resourceLocation.equals(MissingTextureAtlasSprite.getLocation())) {
                    iterator.remove();
                    continue;
                }
                abstractTexture.reset(this, resourceManager, resourceLocation, executor2);
            }
            Minecraft.getInstance().tell(() -> completableFuture.complete(null));
        }, runnable -> RenderSystem.recordRenderCall(runnable::run));
        return completableFuture;
    }

    public void dumpAllSheets(Path path) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._dumpAllSheets(path));
        } else {
            this._dumpAllSheets(path);
        }
    }

    private void _dumpAllSheets(Path path) {
        try {
            Files.createDirectories(path, new FileAttribute[0]);
        } catch (IOException iOException) {
            LOGGER.error("Failed to create directory {}", (Object)path, (Object)iOException);
            return;
        }
        this.byPath.forEach((resourceLocation, abstractTexture) -> {
            if (abstractTexture instanceof Dumpable) {
                Dumpable dumpable = (Dumpable)((Object)abstractTexture);
                try {
                    dumpable.dumpContents((ResourceLocation)resourceLocation, path);
                } catch (IOException iOException) {
                    LOGGER.error("Failed to dump texture {}", resourceLocation, (Object)iOException);
                }
            }
        });
    }
}

