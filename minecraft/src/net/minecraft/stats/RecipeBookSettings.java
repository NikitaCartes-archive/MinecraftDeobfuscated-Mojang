package net.minecraft.stats;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {
	public static final StreamCodec<FriendlyByteBuf, RecipeBookSettings> STREAM_CODEC = StreamCodec.ofMember(RecipeBookSettings::write, RecipeBookSettings::read);
	private static final Map<RecipeBookType, Pair<String, String>> TAG_FIELDS = ImmutableMap.of(
		RecipeBookType.CRAFTING,
		Pair.of("isGuiOpen", "isFilteringCraftable"),
		RecipeBookType.FURNACE,
		Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"),
		RecipeBookType.BLAST_FURNACE,
		Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"),
		RecipeBookType.SMOKER,
		Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable")
	);
	private final Map<RecipeBookType, RecipeBookSettings.TypeSettings> states;

	private RecipeBookSettings(Map<RecipeBookType, RecipeBookSettings.TypeSettings> map) {
		this.states = map;
	}

	public RecipeBookSettings() {
		this(new EnumMap(RecipeBookType.class));
	}

	private RecipeBookSettings.TypeSettings getSettings(RecipeBookType recipeBookType) {
		return (RecipeBookSettings.TypeSettings)this.states.getOrDefault(recipeBookType, RecipeBookSettings.TypeSettings.DEFAULT);
	}

	private void updateSettings(RecipeBookType recipeBookType, UnaryOperator<RecipeBookSettings.TypeSettings> unaryOperator) {
		this.states.compute(recipeBookType, (recipeBookTypex, typeSettings) -> {
			if (typeSettings == null) {
				typeSettings = RecipeBookSettings.TypeSettings.DEFAULT;
			}

			typeSettings = (RecipeBookSettings.TypeSettings)unaryOperator.apply(typeSettings);
			if (typeSettings.equals(RecipeBookSettings.TypeSettings.DEFAULT)) {
				typeSettings = null;
			}

			return typeSettings;
		});
	}

	public boolean isOpen(RecipeBookType recipeBookType) {
		return this.getSettings(recipeBookType).open;
	}

	public void setOpen(RecipeBookType recipeBookType, boolean bl) {
		this.updateSettings(recipeBookType, typeSettings -> typeSettings.setOpen(bl));
	}

	public boolean isFiltering(RecipeBookType recipeBookType) {
		return this.getSettings(recipeBookType).filtering;
	}

	public void setFiltering(RecipeBookType recipeBookType, boolean bl) {
		this.updateSettings(recipeBookType, typeSettings -> typeSettings.setFiltering(bl));
	}

	private static RecipeBookSettings read(FriendlyByteBuf friendlyByteBuf) {
		Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = new EnumMap(RecipeBookType.class);

		for (RecipeBookType recipeBookType : RecipeBookType.values()) {
			boolean bl = friendlyByteBuf.readBoolean();
			boolean bl2 = friendlyByteBuf.readBoolean();
			if (bl || bl2) {
				map.put(recipeBookType, new RecipeBookSettings.TypeSettings(bl, bl2));
			}
		}

		return new RecipeBookSettings(map);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		for (RecipeBookType recipeBookType : RecipeBookType.values()) {
			RecipeBookSettings.TypeSettings typeSettings = (RecipeBookSettings.TypeSettings)this.states
				.getOrDefault(recipeBookType, RecipeBookSettings.TypeSettings.DEFAULT);
			friendlyByteBuf.writeBoolean(typeSettings.open);
			friendlyByteBuf.writeBoolean(typeSettings.filtering);
		}
	}

	public static RecipeBookSettings read(CompoundTag compoundTag) {
		Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = new EnumMap(RecipeBookType.class);
		TAG_FIELDS.forEach((recipeBookType, pair) -> {
			boolean bl = compoundTag.getBoolean((String)pair.getFirst());
			boolean bl2 = compoundTag.getBoolean((String)pair.getSecond());
			if (bl || bl2) {
				map.put(recipeBookType, new RecipeBookSettings.TypeSettings(bl, bl2));
			}
		});
		return new RecipeBookSettings(map);
	}

	public void write(CompoundTag compoundTag) {
		TAG_FIELDS.forEach(
			(recipeBookType, pair) -> {
				RecipeBookSettings.TypeSettings typeSettings = (RecipeBookSettings.TypeSettings)this.states
					.getOrDefault(recipeBookType, RecipeBookSettings.TypeSettings.DEFAULT);
				compoundTag.putBoolean((String)pair.getFirst(), typeSettings.open);
				compoundTag.putBoolean((String)pair.getSecond(), typeSettings.filtering);
			}
		);
	}

	public RecipeBookSettings copy() {
		return new RecipeBookSettings(new EnumMap(this.states));
	}

	public void replaceFrom(RecipeBookSettings recipeBookSettings) {
		this.states.clear();
		this.states.putAll(recipeBookSettings.states);
	}

	public boolean equals(Object object) {
		return this == object || object instanceof RecipeBookSettings && this.states.equals(((RecipeBookSettings)object).states);
	}

	public int hashCode() {
		return this.states.hashCode();
	}

	static record TypeSettings(boolean open, boolean filtering) {
		public static final RecipeBookSettings.TypeSettings DEFAULT = new RecipeBookSettings.TypeSettings(false, false);

		public String toString() {
			return "[open=" + this.open + ", filtering=" + this.filtering + "]";
		}

		public RecipeBookSettings.TypeSettings setOpen(boolean bl) {
			return new RecipeBookSettings.TypeSettings(bl, this.filtering);
		}

		public RecipeBookSettings.TypeSettings setFiltering(boolean bl) {
			return new RecipeBookSettings.TypeSettings(this.open, bl);
		}
	}
}
