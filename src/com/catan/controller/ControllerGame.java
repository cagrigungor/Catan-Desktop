package com.catan.controller;

import com.catan.Util.Constants;
import com.catan.interfaces.*;
import com.catan.modal.*;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.sun.deploy.security.SelectableSecurityManager;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;

public class ControllerGame extends ControllerBaseGame implements InterfaceMakeConstruction, InterfaceDevelopmentCard, InterfaceMakeTrade {

    // properties
    private DevelopmentCard developmentCardExchangeProfit;
    private DevelopmentCard developmentCardDestroyRoad;
    private DevelopmentCard developmentCardInvention;
    private boolean constructionUnselect = true;
    private String selectedConstruction = "";
    private Vertex roadVertex1 = null;
    private Vertex roadVertex2 = null;
    private boolean isStepInitial = true;
    private boolean isStepActual = false;
    private boolean isConstructionBuild = false;
    private boolean isRoadBuild = false;
    private int initialStepCount = 0;
    private Settlement tempSettlement;
    private Player currentPlayer;
    private int playerTurn = 0;
    private boolean thiefCanMove = false;
    private boolean initialThief = true;
    private List<String[]> gameLog = new ArrayList<>();
    int gameLogIterator = 0;
    private int noOfRound = 1;
    FlowPane gameLogsFlowPane;
    private ArrayList<ImageView> lumberImages = new ArrayList<>();
    private ArrayList<ImageView> brickImages = new ArrayList<>();
    private ArrayList<ImageView> grainImages = new ArrayList<>();
    private ArrayList<ImageView> oreImages = new ArrayList<>();
    private ArrayList<ImageView> woolImages = new ArrayList<>();
    private Pane[] cardPanes = new Pane[5];
    double[][] cardsPaneLocations = new double[5][2];
    private Road tempRoad;
    private Chest chest;

    @Override
    public void setDevelopmentCardInvention(DevelopmentCard developmentCardInvention) {
        this.developmentCardInvention = developmentCardInvention;
    }

    @Override
    public void initialize() {
        super.initialize();
        developmentCardExchangeProfit = null;
        developmentCardDestroyRoad = null;
        developmentCardInvention = null;
        selectConstructionInitial(imgRoad);
        currentPlayer = getPlayers().get(0);
        activateAllVertices();
        gameLogsFlowPane = (FlowPane)gameLogsScrollPane.getContent();
        chest = new Chest();
        initializeComponentsRelatedToActualPlayerCardsPane();
    }

    // this obnoxiously named function initializes the components related to the
    // section where number of cards of the actual player is shown
    private void initializeComponentsRelatedToActualPlayerCardsPane() {
        ImgViewLumberDummy.setVisible(false);
        lumberImages.add(ImgViewLumberDummy);
        ImgViewBrickDummy.setVisible(false);
        brickImages.add(ImgViewBrickDummy);
        ImgViewGrainDummy.setVisible(false);
        grainImages.add(ImgViewGrainDummy);
        ImgViewOreDummy.setVisible(false);
        oreImages.add(ImgViewOreDummy);
        ImgViewWoolDummy.setVisible(false);
        woolImages.add(ImgViewWoolDummy);

        cardPanes[0] = paneLumbers;
        cardPanes[1] = paneWools;
        cardPanes[2] = paneOres;
        cardPanes[3] = paneBricks;
        cardPanes[4] = paneGrains;

        cardsPaneLocations[0] = new double[] {paneLumbers.getLayoutX(), paneLumbers.getLayoutY()};
        cardsPaneLocations[1] = new double[] {paneWools.getLayoutX(), paneWools.getLayoutY()};
        cardsPaneLocations[2] = new double[] {paneBricks.getLayoutX(), paneBricks.getLayoutY()};
        cardsPaneLocations[3] = new double[] {paneOres.getLayoutX(), paneOres.getLayoutY()};
        cardsPaneLocations[4] = new double[] {paneGrains.getLayoutX(), paneGrains.getLayoutY()};
    }

    @FXML
    public void selectConstruction(MouseEvent mouseEvent) {
        Rectangle rectangle = (Rectangle) mouseEvent.getSource();
        if (isStepInitial) {
            selectConstructionInitial(rectangle);
        } else {
            selectConstructionActual(rectangle);
        }
    }

    @FXML
    public void unselectConstructions(MouseEvent mouseEvent) {
        if ((isStepActual && constructionUnselect) ||
            (isStepInitial && isRoadBuild && isConstructionBuild)) {
            imgRoad.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
            imgCity.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
            imgVillage.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
            imgCivilisation.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
            selectedConstruction = "";
            deActivateAllVertices();
        }
        if (constructionUnselect) {
            refreshRoadSelectionVertices();
        }
        constructionUnselect = true;
    }

