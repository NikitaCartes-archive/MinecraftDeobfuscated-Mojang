/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;

public interface ServerGamePacketListener
extends ServerPacketListener {
    public void handleAnimate(ServerboundSwingPacket var1);

    public void handleChat(ServerboundChatPacket var1);

    public void handleClientCommand(ServerboundClientCommandPacket var1);

    public void handleClientInformation(ServerboundClientInformationPacket var1);

    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket var1);

    public void handleContainerClick(ServerboundContainerClickPacket var1);

    public void handlePlaceRecipe(ServerboundPlaceRecipePacket var1);

    public void handleContainerClose(ServerboundContainerClosePacket var1);

    public void handleCustomPayload(ServerboundCustomPayloadPacket var1);

    public void handleInteract(ServerboundInteractPacket var1);

    public void handleKeepAlive(ServerboundKeepAlivePacket var1);

    public void handleMovePlayer(ServerboundMovePlayerPacket var1);

    public void handlePong(ServerboundPongPacket var1);

    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket var1);

    public void handlePlayerAction(ServerboundPlayerActionPacket var1);

    public void handlePlayerCommand(ServerboundPlayerCommandPacket var1);

    public void handlePlayerInput(ServerboundPlayerInputPacket var1);

    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket var1);

    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket var1);

    public void handleSignUpdate(ServerboundSignUpdatePacket var1);

    public void handleUseItemOn(ServerboundUseItemOnPacket var1);

    public void handleUseItem(ServerboundUseItemPacket var1);

    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket var1);

    public void handleResourcePackResponse(ServerboundResourcePackPacket var1);

    public void handlePaddleBoat(ServerboundPaddleBoatPacket var1);

    public void handleMoveVehicle(ServerboundMoveVehiclePacket var1);

    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket var1);

    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket var1);

    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket var1);

    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket var1);

    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket var1);

    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket var1);

    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket var1);

    public void handlePickItem(ServerboundPickItemPacket var1);

    public void handleRenameItem(ServerboundRenameItemPacket var1);

    public void handleSetBeaconPacket(ServerboundSetBeaconPacket var1);

    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket var1);

    public void handleSelectTrade(ServerboundSelectTradePacket var1);

    public void handleEditBook(ServerboundEditBookPacket var1);

    public void handleEntityTagQuery(ServerboundEntityTagQuery var1);

    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery var1);

    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket var1);

    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket var1);

    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket var1);

    public void handleLockDifficulty(ServerboundLockDifficultyPacket var1);
}

