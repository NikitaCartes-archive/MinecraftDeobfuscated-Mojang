package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.block.Mirror;

public class TemplateMirrorArgument extends StringRepresentableArgument<Mirror> {
	private TemplateMirrorArgument() {
		super(Mirror.CODEC, Mirror::values);
	}

	public static StringRepresentableArgument<Mirror> templateMirror() {
		return new TemplateMirrorArgument();
	}

	public static Mirror getMirror(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, Mirror.class);
	}
}
