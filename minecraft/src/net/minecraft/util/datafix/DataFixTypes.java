package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.References;

public enum DataFixTypes {
	LEVEL(References.LEVEL),
	PLAYER(References.PLAYER),
	CHUNK(References.CHUNK),
	HOTBAR(References.HOTBAR),
	OPTIONS(References.OPTIONS),
	STRUCTURE(References.STRUCTURE),
	STATS(References.STATS),
	SAVED_DATA_COMMAND_STORAGE(References.SAVED_DATA_COMMAND_STORAGE),
	SAVED_DATA_FORCED_CHUNKS(References.SAVED_DATA_FORCED_CHUNKS),
	SAVED_DATA_MAP_DATA(References.SAVED_DATA_MAP_DATA),
	SAVED_DATA_MAP_INDEX(References.SAVED_DATA_MAP_INDEX),
	SAVED_DATA_RAIDS(References.SAVED_DATA_RAIDS),
	SAVED_DATA_RANDOM_SEQUENCES(References.SAVED_DATA_RANDOM_SEQUENCES),
	SAVED_DATA_SCOREBOARD(References.SAVED_DATA_SCOREBOARD),
	SAVED_DATA_STRUCTURE_FEATURE_INDICES(References.SAVED_DATA_STRUCTURE_FEATURE_INDICES),
	ADVANCEMENTS(References.ADVANCEMENTS),
	POI_CHUNK(References.POI_CHUNK),
	WORLD_GEN_SETTINGS(References.WORLD_GEN_SETTINGS),
	ENTITY_CHUNK(References.ENTITY_CHUNK);

	public static final Set<TypeReference> TYPES_FOR_LEVEL_LIST;
	private final TypeReference type;

	private DataFixTypes(TypeReference typeReference) {
		this.type = typeReference;
	}

	private static int currentVersion() {
		return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
	}

	public <T> Dynamic<T> update(DataFixer dataFixer, Dynamic<T> dynamic, int i, int j) {
		return dataFixer.update(this.type, dynamic, i, j);
	}

	public <T> Dynamic<T> updateToCurrentVersion(DataFixer dataFixer, Dynamic<T> dynamic, int i) {
		return this.update(dataFixer, dynamic, i, currentVersion());
	}

	public CompoundTag update(DataFixer dataFixer, CompoundTag compoundTag, int i, int j) {
		return (CompoundTag)this.update(dataFixer, new Dynamic<>(NbtOps.INSTANCE, compoundTag), i, j).getValue();
	}

	public CompoundTag updateToCurrentVersion(DataFixer dataFixer, CompoundTag compoundTag, int i) {
		return this.update(dataFixer, compoundTag, i, currentVersion());
	}

	static {
		TYPES_FOR_LEVEL_LIST = Set.of(LEVEL.type);
	}
}
