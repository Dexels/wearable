package com.dexels.fps.usb;

import com.dexels.fps.packet.Packet;

public interface PacketListener {
	public void packetReceived(Packet p);
}
