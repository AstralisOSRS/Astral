package com.runescape.cache.def;

import com.runescape.Client;
import com.runescape.cache.FileArchive;
import com.runescape.cache.anim.Frame;
import com.runescape.cache.config.VariableBits;
import com.runescape.collection.ReferenceCache;
import com.runescape.entity.model.Model;
import com.runescape.io.Buffer;

/**
 * Refactored reference:
 * http://www.rune-server.org/runescape-development/rs2-client/downloads/575183-almost-fully-refactored-317-client.html
 */
public final class NpcDefinition {

	public int turn90CCWAnimIndex;
	public static int anInt56;
	public int varBitID;
	public int turn180AnimIndex;
	public int settingId;
	public static Buffer dataBuf;
	public int combatLevel;
	public final int anInt64;
	public String name;
	public String actions[];
	public int walkAnim;
	public int size;
	public int[] recolourTarget;
	public static int[] offsets;
	public int[] aditionalModels;
	public int headIcon;
	public int[] recolourOriginal;
	public int standAnim;
	public long interfaceType;
	public int degreesToTurn;
	public static NpcDefinition[] cache;
	public static Client clientInstance;
	public int turn90CWAnimIndex;
	public boolean clickable;
	public int lightModifier;
	public int scaleY;
	public boolean drawMinimapDot;
	public int childrenIDs[];
	public byte description[];
	public int scaleXZ;
	public int shadowModifier;
	public boolean priorityRender;
	public int[] modelId;
	public static ReferenceCache modelCache = new ReferenceCache(30);

	public int id;

	private static final String PETS [][] = {
			{"318", "Dark Core"}, {"495", "Venenatis Spiderling"},
			{"497", "Callisto Cub"}, {"964", "Hellpuppy"},
			{"2055", "Chaos Elemental Jr."}, {"2130", "Snakeling"},
			{"2131", "Magma Snakeling"}, {"2132", "Tanzanite Snakeling"},
			{"5536", "Vet'ion"}, {"5537", "Vet'ion Reborn"},
			{"5561", "Scorpias' Offspring"}, {"5884", "Abyssal Orphan"},
			{"5892", "TzRek-Jad"}, {"6628", "Dagganoth Supreme Jr."},
			{"6629", "Dagganoth Prime Jr."}, {"6630", "Dagganoth Rex Jr."},
			{"6631", "Chick'arra"}, {"6632", "General Awwdor"},
			{"6633", "Commander Miniana"}, {"6634", "K'ril Tinyroth"},
			{"6635", "Baby Mole"}, {"6636", "Prince Black Dragon"},
			{"6637", "Kalphite Princess"}, {"6638", "Kalphite Princess"},
			{"6639", "Smoke Devil"}, {"6640", "Baby Kraken"},
			{"6642", "Penance Princess"}, {"7520", "Olmlet"},

			{"6715", "Heron"}, {"6717", "Beaver"}, {"6718", "Red Chinchompa"},
			{"6719", "Grey Chinchompa"}, {"6720", "Black Chimchompa"}, {"6723", "Rock Golem"},
			{"7334", "Giant Squirrel"}, {"7335", "Tangleroot"}, {"7336", "Rocky"},

			{"7337", "Fire Rift Guardian"}, {"7338", "Air Rift Guardian"},
			{"7339", "Mind Rift Guardian"}, {"7340", "Water Rift Guardian"},
			{"7341", "Earth Rift Guardian"}, {"7342", "Body Rift Guardian"},
			{"7343", "Cosmic Rift Guardian"}, {"7344", "Chaos Rift Guardian"},
			{"7345", "Nature Rift Guardian"}, {"7346", "Law Rift Guardian"},
			{"7347", "Death Rift Guardian"}, {"7348", "Soul Rift Guardian"},
			{"7349", "Astral Rift Guardian"}, {"7350", "Blood Rift Guardian"}
	};

