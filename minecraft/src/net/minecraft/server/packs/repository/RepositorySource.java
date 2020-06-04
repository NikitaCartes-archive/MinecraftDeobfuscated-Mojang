package net.minecraft.server.packs.repository;

import java.util.function.Consumer;

public interface RepositorySource {
	<T extends Pack> void loadPacks(Consumer<T> consumer, Pack.PackConstructor<T> packConstructor);
}
