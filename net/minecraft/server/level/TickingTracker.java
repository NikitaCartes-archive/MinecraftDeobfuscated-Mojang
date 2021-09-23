/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;

public class TickingTracker
extends ChunkTracker {
    private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
    protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
    private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap();

    public TickingTracker() {
        super(34, 16, 256);
        this.chunks.defaultReturnValue((byte)33);
    }

    private SortedArraySet<Ticket<?>> getTickets(long l2) {
        return this.tickets.computeIfAbsent(l2, l -> SortedArraySet.create(4));
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
        this.addTicket(chunkPos.toLong(), new Ticket<T>(ticketType, i, object));
    }

    public <T> void removeTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object) {
        Ticket<T> ticket = new Ticket<T>(ticketType, i, object);
        this.removeTicket(chunkPos.toLong(), ticket);
    }

    public void replacePlayerTicketsLevel(int i) {
        ArrayList<Pair<Ticket, Long>> list = new ArrayList<Pair<Ticket, Long>>();
        for (Long2ObjectMap.Entry entry : this.tickets.long2ObjectEntrySet()) {
            for (Ticket ticket : (SortedArraySet)entry.getValue()) {
                if (ticket.getType() != TicketType.PLAYER) continue;
                list.add(Pair.of(ticket, entry.getLongKey()));
            }
        }
        for (Pair pair : list) {
            Ticket ticket;
            Long long_ = (Long)pair.getSecond();
            ticket = (Ticket)pair.getFirst();
            this.removeTicket(long_, ticket);
            ChunkPos chunkPos = new ChunkPos(long_);
            TicketType ticketType = ticket.getType();
            this.addTicket(ticketType, chunkPos, i, chunkPos);
        }
    }

    @Override
    protected int getLevelFromSource(long l) {
        SortedArraySet<Ticket<?>> sortedArraySet = this.tickets.get(l);
        if (sortedArraySet == null || sortedArraySet.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return sortedArraySet.first().getTicketLevel();
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
        if (i > 33) {
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
        if (sortedArraySet == null || sortedArraySet.isEmpty()) {
            return "no_ticket";
        }
        return sortedArraySet.first().toString();
    }
}