	/**
	 * Lookup an NpcDefinition by its id
	 * @param id
	 */
	public static NpcDefinition lookup(int id) {
		for (int index = 0; index < 20; index++)
			if (cache[index].interfaceType == (long) id)
				return cache[index];

		anInt56 = (anInt56 + 1) % 20;
		NpcDefinition definition = cache[anInt56] = new NpcDefinition();
		dataBuf.currentPosition = offsets[id];
		definition.interfaceType = id;
		definition.id = id;
		definition.readValues(dataBuf);

		for (int i = 0; i < PETS.length; i++) {
			if (id == Integer.parseInt(PETS[i][0])) {
				definition.fixSlide();
				definition.name = PETS[i][1];
			}
		}
		if (id == Integer.parseInt(PETS[22][0]) || id == Integer.parseInt(PETS[23][0])) {
			definition.actions[2] = "Pick-up";
			definition.actions[3] = "Metamorphosis";
		}
		if (id == Integer.parseInt(PETS[8][0]) || id == Integer.parseInt(PETS[9][0]) 
				|| id >= Integer.parseInt(PETS[37][0]) && id <= Integer.parseInt(PETS[50][0]) 
				|| id >= Integer.parseInt(PETS[30][0]) && id <= Integer.parseInt(PETS[32][0])) {
			definition.actions[3] = "Metamorphosis";
		}

		switch(id) {
		//Pets
		case 497: //Callisto pet
			definition.scaleXZ = 45;
			definition.size = 2;
			break;
		case 6609: //Callisto
			definition.size = 4;
			break;
		case 995:
			definition.recolourOriginal = new int[2];
			definition.recolourTarget = new int[2];
			definition.recolourOriginal[0] = 528;
			definition.recolourTarget[0] = 926;
			break;
		case 7456:
			definition.actions = new String[] {"Repairs", null, null, null, null, null, null};
			break;
		case 1274:
			definition.combatLevel = 35;
			break;
		case 2660:
			definition.combatLevel = 0;
			definition.actions = new String[] {"Trade", null, null, null, null, null, null};
			definition.name = "Pker";
			break;
		case 6477:
			definition.combatLevel = 210;
			break;
		case 6471:
			definition.combatLevel = 131;
			break;
		case 5816:
			definition.combatLevel = 38;
			break;
		case 100:
			definition.drawMinimapDot = true;
			break;
		case 1306:
			definition.actions = new String[] {"Make-over", null, null, null, null, null, null};
			break;
		case 3309:
			definition.name = "Mage";
			definition.actions = new String[] {"Trade", null, "Equipment", "Runes", null, null, null};
			break;
		case 1158:
			definition.name = "@or1@Maxed bot";
			definition.combatLevel = 126;
			definition.actions = new String[] {null, "Attack", null, null, null, null, null};
			definition.modelId[5] = 268; //platelegs rune
			definition.modelId[0] = 18954; //Str cape
			definition.modelId[1] = 21873; //Head - neitznot
			definition.modelId[8] = 15413; //Shield rune defender
			definition.modelId[7] = 5409; // weapon whip
			definition.modelId[4] = 13307; //Gloves barrows
			definition.modelId[6] = 3704; // boots climbing
			definition.modelId[9] = 290; //amulet glory			
			break;
		case 1200:
			definition.copy(lookup(1158));
			definition.modelId[7] = 539; // weapon dds
			break;
		case 4096:
			definition.name = "@or1@Archer bot";
			definition.combatLevel = 90;
			definition.actions = new String[] {null, "Attack", null, null, null, null, null};
			definition.modelId[0] = 20423; //cape avas
			definition.modelId[1] = 21873; //Head - neitznot
			definition.modelId[7] = 31237; // weapon crossbow
			definition.modelId[4] = 13307; //Gloves barrows
			definition.modelId[6] = 3704; // boots climbing
			definition.modelId[5] = 20139; //platelegs zammy hides
			definition.modelId[2] = 20157; //platebody zammy hides
			definition.standAnim = 7220;
			definition.walkAnim = 7223;
			definition.turn180AnimIndex = 7220;
			definition.turn90CCWAnimIndex = 7220;
			definition.turn90CWAnimIndex = 7220;
			break;
		case 1576:
			definition.actions = new String[] {"Trade", null, "Equipment", "Ammunition", null, null, null};
			break;
		case 3343:
			definition.actions = new String[] {"Trade", null, "Heal", null, null, null, null};
			break;
		case 506:
		case 526:
			definition.actions = new String[] {"Trade", null, null, null, null, null, null};
			break;
		case 315:
			definition.actions = new String[] {"Talk-to", null, "Trade", "Sell Emblems", "Request Skull", null, null};
			break;


		}
		return definition;
	}