    @FXML
    public void blurVertex(MouseEvent mouseEvent) {
        Circle circle = (Circle) mouseEvent.getSource();
        if (circle.getRadius() != Constants.CONSTRUCTION_RADIUS && circle.getFill() != Constants.COLOR_ROAD_SELECTION_VERTEX) {
            circle.setFill(Constants.COLOR_BLUR_VERTEX);
        }
    }

    @FXML
    public void blendVertex(MouseEvent mouseEvent) {
        Circle circle = (Circle) mouseEvent.getSource();
        Vertex vertex = getCorrespondingVertex(circle);
        if (circle.getRadius() != Constants.CONSTRUCTION_RADIUS && vertex.isActive()) {
            circle.setFill(Constants.COLOR_BLEND_VERTEX);
        }
    }

    @FXML
    void trade(ActionEvent event) {
        if (isStepActual) {
            openDialog(Constants.PATH_VIEW_TRADE_OFFER, "Trade" , null, null);
        }
    }

    @FXML
    public void playDevelopmentCard(ActionEvent event) {
        if (!isStepActual) return;
        if (currentPlayer.getTotalDevelopmentCards() > 0) {
            openDialog(Constants.PATH_VIEW_PLAY_DEVELOPMENT_CARD,
                    "Play Development Card", null, null);
        } else {
            outputNotPossible("No resource");
        }
    }

    @Override
    public void playDevelopmentCard(DevelopmentCard developmentCard) {
        if (!isStepActual) return;

        if (developmentCard != null) {
            developmentCard.performDevelopmentCardFeatures(currentPlayer,
                    getPlayers(), terrainHexes, this);
            if (currentPlayer == playerActual) {
                if (developmentCard.getName().equals(Constants.DEVELOPMENT_CARD_PROFIT_EXCHANGE)) {
                    developmentCardExchangeProfit = developmentCard;
                } else if (developmentCard.getName().equals(Constants.DEVELOPMENT_CARD_ROAD_DESTRUCTION)) {
                    developmentCardDestroyRoad = developmentCard;
                }
            }
        }
    }

    @FXML
    public void destroyRoad(MouseEvent mouseEvent) {
        if (developmentCardDestroyRoad == null) return;
        Line line = (Line)mouseEvent.getSource();
        Road road = getCorrespondingRoad(line);
        developmentCardDestroyRoad.destroyRoad(road,
                currentPlayer, getPlayers());
    }

    @FXML
    public void exchangeTurnProfit(MouseEvent mouseEvent) {
        if (developmentCardExchangeProfit == null) return;
        Circle circle = (Circle) mouseEvent.getSource();
        developmentCardExchangeProfit.exchangeTurnProfit(circle, currentPlayer);
        if (developmentCardExchangeProfit.isDevelopmentCardUsed()) {
            developmentCardExchangeProfit = null;
        }
    }

