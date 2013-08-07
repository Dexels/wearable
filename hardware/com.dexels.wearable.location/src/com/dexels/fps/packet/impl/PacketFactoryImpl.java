package com.dexels.fps.packet.impl;

import com.dexels.fps.packet.Packet;
import com.dexels.fps.packet.PacketFactory;

public class PacketFactoryImpl implements PacketFactory {
	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.PacketFactory#createFactory(byte[], int)
	 */
	@Override
	public Packet createFactory(byte[] data, int length) {
		return new PacketImpl(data, length);
	}
}
