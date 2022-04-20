package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class RotationArgument implements ArgumentType<Coordinates> {
	private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~-5 ~5");
	public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.rotation.incomplete"));

	public static RotationArgument rotation() {
		return new RotationArgument();
	}

	public static Coordinates getRotation(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, Coordinates.class);
	}

	public Coordinates parse(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		if (!stringReader.canRead()) {
			throw ERROR_NOT_COMPLETE.createWithContext(stringReader);
		} else {
			WorldCoordinate worldCoordinate = WorldCoordinate.parseDouble(stringReader, false);
			if (stringReader.canRead() && stringReader.peek() == ' ') {
				stringReader.skip();
				WorldCoordinate worldCoordinate2 = WorldCoordinate.parseDouble(stringReader, false);
				return new WorldCoordinates(worldCoordinate2, worldCoordinate, new WorldCoordinate(true, 0.0));
			} else {
				stringReader.setCursor(i);
				throw ERROR_NOT_COMPLETE.createWithContext(stringReader);
			}
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
