package com.dexels.fps.usb;

import java.io.IOException;
import java.util.ArrayList;

import com.codeminders.hidapi.ClassPathLibraryLoader;
import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;
import com.dexels.fps.packet.Packet;
import com.dexels.fps.packet.impl.PacketImpl;

public class HIDConnection implements Runnable {
	// FPS receiver
	private final static int VENDOR_ID = 4292;
	private final static int PRODUCT_ID = 34128;
	private final String SERIAL = "FFFFFFFF";
	
	//int VENDOR_ID = 1133;
	//int PRODUCT_ID = 49174;
	
	// Apple KB or trackpad
	//private final static int VENDOR_ID = 1452;
	//private final static int PRODUCT_ID = 567;
	
	private Thread t;
	private final static int BUFFER_SIZE = 64;
	private boolean running = false;
	private HIDDevice device;
	private HIDManager hid_manager;
	private ArrayList<PacketListener> listeners = new ArrayList<PacketListener>();
	
	public boolean debug = true;

	public HIDConnection() {
		try {
			ClassPathLibraryLoader.loadNativeHIDLibrary();
			hid_manager = HIDManager.getInstance();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addPacketListener(PacketListener listener){
		if(!listeners.contains(listener)){
			listeners.add(listener);
		}
	}

	public void open() {
		try {
			device = hid_manager.openById(VENDOR_ID, PRODUCT_ID, SERIAL);
			if (device != null) {
				System.err.println("Connected to device: " + device.getManufacturerString() + ": " + device.getProductString() );
				getFeatureReport();
//				byte[] fr = new byte[12];
//				fr[0] = 0x13;
//				fr[1] = 0x0D;
//				fr[2] = 0x03;
//				fr[3] = 0x01;
//				fr[4] = 0x00;
//				fr[5] = 0x00;
//				fr[6] = 0x00;
//				fr[7] = 0x00;
//				fr[8] = 0x00;
//				fr[9] = 0x00;
//				fr[10] = 0x00;
//				fr[11] = 0x00;
//				//System.err.println("Writing data: " + device.write(fr));
				if(t == null){
					t = new Thread(this);
				}
				running = true;
				t.start();
			}
		} catch (Exception e) {
			try{
				hid_manager.release();
			}catch(Exception ex){
				ex.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	private void getFeatureReport(){
		try{
			byte[] buffer = new byte[2];
			buffer[0] = 0;
			int read = device.getFeatureReport(buffer);
			System.err.println("Read:  "+ read);
			if(read > -1){
				for(int i=0;i<read;i++){
					System.err.println("b[" + i + "]: " + buffer[i]);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void startCapture() {
		try {
			System.err.println("Reading data from device");
			byte[] buf = new byte[BUFFER_SIZE];
			device.enableBlocking();
			while (running) {
				System.err.println("Reading");
				int n = device.read(buf);
				System.err.println("got data from device");
				
				Packet packet = new PacketImpl(buf, n);
				packetReceived(packet);
				
				if(debug){
					for (int i = 0; i < n; i++) {
						int v = buf[i];
						if (v < 0)
							v = v + 256;
						String hs = Integer.toHexString(v);
						if (v < 16)
							System.err.print("0");
						System.err.print(hs + " ");
					}
					System.err.println("");
				}
			}
			device.close();
			hid_manager.release();
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private final void packetReceived(Packet p){
		for(int i=0;i<listeners.size();i++){
			listeners.get(i).packetReceived(p);
		}
	}

	public void close() {
		running = false;
		t = null;
		try{
			//device.close();
			hid_manager.release();
			System.gc();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void listDevices(){
		try {
			HIDDeviceInfo[] list = hid_manager.listDevices();
			for (int i = 0; i < list.length; i++) {
				System.err.println(list[i].toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		startCapture();
	}
}