	private void copy(NpcDefinition copy) {
		size = copy.size;
		degreesToTurn = copy.degreesToTurn;
		walkAnim = copy.walkAnim;
		turn180AnimIndex = copy.turn180AnimIndex;
		turn90CWAnimIndex = copy.turn90CWAnimIndex;
		turn90CCWAnimIndex = copy.turn90CCWAnimIndex;
		varBitID = copy.varBitID;
		settingId = copy.settingId;
		combatLevel = copy.combatLevel;
		name = copy.name;
		description = copy.description;
		headIcon = copy.headIcon;
		clickable = copy.clickable;
		lightModifier = copy.lightModifier;
		scaleY = copy.scaleY;
		scaleXZ = copy.scaleXZ;
		drawMinimapDot = copy.drawMinimapDot;
		shadowModifier = copy.shadowModifier;
		actions = new String[copy.actions.length];
		for(int i = 0; i < actions.length; i++) {
			actions[i] = copy.actions[i];
		}
		modelId = new int[copy.modelId.length];
		for(int i = 0; i < modelId.length; i++) {
			modelId[i] = copy.modelId[i];
		}
		priorityRender = copy.priorityRender;
	}

	private void fixSlide() {
		//Fix "slide" anim issue
		turn180AnimIndex = walkAnim;
		turn90CCWAnimIndex = walkAnim;
		turn90CWAnimIndex = walkAnim;
	}

	public Model model() {
		if (childrenIDs != null) {
			NpcDefinition entityDef = morph();
			if (entityDef == null)
				return null;
			else
				return entityDef.model();
		}
		if (aditionalModels == null)
			return null;
		boolean flag1 = false;
		for (int index = 0; index < aditionalModels.length; index++)
			if (!Model.isCached(aditionalModels[index]))
				flag1 = true;

		if (flag1)
			return null;
		Model models[] = new Model[aditionalModels.length];
		for (int index = 0; index < aditionalModels.length; index++)
			models[index] = Model.getModel(aditionalModels[index]);

		Model model;
		if (models.length == 1)
			model = models[0];
		else
			model = new Model(models.length, models);
		if (recolourOriginal != null) {
			for (int index = 0; index < recolourOriginal.length; index++)
				model.recolor(recolourOriginal[index], recolourTarget[index]);

		}
		return model;
	}

	public NpcDefinition morph() {
		int child = -1;
		if (varBitID != -1) {
			VariableBits varBit = VariableBits.varbits[varBitID];
			int variable = varBit.getSetting();
			int low = varBit.getLow();
			int high = varBit.getHigh();
			int mask = Client.BIT_MASKS[high - low];
			child = clientInstance.settings[variable] >> low & mask;
		} else if (settingId != -1)
			child = clientInstance.settings[settingId];
		if (child < 0 || child >= childrenIDs.length
				|| childrenIDs[child] == -1)
			return null;
		else
			return lookup(childrenIDs[child]);
	}

	public static void init(FileArchive archive) {		
		dataBuf = new Buffer(archive.readFile("npc.dat"));		
		Buffer idxBuf = new Buffer(archive.readFile("npc.idx"));		

		int size = idxBuf.readUShort() + 21;	

		offsets = new int[size + 50000];
		
		int offset = 2;

		for (int count = 0; count < size - 21; count++) {
			offsets[count] = offset;
			offset += idxBuf.readUShort();
		}

		cache = new NpcDefinition[20];

		for (int count = 0; count < 20; count++) {
			cache[count] = new NpcDefinition();
		}		

		System.out.println("Loaded: " + size + " Npcs");
	}

	public static void clear() {
		modelCache = null;
		offsets = null;
		cache = null;
		dataBuf = null;
	}

