package com.fumbbl.iconcomposer.controllers;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fumbbl.iconcomposer.ColourTheme;
import com.fumbbl.iconcomposer.Config;
import com.fumbbl.iconcomposer.Diagram;
import com.fumbbl.iconcomposer.ViewState;
import com.fumbbl.iconcomposer.dto.DtoPosition;
import com.fumbbl.iconcomposer.dto.DtoRoster;
import com.fumbbl.iconcomposer.dto.DtoRuleset;
import com.fumbbl.iconcomposer.model.Model;
import com.fumbbl.iconcomposer.model.NamedSVG;
import com.fumbbl.iconcomposer.model.spine.Bone;
import com.fumbbl.iconcomposer.model.spine.Skeleton;
import com.fumbbl.iconcomposer.model.spine.Skin;
import com.fumbbl.iconcomposer.model.spine.Slot;
import com.fumbbl.iconcomposer.svg.SVGRenderer;
import com.fumbbl.iconcomposer.ui.StageManager;
import com.fumbbl.iconcomposer.ui.StageType;
import com.kitfox.svg.SVGDiagram;

import javafx.scene.image.WritableImage;

public class Controller extends BaseController {
	private Model model;
	private SVGRenderer renderer;
	private StageManager stageManager;
	private ControllerManager controllerManager;
	public ViewState viewState;
	ExecutorService threadPool;
	
	public Controller(Model model) {
		this.model = model;
		renderer = new SVGRenderer(model, this);
		viewState = new ViewState();
		threadPool = Executors.newSingleThreadExecutor();
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}

	public void setControllerManager(ControllerManager controllerManager) {
		this.controllerManager = controllerManager;
	}
	
	public SVGRenderer getRenderer() {
		return renderer;
	}

	public StageManager getStageManager() {
		return stageManager;
	}
	
	public void runInBackground(Runnable task) {
		threadPool.execute(task);
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
		controllerManager.getMain().setSkeleton(skeleton);
	}
	
	public void onColourThemesChanged(Collection<ColourTheme> themes) {
		controllerManager.getMain().setColourThemes(themes);
	}

	public void onColourThemeChanged(ColourTheme t) {
		controllerManager.getMain().setColourTheme(t);
	}
	
	public void onImageChanged() {
		WritableImage image = viewState.getImage();
		controllerManager.getMain().setImage(image);
	}
	
	public void onAuthenticateChange(boolean success) {
		controllerManager.getMain().setApiStatus(success ? "Authorized" : "Not Authorized");
	}
	
	public void onPositionsChanged(Collection<DtoPosition> positions) {
		controllerManager.getMain().onPositionsChanged(positions);
	}
	
	public void onImagesChanged(Collection<NamedSVG> images) {
		controllerManager.getMain().setImages(images);
		((NewDiagramController)controllerManager.get(StageType.newDiagram)).setImages(images);
	}

	public void onDiagramsChanged(Collection<Diagram> diagrams) {
		controllerManager.getMain().setDiagrams(diagrams);
	}

	public void onSkinsChanged(Collection<Skin> skins) {
		controllerManager.getMain().onSkinsChanged(skins);
	}
	
	/*
	 * ViewState updates
	 */
	
	public void setBones(Collection<Bone> bones) {
		viewState.getActiveSkeleton().setBones(bones);
	}

	public void setSlots(Collection<Slot> slots) {
		viewState.getActiveSkeleton().setSlots(slots);
	}

	public void setColourTheme(String theme) {
		viewState.setActiveColourTheme(model.getColourTheme(theme));
	}
	
	public void setSkeleton(Skeleton skeleton) {
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

	public DtoRuleset[] loadRulesets() {
		return model.loadRulesets();
	}

	public void loadRuleset(int id) {
		model.loadRuleset(id);
	}
	
	public void loadSkins(int positionId) {
		model.loadSkins(positionId);
	}

	public void loadSkeletons(int positionId) {
		model.loadSkeletons(positionId);
	}
	
	public boolean isAuthorized() {
		return model.isAuthorized();
	}

	public Config getConfig() {
		return model.getConfig();
	}

	public Collection<Bone> loadBones(int skeletonId) {
		return model.loadBones(skeletonId);
	}
	
	public Collection<Slot> loadSlots(int skeletonId) {
		return model.loadSlots(skeletonId);
	}

	public void handleDroppedFile(String path) {
		model.handleDroppedFile(path);
	}
	
	public void authenticate() {
		model.authenticate();
	}
	
	public void loadRoster(DtoRoster selectedItem) {
		model.loadRoster(selectedItem.id);
	}
	
	public void createDiagram(NamedSVG svg) {
		model.addDiagram(svg);
	}
	
	public SVGDiagram getSvg(String svgName) {
		return model.getSvg(svgName);
	}

	public void deleteSkeleton(Skeleton skeleton) {
		model.deleteSkeleton(skeleton);
	}
	
	/*
	 * Renderer
	 */
	
	public void displayImage(String image) {
		viewState.setActiveDiagram(model.getDiagram(image));
		displayDiagram();
	}

	public void displayImage(NamedSVG image) {
		renderer.renderSvg(image.diagram);
		onImageChanged();
	}
	
	public void displaySkin(Skin skin) {
		viewState.setActiveSkin(skin);
		controllerManager.getMain().hideColourPane();
		renderer.renderSkin(skin);
        onImageChanged();
	}
	
	public void displayDiagram() {
		Diagram d = viewState.getActiveDiagram();
		displayDiagram(d);
	}
	
	public void displayDiagram(Diagram d) {
		viewState.setActiveDiagram(d);
		controllerManager.getMain().showColourPane();
		renderer.renderDiagram(d);

		Slot slot = d.getSlot();
		
		controllerManager.getMain().setSlotInfo(slot, d.x, d.y);

		renderer.renderCursor(d.x, d.y);
		onImageChanged();
	}

	public void displayBones(String value) {
		controllerManager.getMain().hideColourPane();
		renderer.renderSkeleton(viewState.getActiveSkeleton(), value);
		onImageChanged();
	}
	
	public void onRulesetLoaded(DtoRuleset ruleset) {
		((OpenRosterController)controllerManager.get(StageType.openRoster)).onRulesetLoaded(ruleset);
	}

	public void onPositionChanged(DtoPosition position) {
		controllerManager.getMain().onPositionChanged(position);
	}

	public void onImportStart() {
		controllerManager.getMain().onImportStart();
	}

	public void onImportComplete() {
		controllerManager.getMain().onImportComplete();
	}

}
