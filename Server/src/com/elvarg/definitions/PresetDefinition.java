package com.elvarg.definitions;

import java.security.InvalidParameterException;

import com.elvarg.GameConstants;
import com.elvarg.util.JsonLoader;
import com.elvarg.world.content.Presetables;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.MagicSpellbook;
import com.elvarg.world.model.Presetable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Represents Preset Definitions.
 * @author Professor Oak
 */
public class PresetDefinition {
	
	public static int PRESETS_LOADED;
	
	public static JsonLoader parse() {
		PRESETS_LOADED = 0;
		return new JsonLoader() {
			@Override
			public void load(JsonObject reader, Gson builder) {
				String name = reader.get("name").getAsString();
				String spellbook = reader.get("spellbook").getAsString();
				int[] stats = builder.fromJson(reader.get("stats").getAsJsonArray(), int[].class);
				Item[] inventory = builder.fromJson(reader.get("inventory").getAsJsonArray(), Item[].class);
				Item[] equipment = builder.fromJson(reader.get("equipment").getAsJsonArray(), Item[].class);
				
				if(inventory.length > 29) {
					throw new InvalidParameterException("Preset "+name+" has too many inventory items. Max is 28!");
				} 
				
				if(equipment.length > 14) {
					throw new InvalidParameterException("Preset "+name+" has too many equipment items. Max is 14!");
				}
				
				Presetables.GLOBAL_PRESETS[PRESETS_LOADED] = new Presetable(name, PRESETS_LOADED, inventory, equipment, stats, MagicSpellbook.valueOf(spellbook), true);
				PRESETS_LOADED++;
			}

			@Override
			public String filePath() {
				return GameConstants.DEFINITIONS_DIRECTORY + "presets.json";
			}
		};
	}
}
