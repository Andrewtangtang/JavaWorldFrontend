package com.javaworld.clientapp.scene;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FontFactory;
import com.javaworld.clientapp.Application;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import static com.javaworld.clientapp.Application.pixelFontFact;

public class MainMenu {
    private final Font titleFontOut, titleFontIn, subtitleFont, buttonFont;
    private final Application application;
    private Pane titleContainer;
    private VBox formContainer;

    public MainMenu(Application application) {
        this.application = application;
        FontFactory titleInFontFact = FXGL.getAssetLoader().loadFont("8-bit Arcade In.ttf");
        FontFactory titleOutFontFact = FXGL.getAssetLoader().loadFont("8-bit Arcade Out.ttf");
        titleFontIn = titleInFontFact.newFont(100);
        titleFontOut = titleOutFontFact.newFont(100);
        subtitleFont = pixelFontFact.newFont(25);
        buttonFont = pixelFontFact.newFont(20);
    }

    public void initUI() {
        FXGL.getGameScene().getRoot().getStylesheets().add(FXGL.getAssetLoader().loadCSS("styles.css").getExternalForm());

        ImageView background = FXGL.getAssetLoader().loadTexture("title_screen_background.jpg");
        background.setEffect(new BoxBlur());
        background.setFitWidth(FXGL.getAppWidth());
        background.setFitHeight(FXGL.getAppHeight());

        FXGL.entityBuilder()
                .view(background)
                .zIndex(-1) // Ensure it is drawn behind other entities
                .buildAndAttach();

        // Title background
        Image loadBg = FXGL.getAssetLoader().loadImage("title_background.png");
        Image titleBackground = new Image(loadBg.getUrl(), loadBg.getWidth() * 1.6, loadBg.getHeight() * 1.6,
                true, false);
        ImageView titleView = new ImageView();
        titleView.setSmooth(false);
        titleView.setEffect(new DropShadow());
        titleView.setImage(titleBackground);
        titleView.setFitWidth(titleBackground.getWidth());
        titleView.setFitHeight(titleBackground.getHeight());
        titleView.setTranslateX((int) ((FXGL.getAppWidth() - titleView.getFitWidth()) / 2));
        titleView.setTranslateY(40);

        // Title
        Text title = new Text("Java\nWorld");
        title.setFont(titleFontIn);
        title.setFill(Color.rgb(158, 65, 43));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setTranslateX((FXGL.getAppWidth() - title.getLayoutBounds().getWidth()) / 2);
        title.setTranslateY(titleView.getTranslateY() + titleBackground.getHeight() / 2 - title.getFont().getSize() / 4);
        Text titleOut = new Text(title.getText());
        titleOut.setFont(titleFontOut);
        titleOut.setFill(Color.BLACK);
        titleOut.setTextAlignment(TextAlignment.CENTER);
        titleOut.setTranslateX(title.getTranslateX());
        titleOut.setTranslateY(title.getTranslateY());

        Text subtitle = new Text("Welcome to Java World");
        subtitle.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0, 0, 0, 0.5), 7, 2, 0, 0));
        subtitle.setRotate(-20);
        subtitle.setFill(Color.YELLOW);
        subtitle.setFont(subtitleFont);
        subtitle.setTranslateX(titleView.getTranslateX() + titleView.getFitWidth() - subtitle.getBoundsInLocal().getWidth() / 2);
        subtitle.setTranslateY(titleView.getTranslateY() + titleView.getFitHeight() - 50);
        ScaleTransition st = new ScaleTransition(Duration.millis(500), subtitle);
        st.setByX(0.2f);
        st.setByY(0.2f);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.play();

        titleContainer = new Pane(titleView, titleOut, title, subtitle);

        // Text field for username
        TextField ipField = new TextField();
        ipField.getStyleClass().add("roundBorder");
        ipField.setPrefWidth(160);
        ipField.setPrefHeight(50);
        ipField.setMaxWidth(160);
        ipField.setPromptText("localhost");
        ipField.setText("localhost");
        ipField.setFont(buttonFont);

        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("roundBorder");
        usernameField.setPrefWidth(160);
        usernameField.setPrefHeight(50);
        usernameField.setMaxWidth(160);
        usernameField.setPromptText("Name");
        usernameField.setFont(buttonFont);

        // Button to extract the input and perform an action
        Button connectButton = new Button("Start");
        connectButton.setPrefWidth(160); // Set preferred width
        connectButton.setPrefHeight(50); // Set preferred height
        connectButton.getStyleClass().add("roundBorder");
        connectButton.setFont(buttonFont);

        // Create a VBox to hold all UI components
        formContainer = new VBox(30, ipField, usernameField, connectButton); // 20 pixels space between elements
        formContainer.setAlignment(Pos.CENTER); // Center alignment
        formContainer.setTranslateY(titleView.getTranslateY() + titleView.getFitHeight() + 50);
        formContainer.setPrefWidth(FXGL.getAppWidth());

        connectButton.setOnAction(e -> {
            connectButton.setDisable(true);
            if (application.joinServer(ipField.getText().trim(), usernameField.getText().trim()))
                closeUI();
            connectButton.setDisable(false);
        });

        FXGL.addUINode(titleContainer);
        FXGL.addUINode(formContainer);
    }

    public void closeUI() {
        FXGL.removeUINode(titleContainer);
        FXGL.removeUINode(formContainer);
    }
}
