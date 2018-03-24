package com.runescape.fileserver.request.impl;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;

import com.runescape.Client;
import com.runescape.fileserver.FileServer;
import com.runescape.fileserver.request.Request;
import com.runescape.sign.SignLink;
import com.runescape.util.FileUtils;

public class RequestPreload implements Request {

	private final String fileName;
	private final int crcIndex;
	
	public RequestPreload(String fileName, int crcIndex) {
		this.fileName = fileName;
		this.crcIndex = crcIndex;
	}

	@Override
	public boolean loaded() {
		byte[] fileData = null;

		//Attempt to read file if it exists..
		File file = new File(SignLink.findcachedir() + fileName);
		if(file.exists() && !file.isDirectory()) {
			fileData = FileUtils.readFile(file.getAbsolutePath());
		}

		//Check if the file is "updated" by comparing crc..
		if(fileData != null) {
			if (!Client.instance.compareCrc(fileData, Client.instance.CRCs[crcIndex])) {
				fileData = null;
			}
		}

		return fileData != null;
	}

	@Override
	public void load() throws Exception {
		Client.instance.drawLoadingText(20, "Requesting "+fileName+"..");
		
		byte[] fileData = Client.instance.getFileServer().request(FileServer.REGULAR_FILE_OPCODE, "preload/" + fileName);
		
		if(fileData != null) {
			if (!Client.instance.compareCrc(fileData, Client.instance.CRCs[crcIndex])) {
			//	fileData = null;
			}
		}
		
		if(fileData != null) {
			File file = new File(SignLink.findcachedir() + fileName);
			FileUtils.writeFile(file, fileData);
		} else {
			Client.instance.drawLoadingText(20, "File Error! Retrying in 5 seconds.");
			Thread.sleep(5000);
		}
	}

}
