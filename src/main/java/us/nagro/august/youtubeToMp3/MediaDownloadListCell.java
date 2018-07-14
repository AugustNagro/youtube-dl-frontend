package us.nagro.august.youtubeToMp3;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.cell.TextFieldListCell;

public class MediaDownloadListCell extends TextFieldListCell<String> {

    public MediaDownloadListCell() {
        setAlignment(Pos.CENTER);
        setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        setPrefWidth(397);
        setContentDisplay(ContentDisplay.RIGHT);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item);

            ProgressIndicator progress = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
            progress.setPrefSize(20, 20);
            setGraphic(progress);
        }
    }
}
