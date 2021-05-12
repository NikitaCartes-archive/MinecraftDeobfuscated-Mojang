package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class Stitcher {
	private static final Comparator<Stitcher.Holder> HOLDER_COMPARATOR = Comparator.comparing(holder -> -holder.height)
		.thenComparing(holder -> -holder.width)
		.thenComparing(holder -> holder.spriteInfo.name());
	private final int mipLevel;
	private final Set<Stitcher.Holder> texturesToBeStitched = Sets.<Stitcher.Holder>newHashSetWithExpectedSize(256);
	private final List<Stitcher.Region> storage = Lists.<Stitcher.Region>newArrayListWithCapacity(256);
	private int storageX;
	private int storageY;
	private final int maxWidth;
	private final int maxHeight;

	public Stitcher(int i, int j, int k) {
		this.mipLevel = k;
		this.maxWidth = i;
		this.maxHeight = j;
	}

	public int getWidth() {
		return this.storageX;
	}

	public int getHeight() {
		return this.storageY;
	}

	public void registerSprite(TextureAtlasSprite.Info info) {
		Stitcher.Holder holder = new Stitcher.Holder(info, this.mipLevel);
		this.texturesToBeStitched.add(holder);
	}

	public void stitch() {
		List<Stitcher.Holder> list = Lists.<Stitcher.Holder>newArrayList(this.texturesToBeStitched);
		list.sort(HOLDER_COMPARATOR);

		for (Stitcher.Holder holder : list) {
			if (!this.addToStorage(holder)) {
				throw new StitcherException(
					holder.spriteInfo, (Collection<TextureAtlasSprite.Info>)list.stream().map(holderx -> holderx.spriteInfo).collect(ImmutableList.toImmutableList())
				);
			}
		}

		this.storageX = Mth.smallestEncompassingPowerOfTwo(this.storageX);
		this.storageY = Mth.smallestEncompassingPowerOfTwo(this.storageY);
	}

	public void gatherSprites(Stitcher.SpriteLoader spriteLoader) {
		for (Stitcher.Region region : this.storage) {
			region.walk(regionx -> {
				Stitcher.Holder holder = regionx.getHolder();
				TextureAtlasSprite.Info info = holder.spriteInfo;
				spriteLoader.load(info, this.storageX, this.storageY, regionx.getX(), regionx.getY());
			});
		}
	}

	static int smallestFittingMinTexel(int i, int j) {
		return (i >> j) + ((i & (1 << j) - 1) == 0 ? 0 : 1) << j;
	}

	private boolean addToStorage(Stitcher.Holder holder) {
		for (Stitcher.Region region : this.storage) {
			if (region.add(holder)) {
				return true;
			}
		}

		return this.expand(holder);
	}

	private boolean expand(Stitcher.Holder holder) {
		int i = Mth.smallestEncompassingPowerOfTwo(this.storageX);
		int j = Mth.smallestEncompassingPowerOfTwo(this.storageY);
		int k = Mth.smallestEncompassingPowerOfTwo(this.storageX + holder.width);
		int l = Mth.smallestEncompassingPowerOfTwo(this.storageY + holder.height);
		boolean bl = k <= this.maxWidth;
		boolean bl2 = l <= this.maxHeight;
		if (!bl && !bl2) {
			return false;
		} else {
			boolean bl3 = bl && i != k;
			boolean bl4 = bl2 && j != l;
			boolean bl5;
			if (bl3 ^ bl4) {
				bl5 = bl3;
			} else {
				bl5 = bl && i <= j;
			}

			Stitcher.Region region;
			if (bl5) {
				if (this.storageY == 0) {
					this.storageY = holder.height;
				}

				region = new Stitcher.Region(this.storageX, 0, holder.width, this.storageY);
				this.storageX = this.storageX + holder.width;
			} else {
				region = new Stitcher.Region(0, this.storageY, this.storageX, holder.height);
				this.storageY = this.storageY + holder.height;
			}

			region.add(holder);
			this.storage.add(region);
			return true;
		}
	}

	@Environment(EnvType.CLIENT)
	static class Holder {
		public final TextureAtlasSprite.Info spriteInfo;
		public final int width;
		public final int height;

		public Holder(TextureAtlasSprite.Info info, int i) {
			this.spriteInfo = info;
			this.width = Stitcher.smallestFittingMinTexel(info.width(), i);
			this.height = Stitcher.smallestFittingMinTexel(info.height(), i);
		}

		public String toString() {
			return "Holder{width=" + this.width + ", height=" + this.height + "}";
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Region {
		private final int originX;
		private final int originY;
		private final int width;
		private final int height;
		private List<Stitcher.Region> subSlots;
		private Stitcher.Holder holder;

		public Region(int i, int j, int k, int l) {
			this.originX = i;
			this.originY = j;
			this.width = k;
			this.height = l;
		}

		public Stitcher.Holder getHolder() {
			return this.holder;
		}

		public int getX() {
			return this.originX;
		}

		public int getY() {
			return this.originY;
		}

		public boolean add(Stitcher.Holder holder) {
			if (this.holder != null) {
				return false;
			} else {
				int i = holder.width;
				int j = holder.height;
				if (i <= this.width && j <= this.height) {
					if (i == this.width && j == this.height) {
						this.holder = holder;
						return true;
					} else {
						if (this.subSlots == null) {
							this.subSlots = Lists.<Stitcher.Region>newArrayListWithCapacity(1);
							this.subSlots.add(new Stitcher.Region(this.originX, this.originY, i, j));
							int k = this.width - i;
							int l = this.height - j;
							if (l > 0 && k > 0) {
								int m = Math.max(this.height, k);
								int n = Math.max(this.width, l);
								if (m >= n) {
									this.subSlots.add(new Stitcher.Region(this.originX, this.originY + j, i, l));
									this.subSlots.add(new Stitcher.Region(this.originX + i, this.originY, k, this.height));
								} else {
									this.subSlots.add(new Stitcher.Region(this.originX + i, this.originY, k, j));
									this.subSlots.add(new Stitcher.Region(this.originX, this.originY + j, this.width, l));
								}
							} else if (k == 0) {
								this.subSlots.add(new Stitcher.Region(this.originX, this.originY + j, i, l));
							} else if (l == 0) {
								this.subSlots.add(new Stitcher.Region(this.originX + i, this.originY, k, j));
							}
						}

						for (Stitcher.Region region : this.subSlots) {
							if (region.add(holder)) {
								return true;
							}
						}

						return false;
					}
				} else {
					return false;
				}
			}
		}

		public void walk(Consumer<Stitcher.Region> consumer) {
			if (this.holder != null) {
				consumer.accept(this);
			} else if (this.subSlots != null) {
				for (Stitcher.Region region : this.subSlots) {
					region.walk(consumer);
				}
			}
		}

		public String toString() {
			return "Slot{originX="
				+ this.originX
				+ ", originY="
				+ this.originY
				+ ", width="
				+ this.width
				+ ", height="
				+ this.height
				+ ", texture="
				+ this.holder
				+ ", subSlots="
				+ this.subSlots
				+ "}";
		}
	}

	@Environment(EnvType.CLIENT)
	public interface SpriteLoader {
		void load(TextureAtlasSprite.Info info, int i, int j, int k, int l);
	}
}
