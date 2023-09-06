//imports
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;


public class App extends Application  {

    //global variables to make my life easier
    Card centerCard;
    Button centerCardButton;
    GridPane opponentHand;
    GridPane playerHand;
    Button testButton;
    GridPane gameBoard;
    Button cardButton;
    Scene gameScene;
    boolean player1Turn;
    ArrayList<Card> player1Cards;
    ArrayList<Card> player2Cards;
    Button shuffleButton;
    Alert gameOverAlert;
    boolean playerWon = false;
    VBox playerHandContainer;
    Pane centerCardPane;
    String name;
    Intro introScreen;
    long startTime;
    AnimationTimer timer;


    //formats the long variable produced by timer
    public String formatElapsedTime(long elapsedTime) {
        long minutes = elapsedTime / 1000000000 / 60;
        long seconds = (elapsedTime / 1000000000) % 60;
     
        return String.format("%02d:%02d", minutes, seconds);
    }


    public void broadcastGameOver(boolean playerWon) {
        //get time
        timer.stop();
        long elapsedTime = System.nanoTime() - startTime;
        String formattedTime = formatElapsedTime(elapsedTime);

        //configs alert message
        gameOverAlert = new Alert(AlertType.CONFIRMATION);
        if (playerWon) {
            gameOverAlert.setTitle("You win!");
            gameOverAlert.setHeaderText(name + " Wins");
            
        } else {
            gameOverAlert.setTitle("You lost!");
            gameOverAlert.setHeaderText("Opponent Wins");
        }
        gameOverAlert.setContentText("Do you want to restart the game?\n" + "Game Length: " + formattedTime);

        //restarts program
        Optional<ButtonType> result = gameOverAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            startGame();
        }
        //quits program
        if (result.isPresent() && result.get() == ButtonType.CANCEL) {
            System.exit(0);
        }
    }
    
    public void shuffleDeck() {
        //performs shuffle algorithm, then rerenders
        for (int i = 0; i < player1Cards.size(); i++) {
            int pos = (int) Math.floor(Math.random() * player1Cards.size());
            Card temp = player1Cards.get(i);
            player1Cards.set(i, player1Cards.get(pos));
            player1Cards.set(pos, temp);
        }
        renderCards(player1Cards);
    }
    
    //sends play by play turns to a file 
    public void appendToGameFeed(String message) {
        //true parameter makes it so messages can be appended to an existing file
        try {
            FileWriter file = new FileWriter("game_feed.txt", true);   
            file.write(message + "\n");  
            file.close();  
        } catch (IOException e) {
            System.out.print("Error");
            e.printStackTrace();
        }
    }


    public void opponentTurn() {
        //protects the program from user playing card when it's not their turn
        boolean canPlayCard = false;

        for (int i = 0; i <= player2Cards.size() - 1; i += 1) {
            Card card = player2Cards.get(i);
            //checks if any cards in deck match the center card
            if (checkCard(card, centerCard)) {
                player2Cards.remove(card);
                centerCard = card;
                renderOpponentCards(player2Cards);
                appendToGameFeed("Opponent played a " + card.colour + " " + card.number); 
                canPlayCard = true;
                break;
            }
        }

        //if there are no cards that match
        if (!canPlayCard) {
            player2Cards.add(generateCard());
            renderOpponentCards(player2Cards);
            appendToGameFeed("Opponent drew a card"); 
        }
        //switches turn and renders the played card
        player1Turn = true;
        styleCard(centerCardButton, centerCard); 
    }

    public void renderCards(ArrayList<Card> cards) {
        playerHand.getChildren().clear();

        for (int i = 0; i < cards.size(); i++) {
            //iterates through the arraylist and creates a card for each spot
            Card card = cards.get(i);
            cardButton = new Button();
            styleCard(cardButton, card);
            playerHand.add(cardButton, i, 0);

            //sets an event for each card when generated
            cardButton.setOnAction(e -> {
                //gets name from textbox, under when clicked on card for delay purposes
                name = introScreen.getName();

                //when the card is pressed, check if it is playable
                if (player1Turn) {
                    if (checkCard(card, centerCard)) {
                        cards.remove(card);
                        centerCard = card;
                        styleCard(centerCardButton, centerCard);
                        appendToGameFeed(name + " played a " + card.colour + " " + card.number);
                        renderCards(cards);
                        player1Turn = false;
                        opponentTurn();
                    }
                }
            });

        }

        //if player wins (no cards left)
        if (cards.size() == 0) {
            playerWon = true;
            appendToGameFeed(name + " Won!");
            broadcastGameOver(playerWon);
        }
    }


    public void renderOpponentCards(ArrayList<Card> cards) {
        opponentHand.getChildren().clear();
        //iterates through opponent arraylist and creates a card for every spot
        for (int i = 0; i <= cards.size() - 1; i += 1) {
            Card card = cards.get(i);
            Button cardButton = new Button();
            cardButton.getStyleClass().add("card-button");
            cardButton.getStyleClass().add("facing-down");
            opponentHand.add(cardButton, i, 0);
        }

        //if opponent wins
        if (cards.size() == 0) {
            playerWon = false;
            appendToGameFeed("Opponent Won!");
            broadcastGameOver(playerWon);
        }
    }

    //does the selected card have either the same colour or number as the center card?
    public boolean checkCard(Card card, Card centerCard) {
        return card.number == centerCard.number || card.colour.equalsIgnoreCase(centerCard.colour);
    }

    //applies respective colour styles to card, as well as general card styles
    public void styleCard(Button cardButton, Card card) {
        cardButton.setText(String.valueOf(card.number));
        cardButton.getStyleClass().removeAll("red", "green", "blue", "yellow");
        cardButton.getStyleClass().add("card-button");
        cardButton.getStyleClass().add(card.colour);
    }

    //creates the number and colour of a card
    public Card generateCard() {
        String[] colours = {"red", "green", "blue", "yellow"};
        int coloursIndex = (int) Math.floor(Math.random() * colours.length);
        int numberIndex = (int) Math.floor(Math.random() * 8 + 1);
        return new Card(colours[coloursIndex], numberIndex);
    }

    //everything that has to be reset or created when a new game starts
    public void startGame() {

        player1Cards.clear();
        player2Cards.clear();
        gameBoard.getChildren().clear();
        playerHand.getChildren().clear();
        opponentHand.getChildren().clear();
    
        for (int i = 0; i <= 7; i += 1) {
            player1Cards.add(generateCard());
            player2Cards.add(generateCard());
        }
    
        Card drawPileCard = generateCard();
        Button drawPileCardButton = new Button();
        drawPileCardButton.getStyleClass().add("card-button");
        drawPileCardButton.getStyleClass().add("facing-down");
        gameBoard.add(drawPileCardButton, 0, 1);
    
        drawPileCardButton.setOnAction(e -> {
            if (player1Turn) {
                player1Cards.add(generateCard());
                renderCards(player1Cards);
                player1Turn = false;
                appendToGameFeed(name + " drew a card"); 
                opponentTurn();
            }
        });
    
        player1Turn = true;
        renderCards(player1Cards);
        renderOpponentCards(player2Cards);
    
        try {
            File file = new File("game_feed.txt");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        appendToGameFeed("New game started!\n\n");
    
        centerCard = generateCard();
        centerCardButton = new Button();
        styleCard(centerCardButton, centerCard);
        gameBoard.add(centerCardButton, 1, 1);
    
        shuffleButton = new Button("Shuffle");
        shuffleButton.setOnAction(e -> shuffleDeck());

        startTime = System.nanoTime();

        //start the game timer
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsedTime = now - startTime;
            }
        };
        timer.start();
    }

    
    @Override
    public void start(Stage primaryStage) {
        //ui and layout purposes
        primaryStage.setTitle("Crazy 8");
    
        player1Cards = new ArrayList<>();
        player2Cards = new ArrayList<>();
    
        BorderPane layout = new BorderPane();

        BackgroundFill fill = new BackgroundFill(Color.rgb(80, 160, 80), null, null);
        layout.setBackground(new Background(fill));
    
        //contains center card, draw pile card
        gameBoard = new GridPane();
        gameBoard.setAlignment(Pos.CENTER);
        gameBoard.setHgap(10);
    
        //player's hand of cards
        playerHand = new GridPane();
        playerHand.setVgap(10);
        playerHand.setPadding(new Insets(10));
        playerHand.setAlignment(Pos.BOTTOM_CENTER);
        playerHand.setHgap(10);
    
        //opponent hand of cards
        opponentHand = new GridPane();
        opponentHand.setHgap(10);
        opponentHand.setVgap(10);
        opponentHand.setPadding(new Insets(10));
        opponentHand.setAlignment(Pos.TOP_CENTER);
    
        playerHandContainer = new VBox();
        playerHandContainer.setAlignment(Pos.CENTER);
        playerHandContainer.setSpacing(10);
    
        shuffleButton = new Button("Shuffle");
        shuffleButton.setOnAction(e -> shuffleDeck());
        playerHandContainer.getChildren().addAll(shuffleButton, playerHand);
    
        layout.setCenter(gameBoard);
        layout.setBottom(playerHandContainer);
        layout.setTop(opponentHand);
    
        gameScene = new Scene(layout, 850, 700);
        gameScene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        primaryStage.setScene(gameScene);
        primaryStage.show();
    
        //intro screen is showed first instead of game screen
        introScreen = new Intro(primaryStage, gameScene, null);
        introScreen.show();

        startGame();
    }


    public static void main(String[] args) {
        launch(args);
    }
}