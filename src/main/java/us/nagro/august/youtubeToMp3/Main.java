package us.nagro.august.youtubeToMp3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.prefs.Preferences;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Main extends Application {

    public static KeyCodeCombination PASTE = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);

    static Preferences PREFS = Preferences.userNodeForPackage(Main.class);

    private StackPane root = new StackPane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        root.setPrefSize(400, 600);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Youtube Downloader");

        String downloadLocation = PREFS.get("downloadLocation", "");
        if (downloadLocation.length() > 2 && Paths.get(downloadLocation).toFile().exists()) {
            showHome();
        } else {
            showWelcome();
        }

        primaryStage.show();
    }

    public static Path changeDownloadLocation(Window owner) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select download location");
        File directory = dc.showDialog(owner);
        if (directory != null) {
            PREFS.put("downloadLocation", directory.getAbsolutePath());
            return directory.toPath();
        }
        return null;
    }

    private void showWelcome() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            PREFS.putBoolean("isWindows", true);
            String userHome = System.getProperty("user.home");
            Path extractedPath = Paths.get(userHome, "youtube-dl.exe");
            try {
                Files.copy(getClass().getResource("/youtube-dl.exe").openStream(), extractedPath, REPLACE_EXISTING);
                //ffmpeg required files
                Files.copy(getClass().getResource("/ffmpeg/ffmpeg.exe").openStream(), Paths.get(userHome, "ffmpeg.exe"), REPLACE_EXISTING);
                Files.copy(getClass().getResource("/ffmpeg/ffplay.exe").openStream(), Paths.get(userHome, "ffplay.exe"), REPLACE_EXISTING);
                Files.copy(getClass().getResource("/ffmpeg/ffprobe.exe").openStream(), Paths.get(userHome, "ffprobe.exe"), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            showDownloadLocPrompt();
        } else {
            PREFS.putBoolean("isWindows", false);

            if (Paths.get("/usr/local/bin/youtube-dl").toFile().exists()) {
                showDownloadLocPrompt();
                return;
            }

            root.getChildren().setAll(new Label("Make sure Python and ffmpeg are is installed,\nand enter root password:\n"));
            TextField passField = new TextField();
            root.getChildren().add(passField);

            passField.setOnAction(ae -> {
                String pass = passField.getText();
                try {
                    Runtime.getRuntime().exec("echo " + pass + " | " + "sudo curl -L https://yt-dl.org/downloads/latest/youtube-dl -o /usr/local/bin/youtube-dl").waitFor();
                    Runtime.getRuntime().exec("echo " + pass + " | " +" sudo chmod a+rx /usr/local/bin/youtube-dl").waitFor();
                    showDownloadLocPrompt();
                } catch (IOException | InterruptedException e) {
                    passField.clear();
                    e.printStackTrace();
                }
            });
        }

    }

    private void showDownloadLocPrompt() {
        root.getChildren().setAll(new Label("Press Enter to select download location"));
        root.getScene().setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ENTER) {
                Path downloadLoc = changeDownloadLocation(root.getScene().getWindow());
                if (downloadLoc != null) showHome();
            }
        });

    }

    private void showHome() {
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/views/home.fxml"));
        try {
            Pane home = fxml.load();
            Home controller = fxml.getController();

            root.getChildren().setAll(home);
            root.getScene().setOnKeyPressed(ke -> {
                if (PASTE.match(ke)) controller.addMedia(Clipboard.getSystemClipboard().getString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
