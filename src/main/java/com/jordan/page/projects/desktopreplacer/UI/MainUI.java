package com.jordan.page.projects.desktopreplacer.UI;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.jordan.page.projects.desktopreplacer.service.WallpaperService;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

@Component
public class MainUI extends Application {

    private final WallpaperService wallpaperService;

    public MainUI(WallpaperService wallpaperService) {
        this.wallpaperService = wallpaperService;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Desktop Wallpaper Replacer");
        primaryStage.setResizable(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER); // Center the grid within the scene

        Label label = new Label("Enter a search term for Wallpaper:");
        GridPane.setConstraints(label, 0, 0);
        GridPane.setColumnSpan(label, 2);
        GridPane.setHalignment(label, javafx.geometry.HPos.CENTER); // Center the label horizontally

        TextField input = new TextField();
        GridPane.setConstraints(input, 0, 1);
        GridPane.setColumnSpan(input, 2);
        GridPane.setHalignment(input, javafx.geometry.HPos.CENTER); // Center the input horizontally

        Button changeButton = new Button("Change Wallpaper");
        GridPane.setConstraints(changeButton, 0, 2);
        GridPane.setColumnSpan(changeButton, 2);
        GridPane.setHalignment(changeButton, javafx.geometry.HPos.CENTER); // Center the button horizontally

        Label feedbackLabel = new Label();
        GridPane.setConstraints(feedbackLabel, 0, 0);
        GridPane.setColumnSpan(feedbackLabel, 2);
        GridPane.setHalignment(feedbackLabel, javafx.geometry.HPos.CENTER); // Center the feedback label horizontally
        feedbackLabel.setVisible(false);

        Button yesButton = new Button("Yes");
        GridPane.setConstraints(yesButton, 0, 1);
        yesButton.setVisible(false);
        yesButton.setMinWidth(100);
        GridPane.setHalignment(yesButton, javafx.geometry.HPos.LEFT); // Center the yes button horizontally

        Button noButton = new Button("No");
        GridPane.setConstraints(noButton, 1, 1);
        noButton.setVisible(false);
        noButton.setMinWidth(100);
        GridPane.setHalignment(noButton, javafx.geometry.HPos.RIGHT); // Center the no button horizontally

        Label enjoyLabel = new Label("Enjoy the new wallpaper!");
        GridPane.setConstraints(enjoyLabel, 0, 0);
        GridPane.setColumnSpan(enjoyLabel, 2);
        GridPane.setHalignment(enjoyLabel, javafx.geometry.HPos.CENTER); // Center the enjoy label horizontally
        enjoyLabel.setVisible(false);

        changeButton.setOnAction(
                e -> generateWallpaper(input.getText(), feedbackLabel, input, changeButton, yesButton, noButton,
                        label));

        // Add event handler for Enter key press on text field
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                generateWallpaper(input.getText(), feedbackLabel, input, changeButton, yesButton, noButton, label);
            }
        });

        yesButton.setOnAction(e -> {
            feedbackLabel.setVisible(false);
            yesButton.setVisible(false);
            noButton.setVisible(false);
            enjoyLabel.setVisible(true);

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> primaryStage.close());
            pause.play();
        });

        noButton.setOnAction(e -> {
            label.setText("Try again! Enter a new search term:");
            label.setVisible(true);
            input.setVisible(true);
            changeButton.setVisible(true);
            feedbackLabel.setVisible(false);
            yesButton.setVisible(false);
            noButton.setVisible(false);
            input.clear();
        });

        grid.getChildren().addAll(label, input, changeButton, feedbackLabel, yesButton, noButton, enjoyLabel);

        Scene scene = new Scene(grid, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to handle generating wallpaper
    private void generateWallpaper(String searchTerm, Label feedbackLabel, TextField input, Button changeButton,
            Button yesButton, Button noButton, Label label) {
        if (!searchTerm.isEmpty()) {
            try {
                wallpaperService.changeWallpaper(searchTerm);
                label.setVisible(false);
                input.setVisible(false);
                changeButton.setVisible(false);
                feedbackLabel.setText("Wallpaper changed. Do you like it?");
                feedbackLabel.setVisible(true);
                yesButton.setVisible(true);
                noButton.setVisible(true);
            } catch (IOException | InterruptedException e) {
                label.setVisible(false);
                input.setVisible(false);
                changeButton.setVisible(false);
                feedbackLabel.setText("Failed to change wallpaper.");
                feedbackLabel.setVisible(true);
                e.printStackTrace();
            } catch (IllegalArgumentException e1) {
                label.setVisible(false);
                input.setVisible(false);
                changeButton.setVisible(false);
                feedbackLabel.setText("No wallpaper found for the search term. Retry...");
                feedbackLabel.setVisible(true);

                // Pause for 2 seconds before resetting the UI
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(event -> {
                    label.setText("Sorry, try a new search term:");
                    input.setVisible(true);
                    input.clear();
                    label.setVisible(true);
                    changeButton.setVisible(true);
                    feedbackLabel.setVisible(false);
                    yesButton.setVisible(false);
                    noButton.setVisible(false);
                });
                pause.play();
            }
        } else {
            feedbackLabel.setText("Search term cannot be empty.");
            feedbackLabel.setVisible(true);

        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}