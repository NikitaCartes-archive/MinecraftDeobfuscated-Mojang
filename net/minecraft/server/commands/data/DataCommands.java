/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.commands.data.StorageDataAccessor;
import net.minecraft.util.Mth;

public class DataCommands {
    private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.data.merge.failed"));
    private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType(object -> Component.translatable("commands.data.get.invalid", object));
    private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType(object -> Component.translatable("commands.data.get.unknown", object));
    private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(Component.translatable("commands.data.get.multiple"));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType(object -> Component.translatable("commands.data.modify.expected_object", object));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType(object -> Component.translatable("commands.data.modify.expected_value", object));
    public static final List<Function<String, DataProvider>> ALL_PROVIDERS = ImmutableList.of(EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER);
    public static final List<DataProvider> TARGET_PROVIDERS = ALL_PROVIDERS.stream().map(function -> (DataProvider)function.apply("target")).collect(ImmutableList.toImmutableList());
    public static final List<DataProvider> SOURCE_PROVIDERS = ALL_PROVIDERS.stream().map(function -> (DataProvider)function.apply("source")).collect(ImmutableList.toImmutableList());

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)Commands.literal("data").requires(commandSourceStack -> commandSourceStack.hasPermission(2));
        for (DataProvider dataProvider : TARGET_PROVIDERS) {
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(dataProvider.wrap(Commands.literal("merge"), argumentBuilder -> argumentBuilder.then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes(commandContext -> DataCommands.mergeData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), CompoundTagArgument.getCompoundTag(commandContext, "nbt"))))))).then(dataProvider.wrap(Commands.literal("get"), argumentBuilder -> ((ArgumentBuilder)argumentBuilder.executes(commandContext -> DataCommands.getData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext)))).then(((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).executes(commandContext -> DataCommands.getData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path")))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(commandContext -> DataCommands.getNumeric((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"), DoubleArgumentType.getDouble(commandContext, "scale")))))))).then(dataProvider.wrap(Commands.literal("remove"), argumentBuilder -> argumentBuilder.then(Commands.argument("path", NbtPathArgument.nbtPath()).executes(commandContext -> DataCommands.removeData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"))))))).then(DataCommands.decorateModification((argumentBuilder, dataManipulatorDecorator) -> ((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)argumentBuilder.then(Commands.literal("insert").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("index", IntegerArgumentType.integer()).then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> nbtPath.insert(IntegerArgumentType.getInteger(commandContext, "index"), compoundTag, list)))))).then(Commands.literal("prepend").then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> nbtPath.insert(0, compoundTag, list))))).then(Commands.literal("append").then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> nbtPath.insert(-1, compoundTag, list))))).then(Commands.literal("set").then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> nbtPath.set(compoundTag, (Tag)Iterables.getLast(list)))))).then(Commands.literal("merge").then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> {
                CompoundTag compoundTag2 = new CompoundTag();
                for (Tag tag : list) {
                    if (NbtPathArgument.NbtPath.isTooDeep(tag, 0)) {
                        throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                    }
                    if (tag instanceof CompoundTag) {
                        CompoundTag compoundTag3 = (CompoundTag)tag;
                        compoundTag2.merge(compoundTag3);
                        continue;
                    }
                    throw ERROR_EXPECTED_OBJECT.create(tag);
                }
                List<Tag> collection = nbtPath.getOrCreate(compoundTag, CompoundTag::new);
                int i = 0;
                for (Tag tag2 : collection) {
                    if (!(tag2 instanceof CompoundTag)) {
                        throw ERROR_EXPECTED_OBJECT.create(tag2);
                    }
                    CompoundTag compoundTag4 = (CompoundTag)tag2;
                    CompoundTag compoundTag5 = compoundTag4.copy();
                    compoundTag4.merge(compoundTag2);
                    i += compoundTag5.equals(compoundTag4) ? 0 : 1;
                }
                return i;
            })))));
        }
        commandDispatcher.register(literalArgumentBuilder);
    }

    private static String getAsText(Tag tag) throws CommandSyntaxException {
        if (tag.getType().isValue()) {
            return tag.getAsString();
        }
        throw ERROR_EXPECTED_VALUE.create(tag);
    }

    private static List<Tag> stringifyTagList(List<Tag> list, Function<String, String> function) throws CommandSyntaxException {
        ArrayList<Tag> list2 = new ArrayList<Tag>(list.size());
        for (Tag tag : list) {
            String string = DataCommands.getAsText(tag);
            list2.add(StringTag.valueOf(function.apply(string)));
        }
        return list2;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataManipulatorDecorator> biConsumer) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("modify");
        for (DataProvider dataProvider : TARGET_PROVIDERS) {
            dataProvider.wrap(literalArgumentBuilder, argumentBuilder -> {
                RequiredArgumentBuilder<CommandSourceStack, NbtPathArgument.NbtPath> argumentBuilder2 = Commands.argument("targetPath", NbtPathArgument.nbtPath());
                for (DataProvider dataProvider2 : SOURCE_PROVIDERS) {
                    biConsumer.accept(argumentBuilder2, dataManipulator -> dataProvider2.wrap(Commands.literal("from"), argumentBuilder -> ((ArgumentBuilder)argumentBuilder.executes(commandContext -> DataCommands.manipulateData(commandContext, dataProvider, dataManipulator, DataCommands.getSingletonSource(commandContext, dataProvider2)))).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes(commandContext -> DataCommands.manipulateData(commandContext, dataProvider, dataManipulator, DataCommands.resolveSourcePath(commandContext, dataProvider2))))));
                    biConsumer.accept(argumentBuilder2, dataManipulator -> dataProvider2.wrap(Commands.literal("string"), argumentBuilder -> ((ArgumentBuilder)argumentBuilder.executes(commandContext -> DataCommands.manipulateData(commandContext, dataProvider, dataManipulator, DataCommands.stringifyTagList(DataCommands.getSingletonSource(commandContext, dataProvider2), string -> string)))).then(((RequiredArgumentBuilder)Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes(commandContext -> DataCommands.manipulateData(commandContext, dataProvider, dataManipulator, DataCommands.stringifyTagList(DataCommands.resolveSourcePath(commandContext, dataProvider2), string -> string)))).then(((RequiredArgumentBuilder)Commands.argument("start", IntegerArgumentType.integer(0)).executes(commandContext -> DataCommands.manipulateData(commandContext, dataProvider, dataManipulator, DataCommands.stringifyTagList(DataCommands.resolveSourcePath(commandContext, dataProvider2), string -> string.substring(IntegerArgumentType.getInteger(commandContext, "start")))))).then(Commands.argument("end", IntegerArgumentType.integer(0)).executes(commandContext -> DataCommands.manipulateData(commandContext, dataProvider, dataManipulator, DataCommands.stringifyTagList(DataCommands.resolveSourcePath(commandContext, dataProvider2), string -> string.substring(IntegerArgumentType.getInteger(commandContext, "start"), IntegerArgumentType.getInteger(commandContext, "end"))))))))));
                }
                biConsumer.accept(argumentBuilder2, dataManipulator -> Commands.literal("value").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("value", NbtTagArgument.nbtTag()).executes(commandContext -> {
                    List<Tag> list = Collections.singletonList(NbtTagArgument.getNbtTag(commandContext, "value"));
                    return DataCommands.manipulateData(commandContext, dataProvider, dataManipulator, list);
                })));
                return argumentBuilder.then(argumentBuilder2);
            });
        }
        return literalArgumentBuilder;
    }

    private static List<Tag> getSingletonSource(CommandContext<CommandSourceStack> commandContext, DataProvider dataProvider) throws CommandSyntaxException {
        DataAccessor dataAccessor = dataProvider.access(commandContext);
        return Collections.singletonList(dataAccessor.getData());
    }

    private static List<Tag> resolveSourcePath(CommandContext<CommandSourceStack> commandContext, DataProvider dataProvider) throws CommandSyntaxException {
        DataAccessor dataAccessor = dataProvider.access(commandContext);
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(commandContext, "sourcePath");
        return nbtPath.get(dataAccessor.getData());
    }

    private static int manipulateData(CommandContext<CommandSourceStack> commandContext, DataProvider dataProvider, DataManipulator dataManipulator, List<Tag> list) throws CommandSyntaxException {
        DataAccessor dataAccessor = dataProvider.access(commandContext);
        NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(commandContext, "targetPath");
        CompoundTag compoundTag = dataAccessor.getData();
        int i = dataManipulator.modify(commandContext, compoundTag, nbtPath, list);
        if (i == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        dataAccessor.setData(compoundTag);
        commandContext.getSource().sendSuccess(dataAccessor.getModifiedSuccess(), true);
        return i;
    }

    private static int removeData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath) throws CommandSyntaxException {
        CompoundTag compoundTag = dataAccessor.getData();
        int i = nbtPath.remove(compoundTag);
        if (i == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        dataAccessor.setData(compoundTag);
        commandSourceStack.sendSuccess(dataAccessor.getModifiedSuccess(), true);
        return i;
    }

    private static Tag getSingleTag(NbtPathArgument.NbtPath nbtPath, DataAccessor dataAccessor) throws CommandSyntaxException {
        List<Tag> collection = nbtPath.get(dataAccessor.getData());
        Iterator iterator = collection.iterator();
        Tag tag = (Tag)iterator.next();
        if (iterator.hasNext()) {
            throw ERROR_MULTIPLE_TAGS.create();
        }
        return tag;
    }

    private static int getData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath) throws CommandSyntaxException {
        int i;
        Tag tag = DataCommands.getSingleTag(nbtPath, dataAccessor);
        if (tag instanceof NumericTag) {
            i = Mth.floor(((NumericTag)tag).getAsDouble());
        } else if (tag instanceof CollectionTag) {
            i = ((CollectionTag)tag).size();
        } else if (tag instanceof CompoundTag) {
            i = ((CompoundTag)tag).size();
        } else if (tag instanceof StringTag) {
            i = tag.getAsString().length();
        } else {
            throw ERROR_GET_NON_EXISTENT.create(nbtPath.toString());
        }
        commandSourceStack.sendSuccess(dataAccessor.getPrintSuccess(tag), false);
        return i;
    }

    private static int getNumeric(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath, double d) throws CommandSyntaxException {
        Tag tag = DataCommands.getSingleTag(nbtPath, dataAccessor);
        if (!(tag instanceof NumericTag)) {
            throw ERROR_GET_NOT_NUMBER.create(nbtPath.toString());
        }
        int i = Mth.floor(((NumericTag)tag).getAsDouble() * d);
        commandSourceStack.sendSuccess(dataAccessor.getPrintSuccess(nbtPath, d, i), false);
        return i;
    }

    private static int getData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor) throws CommandSyntaxException {
        commandSourceStack.sendSuccess(dataAccessor.getPrintSuccess(dataAccessor.getData()), false);
        return 1;
    }

    private static int mergeData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, CompoundTag compoundTag) throws CommandSyntaxException {
        CompoundTag compoundTag2 = dataAccessor.getData();
        if (NbtPathArgument.NbtPath.isTooDeep(compoundTag, 0)) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
        }
        CompoundTag compoundTag3 = compoundTag2.copy().merge(compoundTag);
        if (compoundTag2.equals(compoundTag3)) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        dataAccessor.setData(compoundTag3);
        commandSourceStack.sendSuccess(dataAccessor.getModifiedSuccess(), true);
        return 1;
    }

    public static interface DataProvider {
        public DataAccessor access(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> var1, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> var2);
    }

    static interface DataManipulator {
        public int modify(CommandContext<CommandSourceStack> var1, CompoundTag var2, NbtPathArgument.NbtPath var3, List<Tag> var4) throws CommandSyntaxException;
    }

    static interface DataManipulatorDecorator {
        public ArgumentBuilder<CommandSourceStack, ?> create(DataManipulator var1);
    }
}

