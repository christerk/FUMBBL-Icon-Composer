package com.fumbbl.iconcomposer.controllers;

import java.util.Collection;
import java.util.HashSet;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.ViewState;
import com.fumbbl.iconcomposer.model.Model;
import com.fumbbl.iconcomposer.model.Perspective;
import com.fumbbl.iconcomposer.model.types.*;
import com.fumbbl.iconcomposer.image.BaseRenderer;
import com.fumbbl.iconcomposer.ui.StageManager;
import com.fumbbl.iconcomposer.ui.StageType;

import javafx.application.Platform;
import javafx.scene.image.WritableImage;

public class Controller extends BaseController {
	private Model model;
	private BaseRenderer renderer;
	private StageManager stageManager;
	private ControllerManager controllerManager;
	public ViewState viewState;
	TaskQueue taskQueue;
	
	public Controller(Model model) {
		this.model = model;
		renderer = new BaseRenderer(model, this);
		viewState = new ViewState();
		taskQueue = new TaskQueue(this);
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}

	public void setControllerManager(ControllerManager controllerManager) {
		this.controllerManager = controllerManager;
	}

	public MainController getMainController() {
		return controllerManager.getMain();
	}
	
	public BaseRenderer getRenderer() {
		return renderer;
	}

	public StageManager getStageManager() {
		return stageManager;
	}
	
	public void runInBackground(Runnable task) {
		taskQueue.execute(task);
	}
	
	public void startProgress() {
		taskQueue.startProgress();
	}
	
	public void onProgress(double progress, boolean complete) {
		controllerManager.getMain().onProgress(progress, complete);
		if (complete) {
			controllerManager.getMain().onProgressComplete();
		}
	}
	
	public void stopProgress() {
		taskQueue.stopProgress();
	}
	
	public void shutdown() {
		taskQueue.shutdownNow();
		Platform.exit();
	}
	
	/*
	 * Change events 
	 */
	
	public void onBonesChanged(Collection<Bone> bones) {
		controllerManager.getMain().setBones(bones);
	}

	public void onSlotsChanged(Collection<Slot> slots) {
		controllerManager.getMain().setSlots(slots);
	}

	public void onSkeletonsChanged(Collection<Skeleton> skeletons) {
		controllerManager.getMain().onSkeletonsChanged(skeletons);
	}

	public void onSkeletonChanged(Skeleton skeleton) {
		//controllerManager.getMain().setSkeleton(skeleton);
	}
	
	public void onColourThemesChanged(Collection<ColourTheme> themes) {
		controllerManager.getMain().setColourThemes(themes);
	}

	public void onColourThemeChanged(ColourTheme t) {
		controllerManager.getMain().setColourTheme(t);
	}

	public void onDiagramImageChanged() {
		WritableImage image = viewState.getDiagramImage(Perspective.Front);
		controllerManager.getMain().setFrontDiagramImage(image);

		image = viewState.getDiagramImage(Perspective.Side);
		controllerManager.getMain().setSideDiagramImage(image);
	}

	public void onPreviewImageChanged() {
		WritableImage image = viewState.getPreviewImage();
		controllerManager.getMain().setPreviewImage(image);
	}

	public void onSkeletonImageChanged() {
		WritableImage image = viewState.getSkeletonImage(Perspective.Front);
		controllerManager.getMain().setFrontSkeletonImage(image);

		image = viewState.getSkeletonImage(Perspective.Side);
		controllerManager.getMain().setSideSkeletonImage(image);
	}
	public void onAuthenticateChange(boolean success) {
		controllerManager.getMain().setApiStatus(success ? "Authorized" : "Not Authorized");
	}
	
	public void onPositionsChanged(Collection<Position> positions) {
		controllerManager.getMain().onPositionsChanged(positions);
	}
	
	public void onImagesChanged(Collection<NamedImage> images) {
		controllerManager.getMain().setImages(images);
		((NewDiagramController)controllerManager.get(StageType.newDiagram)).setImages(images);
	}

	public void addImage(NamedImage newImage) {
		controllerManager.getMain().addImage(newImage);
	}

	public void onDiagramsChanged(Collection<Diagram> diagrams) {
		Collection<VirtualDiagram> virtualDiagrams = new HashSet<>();

		for (Diagram d : diagrams) {
			virtualDiagrams.add(new VirtualDiagram(d));
		}

		controllerManager.getMain().setDiagrams(virtualDiagrams);
	}

	/*
	 * ViewState updates
	 */
	
	public void setBones(Perspective perspective, Collection<Bone> bones) {
		viewState.getActiveSkeleton(perspective).setBones(bones);
	}

