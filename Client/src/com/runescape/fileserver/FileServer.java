package com.runescape.fileserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.runescape.Client;
import com.runescape.Configuration;
import com.runescape.fileserver.request.Request;
import com.runescape.fileserver.request.impl.RequestCRC;
import com.runescape.fileserver.request.impl.RequestPreload;

public class FileServer extends FileserverConstants {

	/**
	 * This array holds the names of all custom preloadable files.
	 * These files wil
	 */
	public static final String[] CUSTOM_PRELOAD_FILES = {
			"sprites.idx", "sprites.dat", 
			"obj.idx", "obj.dat"
	};

	/**
	 * The opcode for requesting a regular file
	 * from the file-server.
	 */
	public static final byte REGULAR_FILE_OPCODE = 0;
	
	/**
	 * The opcode for requesting a game content file
	 * from the file-server.
	 */
	public static final byte INGAME_FILE_OPCODE = 1;


	public void start() {
		List<Request> requests = new ArrayList<Request>();
		
		//First request crcs
		requests.add(new RequestCRC());
		
		//Then request preload files
		for(int i = 0; i < CUSTOM_PRELOAD_FILES.length; i++) {
			requests.add(new RequestPreload(CUSTOM_PRELOAD_FILES[i], Client.TOTAL_ARCHIVE_CRCS + i));
		}
		
		for(Request request : requests) {
			while(!request.loaded()) {
				try {
					request.load();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public byte[] request(byte opcode, String file) throws Exception {
		byte[] filePathB = file.getBytes();
		byte[] payload = new byte[1 + filePathB.length];

		//put opcode
		payload[0] = opcode;

		//Put the file
		for(int i = 0; i < filePathB.length; i++) {
			payload[i + 1] = filePathB[i];
		}
		
		return request(payload);
	}

	private byte[] request(byte[] request) throws Exception {		
		//Create a new socket
		DatagramSocket socket = new DatagramSocket();
		//socket.setSoTimeout(2000);
		
		//Create new incoming packet
		final byte[] inData = new byte[10000000]; //10mb is currently max file size to read
		final DatagramPacket in = new DatagramPacket(inData, inData.length);
		
		//Create new outgoing packet
		final DatagramPacket out = new DatagramPacket(request, request.length, InetAddress.getByName(Configuration.FILE_SERVER_ADDRESS), Configuration.FILE_SERVER_PORT);

		//Send outgoing data
		socket.send(out);

		//Receive response data
		socket.receive(out);
		byte[] buf = new byte[in.getLength()];
		for(int i = 0; i < buf.length; i++) {
			buf[i] = in.getData()[i];
		}
		System.out.println("Received file with size: "+buf.length);

		//Close socket
		socket.close();
		
		return buf;
	}
}
