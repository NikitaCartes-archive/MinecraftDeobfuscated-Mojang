/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.world.level.storage.CommandStorage;

public class StorageDataAccessor
implements DataAccessor {
    static final SuggestionProvider<CommandSourceStack> SUGGEST_STORAGE = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(StorageDataAccessor.getGlobalTags(commandContext).keys(), suggestionsBuilder);
    public static final Function<String, DataCommands.DataProvider> PROVIDER = string -> new DataCommands.DataProvider((String)string){
        final /* synthetic */ String val$arg;
        {
            this.val$arg = string;
        }

        @Override
        public DataAccessor access(CommandContext<CommandSourceStack> commandContext) {
            return new StorageDataAccessor(StorageDataAccessor.getGlobalTags(commandContext), ResourceLocationArgument.getId(commandContext, this.val$arg));
        }

        @Override
        public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function) {
            return argumentBuilder.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("storage").then(function.apply(Commands.argument(this.val$arg, ResourceLocationArgument.id()).suggests(SUGGEST_STORAGE))));
        }
    };
    private final CommandStorage storage;
    private final ResourceLocation id;

    static CommandStorage getGlobalTags(CommandContext<CommandSourceStack> commandContext) {
        return commandContext.getSource().getServer().getCommandStorage();
    }

    StorageDataAccessor(CommandStorage commandStorage, ResourceLocation resourceLocation) {
        this.storage = commandStorage;
        this.id = resourceLocation;
    }

    @Override
    public void setData(CompoundTag compoundTag) {
        this.storage.set(this.id, compoundTag);
    }

    @Override
    public CompoundTag getData() {
        return this.storage.get(this.id);
    }

    @Override
    public Component getModifiedSuccess() {
        return Component.translatable("commands.data.storage.modified", this.id);
    }

    @Override
    public Component getPrintSuccess(Tag tag) {
        return Component.translatable("commands.data.storage.query", this.id, NbtUtils.toPrettyComponent(tag));
    }

    @Override
    public Component getPrintSuccess(NbtPathArgument.NbtPath nbtPath, double d, int i) {
        return Component.translatable("commands.data.storage.get", nbtPath, this.id, String.format(Locale.ROOT, "%.2f", d), i);
    }
}

