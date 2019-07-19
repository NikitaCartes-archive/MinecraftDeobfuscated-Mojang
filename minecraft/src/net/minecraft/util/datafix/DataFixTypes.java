package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL.TypeReference;
import net.minecraft.util.datafix.fixes.References;

public enum DataFixTypes {
	LEVEL(References.LEVEL),
	PLAYER(References.PLAYER),
	CHUNK(References.CHUNK),
	HOTBAR(References.HOTBAR),
	OPTIONS(References.OPTIONS),
	STRUCTURE(References.STRUCTURE),
	STATS(References.STATS),
	SAVED_DATA(References.SAVED_DATA),
	ADVANCEMENTS(References.ADVANCEMENTS),
	POI_CHUNK(References.POI_CHUNK);

	private final TypeReference type;

	private DataFixTypes(TypeReference typeReference) {
		this.type = typeReference;
	}

	public TypeReference getType() {
		return this.type;
	}
}
