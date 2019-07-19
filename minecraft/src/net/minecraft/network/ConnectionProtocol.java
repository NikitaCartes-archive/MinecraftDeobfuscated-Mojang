package net.minecraft.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddGlobalEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerAckPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquippedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerAckPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
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
import net.minecraft.network.protocol.game.ServerboundRecipeBookUpdatePacket;
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
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import org.apache.logging.log4j.LogManager;

public enum ConnectionProtocol {
	HANDSHAKING(-1) {
		{
			this.addPacket(PacketFlow.SERVERBOUND, ClientIntentionPacket.class);
		}
	},
	PLAY(0) {
		{
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundAddEntityPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundAddExperienceOrbPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundAddGlobalEntityPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundAddMobPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundAddPaintingPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundAddPlayerPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundAnimatePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundAwardStatsPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundBlockDestructionPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundBlockEntityDataPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundBlockEventPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundBlockUpdatePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundBossEventPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundChangeDifficultyPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundChatPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundChunkBlocksUpdatePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundCommandSuggestionsPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundCommandsPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundContainerAckPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundContainerClosePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundContainerSetContentPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundContainerSetDataPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundContainerSetSlotPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundCooldownPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundCustomPayloadPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundCustomSoundPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundDisconnectPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundEntityEventPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundExplodePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundForgetLevelChunkPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundGameEventPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundHorseScreenOpenPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundKeepAlivePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundLevelChunkPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundLevelEventPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundLevelParticlesPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundLightUpdatePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundLoginPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundMapItemDataPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundMerchantOffersPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundMoveEntityPacket.Pos.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundMoveEntityPacket.PosRot.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundMoveEntityPacket.Rot.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundMoveEntityPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundMoveVehiclePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundOpenBookPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundOpenScreenPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundOpenSignEditorPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundPlaceGhostRecipePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundPlayerAbilitiesPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundPlayerCombatPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundPlayerInfoPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundPlayerLookAtPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundPlayerPositionPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundRecipePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundRemoveEntitiesPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundRemoveMobEffectPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundResourcePackPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundRespawnPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundRotateHeadPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSelectAdvancementsTabPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetBorderPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetCameraPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetCarriedItemPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetChunkCacheCenterPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetChunkCacheRadiusPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetDisplayObjectivePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetEntityDataPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetEntityLinkPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetEntityMotionPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetEquippedItemPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetExperiencePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetHealthPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetObjectivePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetPassengersPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetPlayerTeamPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetScorePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetSpawnPositionPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetTimePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSetTitlesPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSoundEntityPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundSoundPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundStopSoundPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundTabListPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundTagQueryPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundTakeItemEntityPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundTeleportEntityPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundUpdateAdvancementsPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundUpdateAttributesPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundUpdateMobEffectPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundUpdateRecipesPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundUpdateTagsPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundBlockBreakAckPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundAcceptTeleportationPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundBlockEntityTagQuery.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundChangeDifficultyPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundChatPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundClientCommandPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundClientInformationPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundCommandSuggestionPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundContainerAckPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundContainerButtonClickPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundContainerClickPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundContainerClosePacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundCustomPayloadPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundEditBookPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundEntityTagQuery.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundInteractPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundKeepAlivePacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundLockDifficultyPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundMovePlayerPacket.Pos.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundMovePlayerPacket.PosRot.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundMovePlayerPacket.Rot.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundMovePlayerPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundMoveVehiclePacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundPaddleBoatPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundPickItemPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundPlaceRecipePacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundPlayerAbilitiesPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundPlayerActionPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundPlayerCommandPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundPlayerInputPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundRecipeBookUpdatePacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundRenameItemPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundResourcePackPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSeenAdvancementsPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSelectTradePacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSetBeaconPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSetCarriedItemPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSetCommandBlockPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSetCommandMinecartPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSetCreativeModeSlotPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSetJigsawBlockPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSetStructureBlockPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSignUpdatePacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundSwingPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundTeleportToEntityPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundUseItemOnPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundUseItemPacket.class);
		}
	},
	STATUS(1) {
		{
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundStatusRequestPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundStatusResponsePacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundPingRequestPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundPongResponsePacket.class);
		}
	},
	LOGIN(2) {
		{
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundLoginDisconnectPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundHelloPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundGameProfilePacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundLoginCompressionPacket.class);
			this.addPacket(PacketFlow.CLIENTBOUND, ClientboundCustomQueryPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundHelloPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundKeyPacket.class);
			this.addPacket(PacketFlow.SERVERBOUND, ServerboundCustomQueryPacket.class);
		}
	};

	private static final ConnectionProtocol[] LOOKUP = new ConnectionProtocol[4];
	private static final Map<Class<? extends Packet<?>>, ConnectionProtocol> PROTOCOL_BY_PACKET = Maps.<Class<? extends Packet<?>>, ConnectionProtocol>newHashMap();
	private final int id;
	private final Map<PacketFlow, BiMap<Integer, Class<? extends Packet<?>>>> packets = Maps.newEnumMap(PacketFlow.class);

	private ConnectionProtocol(int j) {
		this.id = j;
	}

	protected ConnectionProtocol addPacket(PacketFlow packetFlow, Class<? extends Packet<?>> class_) {
		BiMap<Integer, Class<? extends Packet<?>>> biMap = (BiMap<Integer, Class<? extends Packet<?>>>)this.packets.get(packetFlow);
		if (biMap == null) {
			biMap = HashBiMap.create();
			this.packets.put(packetFlow, biMap);
		}

		if (biMap.containsValue(class_)) {
			String string = packetFlow + " packet " + class_ + " is already known to ID " + biMap.inverse().get(class_);
			LogManager.getLogger().fatal(string);
			throw new IllegalArgumentException(string);
		} else {
			biMap.put(biMap.size(), class_);
			return this;
		}
	}

	public Integer getPacketId(PacketFlow packetFlow, Packet<?> packet) throws Exception {
		return (Integer)((BiMap)this.packets.get(packetFlow)).inverse().get(packet.getClass());
	}

	@Nullable
	public Packet<?> createPacket(PacketFlow packetFlow, int i) throws IllegalAccessException, InstantiationException {
		Class<? extends Packet<?>> class_ = (Class<? extends Packet<?>>)((BiMap)this.packets.get(packetFlow)).get(i);
		return class_ == null ? null : (Packet)class_.newInstance();
	}

	public int getId() {
		return this.id;
	}

	public static ConnectionProtocol getById(int i) {
		return i >= -1 && i <= 2 ? LOOKUP[i - -1] : null;
	}

	public static ConnectionProtocol getProtocolForPacket(Packet<?> packet) {
		return (ConnectionProtocol)PROTOCOL_BY_PACKET.get(packet.getClass());
	}

	static {
		for (ConnectionProtocol connectionProtocol : values()) {
			int i = connectionProtocol.getId();
			if (i < -1 || i > 2) {
				throw new Error("Invalid protocol ID " + Integer.toString(i));
			}

			LOOKUP[i - -1] = connectionProtocol;

			for (PacketFlow packetFlow : connectionProtocol.packets.keySet()) {
				for (Class<? extends Packet<?>> class_ : ((BiMap)connectionProtocol.packets.get(packetFlow)).values()) {
					if (PROTOCOL_BY_PACKET.containsKey(class_) && PROTOCOL_BY_PACKET.get(class_) != connectionProtocol) {
						throw new Error("Packet " + class_ + " is already assigned to protocol " + PROTOCOL_BY_PACKET.get(class_) + " - can't reassign to " + connectionProtocol);
					}

					try {
						class_.newInstance();
					} catch (Throwable var10) {
						throw new Error("Packet " + class_ + " fails instantiation checks! " + class_);
					}

					PROTOCOL_BY_PACKET.put(class_, connectionProtocol);
				}
			}
		}
	}
}
