package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class Controller {

    @FXML
    private Button updateBtn;

    @FXML
    private Button activateBtn;

    @FXML
    private TextArea memoTextArea;

    @FXML
    private TextArea chatTextArea;

    private String memo;

    public void activateBtnHandler(ActionEvent event) {
        String red = "#D04242";
        String green = " #499C54";
        if (!Server.isIsServerOn()) {
            Server.start(8080);
            activateBtn.setText("DEACTIVATE");
            setBGColor(activateBtn, red);
            Server.chatTextArea = chatTextArea;
        } else {
            Server.stop();
            activateBtn.setText("ACTIVATE");
            setBGColor(activateBtn, green);
        }
    }

    private void setBGColor(Button btn, String color) {
        String[] styles = btn.getStyle().split(";");
        String newBGColor = styles[0].substring(0, styles[0].indexOf(':') + 1) + " " + color;
        String newStyle = newBGColor;
        for (int i = 1; i < styles.length; i++)
            newStyle += "; " + styles[i];
        activateBtn.setStyle(newStyle);
    }

    public void updateBtnHandler(ActionEvent event) {
        if (!Server.isIsServerOn()) {
            System.out.println("Server is off");
        } else {
            memo = memoTextArea.getText();
            Server.sendMemo(memo);
        }
    }
}
