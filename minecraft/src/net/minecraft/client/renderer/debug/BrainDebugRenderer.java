package net.minecraft.client.renderer.debug;

import com.google.common.collect.Iterables;
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
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class BrainDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	private final Map<BlockPos, BrainDebugRenderer.PoiInfo> pois = Maps.<BlockPos, BrainDebugRenderer.PoiInfo>newHashMap();
	private final Map<UUID, BrainDebugRenderer.BrainDump> brainDumpsPerEntity = Maps.<UUID, BrainDebugRenderer.BrainDump>newHashMap();
	@Nullable
	private UUID lastLookedAtUuid;

	public BrainDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void clear() {
		this.pois.clear();
		this.brainDumpsPerEntity.clear();
		this.lastLookedAtUuid = null;
	}

	public void addPoi(BrainDebugRenderer.PoiInfo poiInfo) {
		this.pois.put(poiInfo.pos, poiInfo);
	}

	public void removePoi(BlockPos blockPos) {
		this.pois.remove(blockPos);
	}

	public void setFreeTicketCount(BlockPos blockPos, int i) {
		BrainDebugRenderer.PoiInfo poiInfo = (BrainDebugRenderer.PoiInfo)this.pois.get(blockPos);
		if (poiInfo == null) {
			LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: " + blockPos);
		} else {
			poiInfo.freeTicketCount = i;
		}
	}

	public void addOrUpdateBrainDump(BrainDebugRenderer.BrainDump brainDump) {
		this.brainDumpsPerEntity.put(brainDump.uuid, brainDump);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		this.clearRemovedEntities();
		this.doRender(d, e, f);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
		if (!this.minecraft.player.isSpectator()) {
			this.updateLastLookedAtUuid();
		}
	}

	private void clearRemovedEntities() {
		this.brainDumpsPerEntity.entrySet().removeIf(entry -> {
			Entity entity = this.minecraft.level.getEntity(((BrainDebugRenderer.BrainDump)entry.getValue()).id);
			return entity == null || entity.removed;
		});
	}

	private void doRender(double d, double e, double f) {
		BlockPos blockPos = new BlockPos(d, e, f);
		this.brainDumpsPerEntity.values().forEach(brainDump -> {
			if (this.isPlayerCloseEnoughToMob(brainDump)) {
				this.renderBrainInfo(brainDump, d, e, f);
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

	private void renderPoiInfo(BrainDebugRenderer.PoiInfo poiInfo) {
		int i = 0;
		Set<String> set = this.getTicketHolderNames(poiInfo);
		if (set.size() < 4) {
			renderTextOverPoi("Owners: " + set, poiInfo, i, -256);
		} else {
			renderTextOverPoi("" + set.size() + " ticket holders", poiInfo, i, -256);
		}

		i++;
		Set<String> set2 = this.getPotentialTicketHolderNames(poiInfo);
		if (set2.size() < 4) {
			renderTextOverPoi("Candidates: " + set2, poiInfo, i, -23296);
		} else {
			renderTextOverPoi("" + set2.size() + " potential owners", poiInfo, i, -23296);
		}

		renderTextOverPoi("Free tickets: " + poiInfo.freeTicketCount, poiInfo, ++i, -256);
		renderTextOverPoi(poiInfo.type, poiInfo, ++i, -1);
	}

	private void renderPath(BrainDebugRenderer.BrainDump brainDump, double d, double e, double f) {
		if (brainDump.path != null) {
			PathfindingRenderer.renderPath(brainDump.path, 0.5F, false, false, d, e, f);
		}
	}

	private void renderBrainInfo(BrainDebugRenderer.BrainDump brainDump, double d, double e, double f) {
		boolean bl = this.isMobSelected(brainDump);
		int i = 0;
		renderTextOverMob(brainDump.pos, i, brainDump.name, -1, 0.03F);
		i++;
		if (bl) {
			renderTextOverMob(brainDump.pos, i, brainDump.profession + " " + brainDump.xp + " xp", -1, 0.02F);
			i++;
		}

		if (bl) {
			int j = brainDump.health < brainDump.maxHealth ? -23296 : -1;
			renderTextOverMob(brainDump.pos, i, "health: " + String.format("%.1f", brainDump.health) + " / " + String.format("%.1f", brainDump.maxHealth), j, 0.02F);
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

	private static void renderTextOverPoi(String string, BrainDebugRenderer.PoiInfo poiInfo, int i, int j) {
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

	private Set<String> getTicketHolderNames(BrainDebugRenderer.PoiInfo poiInfo) {
		return (Set<String>)this.getTicketHolders(poiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
	}

	private Set<String> getPotentialTicketHolderNames(BrainDebugRenderer.PoiInfo poiInfo) {
		return (Set<String>)this.getPotentialTicketHolders(poiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
	}

	private boolean isMobSelected(BrainDebugRenderer.BrainDump brainDump) {
		return Objects.equals(this.lastLookedAtUuid, brainDump.uuid);
	}

	private boolean isPlayerCloseEnoughToMob(BrainDebugRenderer.BrainDump brainDump) {
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
			.map(BrainDebugRenderer.BrainDump::getUuid)
			.collect(Collectors.toSet());
	}

	private Collection<UUID> getPotentialTicketHolders(BlockPos blockPos) {
		return (Collection<UUID>)this.brainDumpsPerEntity
			.values()
			.stream()
			.filter(brainDump -> brainDump.hasPotentialPoi(blockPos))
			.map(BrainDebugRenderer.BrainDump::getUuid)
			.collect(Collectors.toSet());
	}

	private Map<BlockPos, List<String>> getGhostPois() {
		Map<BlockPos, List<String>> map = Maps.<BlockPos, List<String>>newHashMap();

		for (BrainDebugRenderer.BrainDump brainDump : this.brainDumpsPerEntity.values()) {
			for (BlockPos blockPos : Iterables.concat(brainDump.pois, brainDump.potentialPois)) {
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
		public final float health;
		public final float maxHealth;
		public final Position pos;
		public final String inventory;
		public final Path path;
		public final boolean wantsGolem;
		public final List<String> activities = Lists.<String>newArrayList();
		public final List<String> behaviors = Lists.<String>newArrayList();
		public final List<String> memories = Lists.<String>newArrayList();
		public final List<String> gossips = Lists.<String>newArrayList();
		public final Set<BlockPos> pois = Sets.<BlockPos>newHashSet();
		public final Set<BlockPos> potentialPois = Sets.<BlockPos>newHashSet();

		public BrainDump(UUID uUID, int i, String string, String string2, int j, float f, float g, Position position, String string3, @Nullable Path path, boolean bl) {
			this.uuid = uUID;
			this.id = i;
			this.name = string;
			this.profession = string2;
			this.xp = j;
			this.health = f;
			this.maxHealth = g;
			this.pos = position;
			this.inventory = string3;
			this.path = path;
			this.wantsGolem = bl;
		}

		private boolean hasPoi(BlockPos blockPos) {
			return this.pois.stream().anyMatch(blockPos::equals);
		}

		private boolean hasPotentialPoi(BlockPos blockPos) {
			return this.potentialPois.contains(blockPos);
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
