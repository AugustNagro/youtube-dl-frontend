package us.nagro.august.youtubeToMp3;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Home {

    private static final Path windowsDLExe = Paths.get(System.getProperty("user.home"), "youtube-dl.exe").toAbsolutePath();

    @FXML private ToggleButton audioDownloadBtn;
    @FXML private Label pasteNotice;
    @FXML private Label downloadLocationLabel;
    @FXML private ListView<String> downloadListView;
    @FXML private ToggleGroup mediaSelectionToggleGroup;

    private Path downloadLocation;
    private boolean isWindows = Main.PREFS.getBoolean("isWindows", true);

    public void initialize(){
        downloadLocation = Paths.get(Main.PREFS.get("downloadLocation", ""));
        downloadLocationLabel.setText(downloadLocation.toString());
        pasteNotice.setText("Paste YouTube URL Here");

        downloadListView.setCellFactory(listView -> new MediaDownloadListCell());
        downloadListView.setEditable(false);
    }

    public void addMedia(String youtubeUrl) {

        downloadListView.getItems().add(youtubeUrl);
        boolean downloadAudio = audioDownloadBtn.isSelected();
        String youtubeDlPath = isWindows ? windowsDLExe.toString() : "/usr/local/bin/youtube-dl";

        ProcessBuilder pb;
        if (downloadAudio) pb = new ProcessBuilder(youtubeDlPath, "-x", "--audio-format", "mp3", youtubeUrl);
        else pb = new ProcessBuilder(youtubeDlPath, "-f", "mp4", youtubeUrl);
        pb.directory(downloadLocation.toFile());

        new Thread(() -> {
            try {
                pb.start().waitFor();
                Platform.runLater(() -> downloadListView.getItems().removeAll(youtubeUrl));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setDownloadLocation(ActionEvent actionEvent) {
        Path p = Main.changeDownloadLocation(pasteNotice.getScene().getWindow());
        if (p != null) {
            downloadLocation = p;
            downloadLocationLabel.setText(p.toString());
        }
    }
}
