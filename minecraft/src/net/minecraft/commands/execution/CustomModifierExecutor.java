package net.minecraft.commands.execution;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;

public interface CustomModifierExecutor<T> {
	void apply(T object, List<T> list, ContextChain<T> contextChain, ChainModifiers chainModifiers, ExecutionControl<T> executionControl);

	public interface ModifierAdapter<T> extends RedirectModifier<T>, CustomModifierExecutor<T> {
		@Override
		default Collection<T> apply(CommandContext<T> commandContext) throws CommandSyntaxException {
			throw new UnsupportedOperationException("This function should not run");
		}
	}
}
