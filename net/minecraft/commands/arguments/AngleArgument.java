/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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

public class AngleArgument
implements ArgumentType<SingleAngle> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0", "~", "~-5");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TranslatableComponent("argument.angle.incomplete"));

    public static AngleArgument angle() {
        return new AngleArgument();
    }

    public static float getAngle(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, SingleAngle.class).getAngle(commandContext.getSource());
    }

    @Override
    public SingleAngle parse(StringReader stringReader) throws CommandSyntaxException {
        if (!stringReader.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(stringReader);
        }
        boolean bl = WorldCoordinate.isRelative(stringReader);
        float f = stringReader.canRead() && stringReader.peek() != ' ' ? stringReader.readFloat() : 0.0f;
        return new SingleAngle(f, bl);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
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

