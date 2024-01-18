package org.plateau.citygmleditor.citygmleditor;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.converters.Obj2LodConverter;
import org.plateau.citygmleditor.exporters.GltfExporter;
import org.plateau.citygmleditor.exporters.ObjExporter;
import org.plateau.citygmleditor.world.SceneContent;

import javafx.beans.binding.ObjectBinding;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;

public class HierarchyController implements Initializable {
    private final SceneContent sceneContent = CityGMLEditorApp.getSceneContent();
    public TreeTableView<Node> hierarchyTreeTable;
    public TreeTableColumn<Node, String> nodeColumn;
    public TreeTableColumn<Node, String> idColumn;
    public TreeTableColumn<Node, Boolean> visibilityColumn;
    public ContextMenu hierarchyContextMenu;
    public MenuItem exportGltfMenu;
    public MenuItem exportObjMenu;
    public MenuItem importObjMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hierarchyTreeTable.rootProperty().bind(new ObjectBinding<TreeItem<Node>>() {
            {
                bind(sceneContent.contentProperty());
            }

            @Override
            protected TreeItem<Node> computeValue() {
                Node content3D = sceneContent.getContent();
                if (content3D != null) {
                    return new HierarchyController.TreeItemImpl(content3D);
                } else {
                    return null;
                }
            }
        });
        hierarchyTreeTable.setOnMouseClicked(t -> {
            if (t.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    var item = selectedItem.valueProperty().get();
                    exportGltfMenu.setDisable(!(item instanceof ILODSolidView));
                    exportObjMenu.setDisable(!(item instanceof ILODSolidView));
                    importObjMenu.setDisable(!(item instanceof ILODSolidView));
                }
            }
            if (t.getClickCount() == 2) {
                // TODO: フォーカス

                t.consume();
            }
        });
        hierarchyTreeTable.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.SPACE) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Node node = selectedItem.getValue();
                    node.setVisible(!node.isVisible());
                }
                t.consume();
            }
        });
        nodeColumn.setCellValueFactory(p -> p.getValue().valueProperty().asString());
        idColumn.setCellValueFactory(p -> p.getValue().getValue().idProperty());
        visibilityColumn.setCellValueFactory(p -> p.getValue().getValue().visibleProperty());
        visibilityColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(visibilityColumn));
    }

    /**
     * Export the selected solid to gLTF
     * @param actionEvent the event
     */
    public void exportGltf(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("gLTF", "*.gltf", "*.glb")
        );
        chooser.setTitle("Export gLTF");
        File newFile = chooser.showSaveDialog(hierarchyTreeTable.getScene().getWindow());
        if (newFile == null)
            return;

        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof ILODSolidView))
            return;

        ILODSolidView solid = (ILODSolidView)item;
        BuildingView building = (BuildingView)solid.getParent();
        try {
            new GltfExporter().export(newFile.toString(), solid, building.getId());
        } catch (Exception ex) {
            Logger.getLogger(HierarchyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Export the selected solid to OBJ
     * @param actionEvent the event
     */
    public void exportObj(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OBJ", "*.obj")
        );
        chooser.setTitle("Export OBJ");
        File newFile = chooser.showSaveDialog(hierarchyTreeTable.getScene().getWindow());
        if (newFile == null)
            return;

        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof ILODSolidView))
            return;

        ILODSolidView solid = (ILODSolidView)item;
        BuildingView building = (BuildingView)solid.getParent();
        try {
            new ObjExporter().export(newFile.toString(), solid, building.getId());
        } catch (Exception ex) {
            Logger.getLogger(HierarchyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void importObj(ActionEvent actionEvent) {
        var file = FileChooserService.showOpenDialog("*.obj", SessionManager.OBJ_FILE_PATH_PROPERTY);

        if (file == null)
            return;

        var content = (Group)sceneContent.getContent();
        var cityModelNode = content.getChildren().get(0);
        if (cityModelNode == null)
            return;

        var cityModel = (CityModelView)cityModelNode;
        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof ILODSolidView))
            return;

        ILODSolidView solid = (ILODSolidView)item;
        BuildingView building = (BuildingView)solid.getParent();
        try {
            new Obj2LodConverter(cityModel).convert(file.toString());
            //CityGMLEditorApp.getSceneContent().setContent(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class TreeItemImpl extends TreeItem<Node> {

        public TreeItemImpl(Node node) {
            super(node);
            if (node instanceof Parent) {
                for (Node n : ((Parent) node).getChildrenUnmodifiable()) {
                    getChildren().add(new HierarchyController.TreeItemImpl(n));
                }
            }
            node.setOnMouseClicked(t -> {
                TreeItem<Node> parent = getParent();
                while (parent != null) {
                    parent.setExpanded(true);
                    parent = parent.getParent();
                }
                hierarchyTreeTable.getSelectionModel().select(HierarchyController.TreeItemImpl.this);
                hierarchyTreeTable.scrollTo(hierarchyTreeTable.getSelectionModel().getSelectedIndex());
                t.consume();
            });
        }
    }
}
