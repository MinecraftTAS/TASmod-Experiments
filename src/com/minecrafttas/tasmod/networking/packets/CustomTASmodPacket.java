package com.minecrafttas.tasmod.networking.packets;

import net.minecraft.network.PacketBuffer;

/**
 * This is a Packet transmittable over the TASmod Custom Server.
 * 
 * IMPLEMENTATION NOTICE:
 * There are no clientbound or serverbound packets as they were deemed unnecessary. This means both the server and the client can transmit any packet.
 * @author Pancake
 */
public interface CustomTASmodPacket {

	/**
	 * Once a packet is being received it is immediately handled in this method.
	 * 
	 * IMPLEMENTATION NOTICE:
	 * This process is non-blocking for the game and executed on the tasmod server thread temporarily blocking it.
	 */
	public void handle();
	
	/**
	 * In order to transfer packets over the network connection they need to be serialized into a stream of bytes.
	 * @return A serializable packet buffer
	 */
	public PacketBuffer serialize();
	
	/**
	 * In order to receive packets over the network connection the other end serializes the packet into a stream of bytes. Therefore this end needs to deserialize the packet
	 * @param buf A deserializable packet buffer
	 */
	public void deserialize(PacketBuffer buf);
	
}
