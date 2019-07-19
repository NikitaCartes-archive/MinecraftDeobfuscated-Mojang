package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class LevelType {
	public static final LevelType[] LEVEL_TYPES = new LevelType[16];
	public static final LevelType NORMAL = new LevelType(0, "default", 1).setHasReplacement();
	public static final LevelType FLAT = new LevelType(1, "flat").setCustomOptions(true);
	public static final LevelType LARGE_BIOMES = new LevelType(2, "largeBiomes");
	public static final LevelType AMPLIFIED = new LevelType(3, "amplified").setHasHelpText();
	public static final LevelType CUSTOMIZED = new LevelType(4, "customized", "normal", 0).setCustomOptions(true).setSelectableByUser(false);
	public static final LevelType BUFFET = new LevelType(5, "buffet").setCustomOptions(true);
	public static final LevelType DEBUG_ALL_BLOCK_STATES = new LevelType(6, "debug_all_block_states");
	public static final LevelType NORMAL_1_1 = new LevelType(8, "default_1_1", 0).setSelectableByUser(false);
	private final int id;
	private final String generatorName;
	private final String generatorSerialization;
	private final int version;
	private boolean selectable;
	private boolean replacement;
	private boolean hasHelpText;
	private boolean hasCustomOptions;

	private LevelType(int i, String string) {
		this(i, string, string, 0);
	}

	private LevelType(int i, String string, int j) {
		this(i, string, string, j);
	}

	private LevelType(int i, String string, String string2, int j) {
		this.generatorName = string;
		this.generatorSerialization = string2;
		this.version = j;
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
}
