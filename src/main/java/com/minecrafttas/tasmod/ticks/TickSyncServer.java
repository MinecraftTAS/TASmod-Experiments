package com.minecrafttas.tasmod.ticks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.minecrafttas.tasmod.networking.Server;
import com.minecrafttas.tasmod.networking.packets.ClientTickPacket;

/**
 * This class manages tick sync
 * German: https://1drv.ms/p/s!Av_ysXerhm5CphLvLvguvL5QYe1A?e=MHPldP
 * English: https://1drv.ms/p/s!Av_ysXerhm5Cpha7Qq2tiVebd4DY?e=pzxOva
 *
 * @author Pancake
 */
public class TickSyncServer {

	/**
	 * A multithreadable boolean that tells the MixinMinecraftServer to tick the server or not.
	 */
	public static AtomicBoolean shouldTick = new AtomicBoolean(true);

	private static List<UUID> tickedClients = new ArrayList<>();
	
	/**
	 * Handles incoming tick packets from the client to the server
	 * This will put the uuid into a list of ticked clients and once every client
	 * is in that list, tick the server.
	 *
	 * @param uuid Player UUID
	 */
	public static void onPacket(UUID uuid) {
		// Check if a packet was received from every player
		tickedClients.add(uuid);
		if (tickedClients.size() >= Server.getConnectionCount()) {
			tickedClients.clear();
			shouldTick.set(true);
		}
	}

	/**
	 * Called after a server tick. This will send a packet
	 * to all clients making them tick
	 */
	public static void serverPostTick() {
		Server.sendPacket(new ClientTickPacket());
	}

}
