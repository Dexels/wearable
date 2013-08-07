package com.dexels.wearable.command;


import java.io.IOException;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.wearable.driver.DataProvider;


public class UsbCommands {
	
	
	private final static Logger logger = LoggerFactory
			.getLogger(UsbCommands.class);
	
	private DataProvider dataProvider = null;
    public static final byte[] startRegistration = new byte[]{0x13,0x0d,0x03,0x01,0,0,0,0,0,0,0,0};
    public static final byte[] stopRegistration  = new byte[]{0x13,0x0d,0x03,0x0,0,0,0,0,0,0,0,0};

	
	public UsbCommands() {
	}
	
	public void setDataProvider(DataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	public void clearDataProvider(DataProvider dataProvider) {
		this.dataProvider = null;
	}

	@Descriptor(value = "Switch registration mode on or off")
	public void registration(CommandSession session, @Descriptor(value = "on or off. Default is on") String param) throws IOException {
		session.getConsole().println("-------------->"+param);
		if("on".equals(param.toLowerCase())) {
			dataProvider.sendFeatureReport(startRegistration);
			return;
		}
		if("off".equals(param.toLowerCase())) {
			dataProvider.sendFeatureReport(stopRegistration);
			return;
		}
		session.getConsole().println("Incorrect value for registration: "+param);
	}
}
