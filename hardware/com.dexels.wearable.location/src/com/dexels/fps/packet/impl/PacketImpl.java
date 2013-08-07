package com.dexels.fps.packet.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.dexels.fps.packet.Packet;

public class PacketImpl implements Packet {
	
	private final int ACCELEROMETER_RANGE = 256; // +-2g setting. 128 for +-4g and 64 for +-8g
	private byte[] data = null;
	private int packetType = TYPE_UNKNOWN;
	private int repeaterId = -1;
	private int timestamp = -1;
	private String senderUniqueId = null; // Wristband unique identifier
	private String senderNetworkId = null; // Wristband network identifier
	private int signalStrength = -1;
	private int temperature = -1;
	private int voltage = -1;
	private int keyPressTimestamp = -1;
	private int accXraw = -1;
	private int accYraw = -1;
	private int accZraw = -1;
	private double accX = 0.0;
	private double accY = 0.0;
	private double accZ = 0.0;
	
	public PacketImpl(byte[] raw, int length){
		data = new byte[length];
		for(int i=0;i<length;i++){
			data[i] = raw[i];
		}
		parseData();
	}
	
	private final void parseData(){
		//System.err.println("Received " + data.length + " bytes from device");
		
		// Check if the first byte is 0x01;
		if(data[0] == 0x01){
			// Second byte determines packet type and repeater id
			getPacketHeader(data[1]);
		}
		
		getPacketData();
	}
	
	private final void getPacketData(){
		switch(packetType){
		case TYPE_1:
			timestamp = getTimeStamp(2);
			senderUniqueId = getSenderId(6);
			signalStrength = data[12];
			return;
		case TYPE_2:
			temperature = getTemperature(2);
			voltage = getVoltage(4);
			timestamp = getTimeStamp(6);
			senderNetworkId = getNetworkId(10);
			signalStrength = data[12];
			return;
		case TYPE_3:
			keyPressTimestamp = getTimeStamp(2);
			timestamp = getTimeStamp(6);
			senderNetworkId = getNetworkId(10);
			signalStrength = data[10];
			return;
		case TYPE_4:
			accXraw = data[2];
			accYraw = data[3];
			accZraw = data[4];
			refineAccelerometerData(data[5]);
			timestamp = getTimeStamp(6);
			senderNetworkId = getNetworkId(10);
			signalStrength = data[12];
			return;
		case TYPE_5:
			
			// Not implemented yet, as the packet contains no senderid's
			
			return;
			
		}
	}
	
	
	private void refineAccelerometerData(byte lsb){
		// LSB structure:  00ZZYYXX
		String lsbBin = Integer.toBinaryString(lsb);
		
		accXraw = (accXraw << 2) + Integer.parseInt(lsbBin.substring(5, 6), 2);
		accYraw = (accYraw << 2) + Integer.parseInt(lsbBin.substring(4, 5), 2);
		accZraw = (accZraw << 2) + Integer.parseInt(lsbBin.substring(2, 3), 2);
		
		// Now convert into directional G forces
		accX = calculateGForce(accXraw);
		accY = calculateGForce(accYraw);
		accZ = calculateGForce(accZraw);
		
		
		
	}
	
