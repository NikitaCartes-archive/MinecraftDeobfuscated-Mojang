package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

public class AngleArgument implements ArgumentType<AngleArgument.SingleAngle> {
	private static final Collection<String> EXAMPLES = Arrays.asList("0", "~", "~-5");
	public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TranslatableComponent("argument.angle.incomplete"));

	public static AngleArgument angle() {
		return new AngleArgument();
	}

	public static float getAngle(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.<AngleArgument.SingleAngle>getArgument(string, AngleArgument.SingleAngle.class).getAngle(commandContext.getSource());
	}

	public AngleArgument.SingleAngle parse(StringReader stringReader) throws CommandSyntaxException {
		if (!stringReader.canRead()) {
			throw ERROR_NOT_COMPLETE.createWithContext(stringReader);
		} else {
			boolean bl = WorldCoordinate.isRelative(stringReader);
			float f = stringReader.canRead() && stringReader.peek() != ' ' ? stringReader.readFloat() : 0.0F;
			return new AngleArgument.SingleAngle(f, bl);
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static final class SingleAngle {
		private final float angle;
		private final boolean isRelative;

		private SingleAngle(float f, boolean bl) {
			this.angle = f;
			this.isRelative = bl;
		}

		public float getAngle(CommandSourceStack commandSourceStack) {
			return Mth.wrapDegrees(this.isRelative ? this.angle + commandSourceStack.getRotation().y : this.angle);
		}
	}
}
