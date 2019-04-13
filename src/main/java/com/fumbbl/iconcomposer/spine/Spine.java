package com.fumbbl.iconcomposer.spine;

import java.util.Collection;

public class Spine {
	public Collection<Bone> bones;
	public Collection<Slot> slots;
	public SkinCollection skins;
	
	public Spine() {
		skins = new SkinCollection();
	}
	
	public Bone getBone(int boneId) {
		if (boneId != 0) {
			for (Bone b : bones) {
				if (boneId == b.id) {
					return b;
				}
			}
		}
		return null;
	}

	public Bone getBone(String boneName) {
		if (boneName != null) {
			for (Bone b : bones) {
				if (boneName.equals(b.name)) {
					return b;
				}
			}
		}
		return null;
	}
	
	public Skin getSkin(String skinName) {
		return skins.get(skinName);
	}

	public Slot getSlot(int slotId) {
		if (slotId != 0 && slots != null) {
			for (Slot s : slots) {
				if (slotId == s.id) {
					return s;
				}
			}
		}
		return null;
	}
	
	public Slot getSlot(String slotName) {
		if (slotName != null) {
			for (Slot s : slots) {
				if (slotName.equals(s.name)) {
					return s;
				}
			}
		}
		return null;
	}

	public void clearSkins() {
		skins.clear();
	}

	public void addSkin(int id, Skin skin) {
		skin.id = id;
		skins.put(skin.name, skin);
	}

	public Skin getSkin(int skinId) {
		for(Skin data : skins.values()) {
			if (data.id == skinId) {
				return data;
			}
		}
		return null;
	}
}
