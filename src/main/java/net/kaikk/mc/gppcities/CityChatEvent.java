package net.kaikk.mc.gppcities;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CityChatEvent extends Event implements Cancellable {
	private static final HandlerList handlerList = new HandlerList();
	private boolean isCancelled;
	private Player player;
	private String message;
	private City city;
	
	public CityChatEvent(City city, Player player, String message) {
		this.city = city;
		this.player = player;
		this.message = message;
	}
	
	public Player getPlayer() {
		return player;
	}

	public String getMessage() {
		return message;
	}

	public City getCity() {
		return city;
	}
	
	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

	public static HandlerList getHandlerList() {
		return handlerList;
	}
}
