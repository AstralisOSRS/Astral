package com.runescape.fileserver.request.impl;

import java.net.DatagramPacket;

import com.runescape.Client;
import com.runescape.fileserver.FileServer;
import com.runescape.fileserver.request.Request;
import com.runescape.io.Buffer;

public class RequestCRC implements Request {

	public RequestCRC() {
		resetCrcs();
	}
	
	@Override
	public boolean loaded() {
		for(int i = 0; i < Client.instance.CRCs.length; i++) {
			if(Client.instance.CRCs[i] == -1) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void load() throws Exception {
		Client.instance.drawLoadingText(20, "Requesting CRCs..");
		
		//Send crc request packet and receive incoming data
		Buffer buffer = new Buffer(Client.instance.getFileServer().request(FileServer.REGULAR_FILE_OPCODE, "crc"));
		
		for (int index = 0; index < Client.instance.CRCs.length; index++) {
			Client.instance.CRCs[index] = buffer.readInt();
		}
		
		int expected = buffer.readInt();
		int calculated = 1234;
		for (int index = 0; index < Client.instance.CRCs.length; index++) {
			calculated = (calculated << 1) + Client.instance.CRCs[index];
		}

		if (expected != calculated) {
			Client.instance.drawLoadingText(20, "CRC Error! Retrying in 5 seconds.");
			resetCrcs();
			Thread.sleep(5000);
		}
	}
	
	private void resetCrcs() {
		for(int i = 0; i < Client.instance.CRCs.length; i++) {
			Client.instance.CRCs[i] = -1;
		}
	}
}
