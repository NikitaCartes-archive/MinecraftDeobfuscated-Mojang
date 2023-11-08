package net.minecraft.commands;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CacheableFunction {
	public static final Codec<CacheableFunction> CODEC = ResourceLocation.CODEC.xmap(CacheableFunction::new, CacheableFunction::getId);
	private final ResourceLocation id;
	private boolean resolved;
	private Optional<CommandFunction<CommandSourceStack>> function = Optional.empty();

	public CacheableFunction(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	public Optional<CommandFunction<CommandSourceStack>> get(ServerFunctionManager serverFunctionManager) {
		if (!this.resolved) {
			this.function = serverFunctionManager.get(this.id);
			this.resolved = true;
		}

		return this.function;
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else {
			if (object instanceof CacheableFunction cacheableFunction && this.getId().equals(cacheableFunction.getId())) {
				return true;
			}

			return false;
		}
	}
}
