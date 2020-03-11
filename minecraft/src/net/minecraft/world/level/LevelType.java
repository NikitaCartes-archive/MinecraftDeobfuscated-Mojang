package net.minecraft.world.level;

import com.mojang.datafixers.Dynamic;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;

public class LevelType {
	public static final LevelType[] LEVEL_TYPES = new LevelType[16];
	private static final Dynamic<?> EMPTY_SETTINGS = new Dynamic<>(NbtOps.INSTANCE, new CompoundTag());
	public static final LevelType NORMAL = new LevelType(0, "default", 1, ChunkGeneratorProvider::createNormal).setHasReplacement();
	public static final LevelType FLAT = new LevelType(1, "flat", ChunkGeneratorProvider::createFlat).setCustomOptions(true);
	public static final LevelType LARGE_BIOMES = new LevelType(2, "largeBiomes", ChunkGeneratorProvider::createNormal);
	public static final LevelType AMPLIFIED = new LevelType(3, "amplified", ChunkGeneratorProvider::createNormal).setHasHelpText();
	public static final LevelType CUSTOMIZED = new LevelType(4, "customized", "normal", 0, ChunkGeneratorProvider::createNormal)
		.setCustomOptions(true)
		.setSelectableByUser(false);
	public static final LevelType BUFFET = new LevelType(5, "buffet", ChunkGeneratorProvider::createBuffet).setCustomOptions(true);
	public static final LevelType DEBUG_ALL_BLOCK_STATES = new LevelType(6, "debug_all_block_states", ChunkGeneratorProvider::createDebug);
	public static final LevelType NORMAL_1_1 = new LevelType(8, "default_1_1", 0, ChunkGeneratorProvider::createNormal).setSelectableByUser(false);
	private final int id;
	private final String generatorName;
	private final String generatorSerialization;
	private final int version;
	private final Function<Dynamic<?>, ChunkGeneratorProvider> provider;
	private final LazyLoadedValue<ChunkGeneratorProvider> defaultProvider;
	private boolean selectable;
	private boolean replacement;
	private boolean hasHelpText;
	private boolean hasCustomOptions;

	private LevelType(int i, String string, BiFunction<LevelType, Dynamic<?>, ChunkGeneratorProvider> biFunction) {
		this(i, string, string, 0, biFunction);
	}

	private LevelType(int i, String string, int j, BiFunction<LevelType, Dynamic<?>, ChunkGeneratorProvider> biFunction) {
		this(i, string, string, j, biFunction);
	}

	private LevelType(int i, String string, String string2, int j, BiFunction<LevelType, Dynamic<?>, ChunkGeneratorProvider> biFunction) {
		this.generatorName = string;
		this.generatorSerialization = string2;
		this.version = j;
		this.provider = dynamic -> (ChunkGeneratorProvider)biFunction.apply(this, dynamic);
		this.defaultProvider = new LazyLoadedValue<>(() -> (ChunkGeneratorProvider)biFunction.apply(this, EMPTY_SETTINGS));
		this.selectable = true;
		this.id = i;
		LEVEL_TYPES[i] = this;
	}

	public String getName() {
		return this.generatorName;
	}

	public String getSerialization() {
		return this.generatorSerialization;
	}

	@Environment(EnvType.CLIENT)
	public String getDescriptionId() {
		return "generator." + this.generatorName;
	}

	@Environment(EnvType.CLIENT)
	public String getHelpTextId() {
		return this.getDescriptionId() + ".info";
	}

	public int getVersion() {
		return this.version;
	}

	public LevelType getReplacementForVersion(int i) {
		return this == NORMAL && i == 0 ? NORMAL_1_1 : this;
	}

	@Environment(EnvType.CLIENT)
	public boolean hasCustomOptions() {
		return this.hasCustomOptions;
	}

	public LevelType setCustomOptions(boolean bl) {
		this.hasCustomOptions = bl;
		return this;
	}

	private LevelType setSelectableByUser(boolean bl) {
		this.selectable = bl;
		return this;
	}

	@Environment(EnvType.CLIENT)
	public boolean isSelectable() {
		return this.selectable;
	}

	private LevelType setHasReplacement() {
		this.replacement = true;
		return this;
	}

	public boolean hasReplacement() {
		return this.replacement;
	}

	@Nullable
	public static LevelType getLevelType(String string) {
		for (LevelType levelType : LEVEL_TYPES) {
			if (levelType != null && levelType.generatorName.equalsIgnoreCase(string)) {
				return levelType;
			}
		}

		return null;
	}

	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public boolean hasHelpText() {
		return this.hasHelpText;
	}

	private LevelType setHasHelpText() {
		this.hasHelpText = true;
		return this;
	}

	public ChunkGeneratorProvider createProvider(Dynamic<?> dynamic) {
		return (ChunkGeneratorProvider)this.provider.apply(dynamic);
	}

	public ChunkGeneratorProvider getDefaultProvider() {
		return this.defaultProvider.get();
	}
}
