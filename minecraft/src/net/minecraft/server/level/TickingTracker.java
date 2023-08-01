package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;

public class TickingTracker extends ChunkTracker {
	public static final int MAX_LEVEL = 33;
	private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
	protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
	private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>();

	public TickingTracker() {
		super(34, 16, 256);
		this.chunks.defaultReturnValue((byte)33);
	}

	private SortedArraySet<Ticket<?>> getTickets(long l) {
		return this.tickets.computeIfAbsent(l, (Long2ObjectFunction<? extends SortedArraySet<Ticket<?>>>)(lx -> (SortedArraySet<Ticket<?>>)SortedArraySet.create(4)));
	}

	private int getTicketLevelAt(SortedArraySet<Ticket<?>> sortedArraySet) {
		return sortedArraySet.isEmpty() ? 34 : sortedArraySet.first().getTicketLevel();
	}

	public void addTicket(long l, Ticket<?> ticket) {
		SortedArraySet<Ticket<?>> sortedArraySet = this.getTickets(l);
		int i = this.getTicketLevelAt(sortedArraySet);
		sortedArraySet.add(ticket);
		if (ticket.getTicketLevel() < i) {
			this.update(l, ticket.getTicketLevel(), true);
		}
	}

	public void removeTicket(long l, Ticket<?> ticket) {
		SortedArraySet<Ticket<?>> sortedArraySet = this.getTickets(l);
		sortedArraySet.remove(ticket);
		if (sortedArraySet.isEmpty()) {
			this.tickets.remove(l);
		}

		this.update(l, this.getTicketLevelAt(sortedArraySet), false);
	}

	public <T> void addTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
		this.addTicket(chunkPos.toLong(), new Ticket<>(ticketType, i, object));
	}

	public <T> void removeTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
		Ticket<T> ticket = new Ticket<>(ticketType, i, object);
		this.removeTicket(chunkPos.toLong(), ticket);
	}

	public void replacePlayerTicketsLevel(int i) {
		List<Pair<Ticket<ChunkPos>, Long>> list = new ArrayList();

		for (Entry<SortedArraySet<Ticket<?>>> entry : this.tickets.long2ObjectEntrySet()) {
			for (Ticket<?> ticket : (SortedArraySet)entry.getValue()) {
				if (ticket.getType() == TicketType.PLAYER) {
					list.add(Pair.of(ticket, entry.getLongKey()));
				}
			}
		}

		for (Pair<Ticket<ChunkPos>, Long> pair : list) {
			Long long_ = pair.getSecond();
			Ticket<ChunkPos> ticketx = pair.getFirst();
			this.removeTicket(long_, ticketx);
			ChunkPos chunkPos = new ChunkPos(long_);
			TicketType<ChunkPos> ticketType = ticketx.getType();
			this.addTicket(ticketType, chunkPos, i, chunkPos);
		}
	}

	@Override
	protected int getLevelFromSource(long l) {
		SortedArraySet<Ticket<?>> sortedArraySet = this.tickets.get(l);
		return sortedArraySet != null && !sortedArraySet.isEmpty() ? sortedArraySet.first().getTicketLevel() : Integer.MAX_VALUE;
	}

	public int getLevel(ChunkPos chunkPos) {
		return this.getLevel(chunkPos.toLong());
	}

	@Override
	protected int getLevel(long l) {
		return this.chunks.get(l);
	}

	@Override
	protected void setLevel(long l, int i) {
		if (i >= 33) {
			this.chunks.remove(l);
		} else {
			this.chunks.put(l, (byte)i);
		}
	}

	public void runAllUpdates() {
		this.runUpdates(Integer.MAX_VALUE);
	}

	public String getTicketDebugString(long l) {
		SortedArraySet<Ticket<?>> sortedArraySet = this.tickets.get(l);
		return sortedArraySet != null && !sortedArraySet.isEmpty() ? sortedArraySet.first().toString() : "no_ticket";
	}
}
