package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class VehicleRefresher extends TabFeature {

	//list of players currently in a vehicle
	private final Map<TabPlayer, Entity> playersInVehicle = new ConcurrentHashMap<>();
	
	//map of vehicles carrying players
	private final Map<Integer, List<Entity>> vehicles = new ConcurrentHashMap<>();
	
	//list of players currently on boats
	private final List<TabPlayer> playersOnBoats = new ArrayList<>();
	
	private final NameTagX feature;
		
	public VehicleRefresher(NameTagX feature) {
		super(feature.getFeatureName(), "Refreshing vehicles");
		this.feature = feature;
		addUsedPlaceholders(Arrays.asList("%vehicle%"));
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%vehicle%", 100, p -> String.valueOf(((Player)p.getPlayer()).getVehicle()));
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (feature.isPlayerDisabled(p)) return;
		Entity vehicle = ((Player)p.getPlayer()).getVehicle();
		if (playersInVehicle.containsKey(p) && vehicle == null) {
			//vehicle exit
			vehicles.remove(playersInVehicle.get(p).getEntityId());
			p.getArmorStandManager().teleport();
			playersInVehicle.remove(p);
			if (feature.isDisableOnBoats() && playersOnBoats.contains(p)) {
				playersOnBoats.remove(p);
				feature.updateTeamData(p);
			}
		}
		if (!playersInVehicle.containsKey(p) && vehicle != null) {
			//vehicle enter
			vehicles.put(vehicle.getEntityId(), getPassengers(vehicle));
			p.getArmorStandManager().respawn(); //making teleport instant instead of showing teleport animation
			playersInVehicle.put(p, vehicle);
			if (feature.isDisableOnBoats() && vehicle.getType() == EntityType.BOAT) {
				playersOnBoats.add(p);
				feature.updateTeamData(p);
			}
		}
	}

	public boolean isOnBoat(TabPlayer p) {
		return playersOnBoats.contains(p);
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		if (playersInVehicle.containsKey(disconnectedPlayer)) vehicles.remove(playersInVehicle.get(disconnectedPlayer).getEntityId());
		playersInVehicle.remove(disconnectedPlayer);
		playersOnBoats.remove(disconnectedPlayer);
	}
	
	public Map<Integer, List<Entity>> getVehicles() {
		return vehicles;
	}
	
	/**
	 * Returns list of all passengers on specified vehicle
	 * @param vehicle - vehicle to check passengers of
	 * @return list of passengers
	 */
	@SuppressWarnings("deprecation")
	public List<Entity> getPassengers(Entity vehicle){
		if (TAB.getInstance().getServerVersion().getMinorVersion() >= 11) {
			return vehicle.getPassengers();
		} else {
			if (vehicle.getPassenger() != null) {
				return Arrays.asList(vehicle.getPassenger());
			} else {
				return new ArrayList<>();
			}
		}
	}
	
	/**
	 * Loads all passengers riding this player and adds them to vehicle list
	 * @param p - player to load passengers of
	 */
	public void loadPassengers(TabPlayer p) {
		if (((Entity) p.getPlayer()).getVehicle() == null) return;
		Entity vehicle = ((Entity) p.getPlayer()).getVehicle();
		vehicles.put(vehicle.getEntityId(), getPassengers(vehicle));
	}
	
	/**
	 * Teleports armor stands of all passengers on specified vehicle
	 * @param vehicle - entity to check passengers of
	 */
	public void processPassengers(Entity vehicle) {
		for (Entity passenger : getPassengers(vehicle)) {
			if (passenger instanceof Player) {
				TAB.getInstance().getPlayer(passenger.getUniqueId()).getArmorStandManager().teleport();
			}
			processPassengers(passenger);
		}
	}

	public Map<TabPlayer, Entity> getPlayersInVehicle() {
		return playersInVehicle;
	}
}