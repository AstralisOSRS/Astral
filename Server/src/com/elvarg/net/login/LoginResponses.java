package com.elvarg.net.login;

import com.elvarg.Elvarg;
import com.elvarg.GameConstants;
import com.elvarg.util.Misc;
import com.elvarg.util.PlayerPunishment;
import com.elvarg.world.World;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.entity.impl.player.PlayerLoading;

public final class LoginResponses {
	
	public static final int evaulate(Player player, LoginDetailsMessage msg) {
		if (World.getPlayers().isFull()) {
			return LOGIN_WORLD_FULL;
		}
		
		if(Elvarg.isUpdating()) {
			return LOGIN_GAME_UPDATE;
		}
		
		if (player.getUsername().startsWith(" ") || player.getUsername().endsWith(" ") ||
				!Misc.isValidName(player.getUsername())) {
			return INVALID_CREDENTIALS_COMBINATION;
		}
		
		if(msg.getClientVersion() != GameConstants.GAME_VERSION || msg.getUid() != GameConstants.GAME_UID) {
			return OLD_CLIENT_VERSION;
		}
		
		if(World.getPlayerByName(player.getUsername()) != null || World.getPlayerRemoveQueue().contains(player) || World.getPlayerAddQueue().contains(player)) {
			return LOGIN_ACCOUNT_ONLINE;
		}
		
		if (PlayerPunishment.banned(player.getUsername())) {
			return LoginResponses.LOGIN_DISABLED_ACCOUNT;
		}
		
		if (PlayerPunishment.IPBanned(msg.getHost())) {
			return LoginResponses.LOGIN_DISABLED_IP;
		}
		
		/** CHAR FILE LOADING **/
		int playerLoadingResponse = PlayerLoading.getResult(player);
		
		//New player?
		if(playerLoadingResponse == NEW_ACCOUNT) {
			player.setNewPlayer(true);
			playerLoadingResponse = LOGIN_SUCCESSFUL;
		}
		
		//ERROR LOADING PLAYER
		if(playerLoadingResponse != LOGIN_SUCCESSFUL) {
			return playerLoadingResponse;
		}
	
		return LOGIN_SUCCESSFUL;
	}
		
	/**
	 * This login opcode signifies a successful login.
	 */
	public static final int LOGIN_SUCCESSFUL = 2;
	
	/**
	 * This login opcode is used when the player
	 * has entered an invalid username and/or password.
	 */
	public static final int LOGIN_INVALID_CREDENTIALS = 3;
	
	/**
	 * This login opcode is used when the account
	 * has been disabled.
	 */
	public static final int LOGIN_DISABLED_ACCOUNT = 4;
	
	/**
	 * This login opcode is used when the player's IP
	 * has been disabled.
	 */
	public static final int LOGIN_DISABLED_COMPUTER = 22;
	
	/**
	 * This login opcode is used when the player's IP
	 * has been disabled.
	 */
	public static final int LOGIN_DISABLED_IP = 27;
	
	/**
	 * This login opcode is used when the account
	 * attempting to connect is already online in the server.
	 */
	public static final int LOGIN_ACCOUNT_ONLINE = 5;
	
	/**
	 * This login opcode is used when the game has been or
	 * is being updated.
	 */
	public static final int LOGIN_GAME_UPDATE = 6;
	
	/**
	 * This login opcode is used when the world being
	 * connected to is full.
	 */
	public static final int LOGIN_WORLD_FULL = 7;
		
	/**
	 * This login opcode is used when the connections
	 * from an ip address has exceeded {@link org.FileServerConstants.net.NetworkConstants.MAXIMUM_CONNECTIONS}.
	 */
	public static final int LOGIN_CONNECTION_LIMIT = 9;
	
	/**
	 * This login opcode is used when a connection
	 * has received a bad session id.
	 */
	public static final int LOGIN_BAD_SESSION_ID = 10;
	
	/**
	 * This login opcode is used when the login procedure
	 * has rejected the session.
	 */
	public static final int LOGIN_REJECT_SESSION = 11;
			
	/**
	 * This login opcode is used when a player has
	 * entered invalid credentials.
	 */
	public static final int INVALID_CREDENTIALS_COMBINATION = 28;
	
	/**
	 * This login opcode is used when a player has
	 * attempted to login with a old client.
	 */
	public static final int OLD_CLIENT_VERSION = 30;
	
	/**
	 * New account
	 */
	public static final int NEW_ACCOUNT = -1;
	
}