	public void setSlots(Perspective perspective, Collection<Slot> slots) {
		viewState.getActiveSkeleton(perspective).setSlots(slots);
	}

	public void setColourTheme(String theme) {
		viewState.setActiveColourTheme(model.getColourTheme(theme));
	}
	
	public void setSkeleton(Skeleton skeleton) {
		model.setPerspective(skeleton);
		viewState.setActiveSkeleton(skeleton);
	}

	public ColourTheme getColourTheme() {
		return viewState.getActiveColourTheme();
	}

	/*
	 * Model
	 */

	public void loadPosition(int id) {
		model.loadPosition(id);
	}

	public Collection<Ruleset> loadRulesets() {
		return model.loadRulesets();
	}

	public void loadRuleset(int id) {
		model.loadRuleset(id);
	}
	
	public void loadSkeletons(Position position) {
		model.loadSkeletons(position);
	}

	public void loadDiagrams(Perspective perspective, Skeleton skeleton) {
		model.loadDiagrams(perspective, skeleton);
	}
	
	public boolean isAuthorized() {
		return model.isAuthorized();
	}

	public Config getConfig() {
		return model.getConfig();
	}

	public Collection<Bone> loadBones(Skeleton skeleton) {
		return model.loadBones(skeleton);
	}
	
	public Collection<Slot> loadSlots(Skeleton skeleton) {
		return model.loadSlots(skeleton);
	}

	public void handleDroppedFile(String path) {
		model.handleDroppedFile(path);
	}
	
	public void authenticate() {
		model.authenticate();
	}
	
	public void loadRoster(Roster selectedItem) {
		model.loadRoster(selectedItem.id);
	}
	
	public void createDiagram(NamedImage image) {
		model.addDiagram(image);
	}
	
	public void deleteSkeleton(Skeleton skeleton) {
		model.deleteSkeleton(skeleton);
	}
	
	/*
	 * Renderer
	 */

	public void displayImage(Perspective perspective, NamedImage image) {
		controllerManager.getMain().hideColourPane();
		renderer.render(perspective, image);
		onDiagramImageChanged();
	}
	
	public void displayPreview() {
		renderer.renderPreview();
		onPreviewImageChanged();
	}

	public void displayDiagrams(String diagramName) {

		Diagram d = model.getDiagram(viewState.getActiveSkeleton(Perspective.Front).id, diagramName);
		displayDiagram(d);

		d = model.getDiagram(viewState.getActiveSkeleton(Perspective.Side).id, diagramName);
		displayDiagram(d);
	}

	public void displayDiagram(Diagram d) {
		if (d == null) {
			return;
		}
		viewState.setActiveDiagram(d.perspective, d);
		controllerManager.getMain().showColourPane();

		renderer.renderDiagram(d.perspective, d);

		Slot slot = d.getSlot();
		
		controllerManager.getMain().setSlotInfo(d.perspective, slot, d.x, d.y);

		renderer.renderCursor(d.perspective, d.x, d.y);
		onDiagramImageChanged();
	}

	public void displayBones(String value) {
		controllerManager.getMain().hideColourPane();
		renderer.renderSkeleton(Perspective.Front, viewState.getActiveSkeleton(Perspective.Front), value);
		renderer.renderSkeleton(Perspective.Side, viewState.getActiveSkeleton(Perspective.Side), value);
		onSkeletonImageChanged();
	}
	
	public void onRulesetLoaded(Ruleset ruleset) {
		((OpenRosterController)controllerManager.get(StageType.openRoster)).onRulesetLoaded(ruleset);
	}

	public void onPositionChanged(Position position) {
		controllerManager.getMain().onPositionChanged(position);
	}

	public void onProgressStart(String description) {
		controllerManager.getMain().onProgressStart(description);
	}

	public void onItemRenamed(NamedItem item, String oldName) {
		model.onItemRenamed(item, oldName);
	}

	public void startBatch() {
		taskQueue.startBatch();
	}

	public void runBatch() {
		taskQueue.runBatch();
	}

	public void clearDiagrams() {
		model.clearDiagrams();
	}

	public Collection<NamedImage> getImagesForDiagram(VirtualDiagram d) {
		return model.getImagesForDiagram(d);
	}

	public void setPositionColour(Position p, ColourTheme.ColourType type, String rgb) {
		model.setPositionColour(p, type, rgb);
	}

	public Diagram getDiagram(int skeletonId, String diagramName) {
		return model.getDiagram(skeletonId, diagramName);
	}
}
