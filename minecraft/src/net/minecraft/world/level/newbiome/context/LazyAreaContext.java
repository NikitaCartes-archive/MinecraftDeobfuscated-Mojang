package net.minecraft.world.level.newbiome.context;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.Random;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public class LazyAreaContext implements BigContext<LazyArea> {
	private final Long2IntLinkedOpenHashMap cache;
	private final int maxCache;
	protected long seedMixup;
	protected ImprovedNoise biomeNoise;
	private long seed;
	private long rval;

	public LazyAreaContext(int i, long l, long m) {
		this.seedMixup = m;
		this.seedMixup = this.seedMixup * (this.seedMixup * 6364136223846793005L + 1442695040888963407L);
		this.seedMixup += m;
		this.seedMixup = this.seedMixup * (this.seedMixup * 6364136223846793005L + 1442695040888963407L);
		this.seedMixup += m;
		this.seedMixup = this.seedMixup * (this.seedMixup * 6364136223846793005L + 1442695040888963407L);
		this.seedMixup += m;
		this.cache = new Long2IntLinkedOpenHashMap(16, 0.25F);
		this.cache.defaultReturnValue(Integer.MIN_VALUE);
		this.maxCache = i;
		this.init(l);
	}

	public LazyArea createResult(PixelTransformer pixelTransformer) {
		return new LazyArea(this.cache, this.maxCache, pixelTransformer);
	}

	public LazyArea createResult(PixelTransformer pixelTransformer, LazyArea lazyArea) {
		return new LazyArea(this.cache, Math.min(1024, lazyArea.getMaxCache() * 4), pixelTransformer);
	}

	public LazyArea createResult(PixelTransformer pixelTransformer, LazyArea lazyArea, LazyArea lazyArea2) {
		return new LazyArea(this.cache, Math.min(1024, Math.max(lazyArea.getMaxCache(), lazyArea2.getMaxCache()) * 4), pixelTransformer);
	}

	public void init(long l) {
		this.seed = l;
		this.seed = this.seed * (this.seed * 6364136223846793005L + 1442695040888963407L);
		this.seed = this.seed + this.seedMixup;
		this.seed = this.seed * (this.seed * 6364136223846793005L + 1442695040888963407L);
		this.seed = this.seed + this.seedMixup;
		this.seed = this.seed * (this.seed * 6364136223846793005L + 1442695040888963407L);
		this.seed = this.seed + this.seedMixup;
		this.biomeNoise = new ImprovedNoise(new Random(l));
	}

	@Override
	public void initRandom(long l, long m) {
		this.rval = this.seed;
		this.rval = this.rval * (this.rval * 6364136223846793005L + 1442695040888963407L);
		this.rval += l;
		this.rval = this.rval * (this.rval * 6364136223846793005L + 1442695040888963407L);
		this.rval += m;
		this.rval = this.rval * (this.rval * 6364136223846793005L + 1442695040888963407L);
		this.rval += l;
		this.rval = this.rval * (this.rval * 6364136223846793005L + 1442695040888963407L);
		this.rval += m;
	}

	@Override
	public int nextRandom(int i) {
		int j = (int)((this.rval >> 24) % (long)i);
		if (j < 0) {
			j += i;
		}

		this.rval = this.rval * (this.rval * 6364136223846793005L + 1442695040888963407L);
		this.rval = this.rval + this.seed;
		return j;
	}

	@Override
	public ImprovedNoise getBiomeNoise() {
		return this.biomeNoise;
	}
}
