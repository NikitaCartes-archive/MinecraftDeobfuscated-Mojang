package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.world.level.block.state.properties.ChestType;

@Environment(EnvType.CLIENT)
public class PackResourcesAdapterV4 implements PackResources {
	private static final Map<String, Pair<ChestType, ResourceLocation>> CHESTS = Util.make(
		Maps.<String, Pair<ChestType, ResourceLocation>>newHashMap(), hashMap -> {
			hashMap.put("textures/entity/chest/normal_left.png", new Pair<>(ChestType.LEFT, new ResourceLocation("textures/entity/chest/normal_double.png")));
			hashMap.put("textures/entity/chest/normal_right.png", new Pair<>(ChestType.RIGHT, new ResourceLocation("textures/entity/chest/normal_double.png")));
			hashMap.put("textures/entity/chest/normal.png", new Pair<>(ChestType.SINGLE, new ResourceLocation("textures/entity/chest/normal.png")));
			hashMap.put("textures/entity/chest/trapped_left.png", new Pair<>(ChestType.LEFT, new ResourceLocation("textures/entity/chest/trapped_double.png")));
			hashMap.put("textures/entity/chest/trapped_right.png", new Pair<>(ChestType.RIGHT, new ResourceLocation("textures/entity/chest/trapped_double.png")));
			hashMap.put("textures/entity/chest/trapped.png", new Pair<>(ChestType.SINGLE, new ResourceLocation("textures/entity/chest/trapped.png")));
			hashMap.put("textures/entity/chest/christmas_left.png", new Pair<>(ChestType.LEFT, new ResourceLocation("textures/entity/chest/christmas_double.png")));
			hashMap.put("textures/entity/chest/christmas_right.png", new Pair<>(ChestType.RIGHT, new ResourceLocation("textures/entity/chest/christmas_double.png")));
			hashMap.put("textures/entity/chest/christmas.png", new Pair<>(ChestType.SINGLE, new ResourceLocation("textures/entity/chest/christmas.png")));
			hashMap.put("textures/entity/chest/ender.png", new Pair<>(ChestType.SINGLE, new ResourceLocation("textures/entity/chest/ender.png")));
		}
	);
	private static final List<String> PATTERNS = Lists.<String>newArrayList(
		"base",
		"border",
		"bricks",
		"circle",
		"creeper",
		"cross",
		"curly_border",
		"diagonal_left",
		"diagonal_right",
		"diagonal_up_left",
		"diagonal_up_right",
		"flower",
		"globe",
		"gradient",
		"gradient_up",
		"half_horizontal",
		"half_horizontal_bottom",
		"half_vertical",
		"half_vertical_right",
		"mojang",
		"rhombus",
		"skull",
		"small_stripes",
		"square_bottom_left",
		"square_bottom_right",
		"square_top_left",
		"square_top_right",
		"straight_cross",
		"stripe_bottom",
		"stripe_center",
		"stripe_downleft",
		"stripe_downright",
		"stripe_left",
		"stripe_middle",
		"stripe_right",
		"stripe_top",
		"triangle_bottom",
		"triangle_top",
		"triangles_bottom",
		"triangles_top"
	);
	private static final Set<String> SHIELDS = (Set<String>)PATTERNS.stream()
		.map(string -> "textures/entity/shield/" + string + ".png")
		.collect(Collectors.toSet());
	private static final Set<String> BANNERS = (Set<String>)PATTERNS.stream()
		.map(string -> "textures/entity/banner/" + string + ".png")
		.collect(Collectors.toSet());
	public static final ResourceLocation SHIELD_BASE = new ResourceLocation("textures/entity/shield_base.png");
	public static final ResourceLocation BANNER_BASE = new ResourceLocation("textures/entity/banner_base.png");
	public static final int DEFAULT_CHEST_SIZE = 64;
	public static final int DEFAULT_SHIELD_SIZE = 64;
	public static final int DEFAULT_BANNER_SIZE = 64;
	public static final ResourceLocation OLD_IRON_GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem.png");
	public static final String NEW_IRON_GOLEM_PATH = "textures/entity/iron_golem/iron_golem.png";
	private final PackResources pack;

