package com.jordan.page.projects.desktopreplacer;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.jordan.page.projects.desktopreplacer.UI.MainUI;
import com.jordan.page.projects.desktopreplacer.service.WallpaperService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class DesktopReplacerApplication extends Application {

    private static String[] args;
    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        DesktopReplacerApplication.args = args;
        Application.launch(DesktopReplacerApplication.class, args);
    }

    @Override
    public void init() throws Exception {
        context = SpringApplication.run(DesktopReplacerApplication.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainUI mainUI = context.getBean(MainUI.class);
        mainUI.start(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        context.close();
        Platform.exit();
    }
}
