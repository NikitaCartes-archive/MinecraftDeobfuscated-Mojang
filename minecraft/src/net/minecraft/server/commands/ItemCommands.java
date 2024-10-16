package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
	static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
		(object, object2, object3) -> Component.translatableEscape("commands.item.target.not_a_container", object, object2, object3)
	);
	static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
		(object, object2, object3) -> Component.translatableEscape("commands.item.source.not_a_container", object, object2, object3)
	);
	static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.item.target.no_such_slot", object)
	);
	private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.item.source.no_such_slot", object)
	);
	private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.item.target.no_changes", object)
	);
	private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.item.target.no_changed.known_item", object, object2)
	);
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODIFIER = (commandContext, suggestionsBuilder) -> {
		ReloadableServerRegistries.Holder holder = commandContext.getSource().getServer().reloadableRegistries();
		return SharedSuggestionProvider.suggestResource(holder.getKeys(Registries.ITEM_MODIFIER), suggestionsBuilder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("item")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("replace")
						.then(
							Commands.literal("block")
								.then(
									Commands.argument("pos", BlockPosArgument.blockPos())
										.then(
											Commands.argument("slot", SlotArgument.slot())
												.then(
													Commands.literal("with")
														.then(
															Commands.argument("item", ItemArgument.item(commandBuildContext))
																.executes(
																	commandContext -> setBlockItem(
																			commandContext.getSource(),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																			SlotArgument.getSlot(commandContext, "slot"),
																			ItemArgument.getItem(commandContext, "item").createItemStack(1, false)
																		)
																)
																.then(
																	Commands.argument("count", IntegerArgumentType.integer(1, 99))
																		.executes(
																			commandContext -> setBlockItem(
																					commandContext.getSource(),
																					BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																					SlotArgument.getSlot(commandContext, "slot"),
																					ItemArgument.getItem(commandContext, "item").createItemStack(IntegerArgumentType.getInteger(commandContext, "count"), true)
																				)
																		)
																)
														)
												)
												.then(
													Commands.literal("from")
														.then(
															Commands.literal("block")
																.then(
																	Commands.argument("source", BlockPosArgument.blockPos())
																		.then(
																			Commands.argument("sourceSlot", SlotArgument.slot())
																				.executes(
																					commandContext -> blockToBlock(
																							commandContext.getSource(),
																							BlockPosArgument.getLoadedBlockPos(commandContext, "source"),
																							SlotArgument.getSlot(commandContext, "sourceSlot"),
																							BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																							SlotArgument.getSlot(commandContext, "slot")
																						)
																				)
																				.then(
																					Commands.argument("modifier", ResourceOrIdArgument.lootModifier(commandBuildContext))
																						.suggests(SUGGEST_MODIFIER)
																						.executes(
																							commandContext -> blockToBlock(
																									(CommandSourceStack)commandContext.getSource(),
																									BlockPosArgument.getLoadedBlockPos(commandContext, "source"),
																									SlotArgument.getSlot(commandContext, "sourceSlot"),
																									BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																									SlotArgument.getSlot(commandContext, "slot"),
																									ResourceOrIdArgument.getLootModifier(commandContext, "modifier")
																								)
																						)
																				)
																		)
																)
														)
														.then(
															Commands.literal("entity")
																.then(
																	Commands.argument("source", EntityArgument.entity())
																		.then(
																			Commands.argument("sourceSlot", SlotArgument.slot())
																				.executes(
																					commandContext -> entityToBlock(
																							commandContext.getSource(),
																							EntityArgument.getEntity(commandContext, "source"),
																							SlotArgument.getSlot(commandContext, "sourceSlot"),
																							BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																							SlotArgument.getSlot(commandContext, "slot")
																						)
																				)
																				.then(
																					Commands.argument("modifier", ResourceOrIdArgument.lootModifier(commandBuildContext))
																						.suggests(SUGGEST_MODIFIER)
																						.executes(
																							commandContext -> entityToBlock(
																									(CommandSourceStack)commandContext.getSource(),
																									EntityArgument.getEntity(commandContext, "source"),
																									SlotArgument.getSlot(commandContext, "sourceSlot"),
																									BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																									SlotArgument.getSlot(commandContext, "slot"),
																									ResourceOrIdArgument.getLootModifier(commandContext, "modifier")
																								)
																						)
																				)
																		)
																)
														)
												)
										)
								)
						)
						.then(
							Commands.literal("entity")
								.then(
									Commands.argument("targets", EntityArgument.entities())
										.then(
											Commands.argument("slot", SlotArgument.slot())
												.then(
													Commands.literal("with")
														.then(
															Commands.argument("item", ItemArgument.item(commandBuildContext))
																.executes(
																	commandContext -> setEntityItem(
																			commandContext.getSource(),
																			EntityArgument.getEntities(commandContext, "targets"),
																			SlotArgument.getSlot(commandContext, "slot"),
																			ItemArgument.getItem(commandContext, "item").createItemStack(1, false)
																		)
																)
																.then(
																	Commands.argument("count", IntegerArgumentType.integer(1, 99))
																		.executes(
																			commandContext -> setEntityItem(
																					commandContext.getSource(),
																					EntityArgument.getEntities(commandContext, "targets"),
																					SlotArgument.getSlot(commandContext, "slot"),
																					ItemArgument.getItem(commandContext, "item").createItemStack(IntegerArgumentType.getInteger(commandContext, "count"), true)
																				)
																		)
																)
														)
												)
												.then(
													Commands.literal("from")
														.then(
															Commands.literal("block")
																.then(
																	Commands.argument("source", BlockPosArgument.blockPos())
																		.then(
																			Commands.argument("sourceSlot", SlotArgument.slot())
																				.executes(
																					commandContext -> blockToEntities(
																							commandContext.getSource(),
																							BlockPosArgument.getLoadedBlockPos(commandContext, "source"),
																							SlotArgument.getSlot(commandContext, "sourceSlot"),
																							EntityArgument.getEntities(commandContext, "targets"),
																							SlotArgument.getSlot(commandContext, "slot")
																						)
																				)
																				.then(
																					Commands.argument("modifier", ResourceOrIdArgument.lootModifier(commandBuildContext))
																						.suggests(SUGGEST_MODIFIER)
																						.executes(
																							commandContext -> blockToEntities(
																									(CommandSourceStack)commandContext.getSource(),
																									BlockPosArgument.getLoadedBlockPos(commandContext, "source"),
																									SlotArgument.getSlot(commandContext, "sourceSlot"),
																									EntityArgument.getEntities(commandContext, "targets"),
																									SlotArgument.getSlot(commandContext, "slot"),
																									ResourceOrIdArgument.getLootModifier(commandContext, "modifier")
																								)
																						)
																				)
																		)
																)
														)
														.then(
															Commands.literal("entity")
																.then(
																	Commands.argument("source", EntityArgument.entity())
																		.then(
																			Commands.argument("sourceSlot", SlotArgument.slot())
																				.executes(
																					commandContext -> entityToEntities(
																							commandContext.getSource(),
																							EntityArgument.getEntity(commandContext, "source"),
																							SlotArgument.getSlot(commandContext, "sourceSlot"),
																							EntityArgument.getEntities(commandContext, "targets"),
																							SlotArgument.getSlot(commandContext, "slot")
																						)
																				)
																				.then(
																					Commands.argument("modifier", ResourceOrIdArgument.lootModifier(commandBuildContext))
																						.suggests(SUGGEST_MODIFIER)
																						.executes(
																							commandContext -> entityToEntities(
																									(CommandSourceStack)commandContext.getSource(),
																									EntityArgument.getEntity(commandContext, "source"),
																									SlotArgument.getSlot(commandContext, "sourceSlot"),
																									EntityArgument.getEntities(commandContext, "targets"),
																									SlotArgument.getSlot(commandContext, "slot"),
																									ResourceOrIdArgument.getLootModifier(commandContext, "modifier")
																								)
																						)
																				)
																		)
																)
														)
												)
										)
								)
						)
				)
				.then(
					Commands.literal("modify")
						.then(
							Commands.literal("block")
								.then(
									Commands.argument("pos", BlockPosArgument.blockPos())
										.then(
											Commands.argument("slot", SlotArgument.slot())
												.then(
													Commands.argument("modifier", ResourceOrIdArgument.lootModifier(commandBuildContext))
														.suggests(SUGGEST_MODIFIER)
														.executes(
															commandContext -> modifyBlockItem(
																	(CommandSourceStack)commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																	SlotArgument.getSlot(commandContext, "slot"),
																	ResourceOrIdArgument.getLootModifier(commandContext, "modifier")
																)
														)
												)
										)
								)
						)
						.then(
							Commands.literal("entity")
								.then(
									Commands.argument("targets", EntityArgument.entities())
										.then(
											Commands.argument("slot", SlotArgument.slot())
												.then(
													Commands.argument("modifier", ResourceOrIdArgument.lootModifier(commandBuildContext))
														.suggests(SUGGEST_MODIFIER)
														.executes(
															commandContext -> modifyEntityItem(
																	(CommandSourceStack)commandContext.getSource(),
																	EntityArgument.getEntities(commandContext, "targets"),
																	SlotArgument.getSlot(commandContext, "slot"),
																	ResourceOrIdArgument.getLootModifier(commandContext, "modifier")
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static int modifyBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, Holder<LootItemFunction> holder) throws CommandSyntaxException {
		Container container = getContainer(commandSourceStack, blockPos, ERROR_TARGET_NOT_A_CONTAINER);
		if (i >= 0 && i < container.getContainerSize()) {
			ItemStack itemStack = applyModifier(commandSourceStack, holder, container.getItem(i));
			container.setItem(i, itemStack);
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.item.block.set.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.getDisplayName()), true
			);
			return 1;
		} else {
			throw ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
		}
	}

	private static int modifyEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int i, Holder<LootItemFunction> holder) throws CommandSyntaxException {
		Map<Entity, ItemStack> map = Maps.<Entity, ItemStack>newHashMapWithExpectedSize(collection.size());

		for (Entity entity : collection) {
			SlotAccess slotAccess = entity.getSlot(i);
			if (slotAccess != SlotAccess.NULL) {
				ItemStack itemStack = applyModifier(commandSourceStack, holder, slotAccess.get().copy());
				if (slotAccess.set(itemStack)) {
					map.put(entity, itemStack);
					if (entity instanceof ServerPlayer) {
						((ServerPlayer)entity).containerMenu.broadcastChanges();
					}
				}
			}
		}

		if (map.isEmpty()) {
			throw ERROR_TARGET_NO_CHANGES.create(i);
		} else {
			if (map.size() == 1) {
				Entry<Entity, ItemStack> entry = (Entry<Entity, ItemStack>)map.entrySet().iterator().next();
				commandSourceStack.sendSuccess(
					() -> Component.translatable(
							"commands.item.entity.set.success.single", ((Entity)entry.getKey()).getDisplayName(), ((ItemStack)entry.getValue()).getDisplayName()
						),
					true
				);
			} else {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", map.size()), true);
			}

			return map.size();
		}
	}

	private static int setBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, ItemStack itemStack) throws CommandSyntaxException {
		Container container = getContainer(commandSourceStack, blockPos, ERROR_TARGET_NOT_A_CONTAINER);
		if (i >= 0 && i < container.getContainerSize()) {
			container.setItem(i, itemStack);
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.item.block.set.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.getDisplayName()), true
			);
			return 1;
		} else {
			throw ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
		}
	}

	static Container getContainer(CommandSourceStack commandSourceStack, BlockPos blockPos, Dynamic3CommandExceptionType dynamic3CommandExceptionType) throws CommandSyntaxException {
		BlockEntity blockEntity = commandSourceStack.getLevel().getBlockEntity(blockPos);
		if (!(blockEntity instanceof Container)) {
			throw dynamic3CommandExceptionType.create(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		} else {
			return (Container)blockEntity;
		}
	}

	private static int setEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int i, ItemStack itemStack) throws CommandSyntaxException {
		List<Entity> list = Lists.<Entity>newArrayListWithCapacity(collection.size());

		for (Entity entity : collection) {
			SlotAccess slotAccess = entity.getSlot(i);
			if (slotAccess != SlotAccess.NULL && slotAccess.set(itemStack.copy())) {
				list.add(entity);
				if (entity instanceof ServerPlayer) {
					((ServerPlayer)entity).containerMenu.broadcastChanges();
				}
			}
		}

		if (list.isEmpty()) {
			throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(itemStack.getDisplayName(), i);
		} else {
			if (list.size() == 1) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.item.entity.set.success.single", ((Entity)list.iterator().next()).getDisplayName(), itemStack.getDisplayName()),
					true
				);
			} else {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", list.size(), itemStack.getDisplayName()), true);
			}

			return list.size();
		}
	}

	private static int blockToEntities(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
		return setEntityItem(commandSourceStack, collection, j, getBlockItem(commandSourceStack, blockPos, i));
	}

	private static int blockToEntities(
		CommandSourceStack commandSourceStack, BlockPos blockPos, int i, Collection<? extends Entity> collection, int j, Holder<LootItemFunction> holder
	) throws CommandSyntaxException {
		return setEntityItem(commandSourceStack, collection, j, applyModifier(commandSourceStack, holder, getBlockItem(commandSourceStack, blockPos, i)));
	}

	private static int blockToBlock(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, BlockPos blockPos2, int j) throws CommandSyntaxException {
		return setBlockItem(commandSourceStack, blockPos2, j, getBlockItem(commandSourceStack, blockPos, i));
	}

	private static int blockToBlock(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, BlockPos blockPos2, int j, Holder<LootItemFunction> holder) throws CommandSyntaxException {
		return setBlockItem(commandSourceStack, blockPos2, j, applyModifier(commandSourceStack, holder, getBlockItem(commandSourceStack, blockPos, i)));
	}

	private static int entityToBlock(CommandSourceStack commandSourceStack, Entity entity, int i, BlockPos blockPos, int j) throws CommandSyntaxException {
		return setBlockItem(commandSourceStack, blockPos, j, getEntityItem(entity, i));
	}

	private static int entityToBlock(CommandSourceStack commandSourceStack, Entity entity, int i, BlockPos blockPos, int j, Holder<LootItemFunction> holder) throws CommandSyntaxException {
		return setBlockItem(commandSourceStack, blockPos, j, applyModifier(commandSourceStack, holder, getEntityItem(entity, i)));
	}

	private static int entityToEntities(CommandSourceStack commandSourceStack, Entity entity, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
		return setEntityItem(commandSourceStack, collection, j, getEntityItem(entity, i));
	}

	private static int entityToEntities(
		CommandSourceStack commandSourceStack, Entity entity, int i, Collection<? extends Entity> collection, int j, Holder<LootItemFunction> holder
	) throws CommandSyntaxException {
		return setEntityItem(commandSourceStack, collection, j, applyModifier(commandSourceStack, holder, getEntityItem(entity, i)));
	}

	private static ItemStack applyModifier(CommandSourceStack commandSourceStack, Holder<LootItemFunction> holder, ItemStack itemStack) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		LootParams lootParams = new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition())
			.withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity())
			.create(LootContextParamSets.COMMAND);
		LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
		lootContext.pushVisitedElement(LootContext.createVisitedEntry(holder.value()));
		ItemStack itemStack2 = (ItemStack)holder.value().apply(itemStack, lootContext);
		itemStack2.limitSize(itemStack2.getMaxStackSize());
		return itemStack2;
	}

	private static ItemStack getEntityItem(Entity entity, int i) throws CommandSyntaxException {
		SlotAccess slotAccess = entity.getSlot(i);
		if (slotAccess == SlotAccess.NULL) {
			throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(i);
		} else {
			return slotAccess.get().copy();
		}
	}

	private static ItemStack getBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int i) throws CommandSyntaxException {
		Container container = getContainer(commandSourceStack, blockPos, ERROR_SOURCE_NOT_A_CONTAINER);
		if (i >= 0 && i < container.getContainerSize()) {
			return container.getItem(i).copy();
		} else {
			throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(i);
		}
	}
}
