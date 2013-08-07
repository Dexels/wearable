package com.dexels.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.dexels.fps.packet.Packet;


public class Beacon {
	private int MAX_TIME_DEVIATION = 100;     // This sets the maximum time difference between RSSI measurements
	private String beaconId;                  // in order to do determine a reliable location.
	private Health health = new Health();
	private Accelerometer accelerometer = new Accelerometer();
	private KeyPress keypress = new KeyPress();
	HashMap<Integer, RSSI> repeaterRSSI = new HashMap<Integer, RSSI>();
	ArrayList<RSSI> locationData = new ArrayList<RSSI>();
	
	public Beacon(String beaconId){
		this.setBeaconId(beaconId);
	}
	
	public void update(Packet p){
		int type = p.getPacketType();
		switch(type){
		case Packet.TYPE_1:
			setRSSI(p.getRepeaterId(), p.getSignalStrength(), p.getTimestamp());
			break;
		case Packet.TYPE_2:
			setRSSI(p.getRepeaterId(), p.getSignalStrength(), p.getTimestamp());
			setHealth(p.getVoltage(), p.getTemperature(), p.getTimestamp());
			break;
		case Packet.TYPE_3:
			setRSSI(p.getRepeaterId(), p.getSignalStrength(), p.getTimestamp());
			setKeyPress(p.getKeyPressTimestamp(), p.getTimestamp());
			break;
		case Packet.TYPE_4:
			setRSSI(p.getRepeaterId(), p.getSignalStrength(), p.getTimestamp());
			setAccelerometer(p.getAccX(), p.getAccY(), p.getAccZ(), p.getTimestamp());
			break;
		case Packet.TYPE_5:
			
			break;
		}
		
		filterRSSI();
	}
	
	private final void setAccelerometer(double x, double y, double z, int timestamp){
		accelerometer.setX(x);
		accelerometer.setY(y);
		accelerometer.setZ(z);
		accelerometer.setTimestamp(timestamp);
	}
	
	private final void setKeyPress(int kp_timestamp, int timestamp){
		keypress.setKeypressTimestamp(kp_timestamp);
		keypress.setTimestamp(timestamp);
	}
	
	private final void setHealth(int voltage, int temperature, int timestamp){
		health.setVoltage(voltage);
		health.setTemperature(temperature);
		health.setTimestamp(timestamp);
	}
	
	private final void setRSSI(int repeater, int signal, int timestamp){
		RSSI rssi = repeaterRSSI.get(repeater);
		rssi.setTimestamp(timestamp);
		rssi.setSignalStrength(signal);
	}
	
	public Accelerometer getAccelerometer(){
		return accelerometer;
	}
	
	public KeyPress getKeyPress(){
		return keypress;
	}
	
	public Health getHealth(){
		return health;
	}
	
	public RSSI getRSSI(int repeater){
		return repeaterRSSI.get(repeater);
	}

	public String getBeaconId() {
		return beaconId;
	}

	public void setBeaconId(String beaconId) {
		this.beaconId = beaconId;
	}
	
	/*
	 *  RSSI measurements for each repeater
	 *  should be within the MAX_TIME_DEVIATION parameter
	 *  of the most recent measurement. 
	 */
	
	private final void filterRSSI(){
		ArrayList<RSSI> dataset = new ArrayList<RSSI>();
		int max = 0;
		
		if(!repeaterRSSI.isEmpty()){
			Iterator<Integer> it = repeaterRSSI.keySet().iterator();
			
			while(it.hasNext()){
				RSSI cur = repeaterRSSI.get(it.next());
				if(cur.getTimestamp() > max){
					max = cur.getTimestamp();
				}
			}
			// Loop over the RSSI again to filter expired measurements
			it = repeaterRSSI.keySet().iterator();
			while(it.hasNext()){
				RSSI cur = repeaterRSSI.get(it.next());
				if((max - cur.getTimestamp()) <= MAX_TIME_DEVIATION){
					dataset.add(cur);
				}
			}
		}
		locationData = dataset;
	}
}