    @Override
    public void openDialog(String viewPath, String title, DevelopmentCard developmentCard, Trade trade) {
        try {
            Dialog dialog = new Dialog<>();
            dialog.initOwner(root.getScene().getWindow());
            dialog.setTitle(title);
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getClassLoader().getResource(viewPath));
            dialog.getDialogPane().setContent(fxmlLoader.load());

            // Monopoly development card dialog.
            if (viewPath.equals(Constants.PATH_VIEW_DEV_MONOPOL_CARD)) {
                ControllerDevMonopol monopolController = fxmlLoader.getController();
                monopolController.setProperties(currentPlayer, developmentCard, getPlayers());
            }

            // Invention development card dialog.
            else if (viewPath.equals(Constants.PATH_VIEW_DEV_INVENTION_CARD)) {
                ControllerDevInvention inventionController = fxmlLoader.getController();
                inventionController.setProperties(currentPlayer, developmentCard);
            }

            // Trade offer dialog.
            else if (viewPath.equals(Constants.PATH_VIEW_TRADE_OFFER)) {
                ControllerTradeOffer tradeController = fxmlLoader.getController();
                // TODO actual player needs to be passed here afterwards.
                tradeController.setActualPlayerAndLabels(playerActual);
                tradeController.setAllPlayers(getPlayers());
            }

            // Incoming Trade Request dialog.
            else if (viewPath.equals(Constants.PATH_VIEW_TRADE_REQUEST) && trade.isTradePossible()) {
                ControllerTradeRequest tradeRequestController = fxmlLoader.getController();
                tradeRequestController.setProperties(trade);
            }

            // Thief punishment dialog.
            else if (viewPath.equals(Constants.PATH_VIEW_PUNISHMENT)) {
                ControllerThiefPunishment controller = fxmlLoader.getController();
                controller.setPlayer(playerActual);
            }

            // Play development card dialog.
            else if (viewPath.equals(Constants.PATH_VIEW_PLAY_DEVELOPMENT_CARD)) {
                ControllerPlayDevelopmentCard controller = fxmlLoader.getController();
                controller.setProperties(currentPlayer, this);
            }

            dialog.showAndWait();
            if (developmentCardInvention != null) {
                DevelopmentCard card = developmentCardInvention;
                developmentCardInvention = null;
                playDevelopmentCard(card);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    @Override
    public void buyDevelopmentCard(ActionEvent event) {
        if (isStepActual) {
            boolean hasEnoughResources = currentPlayer.hasEnoughResources(Constants.DEVELOPMENT_CARD);
            if (hasEnoughResources) {
                currentPlayer.buyDevelopmentCard(chest);
            } else {
                outputNotPossible("not enough resources");
            }
        }
    }

    private void rollDie() {
        if (isStepActual) {
            die.rollDie();
            Image img = new Image("./com/catan/assets/die"+die.getDice1()+".png");
            getImgDie1().setFill(new ImagePattern(img));
            Image img2 = new Image("./com/catan/assets/die"+die.getDice2()+".png");
            getImgDie2().setFill(new ImagePattern(img2));
        }
    }

    @FXML
    public void makeConstruction(MouseEvent mouseEvent) {
        Circle circle = (Circle) mouseEvent.getSource();
        if (isStepInitial) {
            makeConstructionInitial(circle);
        } else if (isStepActual) {
            makeConstructionActual(circle);
        }
    }

    @FXML
    public void endTurn(ActionEvent actionEvent) throws IOException {
        if(isStepInitial) {
            initialTurn();
        } else if (isStepActual) {
            actualTurn();
            gameLog.add(new String[] {"Round "  + noOfRound + " has ended.", "-1"});
            noOfRound++;
        }
        updateGameLogsInView();
        updateCardsOfActualPlayerInView();
    }

    private void updateCardsOfActualPlayerInView() {
        updateNumbersOfCardsInPanes();
        updateOrderOfCardPanes();
    }

    // updates the card numbers and the number of card images on the cards pane
    private void updateNumbersOfCardsInPanes() {
        int imgHeight = 87;
        int imgWidth = 60;
        int spaceBetweenImages = 6;

        // reason of this map is to update all card panes in a loop
        HashMap<String, Object[]> map = new HashMap<>();

        map.put("wool", new Object[] {woolLabel, woolImages, paneWools, Constants.PATH_RESOURCE_SHEEP});
        map.put("ore", new Object[] {oreLabel, oreImages, paneOres, Constants.PATH_RESOURCE_ORE});
        map.put("lumber", new Object[] {lumberLabel, lumberImages, paneLumbers, Constants.PATH_RESOURCE_WOOD});
        map.put("brick", new Object[] {brickLabel, brickImages, paneBricks, Constants.PATH_RESOURCE_BRICK});
        map.put("grain", new Object[] {grainLabel, grainImages, paneGrains, Constants.PATH_RESOURCE_GRAIN});

        Player actualPlayer = getPlayers().get(0);
        HashMap<String, ArrayList<SourceCard>> sourceCards = actualPlayer.getSourceCards();
        Set<String> keys = sourceCards.keySet();

        for (String key: keys) {
            // this object array contains all card related objects in every iteration
            // ex:
            Object[] currentCardRelated = map.get(key);
            String noOfCards = sourceCards.get(key).size() == 0 ? "" : sourceCards.get(key).size() + "";

            // updates the textual representation of the number of cards
            ((Label)currentCardRelated[0]).setText(noOfCards);
            // if the number of source cards it has is more than what is displayed on the screen,
            // add more card images to the display
            while (sourceCards.get(key).size() > ((ArrayList<ImageView>)currentCardRelated[1]).size() - 1) {
                ImageView imgToAdd = new ImageView(((String)currentCardRelated[3]));
                // puts the image right next to its predecessor
                imgToAdd.setLayoutX(((ArrayList<ImageView>)currentCardRelated[1]).get(((ArrayList<ImageView>)currentCardRelated[1]).size() - 1).getLayoutX() + spaceBetweenImages);
                imgToAdd.setLayoutY(((ArrayList<ImageView>)currentCardRelated[1]).get(((ArrayList<ImageView>)currentCardRelated[1]).size() - 1).getLayoutY());
                imgToAdd.setFitHeight(imgHeight);
                imgToAdd.setFitWidth(imgWidth);
                // puts the image into its corresponding list. ex: grainImage -> grainImages
                ((ArrayList<ImageView>)currentCardRelated[1]).add(imgToAdd);
                // puts image into its corresponding pane. ex : grainImage -> grainsPane
                ((Pane)currentCardRelated[2]).getChildren().add(imgToAdd);
            }
            // if number of source cards it has is less than what is displayed on the screen,
            // remove the displayed card images
            while (sourceCards.get(key).size() < ((ArrayList<ImageView>)currentCardRelated[1]).size() - 1) {
                // removes the image from its corresponding pane
                ((Pane)currentCardRelated[2]).getChildren().remove(((ArrayList<ImageView>)currentCardRelated[1]).get(((ArrayList<ImageView>)currentCardRelated[1]).size() - 1));
                // removes the image from its corresponding list
                ((ArrayList<ImageView>)currentCardRelated[1]).remove(((ArrayList<ImageView>)currentCardRelated[1]).size() - 1);
            }
        }
    }

    // makes the card panes sorted in descending order
    // ex: 4 lumbers will be presented before 3 wools
    private void updateOrderOfCardPanes() {
        Arrays.sort(cardPanes, (a, b) -> b.getChildren().size() - a.getChildren().size());
        for (int i = 0; i < cardPanes.length; i++) {
            cardPanes[i].setLayoutX(cardsPaneLocations[i][0]);
            cardPanes[i].setLayoutY(cardsPaneLocations[i][1]);
        }
    }

    private void updateGameLogsInView() {
        for (; gameLogIterator < gameLog.size(); gameLogIterator++) {
            String[] log = gameLog.get(gameLogIterator);
            Label logLabel = new Label("  " + log[0]);
            logLabel.setMinWidth(400);
            logLabel.setMinHeight(35);
            logLabel.setCursor(Cursor.DEFAULT);
            String marginProperty = " -fx-padding: 2px;" + "-fx-border-insets: 2px;" + "-fx-background-insets: 2px;";
            if (log[0].indexOf("has ended") >= 0) {
                logLabel.setStyle("-fx-background-color: gray; -fx-text-fill: white;" + marginProperty);
            } else {
                Player playerOfLog = getPlayers().get(Integer.parseInt(log[1]));
                String playerColor = playerOfLog.getColor();
                logLabel.setStyle("-fx-background-color:" + playerColor + ";" + "-fx-text-fill: white;" + marginProperty);
            }
            gameLogsFlowPane.getChildren().add(0, logLabel);
        }
    }

    @Override
    public void outputNotPossible(String warningType) {
        Label warning = getWarningLabel();
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2.0), warning);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        switch (warningType) {
            case "Not possible":
                warning.setText("Not Possible");
                warning.setOpacity(1);
                break;
            case "Thief":
                warning.setText("You must move the thief");
                warning.setOpacity(1);
                break;
            case "ProfitExchange":
                warning.setText("Please exchange the profits of terrain hexes by selecting them.");
                warning.setOpacity(1);
                break;
            case "RoadDestruction":
                warning.setText("Please destroy a road.");
                warning.setOpacity(1);
                break;
            default:
                warning.setText("Not enough resource for this type construction");
                warning.setOpacity(1);
                break;
        }
        fadeTransition.play();
    }