	public PackResourcesAdapterV4(PackResources packResources) {
		this.pack = packResources;
	}

	@Override
	public InputStream getRootResource(String string) throws IOException {
		return this.pack.getRootResource(string);
	}

	@Override
	public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
		if (!"minecraft".equals(resourceLocation.getNamespace())) {
			return this.pack.hasResource(packType, resourceLocation);
		} else {
			String string = resourceLocation.getPath();
			if ("textures/misc/enchanted_item_glint.png".equals(string)) {
				return false;
			} else if ("textures/entity/iron_golem/iron_golem.png".equals(string)) {
				return this.pack.hasResource(packType, OLD_IRON_GOLEM_LOCATION);
			} else if ("textures/entity/conduit/wind.png".equals(string) || "textures/entity/conduit/wind_vertical.png".equals(string)) {
				return false;
			} else if (SHIELDS.contains(string)) {
				return this.pack.hasResource(packType, SHIELD_BASE) && this.pack.hasResource(packType, resourceLocation);
			} else if (!BANNERS.contains(string)) {
				Pair<ChestType, ResourceLocation> pair = (Pair<ChestType, ResourceLocation>)CHESTS.get(string);
				return pair != null && this.pack.hasResource(packType, pair.getSecond()) ? true : this.pack.hasResource(packType, resourceLocation);
			} else {
				return this.pack.hasResource(packType, BANNER_BASE) && this.pack.hasResource(packType, resourceLocation);
			}
		}
	}

	@Override
	public InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException {
		if (!"minecraft".equals(resourceLocation.getNamespace())) {
			return this.pack.getResource(packType, resourceLocation);
		} else {
			String string = resourceLocation.getPath();
			if ("textures/entity/iron_golem/iron_golem.png".equals(string)) {
				return this.pack.getResource(packType, OLD_IRON_GOLEM_LOCATION);
			} else {
				if (SHIELDS.contains(string)) {
					InputStream inputStream = fixPattern(this.pack.getResource(packType, SHIELD_BASE), this.pack.getResource(packType, resourceLocation), 64, 2, 2, 12, 22);
					if (inputStream != null) {
						return inputStream;
					}
				} else if (BANNERS.contains(string)) {
					InputStream inputStream = fixPattern(this.pack.getResource(packType, BANNER_BASE), this.pack.getResource(packType, resourceLocation), 64, 0, 0, 42, 41);
					if (inputStream != null) {
						return inputStream;
					}
				} else {
					if ("textures/entity/enderdragon/dragon.png".equals(string) || "textures/entity/enderdragon/dragon_exploding.png".equals(string)) {
						ByteArrayInputStream var23;
						try (NativeImage nativeImage = NativeImage.read(this.pack.getResource(packType, resourceLocation))) {
							int i = nativeImage.getWidth() / 256;

							for (int j = 88 * i; j < 200 * i; j++) {
								for (int k = 56 * i; k < 112 * i; k++) {
									nativeImage.setPixelRGBA(k, j, 0);
								}
							}

							var23 = new ByteArrayInputStream(nativeImage.asByteArray());
						}

						return var23;
					}

					if ("textures/entity/conduit/closed_eye.png".equals(string) || "textures/entity/conduit/open_eye.png".equals(string)) {
						return fixConduitEyeTexture(this.pack.getResource(packType, resourceLocation));
					}

					Pair<ChestType, ResourceLocation> pair = (Pair<ChestType, ResourceLocation>)CHESTS.get(string);
					if (pair != null) {
						ChestType chestType = pair.getFirst();
						InputStream inputStream2 = this.pack.getResource(packType, pair.getSecond());
						if (chestType == ChestType.SINGLE) {
							return fixSingleChest(inputStream2);
						}

						if (chestType == ChestType.LEFT) {
							return fixLeftChest(inputStream2);
						}

						if (chestType == ChestType.RIGHT) {
							return fixRightChest(inputStream2);
						}
					}
				}

				return this.pack.getResource(packType, resourceLocation);
			}
		}
	}

	@Nullable
	public static InputStream fixPattern(InputStream inputStream, InputStream inputStream2, int i, int j, int k, int l, int m) throws IOException {
		ByteArrayInputStream var71;
		try (
			NativeImage nativeImage = NativeImage.read(inputStream);
			NativeImage nativeImage2 = NativeImage.read(inputStream2);
		) {
			int n = nativeImage.getWidth();
			int o = nativeImage.getHeight();
			if (n != nativeImage2.getWidth() || o != nativeImage2.getHeight()) {
				return null;
			}

			try (NativeImage nativeImage3 = new NativeImage(n, o, true)) {
				int p = n / i;

				for (int q = k * p; q < m * p; q++) {
					for (int r = j * p; r < l * p; r++) {
						int s = NativeImage.getR(nativeImage2.getPixelRGBA(r, q));
						int t = nativeImage.getPixelRGBA(r, q);
						nativeImage3.setPixelRGBA(r, q, NativeImage.combine(s, NativeImage.getB(t), NativeImage.getG(t), NativeImage.getR(t)));
					}
				}

				var71 = new ByteArrayInputStream(nativeImage3.asByteArray());
			}
		}

		return var71;
	}

	public static InputStream fixConduitEyeTexture(InputStream inputStream) throws IOException {
		ByteArrayInputStream var7;
		try (NativeImage nativeImage = NativeImage.read(inputStream)) {
			int i = nativeImage.getWidth();
			int j = nativeImage.getHeight();

			try (NativeImage nativeImage2 = new NativeImage(2 * i, 2 * j, true)) {
				copyRect(nativeImage, nativeImage2, 0, 0, 0, 0, i, j, 1, false, false);
				var7 = new ByteArrayInputStream(nativeImage2.asByteArray());
			}
		}

		return var7;
	}

	public static InputStream fixLeftChest(InputStream inputStream) throws IOException {
		ByteArrayInputStream var8;
		try (NativeImage nativeImage = NativeImage.read(inputStream)) {
			int i = nativeImage.getWidth();
			int j = nativeImage.getHeight();

			try (NativeImage nativeImage2 = new NativeImage(i / 2, j, true)) {
				int k = j / 64;
				copyRect(nativeImage, nativeImage2, 29, 0, 29, 0, 15, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 59, 0, 14, 0, 15, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 29, 14, 43, 14, 15, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 44, 14, 29, 14, 14, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 58, 14, 14, 14, 15, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 29, 19, 29, 19, 15, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 59, 19, 14, 19, 15, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 29, 33, 43, 33, 15, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 44, 33, 29, 33, 14, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 58, 33, 14, 33, 15, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 2, 0, 2, 0, 1, 1, k, false, true);
				copyRect(nativeImage, nativeImage2, 4, 0, 1, 0, 1, 1, k, false, true);
				copyRect(nativeImage, nativeImage2, 2, 1, 3, 1, 1, 4, k, true, true);
				copyRect(nativeImage, nativeImage2, 3, 1, 2, 1, 1, 4, k, true, true);
				copyRect(nativeImage, nativeImage2, 4, 1, 1, 1, 1, 4, k, true, true);
				var8 = new ByteArrayInputStream(nativeImage2.asByteArray());
			}
		}

		return var8;
	}

	public static InputStream fixRightChest(InputStream inputStream) throws IOException {
		ByteArrayInputStream var8;
		try (NativeImage nativeImage = NativeImage.read(inputStream)) {
			int i = nativeImage.getWidth();
			int j = nativeImage.getHeight();

			try (NativeImage nativeImage2 = new NativeImage(i / 2, j, true)) {
				int k = j / 64;
				copyRect(nativeImage, nativeImage2, 14, 0, 29, 0, 15, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 44, 0, 14, 0, 15, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 0, 14, 0, 14, 14, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 14, 14, 43, 14, 15, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 73, 14, 14, 14, 15, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 14, 19, 29, 19, 15, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 44, 19, 14, 19, 15, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 0, 33, 0, 33, 14, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 14, 33, 43, 33, 15, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 73, 33, 14, 33, 15, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 1, 0, 2, 0, 1, 1, k, false, true);
				copyRect(nativeImage, nativeImage2, 3, 0, 1, 0, 1, 1, k, false, true);
				copyRect(nativeImage, nativeImage2, 0, 1, 0, 1, 1, 4, k, true, true);
				copyRect(nativeImage, nativeImage2, 1, 1, 3, 1, 1, 4, k, true, true);
				copyRect(nativeImage, nativeImage2, 5, 1, 1, 1, 1, 4, k, true, true);
				var8 = new ByteArrayInputStream(nativeImage2.asByteArray());
			}
		}

		return var8;
	}

	public static InputStream fixSingleChest(InputStream inputStream) throws IOException {
		ByteArrayInputStream var8;
		try (NativeImage nativeImage = NativeImage.read(inputStream)) {
			int i = nativeImage.getWidth();
			int j = nativeImage.getHeight();

			try (NativeImage nativeImage2 = new NativeImage(i, j, true)) {
				int k = j / 64;
				copyRect(nativeImage, nativeImage2, 14, 0, 28, 0, 14, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 28, 0, 14, 0, 14, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 0, 14, 0, 14, 14, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 14, 14, 42, 14, 14, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 28, 14, 28, 14, 14, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 42, 14, 14, 14, 14, 5, k, true, true);
				copyRect(nativeImage, nativeImage2, 14, 19, 28, 19, 14, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 28, 19, 14, 19, 14, 14, k, false, true);
				copyRect(nativeImage, nativeImage2, 0, 33, 0, 33, 14, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 14, 33, 42, 33, 14, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 28, 33, 28, 33, 14, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 42, 33, 14, 33, 14, 10, k, true, true);
				copyRect(nativeImage, nativeImage2, 1, 0, 3, 0, 2, 1, k, false, true);
				copyRect(nativeImage, nativeImage2, 3, 0, 1, 0, 2, 1, k, false, true);
				copyRect(nativeImage, nativeImage2, 0, 1, 0, 1, 1, 4, k, true, true);
				copyRect(nativeImage, nativeImage2, 1, 1, 4, 1, 2, 4, k, true, true);
				copyRect(nativeImage, nativeImage2, 3, 1, 3, 1, 1, 4, k, true, true);
				copyRect(nativeImage, nativeImage2, 4, 1, 1, 1, 2, 4, k, true, true);
				var8 = new ByteArrayInputStream(nativeImage2.asByteArray());
			}
		}

		return var8;
	}

	@Override
	public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int i, Predicate<String> predicate) {
		return this.pack.getResources(packType, string, string2, i, predicate);
	}

	@Override
	public Set<String> getNamespaces(PackType packType) {
		return this.pack.getNamespaces(packType);
	}

	@Nullable
	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
		return this.pack.getMetadataSection(metadataSectionSerializer);
	}

	@Override
	public String getName() {
		return this.pack.getName();
	}

	@Override
	public void close() {
		this.pack.close();
	}

	private static void copyRect(NativeImage nativeImage, NativeImage nativeImage2, int i, int j, int k, int l, int m, int n, int o, boolean bl, boolean bl2) {
		n *= o;
		m *= o;
		k *= o;
		l *= o;
		i *= o;
		j *= o;

		for (int p = 0; p < n; p++) {
			for (int q = 0; q < m; q++) {
				nativeImage2.setPixelRGBA(k + q, l + p, nativeImage.getPixelRGBA(i + (bl ? m - 1 - q : q), j + (bl2 ? n - 1 - p : p)));
			}
		}
	}
}
