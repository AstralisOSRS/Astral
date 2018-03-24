package com.elvarg.definitions;

import com.elvarg.GameConstants;
import com.elvarg.definitions.WeaponInterfaces.WeaponInterface;
import com.elvarg.util.JsonLoader;
import com.elvarg.world.model.EquipmentType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


/**
 * Represents an item's definition, containing
 * information about it.
 * 
 * @author Professor Oak
 */

public class ItemDefinition {

	/**
	 * The max amount of items that will be loaded.
	 */
	private static final int MAX_AMOUNT_OF_ITEMS = 21500;

	/**
	 * ItemDefinition array containing all items' definition values.
	 */
	public static final ItemDefinition[] DEFINITIONS = new ItemDefinition[MAX_AMOUNT_OF_ITEMS];

	/**
	 * Gets the item definition correspondent to the id.
	 * 
	 * @param id	The id of the item to fetch definition for.
	 * @return		definitions[id].
	 */
	public static ItemDefinition forId(int id) {
		return (id < 0 || id > DEFINITIONS.length || DEFINITIONS[id] == null) ? new ItemDefinition() : DEFINITIONS[id];
	}

	/**
	 * Loads all the item definitions using
	 * a {@link JsonLoader}.
	 * @return	The JsonLoader
	 */
	public static JsonLoader parse() {
		return new JsonLoader() {
			@Override
			public void load(JsonObject reader, Gson builder) {

				final int id = reader.get("id").getAsInt();
				final ItemDefinition definition = new ItemDefinition();
				definition.id = id;
				definition.name = reader.get("name").getAsString();
				definition.examine = reader.get("examine").getAsString();
				definition.value = reader.get("value").getAsInt();
				definition.stackable = reader.get("stackable").getAsBoolean();
				definition.noted = reader.get("noted").getAsBoolean();
				definition.noteId = reader.get("noteId").getAsInt();
				definition.tradeable = reader.get("tradeable").getAsBoolean();
				definition.sellable = reader.get("sellable").getAsBoolean();
				definition.dropable = reader.get("dropable").getAsBoolean();

				//Attributes that aren't ALWAYS defined for all items

				if(reader.has("doubleHanded")) {
					definition.doubleHanded = reader.get("doubleHanded").getAsBoolean();
				}

				if(reader.has("equipmentType")) {
					definition.equipmentType = EquipmentType.valueOf(reader.get("equipmentType").getAsString());
				}

				if(reader.has("weaponInterface")) {
					definition.weaponInterface = WeaponInterface.valueOf(reader.get("weaponInterface").getAsString());
				}

				if(reader.has("blockAnim")) {
					definition.blockAnim = reader.get("blockAnim").getAsInt();
				}

				if(reader.has("standAnim")) {
					definition.standAnim = reader.get("standAnim").getAsInt();
				}

				if(reader.has("walkAnim")) {
					definition.walkAnim = reader.get("walkAnim").getAsInt();
				}

				if(reader.has("runAnim")) {
					definition.runAnim = reader.get("runAnim").getAsInt();
				}

				if(reader.has("standTurnAnim")) {
					definition.standTurnAnim = reader.get("standTurnAnim").getAsInt();
				}

				if(reader.has("turn180Anim")) {
					definition.turn180Anim = reader.get("turn180Anim").getAsInt();
				}

				if(reader.has("turn90CWAnim")) {
					definition.turn90CWAnim = reader.get("turn90CWAnim").getAsInt();
				}

				if(reader.has("turn90CCWAnim")) {
					definition.turn90CCWAnim = reader.get("turn90CCWAnim").getAsInt();
				}

				if(reader.has("interfaceId")) {
					definition.interfaceId = reader.get("interfaceId").getAsInt();
				}

				if(reader.has("bonus")) {
					definition.bonuses = builder.fromJson(reader.get("bonus").getAsJsonArray(), double[].class);
				}

				if(reader.has("requirements")) {
					definition.requirements = builder.fromJson(reader.get("requirements").getAsJsonArray(), int[].class);
				}

				DEFINITIONS[id] = definition;
			}

			@Override
			public String filePath() {
				return GameConstants.DEFINITIONS_DIRECTORY + "items.json";
			}
		};
	}
	
	/**
	 * Attributes
	 */
	private String name = "";
	private String examine = "";

	private boolean 
	stackable, 
	tradeable, 
	sellable, 
	dropable,
	noted, doubleHanded;

	private int 
	id, value, noteId = -1,
	blockAnim = 424, 
	standAnim = 808, 
	walkAnim = 819, 
	runAnim = 824, 
	standTurnAnim = 823, 
	turn180Anim = 820, 
	turn90CWAnim = 821, 
	turn90CCWAnim = 821, 
	interfaceId;

	private double[] bonuses = new double[18];
	private int[] requirements = null;//new int[SkillManager.AMOUNT_OF_SKILLS];
	private EquipmentType equipmentType = EquipmentType.NONE;
	private WeaponInterface weaponInterface;


	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getExamine() {
		return examine;
	}

	public int getValue() {
		return value;
	}

	public boolean isStackable() {
		return stackable;
	}

	public boolean isTradeable() {
		return tradeable;
	}

	public boolean isSellable() {
		return sellable;
	}

	public boolean isDropable() {
		return dropable;
	}

	public boolean isNoted() {
		return noted;
	}

	public int getNoteId() {
		return noteId;
	}

	public boolean isDoubleHanded() {
		return doubleHanded;
	}

	public int getBlockAnim() {
		return blockAnim;
	}

	public int getStandAnim() {
		return standAnim;
	}

	public int getWalkAnim() {
		return walkAnim;
	}

	public int getRunAnim() {
		return runAnim;
	}

	public int getStandTurnAnim() {
		return standTurnAnim;
	}

	public int getTurn180Anim() {
		return turn180Anim;
	}

	public int getTurn90CWAnim() {
		return turn90CWAnim;
	}

	public int getTurn90CCWAnim() {
		return turn90CCWAnim;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public int[] getRequirements() {
		return requirements;
	}

	public double[] getBonuses() {
		return bonuses;
	}

	public EquipmentType getEquipmentType() {
		return equipmentType;
	}

	public int getEquipmentSlot() {
		return equipmentType.getSlot();
	}

	public WeaponInterface getWeaponInterface() {
		return weaponInterface;
	}
}
