package net.minecraft.client.renderer.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class TextureManager implements PreparableReloadListener, Tickable, AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
	private final Map<ResourceLocation, AbstractTexture> byPath = Maps.<ResourceLocation, AbstractTexture>newHashMap();
	private final Set<Tickable> tickableTextures = Sets.<Tickable>newHashSet();
	private final Map<String, Integer> prefixRegister = Maps.<String, Integer>newHashMap();
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
		AbstractTexture abstractTexture = (AbstractTexture)this.byPath.get(resourceLocation);
		if (abstractTexture == null) {
			abstractTexture = new SimpleTexture(resourceLocation);
			this.register(resourceLocation, abstractTexture);
		}

		abstractTexture.bind();
	}

	public void register(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
		abstractTexture = this.loadTexture(resourceLocation, abstractTexture);
		AbstractTexture abstractTexture2 = (AbstractTexture)this.byPath.put(resourceLocation, abstractTexture);
		if (abstractTexture2 != abstractTexture) {
			if (abstractTexture2 != null && abstractTexture2 != MissingTextureAtlasSprite.getTexture()) {
				this.tickableTextures.remove(abstractTexture2);
				this.safeClose(resourceLocation, abstractTexture2);
			}

			if (abstractTexture instanceof Tickable) {
				this.tickableTextures.add((Tickable)abstractTexture);
			}
		}
	}

	private void safeClose(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
		if (abstractTexture != MissingTextureAtlasSprite.getTexture()) {
			try {
				abstractTexture.close();
			} catch (Exception var4) {
				LOGGER.warn("Failed to close texture {}", resourceLocation, var4);
			}
		}

		abstractTexture.releaseId();
	}

	private AbstractTexture loadTexture(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
		try {
			abstractTexture.load(this.resourceManager);
			return abstractTexture;
		} catch (IOException var6) {
			if (resourceLocation != INTENTIONAL_MISSING_TEXTURE) {
				LOGGER.warn("Failed to load texture: {}", resourceLocation, var6);
			}

			return MissingTextureAtlasSprite.getTexture();
		} catch (Throwable var7) {
			CrashReport crashReport = CrashReport.forThrowable(var7, "Registering texture");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Resource location being registered");
			crashReportCategory.setDetail("Resource location", resourceLocation);
			crashReportCategory.setDetail("Texture object class", (CrashReportDetail<String>)(() -> abstractTexture.getClass().getName()));
			throw new ReportedException(crashReport);
		}
	}

	public AbstractTexture getTexture(ResourceLocation resourceLocation) {
		AbstractTexture abstractTexture = (AbstractTexture)this.byPath.get(resourceLocation);
		if (abstractTexture == null) {
			abstractTexture = new SimpleTexture(resourceLocation);
			this.register(resourceLocation, abstractTexture);
		}

		return abstractTexture;
	}

	public AbstractTexture getTexture(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
		return (AbstractTexture)this.byPath.getOrDefault(resourceLocation, abstractTexture);
	}

	public ResourceLocation register(String string, DynamicTexture dynamicTexture) {
		Integer integer = (Integer)this.prefixRegister.get(string);
		if (integer == null) {
			integer = 1;
		} else {
			integer = integer + 1;
		}

		this.prefixRegister.put(string, integer);
		ResourceLocation resourceLocation = new ResourceLocation(String.format(Locale.ROOT, "dynamic/%s_%d", string, integer));
		this.register(resourceLocation, dynamicTexture);
		return resourceLocation;
	}

	public CompletableFuture<Void> preload(ResourceLocation resourceLocation, Executor executor) {
		if (!this.byPath.containsKey(resourceLocation)) {
			PreloadedTexture preloadedTexture = new PreloadedTexture(this.resourceManager, resourceLocation, executor);
			this.byPath.put(resourceLocation, preloadedTexture);
			return preloadedTexture.getFuture().thenRunAsync(() -> this.register(resourceLocation, preloadedTexture), TextureManager::execute);
		} else {
			return CompletableFuture.completedFuture(null);
		}
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

	public void close() {
		this.byPath.forEach(this::safeClose);
		this.byPath.clear();
		this.tickableTextures.clear();
		this.prefixRegister.clear();
	}

	@Override
	public CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	) {
		return CompletableFuture.allOf(TitleScreen.preloadResources(this, executor), this.preload(AbstractWidget.WIDGETS_LOCATION, executor))
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(void_ -> {
				MissingTextureAtlasSprite.getTexture();
				RealmsMainScreen.updateTeaserImages(this.resourceManager);
				Iterator<Entry<ResourceLocation, AbstractTexture>> iterator = this.byPath.entrySet().iterator();

				while (iterator.hasNext()) {
					Entry<ResourceLocation, AbstractTexture> entry = (Entry<ResourceLocation, AbstractTexture>)iterator.next();
					ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
					AbstractTexture abstractTexture = (AbstractTexture)entry.getValue();
					if (abstractTexture == MissingTextureAtlasSprite.getTexture() && !resourceLocation.equals(MissingTextureAtlasSprite.getLocation())) {
						iterator.remove();
					} else {
						abstractTexture.reset(this, resourceManager, resourceLocation, executor2);
					}
				}
			}, runnable -> RenderSystem.recordRenderCall(runnable::run));
	}
}