    private Road getCorrespondingRoad(Line line) {
        for (Road road: getRoads()) {
            if (road.getRoad() == line) { return road; }
        }
        return null;
    }

    private boolean isVertexSuitableForConstruction(Vertex vertex) {
        ArrayList<Vertex> neighbors = vertex.getNeighbors();
        for (Vertex v: neighbors) {
            if (v.hasConstruction()) { return false; }
        }
        return true;
    }

    private void constructRoad() {
        Road road = getCorrespondingRoad(roadVertex1, roadVertex2);
        if (road != null && !road.getRoad().isVisible()) {
            // coloring road.
            Color color = currentPlayer.getRoadColor();
            road.getRoad().setStroke(color);
            road.getRoad().setVisible(true);
            // adding road;
            currentPlayer.getRoads().add(road);
            activatePlayerVertices();
            isRoadBuild = true;

            if (isStepActual) {
                currentPlayer.subtractPriceOfConstruction(selectedConstruction);
            }
            if (isStepInitial && currentPlayer instanceof PlayerActual) {
                selectConstructionInitial(imgVillage);
            }
            if (isStepInitial && currentPlayer instanceof PlayerAI) {
                tempRoad = road;
            }
        } else {
            currentPlayer = getPlayers().get(playerTurn);
            if(currentPlayer instanceof PlayerActual)
                outputNotPossible(Constants.CONSTRUCTION_STRING);
        }
        unselectConstructions(null);
    }

    private void refreshRoadSelectionVertices() {
        if (roadVertex1 != null && !roadVertex1.hasConstruction()) {
            roadVertex1.getShape().setFill(Constants.COLOR_BLUR_VERTEX);
        }
        if (roadVertex2 != null && !roadVertex2.hasConstruction()) {
            roadVertex2.getShape().setFill(Constants.COLOR_BLUR_VERTEX);
        }
        roadVertex1 = null;
        roadVertex2 = null;
    }

    private Road getCorrespondingRoad(Vertex vertex1, Vertex vertex2) {
        for (Road road: getRoads()) {
            if (road.containsRoad(vertex1, vertex2)) { return road; }
        }
        return null;
    }

