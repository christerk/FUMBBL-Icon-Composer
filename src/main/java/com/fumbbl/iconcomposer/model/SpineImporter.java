package com.fumbbl.iconcomposer.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fumbbl.iconcomposer.dto.spine.DtoAttachment;
import com.fumbbl.iconcomposer.dto.spine.DtoBone;
import com.fumbbl.iconcomposer.dto.spine.DtoSkin;
import com.fumbbl.iconcomposer.dto.spine.DtoSlot;
import com.fumbbl.iconcomposer.dto.spine.DtoSlotData;
import com.fumbbl.iconcomposer.dto.spine.DtoSpine;
import com.fumbbl.iconcomposer.model.types.Bone;
import com.fumbbl.iconcomposer.model.types.Diagram;
import com.fumbbl.iconcomposer.model.types.Skeleton;
import com.fumbbl.iconcomposer.model.types.Skin;
import com.fumbbl.iconcomposer.model.types.Slot;
import com.google.gson.Gson;

public class SpineImporter {
	private Map<String,Slot> slotMap = new HashMap<String,Slot>();
	private Map<String,Bone> boneMap = new HashMap<String,Bone>();
	private Map<String,Diagram> diagramMap = new HashMap<String,Diagram>();
	private Map<String,Skin> skinMap = new HashMap<String,Skin>();

	public SpineImporter() {
	}
	
	public void importSkeleton(String json) {
		Gson gson = new Gson();
		DtoSpine data = gson.fromJson(json, DtoSpine.class);

		// Create Skeleton instance
		Skeleton skeleton = new Skeleton();
		skeleton.setName("Imported");

		processBones(data, skeleton);
		processSlots(data, skeleton);
		processSkins(data, skeleton);
	}

	private void processSlots(DtoSpine data, Skeleton skeleton) {
		data.slots.forEach(s -> slotMap.put(s.name, s.toSlot()));
		
		int order = 1;
		for (DtoSlot s : data.slots) {
			Slot slot = slotMap.get(s.name);
			slot.order = order++;
			slot.setSkeleton(skeleton);
		}
	}

	private void processBones(DtoSpine data, Skeleton skeleton) {
		// Convert to Bone instances
		data.bones.forEach(b -> boneMap.put(b.name, b.toBone()));
		
		// Set up parent / child references
		for (DtoBone b : data.bones) {
			if (b.parent != null) {
				Bone bone = boneMap.get(b.name);
				bone.parentBone = boneMap.get(b.parent);
				bone.parentBone.addChildBone(bone);
				bone.setSkeleton(skeleton);
			}
		}
	}
	
	private void processSkins(DtoSpine spine, Skeleton skeleton) {
		for (Entry<String, DtoSkin>entry : spine.skins.entrySet()) {
			String skinName = entry.getKey();
			DtoSkin s = entry.getValue();
			if ("default".equals(skinName)) {
				continue;
			}

			Skin skin = s.toSkin();
			skin.name = skinName;
			skinMap.put(skinName, skin);
			
			for(Entry<String,DtoSlotData> slotEntry : s.entrySet()) {
				String slotName = slotEntry.getKey();
				DtoSlotData d = slotEntry.getValue();
				for (Entry<String,DtoAttachment> attachmentEntry : d.entrySet()) {
					DtoAttachment attachment = attachmentEntry.getValue();
					
					Diagram diagram = attachment.toDiagram();
					diagram.setSlot(slotMap.get(slotName));
					diagramMap.put(attachment.name, diagram);
				}
			}
		}
	}
}
