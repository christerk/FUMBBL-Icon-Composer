package com.fumbbl.iconcomposer.controllers;

import java.util.Collection;

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
	private final Model model;
	private final BaseRenderer renderer;
	private StageManager stageManager;
	private ControllerManager controllerManager;
	public final ViewState viewState;

	public Controller(Model model) {
		this.model = model;
		renderer = new BaseRenderer(model, this);
		viewState = new ViewState();
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
	

	public void onProgress(double progress, boolean complete) {
		controllerManager.getMain().onProgress(progress, complete);
		if (complete) {
			controllerManager.getMain().onProgressComplete();
		}
	}
	

	/*
	 * Change events 
	 */
	
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

	/*
	 * ViewState updates
	 */
	
	public void setColourTheme(String theme) {
		viewState.setActiveColourTheme(model.getColourTheme(theme));
	}
	
	public void setSkeleton(Skeleton skeleton) {
		model.setPerspective(skeleton);
	}

	public ColourTheme getColourTheme() {
		return viewState.getActiveColourTheme();
	}

	/*
	 * Model
	 */

	public boolean isAuthorized() {
		return model.isAuthenticated();
	}

	public Config getConfig() {
		return model.getConfig();
	}

	public void handleDroppedFile(String path) {
		model.handleDroppedFile(path);
	}

	public void loadRoster(Roster selectedItem) {
		model.loadRoster(selectedItem.id);
	}
	
	public void createDiagram(VirtualSlot slot, String name) {
		if (!name.startsWith(slot.getName() + "-")) {
			name = slot.getName() + "-" + name;
		}
		model.createDiagram(slot, name);
	}
	
	/*
	 * Renderer
	 */

	public void displayPreview() {
		renderer.renderPreview();
		onPreviewImageChanged();
	}

	public void displayDiagrams(String diagramName) {
		Diagram d = model.getDiagram(model.frontSkeleton.get().id, diagramName);
		displayDiagram(d);

		d = model.getDiagram(model.sideSkeleton.get().id, diagramName);
		displayDiagram(d);
	}

	public void displayDiagram(Diagram d) {
		if (d != null) {
			//return;
			viewState.setActiveDiagram(Perspective.Front, null);
			viewState.setActiveDiagram(Perspective.Side, null);
		} else {
			viewState.setActiveDiagram(Perspective.Unknown, null);
		}
		controllerManager.getMain().showColourPane();

		if (d != null) {
			renderer.renderDiagram(d.perspective, d);

			Slot slot = d.getSlot();

			controllerManager.getMain().setSlotInfo(d.perspective, new VirtualSlot(slot), d.x, d.y);

			renderer.renderCursor(d.perspective, d.x, d.y);
		} else {
			renderer.renderDiagram(Perspective.Front, null);
			renderer.renderDiagram(Perspective.Side, null);
		}
		onDiagramImageChanged();
	}

	public void displayBones() {
		controllerManager.getMain().hideColourPane();
		renderer.renderSkeleton(Perspective.Front, model.frontSkeleton.get());
		renderer.renderSkeleton(Perspective.Side, model.sideSkeleton.get());
		onSkeletonImageChanged();
	}
	
	public void onItemRenamed(NamedItem item, String oldName) {
		model.onItemRenamed(item, oldName);
	}

	public Collection<NamedImage> getImagesForDiagram(VirtualDiagram d) {
		return model.getImagesForDiagram(d);
	}

	public void setPositionColour(Position p, ColourTheme.ColourType type, String rgb) {
		model.setPositionColour(p, type, rgb);
	}

	public void showNewComponentDialog(VirtualSlot slot) {
		NewDiagramController ctrl = (NewDiagramController)controllerManager.get(StageType.newDiagram);
		ctrl.setSlot(slot);
		getStageManager().show(StageType.newDiagram);
	}

	public Model getModel() {
		return model;
	}
}
