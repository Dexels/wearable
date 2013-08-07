package com.dexels.data;

public class RSSI {
	private int signalStrength = -1;
	private int timestamp = -1;
	
	public RSSI(int signalStrength, int timestamp){
		this.setSignalStrength(signalStrength);
		this.setTimestamp(timestamp);
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}
	
	
}
