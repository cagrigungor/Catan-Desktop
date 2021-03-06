package com.catan.controller;

import com.catan.Util.Constants;
import com.catan.modal.Harbour;
import com.catan.modal.Player;
import com.catan.modal.Trade;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

public class ControllerTradeOffer {

    @FXML
    private AnchorPane root;
    @FXML
    private Button requestButton;
    @FXML
    private Button clearTradeButton;
    @FXML
    private Label labelOutputOfTrade;
    @FXML
    private ImageView imgOfferBrick;
    @FXML
    private Label labelActualPlayerBrick;
    @FXML
    private ImageView imgOfferGrain;
    @FXML
    private Label labelActualPlayerGrain;
    @FXML
    private ImageView imgOfferOre;
    @FXML
    private Label labelActualPlayerOre;
    @FXML
    private ImageView imgOfferWool;
    @FXML
    private Label labelActualPlayerWool;
    @FXML
    private ImageView imgOfferLumber;
    @FXML
    private Label labelActualPlayerLumber;
    @FXML
    private ComboBox<String> selectorTrade;
    @FXML
    private ImageView imgRequestBrick;
    @FXML
    private ImageView imgRequestGrain;
    @FXML
    private ImageView imgRequestOre;
    @FXML
    private ImageView imgRequestWool;
    @FXML
    private ImageView imgRequestLumber;
    @FXML
    private ImageView imgOfferResultBrick;
    @FXML
    private Label labelOfferedBrick;
    @FXML
    private ImageView imgOfferResultWool;
    @FXML
    private Label labelOfferedWool;
    @FXML
    private ImageView imgOfferResultGrain;
    @FXML
    private Label labelOfferedGrain;
    @FXML
    private ImageView imgOfferResultLumber;
    @FXML
    private Label labelOfferedLumber;
    @FXML
    private ImageView imgOfferResultOre;
    @FXML
    private Label labelOfferedOre;
    @FXML
    private ImageView imgRequestResultBrick;
    @FXML
    private Label labelRequestedBrick;
    @FXML
    private ImageView imgRequestResultWool;
    @FXML
    private Label labelRequestedWool;
    @FXML
    private ImageView imgRequestResultGrain;
    @FXML
    private Label labelRequestedGrain;
    @FXML
    private ImageView imgRequestResultLumber;
    @FXML
    private Label labelRequestedLumber;
    @FXML
    private ImageView imgRequestResultOre;
    @FXML
    private Label labelRequestedOre;
    @FXML
    private ImageView imgTrade1;
    @FXML
    private ImageView imgTrade2;
    @FXML
    ResourceBundle resources;

    // properties
    private HashMap<String, Integer> offeredResources;
    private HashMap<String, Integer> requestedResources;
    private HashMap<String, Integer> actualPlayerResources;
    private boolean isTradeWithChest = false;
    private ArrayList<Player> allPlayers;
    private ArrayList<Label> labelsRequests;
    private ArrayList<Label> labelsOffer;
    private ArrayList<Label> labelsActualPlayer;
    private ArrayList<ImageView> imgOffers;
    private ArrayList<ImageView> imgRequests;
    private Player actualPlayer;
    private Player playerToBeTraded;

