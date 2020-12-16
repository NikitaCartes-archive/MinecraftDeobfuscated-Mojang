package net.minecraft.tags;

public class SerializationTags {
	private static volatile TagContainer instance = StaticTags.createCollection();

	public static TagContainer getInstance() {
		return instance;
	}

	public static void bind(TagContainer tagContainer) {
		instance = tagContainer;
	}
}
