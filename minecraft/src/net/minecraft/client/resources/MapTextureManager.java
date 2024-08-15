package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Environment(EnvType.CLIENT)
public class MapTextureManager implements AutoCloseable {
	private final Int2ObjectMap<MapTextureManager.MapInstance> maps = new Int2ObjectOpenHashMap<>();
	final TextureManager textureManager;

	public MapTextureManager(TextureManager textureManager) {
		this.textureManager = textureManager;
	}

	public void update(MapId mapId, MapItemSavedData mapItemSavedData) {
		this.getOrCreateMapInstance(mapId, mapItemSavedData).forceUpload();
	}

	public ResourceLocation prepareMapTexture(MapId mapId, MapItemSavedData mapItemSavedData) {
		MapTextureManager.MapInstance mapInstance = this.getOrCreateMapInstance(mapId, mapItemSavedData);
		mapInstance.updateTextureIfNeeded();
		return mapInstance.location;
	}

	public void resetData() {
		for (MapTextureManager.MapInstance mapInstance : this.maps.values()) {
			mapInstance.close();
		}

		this.maps.clear();
	}

	private MapTextureManager.MapInstance getOrCreateMapInstance(MapId mapId, MapItemSavedData mapItemSavedData) {
		return this.maps.compute(mapId.id(), (integer, mapInstance) -> {
			if (mapInstance == null) {
				return new MapTextureManager.MapInstance(integer, mapItemSavedData);
			} else {
				mapInstance.replaceMapData(mapItemSavedData);
				return mapInstance;
			}
		});
	}

	public void close() {
		this.resetData();
	}

	@Environment(EnvType.CLIENT)
	class MapInstance implements AutoCloseable {
		private MapItemSavedData data;
		private final DynamicTexture texture;
		private boolean requiresUpload = true;
		final ResourceLocation location;

		MapInstance(final int i, final MapItemSavedData mapItemSavedData) {
			this.data = mapItemSavedData;
			this.texture = new DynamicTexture(128, 128, true);
			this.location = MapTextureManager.this.textureManager.register("map/" + i, this.texture);
		}

		void replaceMapData(MapItemSavedData mapItemSavedData) {
			boolean bl = this.data != mapItemSavedData;
			this.data = mapItemSavedData;
			this.requiresUpload |= bl;
		}

		public void forceUpload() {
			this.requiresUpload = true;
		}

		void updateTextureIfNeeded() {
			if (this.requiresUpload) {
				NativeImage nativeImage = this.texture.getPixels();
				if (nativeImage != null) {
					for (int i = 0; i < 128; i++) {
						for (int j = 0; j < 128; j++) {
							int k = j + i * 128;
							nativeImage.setPixel(j, i, MapColor.getColorFromPackedId(this.data.colors[k]));
						}
					}
				}

				this.texture.upload();
				this.requiresUpload = false;
			}
		}

		public void close() {
			this.texture.close();
		}
	}
}
