package net.minecraft.world.level.newbiome.context;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.Random;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public class LazyAreaContext implements BigContext<LazyArea> {
	private final Long2IntLinkedOpenHashMap cache;
	private final int maxCache;
	private final ImprovedNoise biomeNoise;
	private final long seed;
	private long rval;

	public LazyAreaContext(int i, long l, long m) {
		this.seed = mixSeed(l, m);
		this.biomeNoise = new ImprovedNoise(new Random(l));
		this.cache = new Long2IntLinkedOpenHashMap(16, 0.25F);
		this.cache.defaultReturnValue(Integer.MIN_VALUE);
		this.maxCache = i;
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

	@Override
	public void initRandom(long l, long m) {
		long n = this.seed;
		n = LinearCongruentialGenerator.next(n, l);
		n = LinearCongruentialGenerator.next(n, m);
		n = LinearCongruentialGenerator.next(n, l);
		n = LinearCongruentialGenerator.next(n, m);
		this.rval = n;
	}

	@Override
	public int nextRandom(int i) {
		int j = (int)Math.floorMod(this.rval >> 24, (long)i);
		this.rval = LinearCongruentialGenerator.next(this.rval, this.seed);
		return j;
	}

	@Override
	public ImprovedNoise getBiomeNoise() {
		return this.biomeNoise;
	}

	private static long mixSeed(long l, long m) {
		long n = LinearCongruentialGenerator.next(m, m);
		n = LinearCongruentialGenerator.next(n, m);
		n = LinearCongruentialGenerator.next(n, m);
		long o = LinearCongruentialGenerator.next(l, n);
		o = LinearCongruentialGenerator.next(o, n);
		return LinearCongruentialGenerator.next(o, n);
	}
}
