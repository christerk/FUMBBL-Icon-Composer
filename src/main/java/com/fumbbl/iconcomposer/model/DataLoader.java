package com.fumbbl.iconcomposer.model;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoBone;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoDiagram;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoPosition;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoRoster;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoRuleset;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoSkeleton;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoSkin;
import com.fumbbl.iconcomposer.dto.fumbbl.DtoSlot;
import com.fumbbl.iconcomposer.model.types.Bone;
import com.fumbbl.iconcomposer.model.types.Diagram;
import com.fumbbl.iconcomposer.model.types.Position;
import com.fumbbl.iconcomposer.model.types.Skeleton;
import com.fumbbl.iconcomposer.model.types.Skin;
import com.fumbbl.iconcomposer.model.types.Slot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataLoader {
	private APIClient apiClient;
	private Gson gson;
	private Controller controller;

	private static final Type boneListType = new TypeToken<List<DtoBone>>() {}.getType();
	private static final Type slotListType = new TypeToken<List<DtoSlot>>() {}.getType();
	private static final Type skeletonListType = new TypeToken<List<DtoSkeleton>>() {}.getType();
	private static final Type diagramListType = new TypeToken<List<DtoDiagram>>() {}.getType();
	private static final Type rulesetListType = new TypeToken<List<DtoRuleset>>() {}.getType();
	private static final Type skinListType = new TypeToken<List<DtoSkin>>() {}.getType();

	public DataLoader(Config cfg) {
		apiClient = new APIClient(cfg.getApiBase());
		gson = new Gson();
	}

	public boolean authenticate(String clientId, String clientSecret) {
		return apiClient.authenticate(clientId, clientSecret);
	}

	public Collection<DtoSkin> getSkins(int positionId) {
		String content = apiClient.get("/iconskeleton/skins/" + positionId);
		return gson.fromJson(content, skinListType);
	}

	public Collection<DtoDiagram> getDiagrams(int skeletonId) {
		String content = apiClient.get("/iconskeleton/diagrams/" + skeletonId);
		return gson.fromJson(content, diagramListType);
	}

	public Collection<DtoSlot> getSlots(int skeletonId) {
		String content = apiClient.get("/iconskeleton/slots/" + skeletonId);
		return gson.fromJson(content, slotListType);
	}

	public Collection<Bone> getBones(int i) {
		Map<Integer,Bone> bones = new HashMap<Integer,Bone>();
		String content = apiClient.get("/iconskeleton/bones/" + i);
		Collection<DtoBone> list = gson.fromJson(content, boneListType);
		
		list.forEach(b -> bones.put(b.id, b.toBone()));
		
		for (DtoBone b : list) {
			Bone bone = bones.get(b.id);
			if (b.parentId > 0) {
				bone.parentBone = bones.get(b.parentId);
				bone.parentBone.addChildBone(bone);
			}
		}
		
		return bones.values();
	}

	public DtoRoster getRoster(int rosterId) {
		String content = apiClient.get("/roster/get/" + rosterId);
		return gson.fromJson(content, DtoRoster.class);
	}

	public DtoPosition getPosition(int positionId) {
		String content = apiClient.get("/position/get/" + positionId);
		return gson.fromJson(content, DtoPosition.class);
	}

	public Collection<DtoRuleset> getRulesets() {
		String content = apiClient.post("/ruleset/list", null, true);
		return gson.fromJson(content, rulesetListType);
	}

	public DtoRuleset getRuleset(int rulesetId) {
		String content = apiClient.get("/ruleset/get/" + rulesetId);
		return gson.fromJson(content, DtoRuleset.class);
	}

	public boolean isAuthenticated() {
		return apiClient.isAuthenticated();
	}

	public Collection<DtoSkeleton> getSkeletons(int positionId) {
		String content = apiClient.get("/iconskeleton/list/" + positionId);
		return gson.fromJson(content, skeletonListType);
	}

	public void saveSkeleton(Position position, Skeleton skeleton) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("skeletonId", Integer.toString(skeleton.id));
				params.put("name", skeleton.name);
				params.put("positionId", Integer.toString(position.id));
				String content = apiClient.post("/iconskeleton/create", params, true);
				int skeletonId = gson.fromJson(content, Integer.class);
				skeleton.id = skeletonId;
			}
		};
		controller.runInBackground(task);
	}

	public void saveBones(Skeleton skeleton) {
		Collection<Bone> bones = skeleton.getBones();
		if (bones == null || bones.isEmpty()) {
			return;
		}

		Bone root = bones.iterator().next();
		while (root.parentBone != null) {
			root = root.parentBone;
		}

		List<Bone> list = root.getFlattenedChildBones(new LinkedList<Bone>());

		for (Bone b : list) {
			saveBone(b);
		}
	}

	private void saveBone(Bone bone) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("boneID", Integer.toString(bone.id));
				params.put("skeletonId", Integer.toString(bone.getSkeleton().id));
				params.put("name", bone.name);
				params.put("parentId", Integer.toString(bone.parentBone != null ? bone.parentBone.id : -1));
				params.put("x", Double.toString(bone.x));
				params.put("y", Double.toString(bone.y));
				String content = apiClient.post("/iconskeleton/setBone", params, true);
				bone.id = gson.fromJson(content, Integer.class);
			}
		};
		
		controller.runInBackground(task);
	}

	public void saveSlots(Skeleton skeleton) {
		Collection<Slot> slots = skeleton.getSlots();

		for (Slot slot : slots) {
			saveSlot(slot);
		}
	}

	public void saveSlot(Slot slot) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("slotId", Integer.toString(slot.id));
				params.put("skeletonId", Integer.toString(slot.getSkeleton().id));
				params.put("name", slot.name);
				params.put("boneId", Integer.toString(slot.getBone().id));
				params.put("attachment", slot.attachment);
				params.put("order", Integer.toString(slot.order));
				String content = apiClient.post("/iconskeleton/setSlot", params, true);
				slot.id = gson.fromJson(content, Integer.class);
			}
		};
		
		controller.runInBackground(task);
	}

	public void saveDiagram(Diagram diagram) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("diagramId", Integer.toString(diagram.id));
				params.put("slotId", Integer.toString(diagram.getSlot().id));
				params.put("name", diagram.name);
				params.put("x", Double.toString(diagram.x));
				params.put("y", Double.toString(diagram.y));
				params.put("width", Double.toString(diagram.width));
				params.put("height", Double.toString(diagram.height));
				params.put("svg", diagram.getImage());
				String content = apiClient.post("/iconskeleton/setDiagram", params, true);
				diagram.id = gson.fromJson(content, Integer.class);
			}
		};
		
		controller.runInBackground(task);
	}

	public void deleteSkeleton(Skeleton skeleton) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				apiClient.post("/iconskeleton/delete/" + skeleton.id, null, true);
			}
		};
		
		controller.runInBackground(task);
	}

	public void saveSkin(Position position, Skin skin) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("skinId", Integer.toString(skin.id));
				params.put("skeletonId", Integer.toString(skin.skeleton.id));
				params.put("positionId", Integer.toString(position.id));
				params.put("name", skin.getName());
				
				String content = apiClient.post("/iconskeleton/setSkin", params, true);
				skin.id = gson.fromJson(content, Integer.class);
			}
		};
		
		controller.runInBackground(task);
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
}
