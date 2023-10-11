package net.minecraft.commands;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CacheableFunction {
	public static final CacheableFunction NONE = new CacheableFunction(null);
	@Nullable
	private final ResourceLocation id;
	private boolean resolved;
	private Optional<CommandFunction<CommandSourceStack>> function = Optional.empty();

	public CacheableFunction(@Nullable ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	public Optional<CommandFunction<CommandSourceStack>> get(ServerFunctionManager serverFunctionManager) {
		if (!this.resolved) {
			if (this.id != null) {
				this.function = serverFunctionManager.get(this.id);
			}

			this.resolved = true;
		}

		return this.function;
	}

	@Nullable
	public ResourceLocation getId() {
		return (ResourceLocation)this.function.map(CommandFunction::id).orElse(this.id);
	}
}
