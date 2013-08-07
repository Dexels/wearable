package com.dexels.wearable.driver;

import java.io.IOException;

public interface DataProvider {
    public int sendFeatureReport(byte[] data) throws IOException;
    
    /** 
     * Get a Feature Report from a HID device.
     * @param buf a buffer to put the read data into
     * @return the actual number of bytes read and  -1 on error
     * @throws IOException
     */
    public int getFeatureReport(byte[] buf) throws IOException;
   
}
