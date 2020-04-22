/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ComponentArgument
implements ArgumentType<Component> {
    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType(object -> new TranslatableComponent("argument.component.invalid", object));

    private ComponentArgument() {
    }

    public static Component getComponent(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, Component.class);
    }

    public static ComponentArgument textComponent() {
        return new ComponentArgument();
    }

    @Override
    public Component parse(StringReader stringReader) throws CommandSyntaxException {
        try {
            MutableComponent component = Component.Serializer.fromJson(stringReader);
            if (component == null) {
                throw ERROR_INVALID_JSON.createWithContext(stringReader, "empty");
            }
            return component;
        } catch (JsonParseException jsonParseException) {
            String string = jsonParseException.getCause() != null ? jsonParseException.getCause().getMessage() : jsonParseException.getMessage();
            throw ERROR_INVALID_JSON.createWithContext(stringReader, string);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