    private Vertex getCorrespondingVertex(Circle shape) {
        for (Vertex vertex: getVertices()) {
            if (vertex.getShape() == shape) { return vertex; }
        }
        return null;
    }

    private void activateAllVertices() {
        for (Vertex vertex: getVertices()) {
            if (!vertex.hasConstruction()) { vertex.setActive(true); }
        }
    }

    private void deActivateAllVertices() {
        for (Vertex vertex: getVertices()) {
            vertex.setActive(false);
        }
    }

    private ArrayList<Vertex> getActivatedVertices() {
        ArrayList<Vertex> activated = new ArrayList<>();
        for(Vertex vertex: getVertices()) {
            if (vertex.isActive()) { activated.add(vertex); }
        }
        return activated;
    }

    private void activatePlayerVertices() {
        deActivateAllVertices();
        ArrayList<Road> roads = currentPlayer.getRoads();
        for (Road road: roads) {
            for(Vertex vertex: road.getVertices()) {
                switch (selectedConstruction) {
                    case Constants.ROAD:
                        vertex.setActive(true);
                        activateNeighbours(vertex);
                        break;
                    case Constants.VILLAGE:
                        if (!vertex.hasConstruction() && isVertexSuitableForConstruction(vertex)) {
                            vertex.setActive(true);
                        }
                        break;
                    case Constants.CITY:
                        if (vertex.hasConstruction() && vertex.getSettlement() instanceof Village) {
                            vertex.setActive(true);
                        }
                        break;
                    case Constants.CIVILISATION:
                        if (vertex.hasConstruction() && vertex.getSettlement() instanceof City) {
                            vertex.setActive(true);
                        }
                        break;
                }
            }
        }
    }

    private void  activateNeighbours(Vertex vertex) {
        for (Vertex v: vertex.getNeighbors()) {
            Settlement settlement = v.getSettlement();
            if (settlement == null || settlement.getPlayer() == currentPlayer) {
                Road road = getCorrespondingRoad(vertex, v);
                if (road != null && !road.getRoad().isVisible()) { v.setActive(true); }
            }
        }
    }

    private void setSelectedConstruction(String selectedConstruction) {
        this.selectedConstruction = selectedConstruction;
    }

    private boolean isSelectedSettlement() {
        return selectedConstruction.equals(Constants.VILLAGE)||
               selectedConstruction.equals(Constants.CITY)   ||
               selectedConstruction.equals(Constants.CIVILISATION);
    }

    // ------------ ACTUAL STEP METHODS ---------------- //

    private void actualTurn() {
        developmentCardExchangeProfit = null;
        developmentCardDestroyRoad = null;
        playerTurn = playerTurn % 4;
        currentPlayer = getPlayers().get(playerTurn++);
        rollDie();

        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println(currentPlayer.getName() + " : " + currentPlayer.getColor() + " | Die Result: " + die.getDieSum());
        System.out.println("----------------------------------------------------------------------------------------------");
        gameLog.add(new String[] {"Player " + playerTurn + ": has rolled " + die.getDieSum() + ".", "" + (playerTurn % 4)});
        //  doing thief operations.
        if (die.getDieSum() == 7) {
            thiefResourceCardPunishAI();
            thiefResourceCardPunishActual();
            playThief(currentPlayer);
        }

        getTurnProfit();

        // AI player
        if (currentPlayer instanceof PlayerAI) {
            playAIActualTurn();
            actualTurn();
        }

        // Actual Player
        else if (currentPlayer instanceof PlayerActual) {
            deActivateAllVertices();
            refreshRoadSelectionVertices();
            constructionUnselect = true;
            unselectConstructions(null);
        }
    }

    private void playAIActualTurn() {
        ((PlayerAI) currentPlayer).decideToMakeTrade(this);
        ((PlayerAI) currentPlayer).decideToMakeConstruction(this);
        ((PlayerAI) currentPlayer).decideToBuyDevelopmentCard(this);
        ((PlayerAI) currentPlayer).decideToPlayDevelopmentCard(this);
    }

