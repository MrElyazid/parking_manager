package com.smartparking.controller;

import com.smartparking.model.ParkingSpot;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SpotCardController {

    @FXML
    private VBox spotCardRoot; // The root VBox of SpotCard.fxml

    @FXML
    private Label spotIdLabel;

    @FXML
    private FontAwesomeIconView spotTypeIcon;

    @FXML
    private Label spotTypeLabel;

    @FXML
    private Label spotRateLabel;

    private ParkingSpot parkingSpot;

    public void setData(ParkingSpot spot) {
        this.parkingSpot = spot;

        spotIdLabel.setText(spot.getSpotId());
        spotTypeLabel.setText(spot.getType());
        spotRateLabel.setText(String.format("$%.2f/hr", spot.getHourlyRate()));

        // Set icon based on type
        switch (spot.getType().toLowerCase()) {
            case "ev":
                spotTypeIcon.setGlyphName(FontAwesomeIcon.BOLT.name());
                spotCardRoot.getStyleClass().add("spot-card-type-ev");
                break;
            case "disabled":
                spotTypeIcon.setGlyphName(FontAwesomeIcon.WHEELCHAIR.name());
                spotCardRoot.getStyleClass().add("spot-card-type-disabled");
                break;
            case "standard":
            default:
                spotTypeIcon.setGlyphName(FontAwesomeIcon.CAR.name());
                spotCardRoot.getStyleClass().add("spot-card-type-standard");
                break;
        }
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public VBox getRoot() {
        return spotCardRoot;
    }
}