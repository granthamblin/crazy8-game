import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;

public class Intro {
    private Stage primaryStage;
    private Scene gameScene;

    String name;

    //sends name over to app.java
    public String getName() {
        return name;
    }

    public Intro(Stage primaryStage, Scene gameScene, Scene rulesScene) {
        this.primaryStage = primaryStage;
        this.gameScene = gameScene;
    }

    public void show() {
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(50);

        BackgroundFill fill = new BackgroundFill(Color.rgb(85, 153, 255), null, null);
        layout.setBackground(new Background(fill));

        Scene introScene = new Scene(layout, 850, 700);
        primaryStage.setScene(introScene);
        primaryStage.show();

        Label nameLabel = new Label("Enter your name:");
        TextField nameField = new TextField();

        Button startButton = new Button("Start");

        
        startButton.setOnAction(e -> {
            //gets name and sets to default value if undefined
            name = nameField.getText();
            if (name == "") {
                name = "Player 1";
            }
            //switches to game screen
            primaryStage.setScene(gameScene);
        });


        //title
        VBox headerContainer = new VBox();
        headerContainer.setAlignment(Pos.CENTER);
        Label headerLabel = new Label("Crazy 8");
        headerLabel.setStyle("-fx-font-size: 50px; -fx-text-fill: white; -fx-font-weight: bolder;");
        headerContainer.getChildren().add(headerLabel);

        //start button
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(10);
        buttonContainer.getChildren().addAll(startButton);

        //name container
        HBox inputContainer = new HBox();
        inputContainer.setAlignment(Pos.CENTER);
        inputContainer.setSpacing(10);
        inputContainer.getChildren().addAll(nameLabel, nameField);

        layout.getChildren().addAll(headerContainer, inputContainer, buttonContainer);
    }
}