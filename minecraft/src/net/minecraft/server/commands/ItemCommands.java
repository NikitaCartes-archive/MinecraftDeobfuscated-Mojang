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
import java.util.Map.Entry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
	static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
		(object, object2, object3) -> new TranslatableComponent("commands.item.target.not_a_container", object, object2, object3)
	);
	private static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
		(object, object2, object3) -> new TranslatableComponent("commands.item.source.not_a_container", object, object2, object3)
	);
	static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.item.target.no_such_slot", object)
	);
	private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.item.source.no_such_slot", object)
	);
	private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.item.target.no_changes", object)
	);
	private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("commands.item.target.no_changed.known_item", object, object2)
	);
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODIFIER = (commandContext, suggestionsBuilder) -> {
		ItemModifierManager itemModifierManager = commandContext.getSource().getServer().getItemModifierManager();
		return SharedSuggestionProvider.suggestResource(itemModifierManager.getKeys(), suggestionsBuilder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
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
															Commands.argument("item", ItemArgument.item())
																.executes(
																	commandContext -> setBlockItem(
																			commandContext.getSource(),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																			SlotArgument.getSlot(commandContext, "slot"),
																			ItemArgument.getItem(commandContext, "item").createItemStack(1, false)
																		)
																)
																.then(
																	Commands.argument("count", IntegerArgumentType.integer(1, 64))
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
																					Commands.argument("modifier", ResourceLocationArgument.id())
																						.suggests(SUGGEST_MODIFIER)
																						.executes(
																							commandContext -> blockToBlock(
																									commandContext.getSource(),
																									BlockPosArgument.getLoadedBlockPos(commandContext, "source"),
																									SlotArgument.getSlot(commandContext, "sourceSlot"),
																									BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																									SlotArgument.getSlot(commandContext, "slot"),
																									ResourceLocationArgument.getItemModifier(commandContext, "modifier")
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
																					Commands.argument("modifier", ResourceLocationArgument.id())
																						.suggests(SUGGEST_MODIFIER)
																						.executes(
																							commandContext -> entityToBlock(
																									commandContext.getSource(),
																									EntityArgument.getEntity(commandContext, "source"),
																									SlotArgument.getSlot(commandContext, "sourceSlot"),
																									BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																									SlotArgument.getSlot(commandContext, "slot"),
																									ResourceLocationArgument.getItemModifier(commandContext, "modifier")
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
															Commands.argument("item", ItemArgument.item())
																.executes(
																	commandContext -> setEntityItem(
																			commandContext.getSource(),
																			EntityArgument.getEntities(commandContext, "targets"),
																			SlotArgument.getSlot(commandContext, "slot"),
																			ItemArgument.getItem(commandContext, "item").createItemStack(1, false)
																		)
																)
																.then(
																	Commands.argument("count", IntegerArgumentType.integer(1, 64))
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
																					Commands.argument("modifier", ResourceLocationArgument.id())
																						.suggests(SUGGEST_MODIFIER)
																						.executes(
																							commandContext -> blockToEntities(
																									commandContext.getSource(),
																									BlockPosArgument.getLoadedBlockPos(commandContext, "source"),
																									SlotArgument.getSlot(commandContext, "sourceSlot"),
																									EntityArgument.getEntities(commandContext, "targets"),
																									SlotArgument.getSlot(commandContext, "slot"),
																									ResourceLocationArgument.getItemModifier(commandContext, "modifier")
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
																					Commands.argument("modifier", ResourceLocationArgument.id())
																						.suggests(SUGGEST_MODIFIER)
																						.executes(
																							commandContext -> entityToEntities(
																									commandContext.getSource(),
																									EntityArgument.getEntity(commandContext, "source"),
																									SlotArgument.getSlot(commandContext, "sourceSlot"),
																									EntityArgument.getEntities(commandContext, "targets"),
																									SlotArgument.getSlot(commandContext, "slot"),
																									ResourceLocationArgument.getItemModifier(commandContext, "modifier")
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
													Commands.argument("modifier", ResourceLocationArgument.id())
														.suggests(SUGGEST_MODIFIER)
														.executes(
															commandContext -> modifyBlockItem(
																	commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																	SlotArgument.getSlot(commandContext, "slot"),
																	ResourceLocationArgument.getItemModifier(commandContext, "modifier")
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
													Commands.argument("modifier", ResourceLocationArgument.id())
														.suggests(SUGGEST_MODIFIER)
														.executes(
															commandContext -> modifyEntityItem(
																	commandContext.getSource(),
																	EntityArgument.getEntities(commandContext, "targets"),
																	SlotArgument.getSlot(commandContext, "slot"),
																	ResourceLocationArgument.getItemModifier(commandContext, "modifier")
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static int modifyBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, LootItemFunction lootItemFunction) throws CommandSyntaxException {
		Container container = getContainer(commandSourceStack, blockPos, ERROR_TARGET_NOT_A_CONTAINER);
		if (i >= 0 && i < container.getContainerSize()) {
			ItemStack itemStack = applyModifier(commandSourceStack, lootItemFunction, container.getItem(i));
			container.setItem(i, itemStack);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.item.block.set.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.getDisplayName()), true
			);
			return 1;
		} else {
			throw ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
		}
	}

	private static int modifyEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int i, LootItemFunction lootItemFunction) throws CommandSyntaxException {
		Map<Entity, ItemStack> map = Maps.<Entity, ItemStack>newHashMapWithExpectedSize(collection.size());

		for (Entity entity : collection) {
			SlotAccess slotAccess = entity.getSlot(i);
			if (slotAccess != SlotAccess.NULL) {
				ItemStack itemStack = applyModifier(commandSourceStack, lootItemFunction, slotAccess.get().copy());
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
					new TranslatableComponent(
						"commands.item.entity.set.success.single", ((Entity)entry.getKey()).getDisplayName(), ((ItemStack)entry.getValue()).getDisplayName()
					),
					true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.multiple", map.size()), true);
			}

			return map.size();
		}
	}

	private static int setBlockItem(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, ItemStack itemStack) throws CommandSyntaxException {
		Container container = getContainer(commandSourceStack, blockPos, ERROR_TARGET_NOT_A_CONTAINER);
		if (i >= 0 && i < container.getContainerSize()) {
			container.setItem(i, itemStack);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.item.block.set.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.getDisplayName()), true
			);
			return 1;
		} else {
			throw ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
		}
	}

	private static Container getContainer(CommandSourceStack commandSourceStack, BlockPos blockPos, Dynamic3CommandExceptionType dynamic3CommandExceptionType) throws CommandSyntaxException {
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
					new TranslatableComponent("commands.item.entity.set.success.single", ((Entity)list.iterator().next()).getDisplayName(), itemStack.getDisplayName()), true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.item.entity.set.success.multiple", list.size(), itemStack.getDisplayName()), true);
			}

			return list.size();
		}
	}

	private static int blockToEntities(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
		return setEntityItem(commandSourceStack, collection, j, getBlockItem(commandSourceStack, blockPos, i));
	}

	private static int blockToEntities(
		CommandSourceStack commandSourceStack, BlockPos blockPos, int i, Collection<? extends Entity> collection, int j, LootItemFunction lootItemFunction
	) throws CommandSyntaxException {
		return setEntityItem(commandSourceStack, collection, j, applyModifier(commandSourceStack, lootItemFunction, getBlockItem(commandSourceStack, blockPos, i)));
	}

	private static int blockToBlock(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, BlockPos blockPos2, int j) throws CommandSyntaxException {
		return setBlockItem(commandSourceStack, blockPos2, j, getBlockItem(commandSourceStack, blockPos, i));
	}

	private static int blockToBlock(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, BlockPos blockPos2, int j, LootItemFunction lootItemFunction) throws CommandSyntaxException {
		return setBlockItem(commandSourceStack, blockPos2, j, applyModifier(commandSourceStack, lootItemFunction, getBlockItem(commandSourceStack, blockPos, i)));
	}

	private static int entityToBlock(CommandSourceStack commandSourceStack, Entity entity, int i, BlockPos blockPos, int j) throws CommandSyntaxException {
		return setBlockItem(commandSourceStack, blockPos, j, getEntityItem(entity, i));
	}

	private static int entityToBlock(CommandSourceStack commandSourceStack, Entity entity, int i, BlockPos blockPos, int j, LootItemFunction lootItemFunction) throws CommandSyntaxException {
		return setBlockItem(commandSourceStack, blockPos, j, applyModifier(commandSourceStack, lootItemFunction, getEntityItem(entity, i)));
	}

	private static int entityToEntities(CommandSourceStack commandSourceStack, Entity entity, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
		return setEntityItem(commandSourceStack, collection, j, getEntityItem(entity, i));
	}

	private static int entityToEntities(
		CommandSourceStack commandSourceStack, Entity entity, int i, Collection<? extends Entity> collection, int j, LootItemFunction lootItemFunction
	) throws CommandSyntaxException {
		return setEntityItem(commandSourceStack, collection, j, applyModifier(commandSourceStack, lootItemFunction, getEntityItem(entity, i)));
	}

	private static ItemStack applyModifier(CommandSourceStack commandSourceStack, LootItemFunction lootItemFunction, ItemStack itemStack) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		LootContext.Builder builder = new LootContext.Builder(serverLevel)
			.withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition())
			.withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity());
		return (ItemStack)lootItemFunction.apply(itemStack, builder.create(LootContextParamSets.COMMAND));
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
