package com.dexels.fps.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dexels.fps.packet.Packet;
import com.dexels.fps.usb.HIDConnection;
import com.dexels.fps.usb.PacketListener;


public class Console extends JFrame implements PacketListener, WindowListener{

	private static final long serialVersionUID = 1L;
	private long receivedPacketCount = 0;
	private HIDConnection conn = new HIDConnection();
	JPanel mainPanel = new JPanel();
	JLabel countLabel = new JLabel("Packets received: 0");
	
	public Console(){
		
		mainPanel.setPreferredSize(new Dimension(800,600));
		setSize(800,600);
		this.setTitle("FPS - Console");
		this.setContentPane(mainPanel);
		init();
		conn.addPacketListener(this);
		addWindowListener(this);
		conn.listDevices();
		conn.open();
		
	}
	
	private void init(){
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.add(countLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2), 0, 0));	
	}

	@Override
	public void packetReceived(Packet p) {
		receivedPacketCount++;
		countLabel.setText("Packets received: " + receivedPacketCount);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		conn.close();
		System.exit(0); // Force the USB connection thread to be freed
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}
	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}