    @Override
    public void makeTradeForAI(boolean isTradeWithChest) {
        if (isTradeWithChest) { // trade with chest
            HashMap<String, Integer> requestedRC = ((PlayerAI)currentPlayer).getRequestedResourceCardForChest();
            HashMap<String, Integer> offeredRC   = ((PlayerAI)currentPlayer).getOfferedResourceCardForChest(requestedRC);
            new Trade(currentPlayer, null, requestedRC, offeredRC, isTradeWithChest);
        }

        else { // trade between players
            int idOfPlayerToBeTraded = (int)(Math.random() * 4);
            Player playerToBeTraded = getPlayers().get(idOfPlayerToBeTraded);

            if (currentPlayer instanceof PlayerAI && playerToBeTraded != currentPlayer) {
                // setting trade materials
                HashMap<String, Integer> requestedRC = ((PlayerAI)currentPlayer).getRequestedResourceCards(playerToBeTraded);
                HashMap<String, Integer> offeredRC = ((PlayerAI)currentPlayer).getOfferedResourceCards(requestedRC);
                Trade trade = new Trade(currentPlayer, playerToBeTraded, requestedRC, offeredRC, isTradeWithChest);

                // trade request sent to actual player by playerAI
                if (playerToBeTraded == playerActual && trade.isTradePossible()) {
                    openDialog(Constants.PATH_VIEW_TRADE_REQUEST, "Incoming Trade Offer", null, trade);
                }
            }
        }
    }

    private void thiefResourceCardPunishActual() {
        if(playerActual.getTotalCards() > 7){
            openDialog(Constants.PATH_VIEW_PUNISHMENT, "Thief will steal something", null, null);
        }
    }

    private void thiefResourceCardPunishAI(){
        for (int i = 0; i < getPlayers().size(); i++) {
            Player player = getPlayers().get(i);
            if (player instanceof PlayerAI && player.getTotalCards() > 7) {
                ((PlayerAI)player).punish();
            }
        }
    }

    private void playThief(Player currentPlayer){
        if (currentPlayer instanceof PlayerAI) {
            int randomHexIndex = (int)(Math.random() * 19);
            thief.setTerrainHex(terrainHexes.get(randomHexIndex));
            thief.punishUsersAroundHexByThief(currentPlayer);
        }
        else if (currentPlayer instanceof PlayerActual) {
            thief.setCanThiefMove(true);
            outputNotPossible(Constants.THIEF_STRING);
        }
    }