	private final double calculateGForce(int value){
		double gStep = 1.0 / ACCELEROMETER_RANGE;
		double result = 0.0;
		
		if(value > (4 * ACCELEROMETER_RANGE)){				// Negative force
			value = (4 * ACCELEROMETER_RANGE) - value;
			result = -(2 * ACCELEROMETER_RANGE - value) * gStep;
		} else {											// Positive force
			result = value * gStep;
		}
		return result;
	}
	
	
	private int getTemperature(int offset){
		byte[] intVal = new byte[2];
		for(int i=0;i<2;i++){
			intVal[i] = data[offset + i];
		}
		ByteBuffer bb= ByteBuffer.wrap(intVal);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	
	private int getVoltage(int offset){
		byte[] intVal = new byte[2];
		for(int i=0;i<2;i++){
			intVal[i] = data[offset + i];
		}
		ByteBuffer bb= ByteBuffer.wrap(intVal);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	
	private int getTimeStamp(int offset){
		// RTC timestamp
		byte[] intVal = new byte[4];
		for(int i=0;i<4;i++){
			intVal[i] = data[offset + i];
		}
		ByteBuffer bb= ByteBuffer.wrap(intVal);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	
	private String getSenderId(int offset){
		return getId(offset, 6);
	}
	
	private String getNetworkId(int offset){
		return getId(offset, 2);
	}
	
	private String getId(int offset, int length){
		// SenderId is six bytes
		byte[] sid = new byte[length];
		for(int i=0;i<length;i++){
			sid[i] = data[offset + i];
		}
		
		String id = "";
		for (int i = 0; i < length; i++) {
			int v = sid[i];
			if (v < 0){
				v = v + 256;
			}
			String hs = Integer.toHexString(v);
			if (v < 16){
				hs = "0" + hs;
			}
			id = id + hs;
		}
		return id;
	}
	
	private final void getPacketHeader(byte b){
		repeaterId = 0;
		while(b > 0x4F){
			b -= 0x20;    // 0x10 or 0x20, bit unclear
			repeaterId++;
		}
		
		// Check remainder for packet type
		if(b == 0x48){
			packetType = TYPE_1;
			return;
		}
		if(b == 0x49){
			packetType = TYPE_2;
			return;
		}
		if(b == 0x4A){
			packetType = TYPE_3;
			return;
		}
		if(b == 0x4B){
			packetType = TYPE_4;
			return;
		}
		if(b == 0x4D){
			packetType = TYPE_5;
			return;
		}
		packetType = TYPE_UNKNOWN;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getData()
	 */
	@Override
	public byte[] getData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setData(byte[])
	 */
	@Override
	public void setData(byte[] data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getPacketType()
	 */
	@Override
	public int getPacketType() {
		return packetType;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setPacketType(int)
	 */
	@Override
	public void setPacketType(int packetType) {
		this.packetType = packetType;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getRepeaterId()
	 */
	@Override
	public int getRepeaterId() {
		return repeaterId;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setRepeaterId(int)
	 */
	@Override
	public void setRepeaterId(int repeaterId) {
		this.repeaterId = repeaterId;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getTimestamp()
	 */
	@Override
	public int getTimestamp() {
		return timestamp;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setTimestamp(int)
	 */
	@Override
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getSenderUniqueId()
	 */
	@Override
	public String getSenderUniqueId() {
		return senderUniqueId;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setSenderUniqueId(java.lang.String)
	 */
	@Override
	public void setSenderUniqueId(String senderUniqueId) {
		this.senderUniqueId = senderUniqueId;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getSenderNetworkId()
	 */
	@Override
	public String getSenderNetworkId() {
		return senderNetworkId;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setSenderNetworkId(java.lang.String)
	 */
	@Override
	public void setSenderNetworkId(String senderNetworkId) {
		this.senderNetworkId = senderNetworkId;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getSignalStrength()
	 */
	@Override
	public int getSignalStrength() {
		return signalStrength;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setSignalStrength(int)
	 */
	@Override
	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getTemperature()
	 */
	@Override
	public int getTemperature() {
		return temperature;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setTemperature(int)
	 */
	@Override
	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getVoltage()
	 */
	@Override
	public int getVoltage() {
		return voltage;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setVoltage(int)
	 */
	@Override
	public void setVoltage(int voltage) {
		this.voltage = voltage;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getKeyPressTimestamp()
	 */
	@Override
	public int getKeyPressTimestamp() {
		return keyPressTimestamp;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#setKeyPressTimestamp(int)
	 */
	@Override
	public void setKeyPressTimestamp(int keyPressTimestamp) {
		this.keyPressTimestamp = keyPressTimestamp;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getAccX()
	 */
	@Override
	public double getAccX() {
		return accX;
	}
	
	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getAccY()
	 */
	@Override
	public double getAccY() {
		return accY;
	}

	/* (non-Javadoc)
	 * @see com.dexels.fps.packet.impl.Packet#getAccZ()
	 */
	@Override
	public double getAccZ() {
		return accZ;
	}

	
}
