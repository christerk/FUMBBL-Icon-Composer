package com.fumbbl.iconcomposer.model;

import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.dto.fumbbl.*;
import com.fumbbl.iconcomposer.model.types.Bone;
import com.fumbbl.iconcomposer.model.types.Diagram;
import com.fumbbl.iconcomposer.model.types.Position;
import com.fumbbl.iconcomposer.model.types.Skeleton;
import com.fumbbl.iconcomposer.model.types.Skin;
import com.fumbbl.iconcomposer.model.types.Slot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Callback;

public class DataLoader {
	private final APIClient apiClient;
	private final Gson gson;
	private Controller controller;

	private static final Type boneListType = new TypeToken<List<DtoBone>>() {}.getType();
	private static final Type slotListType = new TypeToken<List<DtoSlot>>() {}.getType();
	private static final Type skeletonListType = new TypeToken<List<DtoSkeleton>>() {}.getType();
	private static final Type diagramListType = new TypeToken<List<DtoDiagram>>() {}.getType();
	private static final Type rulesetListType = new TypeToken<List<DtoRuleset>>() {}.getType();
	private static final Type skinListType = new TypeToken<List<DtoSkin>>() {}.getType();

	public DataLoader(Config cfg) {
		apiClient = new APIClient(cfg.getSiteBase(), cfg.getApiBase());
		gson = new Gson();
	}

	public boolean authenticate(String clientId, String clientSecret) {
		return apiClient.authenticate(clientId, clientSecret);
	}

	public Collection<DtoSkin> getSkins(int positionId) {
		String content = apiClient.get("/iconskeleton/skins/" + positionId);
		return gson.fromJson(content, skinListType);
	}

	public Collection<DtoDiagram> getDiagrams(Skeleton skeleton) {
		String content = apiClient.get("/iconskeleton/diagrams/" + skeleton.id);
		return gson.fromJson(content, diagramListType);
	}

	public Collection<DtoSlot> getSlots(int skeletonId) {
		String content = apiClient.get("/iconskeleton/slots/" + skeletonId);
		return gson.fromJson(content, slotListType);
	}

	public Collection<Bone> getBones(int i) {
		Map<Integer,Bone> bones = new HashMap<>();
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

	public DtoPositionData getPositionData(int positionId) {
		String content = apiClient.get("/iconskeleton/list/" + positionId);
		return gson.fromJson(content, DtoPositionData.class);
	}

	public void saveSkeleton(Position position, Skeleton skeleton) {
		Runnable task = () -> {
			Map<String, String> params = new HashMap<>();
			params.put("skeletonId", Integer.toString(skeleton.id));
			params.put("name", skeleton.name);
			params.put("positionId", Integer.toString(position.id));
			params.put("perspective", skeleton.perspective.name());
			String content = apiClient.post("/iconskeleton/create", params, true);
			skeleton.id = gson.fromJson(content, Integer.class);
		};
		controller.runInBackground(task);
	}

	public void setPerspective(Position position, Skeleton skeleton)
	{
		if (skeleton == null) {
			return;
		}
		Runnable task = () -> {
			Map<String, String> params = new HashMap<>();
			params.put("skeletonId", Integer.toString(skeleton.id));
			params.put("perspective", skeleton.perspective.name());
			params.put("positionId", Integer.toString(position.id));
			String content = apiClient.post("/iconskeleton/setPerspective", params, true);
		};
		controller.runInBackground(task);
	}

	public void setPositionVariable(Position position, String variable, String value)
	{
		if (position == null) {
			return;
		}

		Runnable task = () -> {
			Map<String, String> params = new HashMap<>();
			params.put("positionId", Integer.toString(position.id));
			params.put("variable", variable);
			params.put("value", value);
			String result = apiClient.post("/iconskeleton/setPositionVariable", params, true);
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

		List<Bone> list = root.getFlattenedChildBones(new LinkedList<>());

		for (Bone b : list) {
			saveBone(b);
		}
	}

	private void saveBone(Bone bone) {
		Runnable task = () -> {
			Map<String, String> params = new HashMap<>();
			params.put("boneID", Integer.toString(bone.id));
			params.put("skeletonId", Integer.toString(bone.getSkeleton().id));
			params.put("name", bone.name);
			params.put("parentId", Integer.toString(bone.parentBone != null ? bone.parentBone.id : -1));
			params.put("x", Double.toString(bone.x));
			params.put("y", Double.toString(bone.y));
			String content = apiClient.post("/iconskeleton/setBone", params, true);
			bone.id = gson.fromJson(content, Integer.class);
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
		Runnable task = () -> {
			Map<String, String> params = new HashMap<>();
			params.put("slotId", Integer.toString(slot.id));
			params.put("skeletonId", Integer.toString(slot.getSkeleton().id));
			params.put("name", slot.name);
			params.put("boneId", Integer.toString(slot.getBone().id));
			params.put("order", Integer.toString(slot.order));
			String content = apiClient.post("/iconskeleton/setSlot", params, true);
			slot.id = gson.fromJson(content, Integer.class);
		};
		
		controller.runInBackground(task);
	}

	public void saveDiagram(Diagram diagram) {
		Runnable task = () -> {
			Map<String, String> params = new HashMap<>();
			params.put("diagramId", Integer.toString(diagram.id));
			params.put("slotId", Integer.toString(diagram.getSlot().id));
			params.put("name", diagram.name);
			params.put("x", Double.toString(diagram.x));
			params.put("y", Double.toString(diagram.y));
			params.put("width", Double.toString(diagram.width));
			params.put("height", Double.toString(diagram.height));
			String content = apiClient.post("/iconskeleton/setDiagram", params, true);
			diagram.id = gson.fromJson(content, Integer.class);
		};
		
		controller.runInBackground(task);
	}

	public void deleteSkeleton(Skeleton skeleton) {
		Runnable task = () -> apiClient.post("/iconskeleton/delete/" + skeleton.id, null, true);
		
		controller.runInBackground(task);
	}

	public void saveSkin(Position position, Skin skin) {
		Runnable task = () -> {
			Map<String, String> params = new HashMap<>();
			params.put("skinId", Integer.toString(skin.id));
			params.put("positionId", Integer.toString(position.id));
			params.put("name", skin.getName());

			String content = apiClient.post("/iconskeleton/setSkin", params, true);
			skin.id = gson.fromJson(content, Integer.class);
		};
		
		controller.runInBackground(task);
	}

	public void uploadFile(int diagramId, String fileId, byte[] image)
	{
		apiClient.uploadIconGraphic(diagramId, fileId, image);
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public void loadImage(int imageId, Callback<BufferedImage, BufferedImage> callback) {
		apiClient.loadImage(imageId, callback);
	}
}
