package com.elvarg;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.elvarg.definitions.ItemDefinition;
import com.elvarg.definitions.NpcDefinition;
import com.elvarg.definitions.NpcDropDefinition;
import com.elvarg.definitions.NpcSpawnsDefinition;
import com.elvarg.definitions.ObjectDefinition;
import com.elvarg.definitions.PresetDefinition;
import com.elvarg.definitions.ShopDefinition;
import com.elvarg.engine.GameEngine;
import com.elvarg.engine.task.impl.CombatPoisonEffect.CombatPoisonData;
import com.elvarg.net.NetworkConstants;
import com.elvarg.net.channel.ChannelPipelineHandler;
import com.elvarg.util.PlayerPunishment;
import com.elvarg.util.ShutdownHook;
import com.elvarg.world.collision.region.RegionClipping;
import com.elvarg.world.content.clan.ClanChatManager;
import com.elvarg.world.model.dialogue.DialogueManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import fileserver.FileServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

/**
 * The starting point of Elvarg.
 * All necessary utilities are loaded
 * here.
 * 
 * @author Professor Oak
 */
public class Elvarg {

	/**
	 * The game engine, executed by {@link ScheduledExecutorService}.
	 * The game engine's cycle rate is normally 600 ms.
	 */
	private static final GameEngine engine = new GameEngine();

	/**
	 * A logic service, used for carrying out
	 * asynchronous tasks such as file-writing.
	 */
	private static final ScheduledExecutorService logicService = createLogicService();   
	
	/**
	 * Is the server currently updating?
	 */
	private static boolean updating;

	/**
	 * The main logger.
	 */
	private static final Logger logger = Logger.getLogger("Elvarg");

	public static void main(String[] params) {
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		try {
			logger.info("Initializing the game...");
			
			//Fileserver
			if(GameConstants.JAGGRAB_ENABLED) {
				FileServer.init();
			}
			
			final ExecutorService serviceLoader = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("GameLoadingThread").build());

			//DEFINITIONS
			logger.info("Loading definitions...");
			serviceLoader.execute(() -> ObjectDefinition.init());
			serviceLoader.execute(() -> RegionClipping.init());
			serviceLoader.execute(() -> ItemDefinition.parse().load());
			serviceLoader.execute(() -> ObjectDefinition.parse().load());
			serviceLoader.execute(() -> NpcDefinition.parse().load());
			serviceLoader.execute(() -> NpcSpawnsDefinition.parse().load());
			serviceLoader.execute(() -> NpcDropDefinition.parse().load());
			serviceLoader.execute(() -> ShopDefinition.parse().load());
			serviceLoader.execute(() -> DialogueManager.parse().load());
			serviceLoader.execute(() -> PresetDefinition.parse().load());
			
			//OTHERS
			serviceLoader.execute(() -> ClanChatManager.init());
			serviceLoader.execute(() -> CombatPoisonData.init());
			serviceLoader.execute(() -> PlayerPunishment.init());
			
			//Shutdown the loader
			serviceLoader.shutdown();

			//Make sure the loader is properly shut down
			if (!serviceLoader.awaitTermination(15, TimeUnit.MINUTES))
				throw new IllegalStateException("The background service load took too long!");        

			//Bind the port...
			logger.info("Binding port "+NetworkConstants.GAME_PORT+"...");
			ResourceLeakDetector.setLevel(Level.DISABLED);
			EventLoopGroup loopGroup = new NioEventLoopGroup();
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(loopGroup).channel(NioServerSocketChannel.class)
			.childHandler(new ChannelPipelineHandler()).bind(NetworkConstants.GAME_PORT).syncUninterruptibly();


			//Start the game engine using a {@link ScheduledExecutorService}
			logger.info("Starting game engine...");
			
			//Start game engine..
			Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("GameThread").build())
			.scheduleAtFixedRate(engine, 0, GameConstants.GAME_ENGINE_PROCESSING_CYCLE_RATE, TimeUnit.MILLISECONDS);
			
			logger.info("The loader has finished loading utility tasks.");
			logger.info("Elvarg is now online on port "+NetworkConstants.GAME_PORT+"!");
		} catch (Exception ex) {
			logger.log(java.util.logging.Level.SEVERE, "Could not start Elvarg! Program terminated.", ex);
			System.exit(1);
		}
	}
	
	/**
	 * Submits a task to the logic service.
	 * @param t
	 */
	public static void submit(Runnable t) {
		try {
			logicService.execute(t);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the logic service.
	 * @return
	 */
	public static ScheduledExecutorService createLogicService() {
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.setRejectedExecutionHandler(new CallerRunsPolicy());
		executor.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("LogicServiceThread").build());
		executor.setKeepAliveTime(45, TimeUnit.SECONDS);
		executor.allowCoreThreadTimeOut(true);
		return Executors.unconfigurableScheduledExecutorService(executor);
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setUpdating(boolean updating) {
		Elvarg.updating = updating;
	}

	public static boolean isUpdating() {
		return Elvarg.updating;
	}
}