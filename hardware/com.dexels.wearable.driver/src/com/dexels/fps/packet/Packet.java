package com.dexels.fps.packet;

public interface Packet {

	public final static int TYPE_UNKNOWN = -1;
	public final static int TYPE_1 = 1; // Timestamp, UniqueId and RSSI at BASE?? (= REPEATER -> 0)
	public final static int TYPE_2 = 2; // Timestamp, Voltage, Temperature and RSSI at REPEATER
	public final static int TYPE_3 = 3; // Timestamp, Keypress timestamp and RSSI at REPEATER
	public final static int TYPE_4 = 4; // Timestamp, Accelerometer and RSSI at REPEATER
	public final static int TYPE_5 = 5; // Not implemented yet, RSSI at BASE for 11 Beacons.

	public byte[] getData();

	public void setData(byte[] data);

	public int getPacketType();

	public void setPacketType(int packetType);

	public int getRepeaterId();

	public void setRepeaterId(int repeaterId);

	public int getTimestamp();

	public void setTimestamp(int timestamp);

	public String getSenderUniqueId();

	public void setSenderUniqueId(String senderUniqueId);

	public String getSenderNetworkId();

	public void setSenderNetworkId(String senderNetworkId);

	public int getSignalStrength();

	public void setSignalStrength(int signalStrength);

	public int getTemperature();

	public void setTemperature(int temperature);

	public int getVoltage();

	public void setVoltage(int voltage);

	public int getKeyPressTimestamp();

	public void setKeyPressTimestamp(int keyPressTimestamp);

	public double getAccX();

	public double getAccY();

	public double getAccZ();

}