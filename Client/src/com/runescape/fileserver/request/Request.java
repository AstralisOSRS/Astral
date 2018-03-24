package com.runescape.fileserver.request;

public interface Request {
	
	public boolean loaded();
	
	public void load() throws Exception;
}
