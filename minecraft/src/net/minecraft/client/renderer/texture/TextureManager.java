package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.TextureObject;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
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
public class TextureManager implements Tickable, PreparableReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
	private final Map<ResourceLocation, TextureObject> byPath = Maps.<ResourceLocation, TextureObject>newHashMap();
	private final List<Tickable> tickableTextures = Lists.<Tickable>newArrayList();
	private final Map<String, Integer> prefixRegister = Maps.<String, Integer>newHashMap();
	private final ResourceManager resourceManager;

	public TextureManager(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void bind(ResourceLocation resourceLocation) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this._bind(resourceLocation));
		} else {
			this._bind(resourceLocation);
		}
	}

	private void _bind(ResourceLocation resourceLocation) {
		TextureObject textureObject = (TextureObject)this.byPath.get(resourceLocation);
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
		} else {
			return false;
		}
	}

	public boolean register(ResourceLocation resourceLocation, TextureObject textureObject) {
		boolean bl = true;

		try {
			textureObject.load(this.resourceManager);
		} catch (IOException var8) {
			if (resourceLocation != INTENTIONAL_MISSING_TEXTURE) {
				LOGGER.warn("Failed to load texture: {}", resourceLocation, var8);
			}

			textureObject = MissingTextureAtlasSprite.getTexture();
			this.byPath.put(resourceLocation, textureObject);
			bl = false;
		} catch (Throwable var9) {
			CrashReport crashReport = CrashReport.forThrowable(var9, "Registering texture");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Resource location being registered");
			crashReportCategory.setDetail("Resource location", resourceLocation);
			crashReportCategory.setDetail("Texture object class", (CrashReportDetail<String>)(() -> textureObject.getClass().getName()));
			throw new ReportedException(crashReport);
		}

		this.byPath.put(resourceLocation, textureObject);
		return bl;
	}

	@Nullable
	public TextureObject getTexture(ResourceLocation resourceLocation) {
		return (TextureObject)this.byPath.get(resourceLocation);
	}

	public ResourceLocation register(String string, DynamicTexture dynamicTexture) {
		Integer integer = (Integer)this.prefixRegister.get(string);
		if (integer == null) {
			integer = 1;
		} else {
			integer = integer + 1;
		}

		this.prefixRegister.put(string, integer);
		ResourceLocation resourceLocation = new ResourceLocation(String.format("dynamic/%s_%d", string, integer));
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
		TextureObject textureObject = this.getTexture(resourceLocation);
		if (textureObject != null) {
			TextureUtil.releaseTextureId(textureObject.getId());
		}
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
				Iterator<Entry<ResourceLocation, TextureObject>> iterator = this.byPath.entrySet().iterator();

				while (iterator.hasNext()) {
					Entry<ResourceLocation, TextureObject> entry = (Entry<ResourceLocation, TextureObject>)iterator.next();
					ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
					TextureObject textureObject = (TextureObject)entry.getValue();
					if (textureObject == MissingTextureAtlasSprite.getTexture() && !resourceLocation.equals(MissingTextureAtlasSprite.getLocation())) {
						iterator.remove();
					} else {
						textureObject.reset(this, resourceManager, resourceLocation, executor2);
					}
				}
			}, runnable -> RenderSystem.recordRenderCall(runnable::run));
	}
}