    @FXML
    public void moveThief(MouseEvent mouseEvent){
        if (thief.canThiefMove()) {
            thief.updateLocation(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        }
    }

    @FXML
    public void thiefMoved(MouseEvent mouseEvent){
        if (thief.canThiefMove()) {
            TerrainHex newHex = getHexWithCoordinates(imgMovingThief);
            if (newHex != null) {
                thief.setCanThiefMove(false);
                thief.setTerrainHex(newHex);
                thief.punishUsersAroundHexByThief(currentPlayer);
            }
        }
    }

    private void getTurnProfit() {
        System.out.println("----------------------------------------------------------------------------------------------");
        for (Player player: getPlayers()) {
            player.getTurnProfit(die.getDieSum(), thief.getTerrainHex());
            player.showSourceCards();
        }
        System.out.println("----------------------------------------------------------------------------------------------");
    }

    @Override
    public void makeConstructionActual(Circle circle) {
        if (!currentPlayer.hasEnoughResources(selectedConstruction)) {
            if(currentPlayer instanceof PlayerActual)
                outputNotPossible(Constants.CONSTRUCTION_STRING);
            return;
        }
        if (isSelectedSettlement()) {
            Vertex vertex = getCorrespondingVertex(circle);
            if (vertex != null && isVertexSuitableForConstruction(vertex) && vertex.isActive()) {
                Settlement settlement;
                String imagePath = "";
                switch (selectedConstruction) {
                    case Constants.CITY:
                        imagePath = currentPlayer.getSettlementImagePath(Constants.CITY);
                        settlement = new City(imagePath, vertex, currentPlayer);
                        // FIXME: playerTurn is not who it should correspond to
                        // FIXME: ex: when blue constructs city, the color is purple. something wrong with the
                        // FIXME: incrementation of the currentPlayer variable. It works for some players though.
                        gameLog.add(new String[] {"Player " + playerTurn + ": has built a city.", "" + playerTurn});
                        break;
                    case Constants.VILLAGE:
                        imagePath = currentPlayer.getSettlementImagePath(Constants.VILLAGE);
                        settlement = new Village(imagePath, vertex, currentPlayer);
                        gameLog.add(new String[] {"Player " + playerTurn + ": has built a village.", "" + (playerTurn % 4)});
                        break;
                    default:
                        imagePath = currentPlayer.getSettlementImagePath(Constants.CIVILISATION);
                        settlement = new Civilisation(imagePath, vertex, currentPlayer);
                        gameLog.add(new String[] {"Player " + playerTurn + ": has built civilisation.", "" + (playerTurn % 4)});
                        break;
                }
                Image img = new Image(settlement.getImagePath());
                vertex.getShape().setFill(new ImagePattern(img));
                vertex.getShape().setRadius(Constants.CONSTRUCTION_RADIUS);
                getSettlements().add(settlement);
                currentPlayer.getSettlements().add(settlement);
                vertex.setSettlement(settlement);
                currentPlayer.subtractPriceOfConstruction(selectedConstruction);
                currentPlayer.showSourceCards();
                unselectConstructions(null);
                deActivateAllVertices();
            } else {
                if(currentPlayer instanceof PlayerActual)
                    outputNotPossible("Not possible");
            }
        }
        else if (selectedConstruction.equals(Constants.ROAD)) {
            if (roadVertex1 == null) {
                if (currentPlayer instanceof PlayerActual && isStepInitial && !isRoadBuild) {
                    selectConstructionActual(imgRoad);
                }
                constructionUnselect = false;
                roadVertex1 = getCorrespondingVertex(circle);
                if (circle.getRadius() != Constants.CONSTRUCTION_RADIUS) {
                    circle.setFill(Constants.COLOR_ROAD_SELECTION_VERTEX);
                }
            }
            else {
                constructionUnselect = true;
                roadVertex2 = getCorrespondingVertex(circle);
                constructRoad();
            }
        }
    }

    @Override
    public void makeRoadActualForAI() {
        boolean isRoadConstructed = false;
        while (!isRoadConstructed) {
            ArrayList<Road> roads = currentPlayer.getRoads();
            ArrayList<Vertex> vertices = new ArrayList<>();
            for (Road road: roads) {
                for (Vertex vertex: road.getVertices()) {
                    if (!vertices.contains(vertex)) {
                        vertices.add(vertex);
                    }
                }
            }
            // setting first vertex of road;
            int index = (int) (Math.random() * vertices.size());
            Vertex vertex1 = vertices.get(index);
            // setting second vertex of road;
            index = (int) (Math.random() * vertex1.getNeighbors().size());
            Vertex vertex2 = vertex1.getNeighbors().get(index);
            Road road = getCorrespondingRoad(vertex1, vertex2);

            if (road != null && !road.getRoad().isVisible()) {
                makeConstructionActual(vertex1.getShape());
                makeConstructionActual(vertex2.getShape());
                isRoadConstructed = true;
            }
        }
    }

    @Override
    public void makeVillageActualForAI() {
        ArrayList<Vertex> vertices = getActivatedVertices();
        if (vertices.size() > 0) {
            int index = (int) (Math.random() * vertices.size());
            Circle circle = vertices.get(index).getShape();
            makeConstructionActual(circle);
        }
    }

    @Override
    public void selectActualConstructionForAI(String type) {
        switch (type) {
            case Constants.VILLAGE:
                selectConstructionActual(imgVillage);
                break;
            case Constants.CITY:
                selectConstructionActual(imgCity);
                break;
            case Constants.CIVILISATION:
                selectConstructionActual(imgCivilisation);
                break;
            case Constants.ROAD:
                selectConstructionActual(imgRoad);
                break;
        }
    }

    private void selectConstructionActual(Rectangle rectangle) {
        // setting all of the pane as unselected
        imgRoad.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
        imgCity.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
        imgVillage.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
        imgCivilisation.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);

        // setting selected construction
        if (rectangle == imgRoad) {
            selectedConstruction = Constants.ROAD;
        } else if (rectangle == imgCity) {
            selectedConstruction = Constants.CITY;
        } else if (rectangle == imgVillage) {
            selectedConstruction = Constants.VILLAGE;
        } else if (rectangle == imgCivilisation) {
            selectedConstruction = Constants.CIVILISATION;
        }

        if (!currentPlayer.hasEnoughResources(selectedConstruction)) {
            selectedConstruction = "";
            outputNotPossible(Constants.CONSTRUCTION_STRING);
            return;
        }

        activatePlayerVertices();

        // setting the color of selected construction.
        rectangle.setStroke(Constants.COLOR_CONSTRUCTION_SELECTED);

        // disabling road selection
        refreshRoadSelectionVertices();
        constructionUnselect = false;
    }

    // ------------ INITIAL STEP METHODS ---------------- //

    private void initialTurn() throws IOException {
        while (true) {
            playerTurn = playerTurn % 4;
            Player player = getPlayers().get(playerTurn);
            currentPlayer = player;
            activateAllVertices();

            // AI player
            if (player instanceof PlayerAI) {
                playAIInitialTurn();
                isRoadBuild = false;
                isConstructionBuild = false;
            }
            // Actual Player
            else if (player instanceof  PlayerActual) {
                if ((isStepInitial && isRoadBuild && isConstructionBuild)) {
                    isRoadBuild = false;
                    isConstructionBuild = false;
                } else {
                    if(currentPlayer instanceof PlayerActual)
                        outputNotPossible("Not possible");
                    selectConstructionInitial(imgRoad);
                    return;
                }
            }
            playerTurn++;
            initialStepCount++;

            // terminating initial step
            if (initialStepCount == 8) {
                isStepActual = true;
                isStepInitial = false;
                actualTurn();
                return;
            }
        }
    }