	public Model method164(int j, int frame, int ai[], int nextFrame, int cycle1,
			int cycle2) {
		if (childrenIDs != null) {
			NpcDefinition entityDef = morph();
			if (entityDef == null)
				return null;
			else
				return entityDef.method164(j, frame, ai, nextFrame, cycle1, cycle2);
		}
		Model model = (Model) modelCache.get(interfaceType);
		if (model == null) {
			boolean flag = false;
			for (int i1 = 0; i1 < modelId.length; i1++)
				if (!Model.isCached(modelId[i1]))
					flag = true;

			if (flag)
				return null;
			Model models[] = new Model[modelId.length];
			for (int j1 = 0; j1 < modelId.length; j1++)
				models[j1] = Model.getModel(modelId[j1]);

			if (models.length == 1)
				model = models[0];
			else
				model = new Model(models.length, models);
			if (recolourOriginal != null) {
				for (int k1 = 0; k1 < recolourOriginal.length; k1++)
					model.recolor(recolourOriginal[k1], recolourTarget[k1]);

			}
			model.skin();
			model.scale(132, 132, 132);
			model.light(84 + lightModifier, 1000 + shadowModifier, -90, -580,
					-90, true);
			modelCache.put(model, interfaceType);
		}
		Model empty = Model.EMPTY_MODEL;
		empty.method464(model,
				Frame.noAnimationInProgress(frame) & Frame.noAnimationInProgress(j)
				& Frame.noAnimationInProgress(nextFrame));
		if (frame != -1 && j != -1)
			empty.applyAnimationFrames(ai, j, frame);
		else if (frame != -1 && nextFrame != -1)
			empty.applyAnimationFrame(frame, nextFrame, cycle1, cycle2);
		else if (frame != -1)
			empty.applyTransform(frame);
		if (scaleXZ != 128 || scaleY != 128)
			empty.scale(scaleXZ, scaleXZ, scaleY);
		empty.calculateDistances();
		empty.faceGroups = null;
		empty.vertexGroups = null;
		if (size == 1)
			empty.fits_on_single_square = true;
		return empty;
	}

	public Model getAnimatedModel(int primaryFrame, int secondaryFrame,
			int interleaveOrder[]) {
		if (childrenIDs != null) {
			NpcDefinition definition = morph();
			if (definition == null)
				return null;
			else
				return definition.getAnimatedModel(primaryFrame,
						secondaryFrame, interleaveOrder);
		}
		Model model = (Model) modelCache.get(interfaceType);
		if (model == null) {
			boolean flag = false;
			for (int index = 0; index < modelId.length; index++)
				if (!Model.isCached(modelId[index]))
					flag = true;
			if (flag) {
				return null;
			}
			Model models[] = new Model[modelId.length];
			for (int index = 0; index < modelId.length; index++)
				models[index] = Model.getModel(modelId[index]);

			if (models.length == 1)
				model = models[0];
			else
				model = new Model(models.length, models);
			if (recolourOriginal != null) {
				for (int index = 0; index < recolourOriginal.length; index++)
					model.recolor(recolourOriginal[index],
							recolourTarget[index]);

			}
			model.skin();
			model.light(64 + lightModifier, 850 + shadowModifier, -30, -50,
					-30, true);
			modelCache.put(model, interfaceType);
		}
		Model model_1 = Model.EMPTY_MODEL;
		model_1.method464(model, Frame.noAnimationInProgress(secondaryFrame)
				& Frame.noAnimationInProgress(primaryFrame));
		if (secondaryFrame != -1 && primaryFrame != -1)
			model_1.applyAnimationFrames(interleaveOrder, primaryFrame, secondaryFrame);
		else if (secondaryFrame != -1)
			model_1.applyTransform(secondaryFrame);
		if (scaleXZ != 128 || scaleY != 128)
			model_1.scale(scaleXZ, scaleXZ, scaleY);
		model_1.calculateDistances();
		model_1.faceGroups = null;
		model_1.vertexGroups = null;
		if (size == 1)
			model_1.fits_on_single_square = true;
		return model_1;
	}

