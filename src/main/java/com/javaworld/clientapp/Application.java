package com.javaworld.clientapp;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.adapter.entity.Player;
import com.javaworld.client.ClientGameEvent;
import com.javaworld.client.ClientGameManager;
import com.javaworld.core.Chunk;
import com.javaworld.core.GameManager;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;
import com.javaworld.core.update.ChunkUpdate;
import com.javaworld.data.ServerResponse;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.util.HashMap;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;


public class Application extends GameApplication implements ClientGameEvent {
    public static final int PIXELSIZE = 32;
    public static final double gamestartX = 640 - 16 * PIXELSIZE;
    public static final double gamestartY = 360 + 7 * PIXELSIZE;
    public static final double UIWebSizeX = 640;
    public static final double UIWebSizeY = 360 + 8 * PIXELSIZE;
    int tileSize = 32; // 每個 tile 的寬高
    int numTiles = 32; // 網格的寬度和高度（16x16）
    int totalWidth = tileSize * numTiles;
    int totalHeight = tileSize * numTiles;
    private static final Font defaultFont = Font.font("System", 25);
    private static ClientGameManager gameManager;
    private static final HashMap<Integer, Entity> KeyToId = new HashMap<>();
    private static String username;
    private static Entity[][] blocks;

    private TextArea scoreBoard, leaderboard;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setManualResizeEnabled(true);
        settings.setPreserveResizeRatio(true);
        settings.setFullScreenAllowed(true);
//        settings.setFullScreenFromStart(true);
        settings.setTitle("JavaWorld");
        settings.setAppIcon("icon.jpg");
    }

    @Override
    protected void initUI() {
        FXGL.getGameScene().getRoot().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        Image bgImage = new Image("com/javaworld/clientapp/background2.jpg"); // Ensure the path is correct
        Rectangle bg = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), new ImagePattern(bgImage));
        FXGL.entityBuilder()
                .view(bg)
                .zIndex(-1) // Ensure it is drawn behind other entities
                .buildAndAttach();

        // Image behind the text field
        ImageView imageView = new ImageView();
        Image behindTextFieldImage = new Image("com/javaworld/clientapp/textfield.png"); // Ensure the path is correct
        imageView.setImage(behindTextFieldImage);
        imageView.setFitWidth(237 * 1.5);
        imageView.setFitHeight(157 * 1.5);
        imageView.setTranslateX((FXGL.getAppWidth() - imageView.getFitWidth()) / 2);
        imageView.setTranslateY(50);
        imageView.setSmooth(false);

        FXGL.addUINode(imageView);
        // Title
        Text title = FXGL.getUIFactoryService().newText("Java World", Color.BLACK, 35);
        title.setTranslateX((FXGL.getAppWidth() - title.getLayoutBounds().getWidth()) / 2);
        title.setTranslateY(imageView.getTranslateY() + imageView.getFitHeight() / 2 + title.getFont().getSize() / 2);
        FXGL.addUINode(title);


        // Create a VBox to hold all UI components
        VBox uiContainer = new VBox(30); // 20 pixels space between elements
        uiContainer.setAlignment(Pos.CENTER); // Center alignment
        uiContainer.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight()); // Make the VBox the size of the app
        uiContainer.setTranslateY(50); // Move the VBox down by 100 pixels
        uiContainer.getChildren().add(FXGL.getUIFactoryService().newText("Welcome to Java World", Color.BLACK, 22));

        // Text field for username
        TextField usernameField = new TextField();
        usernameField.setPrefWidth(160); // Set preferred width
        usernameField.setMaxWidth(160);   // Optional: Set maximum width
        usernameField.setPrefHeight(50);  // Set preferred height
        usernameField.setPromptText("Your Player Name");
        usernameField.setFont(defaultFont); // Change "System" to your desired font if needed

        usernameField.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
        usernameField.setBorder(Border.EMPTY); // Optionally remove the border for complete transparency
        uiContainer.getChildren().add(usernameField);

        // Button to extract the input and perform an action
        Button submitButton = new Button("Start");
        submitButton.setPrefWidth(160); // Set preferred width
        submitButton.setPrefHeight(50); // Set preferred height
        submitButton.setFont(defaultFont); // Change "System" to your desired font if needed
        submitButton.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
        submitButton.setOnAction(e -> {
            submitButton.setDisable(true);
            gameManager = new ClientGameManager("localhost", 25565, this);
            getGameWorld().addEntityFactory(new GameFactory());
            GameManager.loadResources();
            username = usernameField.getText();
            ServerResponse result = gameManager.connect(username);
            if (result.success) {
                FXGL.getDialogService().showMessageBox("Welcome to Java World");
                // Clear existing UI components
                FXGL.getGameScene().removeUINode(uiContainer);
                FXGL.getGameScene().removeUINode(imageView);
                FXGL.getGameScene().removeUINode(title);
                initGameComponents();
                initGameUI();
            } else {
                FXGL.getDialogService().showMessageBox(result.message);
                submitButton.setDisable(false);
            }
        });
        uiContainer.getChildren().add(submitButton); // Add button to VBox
        FXGL.addUINode(uiContainer); // Add VBox to the FXGL UI
    }

    private void initGameUI() {
        // 创建并配置三个WebView控制按钮
        createRankLists();
        createScoreboard();
        createWebViewControl(0, 0, null, "Codeview", 640 - 80, gamestartY + 50);
//      createWebViewControl(FXGL.getAppWidth() / 2d, 0, "https://www.google.com.tw/?hl=zh_TW", "Console", FXGL.getAppWidth() / 2 - 80, gamestartY + 50);
//      createWebViewControl(FXGL.getAppWidth() / 2d, 0, "https://www.google.com.tw/?hl=zh_TW", "Google", FXGL.getAppWidth() / 2 + 96, gamestartY + 50);

    }

    private void createWebViewControl(double x, double y, String url, String buttonName, double buttonX, double buttonY) {
        Button button = new Button(buttonName);
        button.setPrefWidth(160); // Set preferred width
        button.setPrefHeight(50); // Set preferred height
        button.setFont(defaultFont);
        button.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
        // Default to code editor
        if (url == null) {
            final CodeEditor editor = new CodeEditor();
            WebView codeWebView = editor.webview;
            codeWebView.setPrefSize(UIWebSizeX, UIWebSizeY);
            codeWebView.setVisible(false);
            Button runButton = new Button("Run");
            runButton.setPrefWidth(160); // Set preferred width
            runButton.setPrefHeight(50); // Set preferred height
            runButton.setFont(defaultFont);
            runButton.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
            runButton.setVisible(false);
            runButton.setOnAction(e -> {
                String code = editor.getCodeAndSnapshot();
                ServerResponse response = gameManager.sendPlayerCode(code);
                if (!response.success)
                    System.out.println(response.message);
                runButton.setVisible(!runButton.isVisible());
                codeWebView.setVisible(!codeWebView.isVisible());
            });
            button.setOnAction(e -> {
                codeWebView.setVisible(!codeWebView.isVisible());
                runButton.setVisible(!runButton.isVisible());
            });
            FXGL.addUINode(runButton, buttonX - 500, buttonY);
            FXGL.addUINode(codeWebView, x, y);
        } else {
            WebView webView = new WebView();
            webView.getEngine().load(url);
            webView.setPrefSize(UIWebSizeX, UIWebSizeY);
            webView.setVisible(false);
            button.setOnAction(e -> webView.setVisible(!webView.isVisible()));
            FXGL.addUINode(webView, x, y);
        }
        FXGL.addUINode(button, buttonX, buttonY);
    }

    private void createRankLists() {
        // 創建一個VBox作為主布局
        VBox vbox = new VBox();
        vbox.setPrefSize(180, 330);
        vbox.setStyle("-fx-background-color: F5F5DCA0;");  // 設置背景顏色
        vbox.setAlignment(Pos.CENTER);  // 將VBox內容居中

        Label title = new Label("RankLists");
        title.setFont(Font.font("System", 20));  // 設置字體為系統字體並設置大小
        title.setAlignment(Pos.CENTER);  // 將標題居中
        title.setPadding(new Insets(5));  // 設置標題的內邊距
        vbox.getChildren().add(title);  // 將標題添加到VBox

        // 創建TextArea來顯示排行榜
        leaderboard = new TextArea();
        leaderboard.setEditable(false); // 設置為不可編輯
        leaderboard.setWrapText(true); // 啟用文字自動換行
        leaderboard.setFont(Font.font("Courier New", 18)); // 設置字體大小為18
        leaderboard.getStyleClass().add("leaderboard");
        leaderboard.setMouseTransparent(true);
        leaderboard.setFocusTraversable(false);

        // 將TextArea添加到VBox並讓它填滿剩餘空間
        VBox.setVgrow(leaderboard, javafx.scene.layout.Priority.ALWAYS); // 使TextArea能夠填滿VBox剩餘的空間
        vbox.getChildren().add(leaderboard);

        FXGL.addUINode(vbox, FXGL.getAppWidth() - 180, 0);
    }

    private void createScoreboard() {
        // 創建一個VBox作為主布局
        VBox vbox = new VBox();
        vbox.setPrefSize(180, 80);
        vbox.setStyle("-fx-background-color: F5F5DCA0;");  // 設置背景顏色
        vbox.setAlignment(Pos.CENTER);  // 將VBox內容居中

        Label title = new Label("Scoreboard");
        title.setFont(Font.font("System", 20));  // 設置字體為系統字體並設置大小
        title.setAlignment(Pos.CENTER);  // 將標題居中
        title.setPadding(new Insets(5));  // 設置標題的內邊距
        vbox.getChildren().add(title);  // 將標題添加到VBox

        // 創建TextArea來顯示排行榜
        scoreBoard = new TextArea();
        scoreBoard.setEditable(false); // 設置為不可編輯
        scoreBoard.setWrapText(true); // 啟用文字自動換行
        scoreBoard.setFont(Font.font("Courier New", 18)); // 設置字體大小為18
        scoreBoard.getStyleClass().add("leaderboard");
        scoreBoard.setMouseTransparent(true);
        scoreBoard.setFocusTraversable(false);

        scoreBoard.setText("Score: 0");
        // 將TextArea添加到VBox並讓它填滿剩餘空間
        VBox.setVgrow(scoreBoard, javafx.scene.layout.Priority.ALWAYS); // 使TextArea能夠填滿VBox剩餘的空間
        vbox.getChildren().add(scoreBoard);
        FXGL.addUINode(vbox, FXGL.getAppWidth() / 2 - 90, 0);
    }

    private void initGameComponents() {
//        getGameWorld().addEntityFactory(new GameFactory());
        int startX = (FXGL.getAppWidth() / 2) - (totalWidth / 2);
        int startY = (FXGL.getAppHeight() / 2) - (totalHeight / 2);
//        for (int i = 0; i < numTiles; i++) {
//            for (int j = 0; j < numTiles; j++) {
//                spawn("stone", startX + i * tileSize, startY + j * tileSize);
//                spawn("soil", startX + i * tileSize, startY + j * tileSize);
//                spawn("grass", startX + i * tileSize, startY + j * tileSize);
//            }
//        }

    }


    public static double coordinateCalculateX(double x) {
        return gamestartX + x * PIXELSIZE - (PIXELSIZE >> 1);
    }

    public static double coordinateCalculateY(double y) {
        return gamestartY - y * PIXELSIZE + (PIXELSIZE >> 1);
    }

    public static double coordinateCalculateX(int x) {
        return gamestartX + x * PIXELSIZE;
    }

    public static double coordinateCalculateY(int y) {
        return gamestartY - y * PIXELSIZE;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void entityCreate(com.javaworld.core.entity.Entity entity) {
        if (entity instanceof Player player) {
            String name = player.getName();
            Platform.runLater(() -> {
                Entity e = spawn("player", new SpawnData(coordinateCalculateX(entity.getPosition().x), coordinateCalculateY(entity.getPosition().y)).put("name", name));
                KeyToId.put(entity.getSerial(), e);
            });
        } else if (entity.entityData.entityType == EntityType.PLANT) {
            Platform.runLater(() -> {
                Entity e = spawn("plant", new SpawnData(coordinateCalculateX(entity.getPosition().x), coordinateCalculateY(entity.getPosition().y)));
                KeyToId.put(entity.getSerial(), e);
            });
        }
    }

    @Override
    public void entityUpdate(com.javaworld.core.entity.Entity entity) {
        Platform.runLater(() -> {
            Entity e = KeyToId.get(entity.getSerial());
            e.setPosition(coordinateCalculateX(entity.getPosition().x), coordinateCalculateY(entity.getPosition().y));
        });
    }


    @Override
    public void entityRemove(com.javaworld.core.entity.Entity entity) {
        Platform.runLater(() -> {
            FXGL.getGameWorld().removeEntity(KeyToId.get(entity.getSerial()));
            KeyToId.remove(entity.getSerial());
        });
    }

    @Override
    public void blockCreate(int x, int y, int z, BlockData blockData, BlockState blockState) {
        Platform.runLater(() -> {
            Entity blockEntity = switch (blockData.name) {
                case "stone" -> spawn("stone", new SpawnData(coordinateCalculateX(x), coordinateCalculateY(y), z));
                case "dirt" -> spawn("soil", new SpawnData(coordinateCalculateX(x), coordinateCalculateY(y), z));
                case "grass_block" ->
                        spawn("grass", new SpawnData(coordinateCalculateX(x), coordinateCalculateY(y), z));
                default -> null;
            };
            blocks[z][x + y * Chunk.CHUNK_SIZE] = blockEntity;
        });
    }

    @Override
    public void blockRemove(int i, int i1, int i2, BlockData blockData, BlockState blockState) {
        Platform.runLater(() -> {
            FXGL.getGameWorld().removeEntity(blocks[i2][i + i1 * Chunk.CHUNK_SIZE]);
            blocks[i2][i + i1 * Chunk.CHUNK_SIZE] = null;
        });
    }

    @Override
    public void blockUpdate(int i, int i1, int i2, BlockData blockData, BlockState blockState) {
        Platform.runLater(() -> {
            if (blocks[i2][i + i1 * Chunk.CHUNK_SIZE] == null) return;
            FXGL.getGameWorld().removeEntity(blocks[i2][i + i1 * Chunk.CHUNK_SIZE]);
            blocks[i2][i + i1 * Chunk.CHUNK_SIZE] = null;
            blockCreate(i, i1, i2, blockData, blockState);
        });
    }

    @Override
    public void chunkInit(ChunkUpdate chunkUpdate) {
        blocks = new Entity[Chunk.CHUNK_HEIGHT][Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];
        Platform.runLater(() -> {
            for (int i = 0; i < Chunk.CHUNK_HEIGHT; i++) {
                for (int j = 0; j < Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE; j++) {
                    blockCreate(j % Chunk.CHUNK_SIZE, j / Chunk.CHUNK_SIZE, i, chunkUpdate.blocks[i][j], null);
                }
            }
        });
    }

    @Override
    public void playerScoreUpdate(String[] playerNames, int[] playerScores) {
        Platform.runLater(() -> {
            StringBuilder leaderboardContent = new StringBuilder();
            int i = 0;
            for (String playerName : playerNames) {
                leaderboardContent.append(String.format("%6s", playerName) + ": " + playerScores[i] + "\n");
                if (playerName.equals(username)) {
                    scoreBoard.setText("Score: " + playerScores[i]);
                }
                i++;
            }
            leaderboard.setText(leaderboardContent.toString());
        });
    }

}
