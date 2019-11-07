package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugMobNameGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class VillageDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	private final Map<BlockPos, VillageDebugRenderer.PoiInfo> pois = Maps.<BlockPos, VillageDebugRenderer.PoiInfo>newHashMap();
	private final Set<SectionPos> villageSections = Sets.<SectionPos>newHashSet();
	private final Map<UUID, VillageDebugRenderer.BrainDump> brainDumpsPerEntity = Maps.<UUID, VillageDebugRenderer.BrainDump>newHashMap();
	private UUID lastLookedAtUuid;

	public VillageDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void clear() {
		this.pois.clear();
		this.villageSections.clear();
		this.brainDumpsPerEntity.clear();
		this.lastLookedAtUuid = null;
	}

	public void addPoi(VillageDebugRenderer.PoiInfo poiInfo) {
		this.pois.put(poiInfo.pos, poiInfo);
	}

	public void removePoi(BlockPos blockPos) {
		this.pois.remove(blockPos);
	}

	public void setFreeTicketCount(BlockPos blockPos, int i) {
		VillageDebugRenderer.PoiInfo poiInfo = (VillageDebugRenderer.PoiInfo)this.pois.get(blockPos);
		if (poiInfo == null) {
			LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: " + blockPos);
		} else {
			poiInfo.freeTicketCount = i;
		}
	}

	public void setVillageSection(SectionPos sectionPos) {
		this.villageSections.add(sectionPos);
	}

	public void setNotVillageSection(SectionPos sectionPos) {
		this.villageSections.remove(sectionPos);
	}

	public void addOrUpdateBrainDump(VillageDebugRenderer.BrainDump brainDump) {
		this.brainDumpsPerEntity.put(brainDump.uuid, brainDump);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, long l) {
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		this.doRender(d, e, f);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
		if (!this.minecraft.player.isSpectator()) {
			this.updateLastLookedAtUuid();
		}
	}

	private void doRender(double d, double e, double f) {
		BlockPos blockPos = new BlockPos(d, e, f);
		this.villageSections.forEach(sectionPos -> {
			if (blockPos.closerThan(sectionPos.center(), 60.0)) {
				highlightVillageSection(sectionPos);
			}
		});
		this.brainDumpsPerEntity.values().forEach(brainDump -> {
			if (this.isPlayerCloseEnoughToMob(brainDump)) {
				this.renderVillagerInfo(brainDump, d, e, f);
			}
		});

		for (BlockPos blockPos2 : this.pois.keySet()) {
			if (blockPos.closerThan(blockPos2, 30.0)) {
				highlightPoi(blockPos2);
			}
		}

		this.pois.values().forEach(poiInfo -> {
			if (blockPos.closerThan(poiInfo.pos, 30.0)) {
				this.renderPoiInfo(poiInfo);
			}
		});
		this.getGhostPois().forEach((blockPos2x, list) -> {
			if (blockPos.closerThan(blockPos2x, 30.0)) {
				this.renderGhostPoi(blockPos2x, list);
			}
		});
	}

	private static void highlightVillageSection(SectionPos sectionPos) {
		float f = 1.0F;
		BlockPos blockPos = sectionPos.center();
		BlockPos blockPos2 = blockPos.offset(-1.0, -1.0, -1.0);
		BlockPos blockPos3 = blockPos.offset(1.0, 1.0, 1.0);
		DebugRenderer.renderFilledBox(blockPos2, blockPos3, 0.2F, 1.0F, 0.2F, 0.15F);
	}

	private static void highlightPoi(BlockPos blockPos) {
		float f = 0.05F;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		DebugRenderer.renderFilledBox(blockPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
	}

	private void renderGhostPoi(BlockPos blockPos, List<String> list) {
		float f = 0.05F;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		DebugRenderer.renderFilledBox(blockPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
		renderTextOverPos("" + list, blockPos, 0, -256);
		renderTextOverPos("Ghost POI", blockPos, 1, -65536);
	}

	private void renderPoiInfo(VillageDebugRenderer.PoiInfo poiInfo) {
		int i = 0;
		if (this.getTicketHolderNames(poiInfo).size() < 4) {
			renderTextOverPoi("" + this.getTicketHolderNames(poiInfo), poiInfo, i, -256);
		} else {
			renderTextOverPoi("" + this.getTicketHolderNames(poiInfo).size() + " ticket holders", poiInfo, i, -256);
		}

		renderTextOverPoi("Free tickets: " + poiInfo.freeTicketCount, poiInfo, ++i, -256);
		renderTextOverPoi(poiInfo.type, poiInfo, ++i, -1);
	}

	private void renderPath(VillageDebugRenderer.BrainDump brainDump, double d, double e, double f) {
		if (brainDump.path != null) {
			PathfindingRenderer.renderPath(brainDump.path, 0.5F, false, false, d, e, f);
		}
	}

	private void renderVillagerInfo(VillageDebugRenderer.BrainDump brainDump, double d, double e, double f) {
		boolean bl = this.isVillagerSelected(brainDump);
		int i = 0;
		renderTextOverMob(brainDump.pos, i, brainDump.name, -1, 0.03F);
		i++;
		if (bl) {
			renderTextOverMob(brainDump.pos, i, brainDump.profession + " " + brainDump.xp + "xp", -1, 0.02F);
			i++;
		}

		if (bl && !brainDump.inventory.equals("")) {
			renderTextOverMob(brainDump.pos, i, brainDump.inventory, -98404, 0.02F);
			i++;
		}

		if (bl) {
			for (String string : brainDump.behaviors) {
				renderTextOverMob(brainDump.pos, i, string, -16711681, 0.02F);
				i++;
			}
		}

		if (bl) {
			for (String string : brainDump.activities) {
				renderTextOverMob(brainDump.pos, i, string, -16711936, 0.02F);
				i++;
			}
		}

		if (brainDump.wantsGolem) {
			renderTextOverMob(brainDump.pos, i, "Wants Golem", -23296, 0.02F);
			i++;
		}

		if (bl) {
			for (String string : brainDump.gossips) {
				if (string.startsWith(brainDump.name)) {
					renderTextOverMob(brainDump.pos, i, string, -1, 0.02F);
				} else {
					renderTextOverMob(brainDump.pos, i, string, -23296, 0.02F);
				}

				i++;
			}
		}

		if (bl) {
			for (String string : Lists.reverse(brainDump.memories)) {
				renderTextOverMob(brainDump.pos, i, string, -3355444, 0.02F);
				i++;
			}
		}

		if (bl) {
			this.renderPath(brainDump, d, e, f);
		}
	}

	private static void renderTextOverPoi(String string, VillageDebugRenderer.PoiInfo poiInfo, int i, int j) {
		BlockPos blockPos = poiInfo.pos;
		renderTextOverPos(string, blockPos, i, j);
	}

	private static void renderTextOverPos(String string, BlockPos blockPos, int i, int j) {
		double d = 1.3;
		double e = 0.2;
		double f = (double)blockPos.getX() + 0.5;
		double g = (double)blockPos.getY() + 1.3 + (double)i * 0.2;
		double h = (double)blockPos.getZ() + 0.5;
		DebugRenderer.renderFloatingText(string, f, g, h, j, 0.02F, true, 0.0F, true);
	}

	private static void renderTextOverMob(Position position, int i, String string, int j, float f) {
		double d = 2.4;
		double e = 0.25;
		BlockPos blockPos = new BlockPos(position);
		double g = (double)blockPos.getX() + 0.5;
		double h = position.y() + 2.4 + (double)i * 0.25;
		double k = (double)blockPos.getZ() + 0.5;
		float l = 0.5F;
		DebugRenderer.renderFloatingText(string, g, h, k, j, f, false, 0.5F, true);
	}

	private Set<String> getTicketHolderNames(VillageDebugRenderer.PoiInfo poiInfo) {
		return (Set<String>)this.getTicketHolders(poiInfo.pos).stream().map(DebugMobNameGenerator::getMobName).collect(Collectors.toSet());
	}

	private boolean isVillagerSelected(VillageDebugRenderer.BrainDump brainDump) {
		return Objects.equals(this.lastLookedAtUuid, brainDump.uuid);
	}

	private boolean isPlayerCloseEnoughToMob(VillageDebugRenderer.BrainDump brainDump) {
		Player player = this.minecraft.player;
		BlockPos blockPos = new BlockPos(player.getX(), brainDump.pos.y(), player.getZ());
		BlockPos blockPos2 = new BlockPos(brainDump.pos);
		return blockPos.closerThan(blockPos2, 30.0);
	}

	private Collection<UUID> getTicketHolders(BlockPos blockPos) {
		return (Collection<UUID>)this.brainDumpsPerEntity
			.values()
			.stream()
			.filter(brainDump -> brainDump.hasPoi(blockPos))
			.map(VillageDebugRenderer.BrainDump::getUuid)
			.collect(Collectors.toSet());
	}

	private Map<BlockPos, List<String>> getGhostPois() {
		Map<BlockPos, List<String>> map = Maps.<BlockPos, List<String>>newHashMap();

		for (VillageDebugRenderer.BrainDump brainDump : this.brainDumpsPerEntity.values()) {
			for (BlockPos blockPos : brainDump.pois) {
				if (!this.pois.containsKey(blockPos)) {
					List<String> list = (List<String>)map.get(blockPos);
					if (list == null) {
						list = Lists.<String>newArrayList();
						map.put(blockPos, list);
					}

					list.add(brainDump.name);
				}
			}
		}

		return map;
	}

	private void updateLastLookedAtUuid() {
		DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(entity -> this.lastLookedAtUuid = entity.getUUID());
	}

	@Environment(EnvType.CLIENT)
	public static class BrainDump {
		public final UUID uuid;
		public final int id;
		public final String name;
		public final String profession;
		public final int xp;
		public final Position pos;
		public final String inventory;
		public final Path path;
		public final boolean wantsGolem;
		public final List<String> activities = Lists.<String>newArrayList();
		public final List<String> behaviors = Lists.<String>newArrayList();
		public final List<String> memories = Lists.<String>newArrayList();
		public final List<String> gossips = Lists.<String>newArrayList();
		public final Set<BlockPos> pois = Sets.<BlockPos>newHashSet();

		public BrainDump(UUID uUID, int i, String string, String string2, int j, Position position, String string3, @Nullable Path path, boolean bl) {
			this.uuid = uUID;
			this.id = i;
			this.name = string;
			this.profession = string2;
			this.xp = j;
			this.pos = position;
			this.inventory = string3;
			this.path = path;
			this.wantsGolem = bl;
		}

		private boolean hasPoi(BlockPos blockPos) {
			return this.pois.stream().anyMatch(blockPos::equals);
		}

		public UUID getUuid() {
			return this.uuid;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class PoiInfo {
		public final BlockPos pos;
		public String type;
		public int freeTicketCount;

		public PoiInfo(BlockPos blockPos, String string, int i) {
			this.pos = blockPos;
			this.type = string;
			this.freeTicketCount = i;
		}
	}
}
