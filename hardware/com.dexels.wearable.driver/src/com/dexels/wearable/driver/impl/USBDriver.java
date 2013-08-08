package com.dexels.wearable.driver.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDDeviceNotFoundException;
import com.codeminders.hidapi.HIDManager;
import com.dexels.fps.packet.Packet;
import com.dexels.fps.packet.impl.PacketFactoryImpl;
import com.dexels.wearable.driver.DataProvider;

public class USBDriver {
	
    static final int VENDOR_ID = 4292;
    static final int PRODUCT_ID = 34128;

    private static final int BUFSIZE = 65;
    private static final long READ_UPDATE_DELAY_MS = 50L;

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	private final static Logger logger = LoggerFactory
			.getLogger(USBDriver.class);
    
    private final Set<HIDDevice> openedDevices = new HashSet<HIDDevice>();
	private HIDManager manager;
	private BundleContext bundleContext;
	private ServiceRegistration<DataProvider> registration;
	private Thread managerThread;
	private boolean running = false;
	
	public void activate(BundleContext bundleContext) throws IOException {
		System.err.println("activated ds!");
		this.bundleContext = bundleContext;
		System.loadLibrary("hidapi-jni");
    	manager = HIDManager.getInstance();
        listDevices();
        DataProvider dp = null;
		try {
	        final HIDDevice hd;
			hd = getDevice(VENDOR_ID, PRODUCT_ID);
			dp = new DataProvider() {
				
				@Override
				public int sendFeatureReport(byte[] data) throws IOException {
					return hd.sendFeatureReport(data);
				}
				
				@Override
				public int getFeatureReport(byte[] buf, int length, int reportId) throws IOException {
					buf[0] = (byte) reportId;
//					buf[1] = (byte)0x4b;
//					byte[] b = Arrays.copyOf(buf, length);
//					System.err.println("length: "+length+" Request: "+bytesToHex(b));
//					hd.getFeatureReport(buf,length);
					return hd.readTimeout(buf,1000);
//					return hd.getFeatureReport(buf,length);
				}
			};
		} catch (HIDDeviceNotFoundException e1) {
			logger.warn("No device found");
			dp = new DataProvider() {
				
				@Override
				public int sendFeatureReport(byte[] data) throws IOException {
					logger.info("Dummy data provider: send");
					return 0;
				}
				
				@Override
				public int getFeatureReport(byte[] buf, int length, int reportId) throws IOException {
					logger.info("Dummy data provider: get reportid: "+reportId);
					return 0;
				}
			};
		}
        registration = bundleContext.registerService(DataProvider.class, dp, null);
        final DataProvider dp2 = dp;
        Runnable t = new Runnable(){

			@Override
			public void run() {
				try {
					readDevice(dp2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}};
			
		running = true;
		managerThread = new Thread(t);
		managerThread.start();
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
    private void readDevice(DataProvider dev) throws IOException
    {
//    	System.err.println("starting read device");
////                dev.enableBlocking();
//                byte[] startRegistration = new byte[]{0x13,0x0d,0x03,0x01,0,0,0,0,0,0,0,0};
//                byte[] stopRegistration  = new byte[]{0x13,0x0d,0x03,0x0,0,0,0,0,0,0,0,0};
//                byte[] registerWristband  = new byte[]{0x13,0x0d,0x04,1,1,1,1,1,1,2,2,0};
//                try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}
//                dev.sendFeatureReport(startRegistration);
                
                while(running)
                {
                	
                	try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
                    byte[] buf = new byte[BUFSIZE];
                	int result = dev.getFeatureReport (buf,64,1);
                	
                	System.err.println("result: "+bytesToHex(buf));
                	if(result>0) {
                	}
//                	PacketFactoryImpl pfi = new PacketFactoryImpl();
//                	Packet p = pfi.createFactory(buf, result);
//                	System.err.println("signal: "+p.getSignalStrength());
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
		if(registration!=null) {
			registration.unregister();
			registration = null;
		}
		
		for (HIDDevice d : openedDevices) {
			try {
				d.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		running = false;
		managerThread.interrupt();
		manager.release();
		System.gc();
		bundleContext = null;
	}


	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
