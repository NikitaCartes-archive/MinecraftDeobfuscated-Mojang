package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.StringUtils;

public class WorldOptions {
	public static final MapCodec<WorldOptions> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.LONG.fieldOf("seed").stable().forGetter(WorldOptions::seed),
					Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(WorldOptions::generateStructures),
					Codec.BOOL.fieldOf("bonus_chest").orElse(false).stable().forGetter(WorldOptions::generateBonusChest),
					Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(worldOptions -> worldOptions.legacyCustomOptions)
				)
				.apply(instance, instance.stable(WorldOptions::new))
	);
	public static final WorldOptions DEMO_OPTIONS = new WorldOptions((long)"North Carolina".hashCode(), true, true);
	private final long seed;
	private final boolean generateStructures;
	private final boolean generateBonusChest;
	private final Optional<String> legacyCustomOptions;

	public WorldOptions(long l, boolean bl, boolean bl2) {
		this(l, bl, bl2, Optional.empty());
	}

	public static WorldOptions defaultWithRandomSeed() {
		return new WorldOptions(randomSeed(), true, false);
	}

	private WorldOptions(long l, boolean bl, boolean bl2, Optional<String> optional) {
		this.seed = l;
		this.generateStructures = bl;
		this.generateBonusChest = bl2;
		this.legacyCustomOptions = optional;
	}

	public long seed() {
		return this.seed;
	}

	public boolean generateStructures() {
		return this.generateStructures;
	}

	public boolean generateBonusChest() {
		return this.generateBonusChest;
	}

	public boolean isOldCustomizedWorld() {
		return this.legacyCustomOptions.isPresent();
	}

	public WorldOptions withBonusChest(boolean bl) {
		return new WorldOptions(this.seed, this.generateStructures, bl, this.legacyCustomOptions);
	}

	public WorldOptions withStructures(boolean bl) {
		return new WorldOptions(this.seed, bl, this.generateBonusChest, this.legacyCustomOptions);
	}

	public WorldOptions withSeed(OptionalLong optionalLong) {
		return new WorldOptions(optionalLong.orElse(randomSeed()), this.generateStructures, this.generateBonusChest, this.legacyCustomOptions);
	}

	public static OptionalLong parseSeed(String string) {
		string = string.trim();
		if (StringUtils.isEmpty(string)) {
			return OptionalLong.empty();
		} else {
			try {
				return OptionalLong.of(Long.parseLong(string));
			} catch (NumberFormatException var2) {
				return OptionalLong.of((long)string.hashCode());
			}
		}
	}

	public static long randomSeed() {
		return RandomSource.create().nextLong();
	}
}
