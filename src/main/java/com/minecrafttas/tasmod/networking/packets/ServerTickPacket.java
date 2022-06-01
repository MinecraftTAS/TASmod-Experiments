package com.minecrafttas.tasmod.networking.packets;

import java.util.UUID;

import com.minecrafttas.tasmod.ticks.TickSyncServer;

import net.minecraft.util.PacketByteBuf;

/**
 * This packet is sent after every tick to let the other server know when to tick.
 * This is part of Tick Sync
 * German: https://1drv.ms/p/s!Av_ysXerhm5CphLvLvguvL5QYe1A?e=MHPldP
 * English: https://1drv.ms/p/s!Av_ysXerhm5Cpha7Qq2tiVebd4DY?e=pzxOva
 *
 * @author Pancake
 */
public class ServerTickPacket implements Packet {

	/**
	 * Creates a new tick packet containing a uuid of the player
	 */
	public ServerTickPacket(UUID uuid) {
		this.uuid = uuid;
	}

	protected UUID uuid;

	/**
	 * This will trigger the packet handler
	 */
	@Override
	public void handle() {
		TickSyncServer.onPacket(this.uuid);
	}

	@Override
	public PacketByteBuf serialize(PacketByteBuf buf) {
		buf.method_12597(this.uuid);
		return buf;
	}

	@Override
	public void deserialize(PacketByteBuf buf) {
		this.uuid = buf.readUuid();
	}

}
