package com.javaworld.clientapp.scene;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.adapter.entity.Player;
import com.javaworld.client.ClientGameEvent;
import com.javaworld.client.ClientGameManager;
import com.javaworld.clientapp.CodeEditor;
import com.javaworld.clientapp.PlayerDisplay;
import com.javaworld.core.Chunk;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;
import com.javaworld.core.update.ChunkUpdate;
import com.javaworld.data.ServerResponseData;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.util.HashMap;

import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;
import static com.javaworld.clientapp.Application.pixelFontFact;

public class GamePlay implements ClientGameEvent {
    public static final int PIXELSENSE = 32;
    public static final double gameStartX = 640 - 16 * PIXELSENSE;
    public static final double gameStartY = 360 + 7 * PIXELSENSE;
    public static final double UIWebSizeX = 640;
    public static final double UIWebSizeY = 360 + 8 * PIXELSENSE;

    private static final HashMap<Integer, Entity> entities = new HashMap<>();
    private static final HashMap<Integer, PlayerDisplay> players = new HashMap<>();
    private static Entity[][] blocks;

    private String playerName;
    private final Font leaderboardFont, buttonFont, consoleFont;
    private TextArea scoreBoard;
    private TextArea leaderboard;
    private VBox console;
    private boolean consoleScrollBottom;

    public GamePlay() {
        leaderboardFont = pixelFontFact.newFont(25);
        consoleFont = Font.font("Courier New", 16);
        buttonFont = pixelFontFact.newFont(25);
    }

