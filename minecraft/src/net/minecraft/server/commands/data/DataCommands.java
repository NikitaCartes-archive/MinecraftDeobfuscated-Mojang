package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

public class DataCommands {
	private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType(new TranslatableComponent("commands.data.merge.failed"));
	private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.data.get.invalid", object)
	);
	private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.data.get.unknown", object)
	);
	private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(new TranslatableComponent("commands.data.get.multiple"));
	private static final DynamicCommandExceptionType ERROR_EXPECTED_LIST = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.data.modify.expected_list", object)
	);
	private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.data.modify.expected_object", object)
	);
	private static final DynamicCommandExceptionType ERROR_INVALID_INDEX = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.data.modify.invalid_index", object)
	);
	public static final List<Function<String, DataCommands.DataProvider>> ALL_PROVIDERS = ImmutableList.of(
		EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER
	);
	public static final List<DataCommands.DataProvider> TARGET_PROVIDERS = (List<DataCommands.DataProvider>)ALL_PROVIDERS.stream()
		.map(function -> (DataCommands.DataProvider)function.apply("target"))
		.collect(ImmutableList.toImmutableList());
	public static final List<DataCommands.DataProvider> SOURCE_PROVIDERS = (List<DataCommands.DataProvider>)ALL_PROVIDERS.stream()
		.map(function -> (DataCommands.DataProvider)function.apply("source"))
		.collect(ImmutableList.toImmutableList());

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("data")
			.requires(commandSourceStack -> commandSourceStack.hasPermission(2));

		for (DataCommands.DataProvider dataProvider : TARGET_PROVIDERS) {
			literalArgumentBuilder.then(
					dataProvider.wrap(
						Commands.literal("merge"),
						argumentBuilder -> argumentBuilder.then(
								Commands.argument("nbt", CompoundTagArgument.compoundTag())
									.executes(
										commandContext -> mergeData(
												commandContext.getSource(), dataProvider.access(commandContext), CompoundTagArgument.getCompoundTag(commandContext, "nbt")
											)
									)
							)
					)
				)
				.then(
					dataProvider.wrap(
						Commands.literal("get"),
						argumentBuilder -> argumentBuilder.executes(
									commandContext -> getData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext))
								)
								.then(
									Commands.argument("path", NbtPathArgument.nbtPath())
										.executes(commandContext -> getData(commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path")))
										.then(
											Commands.argument("scale", DoubleArgumentType.doubleArg())
												.executes(
													commandContext -> getNumeric(
															commandContext.getSource(),
															dataProvider.access(commandContext),
															NbtPathArgument.getPath(commandContext, "path"),
															DoubleArgumentType.getDouble(commandContext, "scale")
														)
												)
										)
								)
					)
				)
				.then(
					dataProvider.wrap(
						Commands.literal("remove"),
						argumentBuilder -> argumentBuilder.then(
								Commands.argument("path", NbtPathArgument.nbtPath())
									.executes(
										commandContext -> removeData(commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"))
									)
							)
					)
				)
				.then(
					decorateModification(
						(argumentBuilder, dataManipulatorDecorator) -> argumentBuilder.then(
									Commands.literal("insert")
										.then(
											Commands.argument("index", IntegerArgumentType.integer()).then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> {
												int i = IntegerArgumentType.getInteger(commandContext, "index");
												return insertAtIndex(i, compoundTag, nbtPath, list);
											}))
										)
								)
								.then(
									Commands.literal("prepend")
										.then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> insertAtIndex(0, compoundTag, nbtPath, list)))
								)
								.then(
									Commands.literal("append")
										.then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> insertAtIndex(-1, compoundTag, nbtPath, list)))
								)
								.then(
									Commands.literal("set")
										.then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> nbtPath.set(compoundTag, Iterables.getLast(list)::copy)))
								)
								.then(Commands.literal("merge").then(dataManipulatorDecorator.create((commandContext, compoundTag, nbtPath, list) -> {
									Collection<Tag> collection = nbtPath.getOrCreate(compoundTag, CompoundTag::new);
									int i = 0;

									for (Tag tag : collection) {
										if (!(tag instanceof CompoundTag compoundTag2)) {
											throw ERROR_EXPECTED_OBJECT.create(tag);
										}

										CompoundTag compoundTag3 = compoundTag2.copy();

										for (Tag tag2 : list) {
											if (!(tag2 instanceof CompoundTag)) {
												throw ERROR_EXPECTED_OBJECT.create(tag2);
											}

											compoundTag2.merge((CompoundTag)tag2);
										}

										i += compoundTag3.equals(compoundTag2) ? 0 : 1;
									}

									return i;
								})))
					)
				);
		}

		commandDispatcher.register(literalArgumentBuilder);
	}

	private static int insertAtIndex(int i, CompoundTag compoundTag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
		Collection<Tag> collection = nbtPath.getOrCreate(compoundTag, ListTag::new);
		int j = 0;

		for (Tag tag : collection) {
			if (!(tag instanceof CollectionTag)) {
				throw ERROR_EXPECTED_LIST.create(tag);
			}

			boolean bl = false;
			CollectionTag<?> collectionTag = (CollectionTag<?>)tag;
			int k = i < 0 ? collectionTag.size() + i + 1 : i;

			for (Tag tag2 : list) {
				try {
					if (collectionTag.addTag(k, tag2.copy())) {
						k++;
						bl = true;
					}
				} catch (IndexOutOfBoundsException var14) {
					throw ERROR_INVALID_INDEX.create(k);
				}
			}

			j += bl ? 1 : 0;
		}

		return j;
	}

	private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(
		BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataCommands.DataManipulatorDecorator> biConsumer
	) {
		LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("modify");

		for (DataCommands.DataProvider dataProvider : TARGET_PROVIDERS) {
			dataProvider.wrap(
				literalArgumentBuilder,
				argumentBuilder -> {
					ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2 = Commands.argument("targetPath", NbtPathArgument.nbtPath());

					for (DataCommands.DataProvider dataProvider2 : SOURCE_PROVIDERS) {
						biConsumer.accept(
							argumentBuilder2,
							(DataCommands.DataManipulatorDecorator)dataManipulator -> dataProvider2.wrap(
									Commands.literal("from"), argumentBuilderx -> argumentBuilderx.executes(commandContext -> {
											List<Tag> list = Collections.singletonList(dataProvider2.access(commandContext).getData());
											return manipulateData(commandContext, dataProvider, dataManipulator, list);
										}).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes(commandContext -> {
											DataAccessor dataAccessor = dataProvider2.access(commandContext);
											NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(commandContext, "sourcePath");
											List<Tag> list = nbtPath.get(dataAccessor.getData());
											return manipulateData(commandContext, dataProvider, dataManipulator, list);
										}))
								)
						);
					}

					biConsumer.accept(
						argumentBuilder2,
						(DataCommands.DataManipulatorDecorator)dataManipulator -> Commands.literal("value")
								.then(Commands.argument("value", NbtTagArgument.nbtTag()).executes(commandContext -> {
									List<Tag> list = Collections.singletonList(NbtTagArgument.getNbtTag(commandContext, "value"));
									return manipulateData(commandContext, dataProvider, dataManipulator, list);
								}))
					);
					return argumentBuilder.then(argumentBuilder2);
				}
			);
		}

		return literalArgumentBuilder;
	}

	private static int manipulateData(
		CommandContext<CommandSourceStack> commandContext, DataCommands.DataProvider dataProvider, DataCommands.DataManipulator dataManipulator, List<Tag> list
	) throws CommandSyntaxException {
		DataAccessor dataAccessor = dataProvider.access(commandContext);
		NbtPathArgument.NbtPath nbtPath = NbtPathArgument.getPath(commandContext, "targetPath");
		CompoundTag compoundTag = dataAccessor.getData();
		int i = dataManipulator.modify(commandContext, compoundTag, nbtPath, list);
		if (i == 0) {
			throw ERROR_MERGE_UNCHANGED.create();
		} else {
			dataAccessor.setData(compoundTag);
			commandContext.getSource().sendSuccess(dataAccessor.getModifiedSuccess(), true);
			return i;
		}
	}

	private static int removeData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath) throws CommandSyntaxException {
		CompoundTag compoundTag = dataAccessor.getData();
		int i = nbtPath.remove(compoundTag);
		if (i == 0) {
			throw ERROR_MERGE_UNCHANGED.create();
		} else {
			dataAccessor.setData(compoundTag);
			commandSourceStack.sendSuccess(dataAccessor.getModifiedSuccess(), true);
			return i;
		}
	}

	private static Tag getSingleTag(NbtPathArgument.NbtPath nbtPath, DataAccessor dataAccessor) throws CommandSyntaxException {
		Collection<Tag> collection = nbtPath.get(dataAccessor.getData());
		Iterator<Tag> iterator = collection.iterator();
		Tag tag = (Tag)iterator.next();
		if (iterator.hasNext()) {
			throw ERROR_MULTIPLE_TAGS.create();
		} else {
			return tag;
		}
	}

	private static int getData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath) throws CommandSyntaxException {
		Tag tag = getSingleTag(nbtPath, dataAccessor);
		int i;
		if (tag instanceof NumericTag) {
			i = Mth.floor(((NumericTag)tag).getAsDouble());
		} else if (tag instanceof CollectionTag) {
			i = ((CollectionTag)tag).size();
		} else if (tag instanceof CompoundTag) {
			i = ((CompoundTag)tag).size();
		} else {
			if (!(tag instanceof StringTag)) {
				throw ERROR_GET_NON_EXISTENT.create(nbtPath.toString());
			}

			i = tag.getAsString().length();
		}

		commandSourceStack.sendSuccess(dataAccessor.getPrintSuccess(tag), false);
		return i;
	}

	private static int getNumeric(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath, double d) throws CommandSyntaxException {
		Tag tag = getSingleTag(nbtPath, dataAccessor);
		if (!(tag instanceof NumericTag)) {
			throw ERROR_GET_NOT_NUMBER.create(nbtPath.toString());
		} else {
			int i = Mth.floor(((NumericTag)tag).getAsDouble() * d);
			commandSourceStack.sendSuccess(dataAccessor.getPrintSuccess(nbtPath, d, i), false);
			return i;
		}
	}

	private static int getData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor) throws CommandSyntaxException {
		commandSourceStack.sendSuccess(dataAccessor.getPrintSuccess(dataAccessor.getData()), false);
		return 1;
	}

	private static int mergeData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, CompoundTag compoundTag) throws CommandSyntaxException {
		CompoundTag compoundTag2 = dataAccessor.getData();
		CompoundTag compoundTag3 = compoundTag2.copy().merge(compoundTag);
		if (compoundTag2.equals(compoundTag3)) {
			throw ERROR_MERGE_UNCHANGED.create();
		} else {
			dataAccessor.setData(compoundTag3);
			commandSourceStack.sendSuccess(dataAccessor.getModifiedSuccess(), true);
			return 1;
		}
	}

	interface DataManipulator {
		int modify(CommandContext<CommandSourceStack> commandContext, CompoundTag compoundTag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException;
	}

	interface DataManipulatorDecorator {
		ArgumentBuilder<CommandSourceStack, ?> create(DataCommands.DataManipulator dataManipulator);
	}

	public interface DataProvider {
		DataAccessor access(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;

		ArgumentBuilder<CommandSourceStack, ?> wrap(
			ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function
		);
	}
}