    private void playAIInitialTurn() {
        tempRoad = null;
        tempSettlement = null;

        while (tempSettlement == null || tempRoad == null) {
            if ((tempRoad != null)) {
                tempRoad.getRoad().setStroke(Color.BLACK);
                tempRoad.getRoad().setVisible(false);
                currentPlayer.getRoads().remove(tempRoad);
                tempRoad = null;
            }

            activateAllVertices();
            setSelectedConstruction(Constants.ROAD);

            // setting first vertex of road;
            int index = (int) (Math.random() * getActivatedVertices().size());
            Vertex vertex = getActivatedVertices().get(index);
            makeConstructionInitial(vertex.getShape());

            // setting second vertex of road;
            index = (int) (Math.random() * vertex.getNeighbors().size());
            vertex = vertex.getNeighbors().get(index);
            makeConstructionInitial(vertex.getShape());

            setSelectedConstruction(Constants.VILLAGE);
            if (tempRoad != null) {
                ArrayList<Vertex> twoVertex = new ArrayList<>();
                twoVertex.add(tempRoad.getVertices().get(0));
                twoVertex.add(tempRoad.getVertices().get(1));

                if (isVertexSuitableForConstruction(twoVertex.get(0))) {
                    makeConstructionInitial(twoVertex.get(0).getShape());
                }
                else if (isVertexSuitableForConstruction(twoVertex.get(1))) {
                    makeConstructionInitial(twoVertex.get(0).getShape());
                }
            }
        }
        tempRoad = null;
        tempSettlement = null;
    }

    private void makeConstructionInitial(Circle circle) {
        if (selectedConstruction.equals(Constants.CITY)   ||
            selectedConstruction.equals(Constants.VILLAGE)||
            selectedConstruction.equals(Constants.CIVILISATION)) {

            if (isRoadBuild && !isConstructionBuild) {
                selectConstructionInitial(imgVillage);
            }

            Vertex vertex = getCorrespondingVertex(circle);
            if (vertex != null && isVertexSuitableForConstruction(vertex) && vertex.isActive()) {
                isConstructionBuild = true;
                String imagePath = currentPlayer.getSettlementImagePath(Constants.VILLAGE);
                Settlement settlement = new Village(imagePath, vertex, currentPlayer);

                // loading image
                Image img = new Image(settlement.getImagePath());
                vertex.getShape().setFill(new ImagePattern(img));
                vertex.getShape().setRadius(Constants.CONSTRUCTION_RADIUS);

                // adding settlement
                getSettlements().add(settlement);
                currentPlayer.getSettlements().add(settlement);
                vertex.setSettlement(settlement);
                unselectConstructions(null);
                activatePlayerVertices();
                tempSettlement = settlement;
            } else {
                if(currentPlayer instanceof PlayerActual)
                    outputNotPossible("Not possible");
            }
        }
        else if (selectedConstruction.equals(Constants.ROAD)) {
            if (roadVertex1 == null) {
                if (!isRoadBuild) {
                    selectConstructionInitial(imgRoad);
                }

                constructionUnselect = false;
                Vertex vertex = getCorrespondingVertex(circle);
                if (vertex != null && !vertex.hasConstruction()) {
                    roadVertex1 = vertex;
                    Color color = Constants.COLOR_ROAD_SELECTION_VERTEX;
                    roadVertex1.getShape().setFill(color);
                }
            }
            else {
                constructionUnselect = true;
                Vertex vertex = getCorrespondingVertex(circle);
                if (vertex != null && !vertex.hasConstruction()) {
                    roadVertex2 = vertex;
                    constructRoad();
                } else {
                    refreshRoadSelectionVertices();
                    if(currentPlayer instanceof PlayerActual)
                        outputNotPossible("Not possible");
                }
            }
        }
    }

    private void selectConstructionInitial(Rectangle rectangle) {
        // setting all of the pane as unselected
        imgRoad.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
        imgCity.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
        imgVillage.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);
        imgCivilisation.setStroke(Constants.COLOR_CONSTRUCTION_UNSELECTED);

        // setting selected construction
        if (rectangle == imgRoad) {
            selectedConstruction = Constants.ROAD;
        } else if (rectangle == imgVillage) {
            selectedConstruction = Constants.VILLAGE;
        }

        //if (currentPlayer instanceof PlayerActual) {
        if (isStepInitial && !isRoadBuild) {
            selectedConstruction = Constants.ROAD;
            rectangle = imgRoad;
        }
        else if (isStepInitial && !isConstructionBuild) {
            selectedConstruction = Constants.VILLAGE;
            rectangle = imgVillage;
        }
        else if (isStepInitial) {
            selectedConstruction = "";
            return;
        }

        // setting the color of selected construction.
        rectangle.setStroke(Constants.COLOR_CONSTRUCTION_SELECTED);

        // disabling road selection
        refreshRoadSelectionVertices();
        constructionUnselect = false;
    }
}