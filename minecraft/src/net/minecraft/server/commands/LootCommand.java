package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LootCommand {
	public static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOT_TABLE = (commandContext, suggestionsBuilder) -> {
		LootTables lootTables = commandContext.getSource().getServer().getLootTables();
		return SharedSuggestionProvider.suggestResource(lootTables.getIds(), suggestionsBuilder);
	};
	private static final DynamicCommandExceptionType ERROR_NO_HELD_ITEMS = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.drop.no_held_items", object)
	);
	private static final DynamicCommandExceptionType ERROR_NO_LOOT_TABLE = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.drop.no_loot_table", object)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			addTargets(
				Commands.literal("loot").requires(commandSourceStack -> commandSourceStack.hasPermission(2)),
				(argumentBuilder, dropConsumer) -> argumentBuilder.then(
							Commands.literal("fish")
								.then(
									Commands.argument("loot_table", ResourceLocationArgument.id())
										.suggests(SUGGEST_LOOT_TABLE)
										.then(
											Commands.argument("pos", BlockPosArgument.blockPos())
												.executes(
													commandContext -> dropFishingLoot(
															commandContext,
															ResourceLocationArgument.getId(commandContext, "loot_table"),
															BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
															ItemStack.EMPTY,
															dropConsumer
														)
												)
												.then(
													Commands.argument("tool", ItemArgument.item())
														.executes(
															commandContext -> dropFishingLoot(
																	commandContext,
																	ResourceLocationArgument.getId(commandContext, "loot_table"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																	ItemArgument.getItem(commandContext, "tool").createItemStack(1, false),
																	dropConsumer
																)
														)
												)
												.then(
													Commands.literal("mainhand")
														.executes(
															commandContext -> dropFishingLoot(
																	commandContext,
																	ResourceLocationArgument.getId(commandContext, "loot_table"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																	getSourceHandItem(commandContext.getSource(), EquipmentSlot.MAINHAND),
																	dropConsumer
																)
														)
												)
												.then(
													Commands.literal("offhand")
														.executes(
															commandContext -> dropFishingLoot(
																	commandContext,
																	ResourceLocationArgument.getId(commandContext, "loot_table"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																	getSourceHandItem(commandContext.getSource(), EquipmentSlot.OFFHAND),
																	dropConsumer
																)
														)
												)
										)
								)
						)
						.then(
							Commands.literal("loot")
								.then(
									Commands.argument("loot_table", ResourceLocationArgument.id())
										.suggests(SUGGEST_LOOT_TABLE)
										.executes(commandContext -> dropChestLoot(commandContext, ResourceLocationArgument.getId(commandContext, "loot_table"), dropConsumer))
								)
						)
						.then(
							Commands.literal("kill")
								.then(
									Commands.argument("target", EntityArgument.entity())
										.executes(commandContext -> dropKillLoot(commandContext, EntityArgument.getEntity(commandContext, "target"), dropConsumer))
								)
						)
						.then(
							Commands.literal("mine")
								.then(
									Commands.argument("pos", BlockPosArgument.blockPos())
										.executes(commandContext -> dropBlockLoot(commandContext, BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), ItemStack.EMPTY, dropConsumer))
										.then(
											Commands.argument("tool", ItemArgument.item())
												.executes(
													commandContext -> dropBlockLoot(
															commandContext,
															BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
															ItemArgument.getItem(commandContext, "tool").createItemStack(1, false),
															dropConsumer
														)
												)
										)
										.then(
											Commands.literal("mainhand")
												.executes(
													commandContext -> dropBlockLoot(
															commandContext,
															BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
															getSourceHandItem(commandContext.getSource(), EquipmentSlot.MAINHAND),
															dropConsumer
														)
												)
										)
										.then(
											Commands.literal("offhand")
												.executes(
													commandContext -> dropBlockLoot(
															commandContext,
															BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
															getSourceHandItem(commandContext.getSource(), EquipmentSlot.OFFHAND),
															dropConsumer
														)
												)
										)
								)
						)
			)
		);
	}

	private static <T extends ArgumentBuilder<CommandSourceStack, T>> T addTargets(T argumentBuilder, LootCommand.TailProvider tailProvider) {
		return argumentBuilder.then(
				Commands.literal("replace")
					.then(
						Commands.literal("entity")
							.then(
								Commands.argument("entities", EntityArgument.entities())
									.then(
										tailProvider.construct(
												Commands.argument("slot", SlotArgument.slot()),
												(commandContext, list, callback) -> entityReplace(
														EntityArgument.getEntities(commandContext, "entities"), SlotArgument.getSlot(commandContext, "slot"), list.size(), list, callback
													)
											)
											.then(
												tailProvider.construct(
													Commands.argument("count", IntegerArgumentType.integer(0)),
													(commandContext, list, callback) -> entityReplace(
															EntityArgument.getEntities(commandContext, "entities"),
															SlotArgument.getSlot(commandContext, "slot"),
															IntegerArgumentType.getInteger(commandContext, "count"),
															list,
															callback
														)
												)
											)
									)
							)
					)
					.then(
						Commands.literal("block")
							.then(
								Commands.argument("targetPos", BlockPosArgument.blockPos())
									.then(
										tailProvider.construct(
												Commands.argument("slot", SlotArgument.slot()),
												(commandContext, list, callback) -> blockReplace(
														commandContext.getSource(),
														BlockPosArgument.getLoadedBlockPos(commandContext, "targetPos"),
														SlotArgument.getSlot(commandContext, "slot"),
														list.size(),
														list,
														callback
													)
											)
											.then(
												tailProvider.construct(
													Commands.argument("count", IntegerArgumentType.integer(0)),
													(commandContext, list, callback) -> blockReplace(
															commandContext.getSource(),
															BlockPosArgument.getLoadedBlockPos(commandContext, "targetPos"),
															IntegerArgumentType.getInteger(commandContext, "slot"),
															IntegerArgumentType.getInteger(commandContext, "count"),
															list,
															callback
														)
												)
											)
									)
							)
					)
			)
			.then(
				Commands.literal("insert")
					.then(
						tailProvider.construct(
							Commands.argument("targetPos", BlockPosArgument.blockPos()),
							(commandContext, list, callback) -> blockDistribute(
									commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "targetPos"), list, callback
								)
						)
					)
			)
			.then(
				Commands.literal("give")
					.then(
						tailProvider.construct(
							Commands.argument("players", EntityArgument.players()),
							(commandContext, list, callback) -> playerGive(EntityArgument.getPlayers(commandContext, "players"), list, callback)
						)
					)
			)
			.then(
				Commands.literal("spawn")
					.then(
						tailProvider.construct(
							Commands.argument("targetPos", Vec3Argument.vec3()),
							(commandContext, list, callback) -> dropInWorld(commandContext.getSource(), Vec3Argument.getVec3(commandContext, "targetPos"), list, callback)
						)
					)
			);
	}

	private static Container getContainer(CommandSourceStack commandSourceStack, BlockPos blockPos) throws CommandSyntaxException {
		BlockEntity blockEntity = commandSourceStack.getLevel().getBlockEntity(blockPos);
		if (!(blockEntity instanceof Container)) {
			throw ReplaceItemCommand.ERROR_NOT_A_CONTAINER.create();
		} else {
			return (Container)blockEntity;
		}
	}

	private static int blockDistribute(CommandSourceStack commandSourceStack, BlockPos blockPos, List<ItemStack> list, LootCommand.Callback callback) throws CommandSyntaxException {
		Container container = getContainer(commandSourceStack, blockPos);
		List<ItemStack> list2 = Lists.<ItemStack>newArrayListWithCapacity(list.size());

		for (ItemStack itemStack : list) {
			if (distributeToContainer(container, itemStack.copy())) {
				container.setChanged();
				list2.add(itemStack);
			}
		}

		callback.accept(list2);
		return list2.size();
	}

	private static boolean distributeToContainer(Container container, ItemStack itemStack) {
		boolean bl = false;

		for (int i = 0; i < container.getContainerSize() && !itemStack.isEmpty(); i++) {
			ItemStack itemStack2 = container.getItem(i);
			if (container.canPlaceItem(i, itemStack)) {
				if (itemStack2.isEmpty()) {
					container.setItem(i, itemStack);
					bl = true;
					break;
				}

				if (canMergeItems(itemStack2, itemStack)) {
					int j = itemStack.getMaxStackSize() - itemStack2.getCount();
					int k = Math.min(itemStack.getCount(), j);
					itemStack.shrink(k);
					itemStack2.grow(k);
					bl = true;
				}
			}
		}

		return bl;
	}

	private static int blockReplace(CommandSourceStack commandSourceStack, BlockPos blockPos, int i, int j, List<ItemStack> list, LootCommand.Callback callback) throws CommandSyntaxException {
		Container container = getContainer(commandSourceStack, blockPos);
		int k = container.getContainerSize();
		if (i >= 0 && i < k) {
			List<ItemStack> list2 = Lists.<ItemStack>newArrayListWithCapacity(list.size());

			for (int l = 0; l < j; l++) {
				int m = i + l;
				ItemStack itemStack = l < list.size() ? (ItemStack)list.get(l) : ItemStack.EMPTY;
				if (container.canPlaceItem(m, itemStack)) {
					container.setItem(m, itemStack);
					list2.add(itemStack);
				}
			}

			callback.accept(list2);
			return list2.size();
		} else {
			throw ReplaceItemCommand.ERROR_INAPPLICABLE_SLOT.create(i);
		}
	}

	private static boolean canMergeItems(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack.getItem() == itemStack2.getItem()
			&& itemStack.getDamageValue() == itemStack2.getDamageValue()
			&& itemStack.getCount() <= itemStack.getMaxStackSize()
			&& Objects.equals(itemStack.getTag(), itemStack2.getTag());
	}

	private static int playerGive(Collection<ServerPlayer> collection, List<ItemStack> list, LootCommand.Callback callback) throws CommandSyntaxException {
		List<ItemStack> list2 = Lists.<ItemStack>newArrayListWithCapacity(list.size());

		for (ItemStack itemStack : list) {
			for (ServerPlayer serverPlayer : collection) {
				if (serverPlayer.inventory.add(itemStack.copy())) {
					list2.add(itemStack);
				}
			}
		}

		callback.accept(list2);
		return list2.size();
	}

	private static void setSlots(Entity entity, List<ItemStack> list, int i, int j, List<ItemStack> list2) {
		for (int k = 0; k < j; k++) {
			ItemStack itemStack = k < list.size() ? (ItemStack)list.get(k) : ItemStack.EMPTY;
			if (entity.setSlot(i + k, itemStack.copy())) {
				list2.add(itemStack);
			}
		}
	}

	private static int entityReplace(Collection<? extends Entity> collection, int i, int j, List<ItemStack> list, LootCommand.Callback callback) throws CommandSyntaxException {
		List<ItemStack> list2 = Lists.<ItemStack>newArrayListWithCapacity(list.size());

		for (Entity entity : collection) {
			if (entity instanceof ServerPlayer) {
				ServerPlayer serverPlayer = (ServerPlayer)entity;
				serverPlayer.inventoryMenu.broadcastChanges();
				setSlots(entity, list, i, j, list2);
				serverPlayer.inventoryMenu.broadcastChanges();
			} else {
				setSlots(entity, list, i, j, list2);
			}
		}

		callback.accept(list2);
		return list2.size();
	}

	private static int dropInWorld(CommandSourceStack commandSourceStack, Vec3 vec3, List<ItemStack> list, LootCommand.Callback callback) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		list.forEach(itemStack -> {
			ItemEntity itemEntity = new ItemEntity(serverLevel, vec3.x, vec3.y, vec3.z, itemStack.copy());
			itemEntity.setDefaultPickUpDelay();
			serverLevel.addFreshEntity(itemEntity);
		});
		callback.accept(list);
		return list.size();
	}

	private static void callback(CommandSourceStack commandSourceStack, List<ItemStack> list) {
		if (list.size() == 1) {
			ItemStack itemStack = (ItemStack)list.get(0);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.drop.success.single", itemStack.getCount(), itemStack.getDisplayName()), false);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.drop.success.multiple", list.size()), false);
		}
	}

	private static void callback(CommandSourceStack commandSourceStack, List<ItemStack> list, ResourceLocation resourceLocation) {
		if (list.size() == 1) {
			ItemStack itemStack = (ItemStack)list.get(0);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.drop.success.single_with_table", itemStack.getCount(), itemStack.getDisplayName(), resourceLocation), false
			);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.drop.success.multiple_with_table", list.size(), resourceLocation), false);
		}
	}

	private static ItemStack getSourceHandItem(CommandSourceStack commandSourceStack, EquipmentSlot equipmentSlot) throws CommandSyntaxException {
		Entity entity = commandSourceStack.getEntityOrException();
		if (entity instanceof LivingEntity) {
			return ((LivingEntity)entity).getItemBySlot(equipmentSlot);
		} else {
			throw ERROR_NO_HELD_ITEMS.create(entity.getDisplayName());
		}
	}

	private static int dropBlockLoot(
		CommandContext<CommandSourceStack> commandContext, BlockPos blockPos, ItemStack itemStack, LootCommand.DropConsumer dropConsumer
	) throws CommandSyntaxException {
		CommandSourceStack commandSourceStack = commandContext.getSource();
		ServerLevel serverLevel = commandSourceStack.getLevel();
		BlockState blockState = serverLevel.getBlockState(blockPos);
		BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
		LootContext.Builder builder = new LootContext.Builder(serverLevel)
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
			.withParameter(LootContextParams.BLOCK_STATE, blockState)
			.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
			.withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity())
			.withParameter(LootContextParams.TOOL, itemStack);
		List<ItemStack> list = blockState.getDrops(builder);
		return dropConsumer.accept(commandContext, list, listx -> callback(commandSourceStack, listx, blockState.getBlock().getLootTable()));
	}

	private static int dropKillLoot(CommandContext<CommandSourceStack> commandContext, Entity entity, LootCommand.DropConsumer dropConsumer) throws CommandSyntaxException {
		if (!(entity instanceof LivingEntity)) {
			throw ERROR_NO_LOOT_TABLE.create(entity.getDisplayName());
		} else {
			ResourceLocation resourceLocation = ((LivingEntity)entity).getLootTable();
			CommandSourceStack commandSourceStack = commandContext.getSource();
			LootContext.Builder builder = new LootContext.Builder(commandSourceStack.getLevel());
			Entity entity2 = commandSourceStack.getEntity();
			if (entity2 instanceof Player) {
				builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, (Player)entity2);
			}

			builder.withParameter(LootContextParams.DAMAGE_SOURCE, DamageSource.MAGIC);
			builder.withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, entity2);
			builder.withOptionalParameter(LootContextParams.KILLER_ENTITY, entity2);
			builder.withParameter(LootContextParams.THIS_ENTITY, entity);
			builder.withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition());
			LootTable lootTable = commandSourceStack.getServer().getLootTables().get(resourceLocation);
			List<ItemStack> list = lootTable.getRandomItems(builder.create(LootContextParamSets.ENTITY));
			return dropConsumer.accept(commandContext, list, listx -> callback(commandSourceStack, listx, resourceLocation));
		}
	}

	private static int dropChestLoot(CommandContext<CommandSourceStack> commandContext, ResourceLocation resourceLocation, LootCommand.DropConsumer dropConsumer) throws CommandSyntaxException {
		CommandSourceStack commandSourceStack = commandContext.getSource();
		LootContext.Builder builder = new LootContext.Builder(commandSourceStack.getLevel())
			.withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity())
			.withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition());
		return drop(commandContext, resourceLocation, builder.create(LootContextParamSets.CHEST), dropConsumer);
	}

	private static int dropFishingLoot(
		CommandContext<CommandSourceStack> commandContext,
		ResourceLocation resourceLocation,
		BlockPos blockPos,
		ItemStack itemStack,
		LootCommand.DropConsumer dropConsumer
	) throws CommandSyntaxException {
		CommandSourceStack commandSourceStack = commandContext.getSource();
		LootContext lootContext = new LootContext.Builder(commandSourceStack.getLevel())
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
			.withParameter(LootContextParams.TOOL, itemStack)
			.withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity())
			.create(LootContextParamSets.FISHING);
		return drop(commandContext, resourceLocation, lootContext, dropConsumer);
	}

	private static int drop(
		CommandContext<CommandSourceStack> commandContext, ResourceLocation resourceLocation, LootContext lootContext, LootCommand.DropConsumer dropConsumer
	) throws CommandSyntaxException {
		CommandSourceStack commandSourceStack = commandContext.getSource();
		LootTable lootTable = commandSourceStack.getServer().getLootTables().get(resourceLocation);
		List<ItemStack> list = lootTable.getRandomItems(lootContext);
		return dropConsumer.accept(commandContext, list, listx -> callback(commandSourceStack, listx));
	}

	@FunctionalInterface
	interface Callback {
		void accept(List<ItemStack> list) throws CommandSyntaxException;
	}

	@FunctionalInterface
	interface DropConsumer {
		int accept(CommandContext<CommandSourceStack> commandContext, List<ItemStack> list, LootCommand.Callback callback) throws CommandSyntaxException;
	}

	@FunctionalInterface
	interface TailProvider {
		ArgumentBuilder<CommandSourceStack, ?> construct(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, LootCommand.DropConsumer dropConsumer);
	}
}
