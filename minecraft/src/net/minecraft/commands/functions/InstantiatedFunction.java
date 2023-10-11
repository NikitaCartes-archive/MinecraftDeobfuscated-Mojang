package net.minecraft.commands.functions;

import java.util.List;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.resources.ResourceLocation;

public interface InstantiatedFunction<T> {
	ResourceLocation id();

	List<UnboundEntryAction<T>> entries();
}
