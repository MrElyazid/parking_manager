package com.smartparking.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // In HomeController.java
    @FXML
    private void handleSignInButtonClick() {
        try {
            // 1. FIRST load MainView to get MainController instance
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/com/smartparking/view/MainView.fxml"));
            Parent mainRoot = mainLoader.load();
            MainController mainController = mainLoader.getController();

            // 2. NOW load LoginView with the MainController reference
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/com/smartparking/view/UserLoginView.fxml"));
            Parent loginRoot = loginLoader.load();
            UserLoginController loginController = loginLoader.getController();
            loginController.setMainController(mainController); // Proper connection

            // 3. Show login window
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot, 450, 400));
            loginStage.setTitle("User Login");
            loginStage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
