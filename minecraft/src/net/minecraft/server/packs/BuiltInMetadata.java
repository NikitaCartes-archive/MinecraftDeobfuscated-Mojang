package net.minecraft.server.packs;

import java.util.Map;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public class BuiltInMetadata {
	private static final BuiltInMetadata EMPTY = new BuiltInMetadata(Map.of());
	private final Map<MetadataSectionSerializer<?>, ?> values;

	private BuiltInMetadata(Map<MetadataSectionSerializer<?>, ?> map) {
		this.values = map;
	}

	public <T> T get(MetadataSectionSerializer<T> metadataSectionSerializer) {
		return (T)this.values.get(metadataSectionSerializer);
	}

	public static BuiltInMetadata of() {
		return EMPTY;
	}

	public static <T> BuiltInMetadata of(MetadataSectionSerializer<T> metadataSectionSerializer, T object) {
		return new BuiltInMetadata(Map.of(metadataSectionSerializer, object));
	}

	public static <T1, T2> BuiltInMetadata of(
		MetadataSectionSerializer<T1> metadataSectionSerializer, T1 object, MetadataSectionSerializer<T2> metadataSectionSerializer2, T2 object2
	) {
		return new BuiltInMetadata(Map.of(metadataSectionSerializer, object, metadataSectionSerializer2, object2));
	}
}
