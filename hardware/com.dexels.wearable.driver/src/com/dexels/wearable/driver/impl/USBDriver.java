package com.dexels.wearable.driver.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

public class USBDriver {
	
    static final int VENDOR_ID = 4292;
    static final int PRODUCT_ID = 34128;

    private static final int BUFSIZE = 2048;
    private static final long READ_UPDATE_DELAY_MS = 50L;

    private final Set<HIDDevice> openedDevices = new HashSet<HIDDevice>();
	private HIDManager manager;
	
	public void activate() throws IOException {
		System.err.println("activated ds!");
        System.loadLibrary("hidapi-jni");
    	manager = HIDManager.getInstance();
        listDevices();
        final HIDDevice hd = getDevice(VENDOR_ID, PRODUCT_ID);
        Runnable t = new Runnable(){

			@Override
			public void run() {
				try {
					readDevice(hd);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}};
			
		Thread tt = new Thread(t);
		tt.start();
	}
	
	private HIDDevice getDevice(int vendor, int productid) throws IOException {
        HIDDevice dev;
        dev = manager.openById(VENDOR_ID, PRODUCT_ID, null);
        openedDevices.add(dev);
        return dev;
	}
	   /**
     * Static function to read an input report to a HID device.
	 * @throws IOException 
     */
    private void readDevice(HIDDevice dev) throws IOException
    {
    	System.err.println("starting read device");
                byte[] buf = new byte[BUFSIZE];
                dev.enableBlocking();
                while(true)
                {
                    int n = dev.read(buf);
                    for(int i=0; i<n; i++)
                    {
                        int v = buf[i];
                        if (v<0) v = v+256;
                        String hs = Integer.toHexString(v);
                        if (v<16) 
                            System.err.print("0");
                        System.err.print(hs + " ");
                    }
                    System.err.println("");
                    
                    try
                    {
                        Thread.sleep(READ_UPDATE_DELAY_MS);
                    } catch(InterruptedException e)
                    {
                        //Ignore
                        e.printStackTrace();
                    }
                }

    }
    
    /**
     * Static function to find the list of all the HID devices
     * attached to the system.
     */
    private static void listDevices()
    {
        try
        {
           
            HIDManager manager = HIDManager.getInstance();
            HIDDeviceInfo[] devs = manager.listDevices();
            System.err.println("Devices:\n\n");
            for(int i=0;i<devs.length;i++)
            {
                System.err.println(""+i+".\t"+devs[i]);
                System.err.println("---------------------------------------------\n");
            }
            System.gc();
        }
        catch(IOException e)
        {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

	
	
	public void deactivate() {
		for (HIDDevice d : openedDevices) {
			try {
				d.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		manager.release();
		System.gc();
		
	}
}
