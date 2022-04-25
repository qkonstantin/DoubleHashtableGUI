package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Controller {
    HashTable<String, String> table = new HashTable<>();

    @FXML
    private ListView<String> keyListView;
    public ListView<String> valueListView;
    public TextField keyTextField;
    public TextField valueTextField;
    public Button putButton;
    public Button removeButton;
    public Button getButton;
    public Label getLabel;
    public TextField containsKeyTextField;
    public Label containsKeyLabel;
    public Button closeButton;

    // Метод инициализации, который делает кнопки заблокированными.
    @FXML
    public void initialize() {
        putButton.disableProperty().bind
                (keyTextField.textProperty().isEmpty().or(valueTextField.textProperty().isEmpty()));
        removeButton.disableProperty().bind
                (keyListView.getSelectionModel().selectedItemProperty().isNull());
        getButton.disableProperty().bind
                (keyListView.getSelectionModel().selectedItemProperty().isNull());
    }

    // Метод для кнопки "put".
    @FXML
    private void putClicked() {
        if (table.containsKey(keyTextField.getText())) {
            valueListView.getItems().remove(table.get(keyTextField.getText()));
            keyListView.getItems().remove(keyTextField.getText());
        }
        table.put(keyTextField.getText(), valueTextField.getText());
        keyListView.getItems().addAll(keyTextField.getText());
        valueListView.getItems().addAll(valueTextField.getText());
        keyTextField.clear();
        valueTextField.clear();
    }

    // Метод для кнопки "get".
    @FXML
    private void getClicked() {
        getLabel.setText(table.get(keyListView.getSelectionModel().getSelectedItem()));
    }

    // Метод для кнопки "remove".
    @FXML
    private void removeClicked() {
        valueListView.getItems().remove(table.get(keyListView.getSelectionModel().getSelectedItem()));
        table.remove(keyListView.getSelectionModel().getSelectedItem());
        keyListView.getItems().remove(keyListView.getSelectionModel().getSelectedItem());
    }

    // Метод для кнопки "close".
    @FXML
    private void closeButtonAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // Метод, проверяющий содержание ключа (да или нет).
    @FXML
    private void containsKeyTyped() {
        containsKeyLabel.setText(table.containsKey(containsKeyTextField.getText()) ? "Yes" : "No");
    }

    // Метод, реализующий нажатие клавиши "Enter".
    @FXML
    public void enterHandle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER && !putButton.isDisabled()) {
            putClicked();
        }
    }

    // Метод, реализующий нажатие клавиши "Delete".
    @FXML
    public void deleteHandle(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) removeClicked();
    }

}
