package net.minecraft.tags;

public class SerializationTags {
	private static volatile TagContainer instance = TagContainer.of(
		BlockTags.getAllTags(), ItemTags.getAllTags(), FluidTags.getAllTags(), EntityTypeTags.getAllTags()
	);

	public static TagContainer getInstance() {
		return instance;
	}

	public static void bind(TagContainer tagContainer) {
		instance = tagContainer;
	}
}