    @FXML
    public void initialize() {
        requestedResources = new HashMap<>();
        actualPlayerResources = new HashMap<>();
        offeredResources = new HashMap<>();
        ArrayList<ImageView> imgOffersResult;
        ArrayList<ImageView> imgRequestsResult;

        labelsRequests = new ArrayList<>(Arrays.asList(
                labelRequestedOre, labelRequestedBrick,
                labelRequestedLumber, labelRequestedGrain,
                labelRequestedWool));

        labelsOffer = new ArrayList<>(Arrays.asList(
                labelOfferedOre, labelOfferedBrick,
                labelOfferedLumber, labelOfferedGrain,
                labelOfferedWool));

        labelsActualPlayer = new ArrayList<>(Arrays.asList(
                labelActualPlayerOre, labelActualPlayerBrick,
                labelActualPlayerLumber, labelActualPlayerGrain,
                labelActualPlayerWool));

        imgOffers = new ArrayList<>(Arrays.asList(
                imgOfferOre, imgOfferBrick,
                imgOfferLumber, imgOfferGrain,
                imgOfferWool));

        imgOffersResult = new ArrayList<>(Arrays.asList(
                imgOfferResultOre, imgOfferResultBrick,
                imgOfferResultLumber, imgOfferResultGrain,
                imgOfferResultWool));

        imgRequests = new ArrayList<>(Arrays.asList(
                imgRequestOre, imgRequestBrick,
                imgRequestLumber, imgRequestGrain,
                imgRequestWool));

        imgRequestsResult = new ArrayList<>(Arrays.asList(
                imgRequestResultOre, imgRequestResultBrick,
                imgRequestResultLumber, imgRequestResultGrain,
                imgRequestResultWool));

        for (int i = 0; i < Constants.resourceNames.size(); i++) {
            String resourceName = Constants.resourceNames.get(i);
            String resourcePath = Constants.getResourcePaths().get(i);
            offeredResources.put(resourceName, 0);
            requestedResources.put(resourceName, 0);
            Image image = new Image(resourcePath);
            imgOffers.get(i).setImage(image);
            imgRequests.get(i).setImage(image);
            imgOffersResult.get(i).setImage(image);
            imgRequestsResult.get(i).setImage(image);
        }

        Image image1 = new Image(Constants.PATH_TRADE_1());
        Image image2 = new Image(Constants.PATH_TRADE_2());
        imgTrade1.setImage(image1);
        imgTrade2.setImage(image2);

        selectorTrade.setStyle("-fx-font: 20px \"Book " +
                "Antiqua\"; -fx-background-color: orange");
    }

    @FXML
    void clearTrade(ActionEvent event) {
        for (String resourceName: Constants.resourceNames) {
            offeredResources.put(resourceName, 0);
            requestedResources.put(resourceName, 0);
        }
        for (Label label: labelsRequests) { label.setText("x0"); }
        for (Label label: labelsOffer)    { label.setText("x0"); }
        setActualPlayerAndLabels(actualPlayer);
    }

    public void setActualPlayerAndLabels(Player actualPlayer) {
        this.actualPlayer = actualPlayer;
        for (int i = 0; i < Constants.resourceNames.size(); i++) {
            String resourceName = Constants.resourceNames.get(i);
            int resourceCount = actualPlayer.getSourceCards().get(resourceName).size();
            actualPlayerResources.put(resourceName, resourceCount);
            labelsActualPlayer.get(i).setText("x" + actualPlayerResources.get(resourceName));
        }
    }

    @FXML
    public int checkTradeWithChestRatio(String resourceType) {
        if (actualPlayer.getHarbours() != null) {
            for (Harbour harbour : actualPlayer.getHarbours()) {
                if (harbour.getAssociatedResourceType().equals(resourceType)) {
                    int tradeWithChestRatioWithHarbour = harbour.getTradeRatio();
                    return tradeWithChestRatioWithHarbour;
                }
            }
        }
        return 4;
    }

    @FXML
    void requestTrade(ActionEvent event) {
        if (isTradeWithChest) {
            Trade trade = new Trade(actualPlayer, null, requestedResources, offeredResources, true);
            terminateTrade(trade);
        } else {
            Trade trade = new Trade(actualPlayer, playerToBeTraded, requestedResources, offeredResources, false);
            terminateTrade(trade);
        }
    }