    public void initGame(ClientGameManager gameManager) {
        createRankLists();
        createScoreboard();

        // Create Console
        console = new VBox();
        console.getStyleClass().add("transparentTextArea");
        ScrollPane consoleScroll = new ScrollPane(console);
        consoleScroll.setPrefSize(UIWebSizeX, UIWebSizeY);
        consoleScroll.setFitToHeight(true);
        consoleScroll.setFitToWidth(true);
        consoleScroll.setVisible(false);
        consoleScroll.getStyleClass().add("transparentScrollPane");
        consoleScroll.setStyle("-fx-background-color: rgba(33,34,41,0.9);");
        consoleScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        consoleScroll.setVvalue(consoleScroll.getVmax());
        consoleScroll.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (consoleScrollBottom) {
                consoleScroll.setVvalue(consoleScroll.getVmax());
                consoleScrollBottom = false;
            }
        });


        // Create code view
        final CodeEditor editor = new CodeEditor();
        WebView codeWebView = editor.webview;
        codeWebView.setPrefSize(UIWebSizeX, UIWebSizeY);
        Button runButton = new Button("Run");
        runButton.setPrefWidth(60); // Set preferred width
        runButton.setPrefHeight(50); // Set preferred height
        runButton.setFont(buttonFont);
        runButton.setTranslateX(10);
        runButton.setTranslateY(UIWebSizeY + 10);
        runButton.setPadding(Insets.EMPTY);
        runButton.getStyleClass().add("roundBorder");
        runButton.setStyle("-fx-background-color: rgba(0,218,8,0.8);");
        Button stopButton = new Button("Stop");
        stopButton.setPrefWidth(60); // Set preferred width
        stopButton.setPrefHeight(50); // Set preferred height
        stopButton.setFont(buttonFont);
        stopButton.setTranslateY(UIWebSizeY + 10);
        stopButton.setTranslateX(80);
        stopButton.setPadding(Insets.EMPTY);
        stopButton.getStyleClass().add("roundBorder");
        stopButton.setStyle("-fx-background-color: rgba(211,8,8,0.8);");
        Pane pane = new Pane(codeWebView, runButton, stopButton);
        runButton.setOnAction(e -> {
            String code = editor.getCodeAndSnapshot();
            ServerResponseData response = gameManager.sendPlayerCode(code);
            if (!response.success)
                appendConsoleError(response.message);
            pane.setVisible(false);
        });
        stopButton.setOnAction(e -> {
            gameManager.sendPlayerCode(null);
        });
        pane.setVisible(false);
        createWebViewControl(0, 0, pane, "Codeview", FXGL.getAppWidth() / 2d - 260, gameStartY + 55);


        // Create GPT
        WebView google = new WebView();
        google.getEngine().load("https://catgpt.wvd.io/");
        google.setPrefSize(UIWebSizeX, UIWebSizeY);
        google.setVisible(false);
        createWebViewControl(FXGL.getAppWidth() - UIWebSizeX, 0, google, "GPT", FXGL.getAppWidth() / 2d - 80, gameStartY + 55);

        createWebViewControl(FXGL.getAppWidth() - UIWebSizeX, 0, consoleScroll, "Console", FXGL.getAppWidth() / 2d + 260, gameStartY + 55);
    }

    private void appendConsole(String text) {
        Text t = new Text(text);
        t.setFont(consoleFont);
        t.setFill(Color.WHITE);
        consoleScrollBottom = true;
        console.getChildren().add(t);
    }

    private void appendConsoleError(String text) {
        Text t = new Text(text);
        t.setStyle("-fx-font-weight: bold");
        t.setFont(consoleFont);
        t.setFill(Color.rgb(255, 50, 50));
        consoleScrollBottom = true;
        console.getChildren().add(t);
    }

    private void createWebViewControl(double x, double y, Node windowNode, String buttonName, double buttonX, double buttonY) {
        Button toggleButton = new Button(buttonName);
        toggleButton.setPrefWidth(160); // Set preferred width
        toggleButton.setPrefHeight(50); // Set preferred height
        toggleButton.setCenterShape(true);
        toggleButton.setFont(buttonFont);
        toggleButton.getStyleClass().add("roundBorder");
        // Default to code editor
        toggleButton.setOnAction(e -> windowNode.setVisible(!windowNode.isVisible()));
        FXGL.addUINode(windowNode, x, y);
        FXGL.addUINode(toggleButton, buttonX, buttonY);
    }

    private void createRankLists() {
        leaderboard = createScoreboard("Leaderboard", FXGL.getAppWidth() - 250, 250, 330);
    }

    private void createScoreboard() {
        scoreBoard = createScoreboard("Scoreboard", FXGL.getAppWidth() / 2 - 90, 180, 80);
        scoreBoard.setText("Score: 0");
    }

    private TextArea createScoreboard(String titleName, int x, int width, int height) {
        // 創建一個VBox作為主布局
        VBox vbox = new VBox();
        vbox.setPrefSize(width, height);
        vbox.setBackground(new Background(new BackgroundFill(Color.rgb(245, 244, 247, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
        vbox.setAlignment(Pos.CENTER);  // 將VBox內容居中

        Label title = new Label(titleName);
        title.setFont(leaderboardFont);
        title.setAlignment(Pos.CENTER);  // 將標題居中
        vbox.getChildren().add(title);  // 將標題添加到VBox

        // 創建TextArea來顯示排行榜
        TextArea textArea = new TextArea();
        textArea.setEditable(false); // 設置為不可編輯
        textArea.setWrapText(true); // 啟用文字自動換行
        textArea.setFont(leaderboardFont);
        textArea.getStyleClass().add("transparentTextArea");
        textArea.setMouseTransparent(true);
        textArea.setFocusTraversable(false);

        VBox.setVgrow(textArea, javafx.scene.layout.Priority.ALWAYS);
        vbox.getChildren().add(textArea);
        FXGL.addUINode(vbox, x, 0);

        return textArea;
    }

    public static double toPlayerX(double x) {
        return gameStartX + x * PIXELSENSE - (PIXELSENSE >> 1);
    }

    public static double toPlayerY(double y) {
        return gameStartY - y * PIXELSENSE + (PIXELSENSE >> 1);
    }

    public static double toBlockX(int x) {
        return gameStartX + x * PIXELSENSE;
    }

    public static double toBlockY(int y) {
        return gameStartY - y * PIXELSENSE;
    }

    private void spawnBlock(int x, int y, int z, BlockData blockData, BlockState blockState) {
        Entity blockEntity = switch (blockData.name) {
            case "stone" -> spawn("stone", new SpawnData(toBlockX(x), toBlockY(y), z));
            case "dirt" -> spawn("dirt", new SpawnData(toBlockX(x), toBlockY(y), z));
            case "grass_block" -> spawn("grass", new SpawnData(toBlockX(x), toBlockY(y), z));
            default -> null;
        };
        blocks[z][x + y * Chunk.CHUNK_SIZE] = blockEntity;
    }

    @Override
    public void entityCreate(com.javaworld.core.entity.Entity entity) {
        Vec2 entityPos = entity.getPosition();
        if (entity instanceof Player player) {
            String name = player.getName();
            Platform.runLater(() -> {
                SpawnData spawnData = new SpawnData(toPlayerX(entityPos.x), toPlayerY(entityPos.y)).put("name", name);
                players.put(entity.getSerial(), new PlayerDisplay(
                        spawn("player", spawnData),
                        spawn("playerNameTag", spawnData)
                ));
            });
        } else if (entity.entityData.entityType == EntityType.PLANT) {
            Platform.runLater(() -> entities.put(entity.getSerial(),
                    spawn("plant", new SpawnData(toPlayerX(entityPos.x), toPlayerY(entityPos.y)))));
        }
    }

    @Override
    public void entityUpdate(com.javaworld.core.entity.Entity entity) {
        Vec2 entityPos = entity.getPosition();
        if (entity instanceof Player) {
            PlayerDisplay e = players.get(entity.getSerial());
            Platform.runLater(() -> {
                e.player().setPosition(toPlayerX(entityPos.x), toPlayerY(entityPos.y));
                e.nameTag().setPosition(toPlayerX(entityPos.x), toPlayerY(entityPos.y));
            });
        } else {
            Entity e = entities.get(entity.getSerial());
            Platform.runLater(() -> {
                e.setPosition(toPlayerX(entityPos.x), toPlayerY(entityPos.y));
            });
        }
    }

    @Override
    public void entityRemove(com.javaworld.core.entity.Entity entity) {
        if (entity instanceof Player) {
            PlayerDisplay e = players.get(entity.getSerial());
            if (e == null) return;
            Platform.runLater(() -> {
                players.remove(entity.getSerial());
                Platform.runLater(() -> FXGL.getGameWorld().removeEntity(e.player()));
                Platform.runLater(() -> FXGL.getGameWorld().removeEntity(e.nameTag()));
            });
        } else {
            Entity e = entities.get(entity.getSerial());
            if (e == null) return;
            Platform.runLater(() -> {
                entities.remove(entity.getSerial());
                Platform.runLater(() -> FXGL.getGameWorld().removeEntity(e));
            });
        }
    }

    @Override
    public void blockCreate(int x, int y, int z, BlockData blockData, BlockState blockState) {
        Platform.runLater(() -> spawnBlock(x, y, z, blockData, blockState));
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
            spawnBlock(i, i1, i2, blockData, blockState);
        });
    }

    @Override
    public void chunkInit(ChunkUpdate chunkUpdate) {
        blocks = new Entity[Chunk.CHUNK_HEIGHT][Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];
        Platform.runLater(() -> {
            for (int i = 0; i < Chunk.CHUNK_HEIGHT; i++) {
                for (int j = 0; j < Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE; j++) {
                    spawnBlock(j % Chunk.CHUNK_SIZE, j / Chunk.CHUNK_SIZE, i, chunkUpdate.blocks[i][j], null);
                }
            }
        });
    }

    @Override
    public void playerScoreUpdate(String[] playerNames, int[] playerScores) {
        Platform.runLater(() -> {
            StringBuilder leaderboardContent = new StringBuilder();
            for (int i = 0; i < playerNames.length; i++) {
                leaderboardContent.append(String.format("%6s: %d\n", playerNames[i], playerScores[i]));
                if (playerNames[i].equals(playerName)) scoreBoard.setText("Score: " + playerScores[i]);
            }
            leaderboard.setText(leaderboardContent.toString());
        });
    }

    @Override
    public void playerLog(String text) {
        String finalText = text.trim();
        Platform.runLater(() -> appendConsole(finalText));
    }

    @Override
    public void playerError(String text) {
        String finalText = text.trim();
        Platform.runLater(() -> appendConsoleError(finalText));
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
