package org.plateau.citygmleditor.citygmleditor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.function.Consumer;
import javafx.scene.control.MultipleSelectionModel;
import org.plateau.citygmleditor.citymodel.AttributeValue;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.SelectionModel;

public class AddingAttributeController {

    @FXML
    private TableView<AttributeValue> attributeTableView;
    @FXML
    private TableColumn<AttributeValue, String> name;
    @FXML
    private TableColumn<AttributeValue, String> description;

    @FXML
    private TextField searchField;

    private final ObservableList<AttributeValue> allAttributes = FXCollections.observableArrayList();
    private Consumer<SelectionModel<AttributeValue>> onItemSelected;

    public void initialize() {
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        // 検索フィールドのリスナーを設定
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterList(newValue));
    }

    // コールバックを設定するメソッド
    public void setItemSelectedCallback(Consumer<SelectionModel<AttributeValue>> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    public void setList(List<AttributeValue> list) {
        allAttributes.setAll(list);
        attributeTableView.setItems(allAttributes); // TableViewにアイテムを設定
        filterList(""); // 初期表示時に全ての項目を表示
    }

    private void filterList(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            attributeTableView.setItems(allAttributes);
        } else {
            ObservableList<AttributeValue> filteredList = FXCollections.observableArrayList();
            for (AttributeValue attribute : allAttributes) {
                if (attribute.nameProperty().getValue().toLowerCase().contains(filter.toLowerCase())) {
                    filteredList.add(attribute);
                }
            }
            attributeTableView.setItems(filteredList);
        }
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            // TableViewの選択モデルを取得
            SelectionModel<AttributeValue> selectionModel = attributeTableView.getSelectionModel();

            if (selectionModel != null && onItemSelected != null) {
                // コンシューマに選択モデルを渡す
                onItemSelected.accept(selectionModel);
            }
        }
    }
}