	public void readValues(Buffer stream) {
		do {
			int opCode = stream.readUnsignedByte();
			if (opCode == 0)
				return;
			if (opCode == 1) {
				int j = stream.readUnsignedByte();
				modelId = new int[j];
				for (int j1 = 0; j1 < j; j1++)
					modelId[j1] = stream.readUShort();

			} else if (opCode == 2)
				name = stream.readString();
			else if (opCode == 3)//always keep this
				description = stream.readBytes();
			else if (opCode == 12)
				size = stream.readUnsignedByte();
			else if (opCode == 13)
				standAnim = stream.readUShort();
			else if (opCode == 14)
				walkAnim = stream.readUShort();
			else if(opCode == 15)
				stream.readUShort();
			else if(opCode == 16)
				stream.readUShort();
			//else if (opCode == 91)//15 / 16 are new for this
			//stream.readUShort();
		//else if (opCode == 92)
			//stream.readUShort();
			else if (opCode == 17) {
				walkAnim = stream.readUShort();
				turn180AnimIndex = stream.readUShort();
				turn90CWAnimIndex = stream.readUShort();
				turn90CCWAnimIndex = stream.readUShort();
			} else if (opCode >= 30 && opCode < 35) {
				if (actions == null)
					actions = new String[5];
				actions[opCode - 30] = stream.readString();
				if (actions[opCode - 30].equalsIgnoreCase("hidden"))
					actions[opCode - 30] = null;
			} else if (opCode == 40) {
				int colours = stream.readUnsignedByte();
				recolourOriginal = new int[colours];
				recolourTarget = new int[colours];
				for (int k1 = 0; k1 < colours; k1++) {
					recolourOriginal[k1] = stream.readUShort();
					recolourTarget[k1] = stream.readUShort();
				}

			} else if (opCode == 41) {
	            int i = stream.readUnsignedByte();//paramclass194.method3537();//unsignedbyte = int
	            short[] field3723 = new short[i];
	            short[] field3724 = new short[i];
	            for (int j = 0; j < i; j++)
	            {
	              field3723[j] = ((short)stream.readUShort());
	              field3724[j] = ((short)stream.readUShort());
	            }
	        } else if (opCode == 60) {
				int additionalModelLen = stream.readUnsignedByte();
				aditionalModels = new int[additionalModelLen];
				for (int l1 = 0; l1 < additionalModelLen; l1++)
					aditionalModels[l1] = stream.readUShort();

			} //else if (opCode == 90)
				//stream.readUShort();
			
			else if (opCode == 93)
				drawMinimapDot = false;
			else if (opCode == 95)
				combatLevel = stream.readUShort();
			else if (opCode == 97)
				scaleXZ = stream.readUShort();
			else if (opCode == 98)
				scaleY = stream.readUShort();
			else if (opCode == 99)
				priorityRender = true;
			else if (opCode == 100)
				lightModifier = stream.readSignedByte();
			else if (opCode == 101)
				shadowModifier = stream.readSignedByte() * 5;
			else if (opCode == 102)
				headIcon = stream.readUShort();
			else if (opCode == 103)
				degreesToTurn = stream.readUShort();
			else if (opCode == 106) {
				varBitID = stream.readUShort();
				if (varBitID == 65535)
					varBitID = -1;
				settingId = stream.readUShort();
				if (settingId == 65535)
					settingId = -1;
				int childCount = stream.readUnsignedByte();
				childrenIDs = new int[childCount + 1];
				for (int i2 = 0; i2 <= childCount; i2++) {
					childrenIDs[i2] = stream.readUShort();
					if (childrenIDs[i2] == 65535)
						childrenIDs[i2] = -1;
				}

			} else if (opCode == 107)
				clickable = false;
		} while (true);
	}

	public NpcDefinition() {
		turn90CCWAnimIndex = -1;
		varBitID = -1;
		turn180AnimIndex = -1;
		settingId = -1;
		combatLevel = -1;
		anInt64 = 1834;
		walkAnim = -1;
		size = 1;
		headIcon = -1;
		standAnim = -1;
		interfaceType = -1L;
		degreesToTurn = 32;
		turn90CWAnimIndex = -1;
		clickable = true;
		scaleY = 128;
		drawMinimapDot = true;
		scaleXZ = 128;
		priorityRender = false;
	}
}
