package com.dexels.fps.packet;


public interface PacketFactory {

	public Packet createFactory(byte[] data, int length);

}