    private void terminateTrade(Trade trade) {
        if (trade.isTradeCompleted()) {
            labelOutputOfTrade.setText(resources.getString("success_tradeSuccesful"));
            labelOutputOfTrade.setStyle("-fx-background-color: orange; -fx-text-fill: black");
            labelOutputOfTrade.setOpacity(1);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    closeDialog(null);
                });
            }).start();
        } else {
            String errorMessage = trade.getErrorMessage();
            if (!errorMessage.isEmpty()) {
                labelOutputOfTrade.setStyle("-fx-background-color: red; -fx-text-fill: white");
                labelOutputOfTrade.setText(errorMessage);
                labelOutputOfTrade.setOpacity(1);
            }
        }
    }

    @FXML
    void selectPlayerToBeTraded(ActionEvent event) {
        clearTrade(null);
        String nameToBeTraded = selectorTrade.getValue();
        playerToBeTraded = null;
        if (!nameToBeTraded.equals("Chest")) {
            for (Player player: allPlayers) {
                if (player.getName().equals(nameToBeTraded)) { playerToBeTraded = player; }
            }
        } else {
            isTradeWithChest = true;
        }
    }

    public void setAllPlayers(ArrayList<Player> allPlayers){
        this.allPlayers = new ArrayList<>();
        for (Player player : allPlayers) {
            if (player != actualPlayer) {
                this.allPlayers.add(player);
                selectorTrade.getItems().add(player.getName());
            }
        }
        selectorTrade.getItems().add("Chest");
        selectorTrade.setPromptText(this.allPlayers.get(0).getName());
        playerToBeTraded = this.allPlayers.get(0);
    }

    @FXML
    public void offerResource(MouseEvent mouseEvent) {
        if (isTradeWithChest) {
            setActualPlayerAndLabels(actualPlayer);

            for (int i = 0; i < Constants.resourceNames.size(); i++) {
                String resourceName = Constants.resourceNames.get(i);
                labelsOffer.get(i).setText("x0");
                offeredResources.put(resourceName, 0);
            }

            for (int i = 0; i < Constants.resourceNames.size(); i++) {
                String resourceName = Constants.resourceNames.get(i);
                int tradeRatio = checkTradeWithChestRatio(resourceName);
                if (mouseEvent.getSource() == imgOffers.get(i) && actualPlayerResources.get(resourceName) >= tradeRatio) {
                    System.out.println(tradeRatio);
                    labelsOffer.get(i).setText("x"+tradeRatio);
                    offeredResources.put(resourceName, tradeRatio);
                    int playerResourceCount = actualPlayerResources.get(resourceName) - tradeRatio;
                    actualPlayerResources.put(resourceName, playerResourceCount);
                    labelsActualPlayer.get(i).setText("x" + playerResourceCount );
                    break;
                }
            }
        }
        else {
            for (int i = 0; i < Constants.resourceNames.size(); i++) {
                String resourceName = Constants.resourceNames.get(i);
                if (mouseEvent.getSource() == imgOffers.get(i) && actualPlayerResources.get(resourceName) > 0){
                    int offeredResourceCount = offeredResources.get(resourceName) + 1;
                    offeredResources.put(resourceName, offeredResourceCount);
                    int playerResourceCount = actualPlayerResources.get(resourceName) - 1;
                    actualPlayerResources.put(resourceName, playerResourceCount);
                    labelsOffer.get(i).setText("x" + offeredResourceCount);
                    labelsActualPlayer.get(i).setText("x" + playerResourceCount);
                    break;
                }
            }
        }
    }

    @FXML
    public void requestResource(MouseEvent mouseEvent) {
        if (isTradeWithChest) {
            for (int i = 0; i < Constants.resourceNames.size(); i++) {
                String resourceName = Constants.resourceNames.get(i);
                labelsRequests.get(i).setText("x0");
                requestedResources.put(resourceName, 0);
            }
        }
        for (int i = 0; i < Constants.resourceNames.size(); i++) {
            String resourceName = Constants.resourceNames.get(i);
            if (mouseEvent.getSource() == imgRequests.get(i)){
                int requestedResourceCount = requestedResources.get(resourceName) + 1;
                requestedResources.put(resourceName, requestedResourceCount);
                labelsRequests.get(i).setText("x" + requestedResourceCount);
                break;
            }
        }
    }

    @FXML
    public void closeDialog(ActionEvent actionEvent) {
        Stage window = (Stage) root.getScene().getWindow();
        window.close();
    }
}