package net.minecraft.network.protocol.game;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;

public interface ServerGamePacketListener extends ServerPingPacketListener, ServerCommonPacketListener {
	@Override
	default ConnectionProtocol protocol() {
		return ConnectionProtocol.PLAY;
	}

	void handleAnimate(ServerboundSwingPacket serverboundSwingPacket);

	void handleChat(ServerboundChatPacket serverboundChatPacket);

	void handleChatCommand(ServerboundChatCommandPacket serverboundChatCommandPacket);

	void handleChatAck(ServerboundChatAckPacket serverboundChatAckPacket);

	void handleClientCommand(ServerboundClientCommandPacket serverboundClientCommandPacket);

	void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket);

	void handleContainerButtonClick(ServerboundContainerButtonClickPacket serverboundContainerButtonClickPacket);

	void handleContainerClick(ServerboundContainerClickPacket serverboundContainerClickPacket);

	void handlePlaceRecipe(ServerboundPlaceRecipePacket serverboundPlaceRecipePacket);

	void handleContainerClose(ServerboundContainerClosePacket serverboundContainerClosePacket);

	void handleInteract(ServerboundInteractPacket serverboundInteractPacket);

	void handleMovePlayer(ServerboundMovePlayerPacket serverboundMovePlayerPacket);

	void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket serverboundPlayerAbilitiesPacket);

	void handlePlayerAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket);

	void handlePlayerCommand(ServerboundPlayerCommandPacket serverboundPlayerCommandPacket);

	void handlePlayerInput(ServerboundPlayerInputPacket serverboundPlayerInputPacket);

	void handleSetCarriedItem(ServerboundSetCarriedItemPacket serverboundSetCarriedItemPacket);

	void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundSetCreativeModeSlotPacket);

	void handleSignUpdate(ServerboundSignUpdatePacket serverboundSignUpdatePacket);

	void handleUseItemOn(ServerboundUseItemOnPacket serverboundUseItemOnPacket);

	void handleUseItem(ServerboundUseItemPacket serverboundUseItemPacket);

	void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket serverboundTeleportToEntityPacket);

	void handlePaddleBoat(ServerboundPaddleBoatPacket serverboundPaddleBoatPacket);

	void handleMoveVehicle(ServerboundMoveVehiclePacket serverboundMoveVehiclePacket);

	void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket serverboundAcceptTeleportationPacket);

	void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket serverboundRecipeBookSeenRecipePacket);

	void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket serverboundRecipeBookChangeSettingsPacket);

	void handleSeenAdvancements(ServerboundSeenAdvancementsPacket serverboundSeenAdvancementsPacket);

	void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket serverboundCommandSuggestionPacket);

	void handleSetCommandBlock(ServerboundSetCommandBlockPacket serverboundSetCommandBlockPacket);

	void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket serverboundSetCommandMinecartPacket);

	void handlePickItem(ServerboundPickItemPacket serverboundPickItemPacket);

	void handleRenameItem(ServerboundRenameItemPacket serverboundRenameItemPacket);

	void handleSetBeaconPacket(ServerboundSetBeaconPacket serverboundSetBeaconPacket);

	void handleSetStructureBlock(ServerboundSetStructureBlockPacket serverboundSetStructureBlockPacket);

	void handleSelectTrade(ServerboundSelectTradePacket serverboundSelectTradePacket);

	void handleEditBook(ServerboundEditBookPacket serverboundEditBookPacket);

	void handleEntityTagQuery(ServerboundEntityTagQuery serverboundEntityTagQuery);

	void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery serverboundBlockEntityTagQuery);

	void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket serverboundSetJigsawBlockPacket);

	void handleJigsawGenerate(ServerboundJigsawGeneratePacket serverboundJigsawGeneratePacket);

	void handleChangeDifficulty(ServerboundChangeDifficultyPacket serverboundChangeDifficultyPacket);

	void handleLockDifficulty(ServerboundLockDifficultyPacket serverboundLockDifficultyPacket);

	void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundChatSessionUpdatePacket);

	void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket serverboundConfigurationAcknowledgedPacket);

	void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket serverboundChunkBatchReceivedPacket);
}
