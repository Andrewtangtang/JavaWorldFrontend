package com.javaworld.clientapp;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.ui.FontFactory;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.adapter.entity.Player;
import com.javaworld.client.ClientGameEvent;
import com.javaworld.client.ClientGameManager;
import com.javaworld.clientapp.scene.GamePlay;
import com.javaworld.clientapp.scene.MainMenu;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;

import java.util.HashMap;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;


public class Application extends GameApplication {
    public static FontFactory pixelFontFact;
    private ClientGameManager gameManager;
    private GamePlay gamePlay;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("JavaWorld");
        settings.setAppIcon("icon.png");
        settings.setVersion("1.0");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setIntroEnabled(false);
        settings.setGameMenuEnabled(false);
        settings.setManualResizeEnabled(true);
        settings.setPreserveResizeRatio(true);
        settings.setFullScreenAllowed(true);
//        settings.setFullScreenFromStart(true);
    }

    @Override
    protected void initGame() {
        pixelFontFact = FXGL.getAssetLoader().loadFont("MinecraftRegular.otf");

        GameManager.loadResources();
        getGameWorld().addEntityFactory(new GameFactory());
        gamePlay = new GamePlay();
    }

    @Override
    protected void initUI() {
        MainMenu mainMenu = new MainMenu(this);
        mainMenu.initUI();

        FXGL.getPrimaryStage().setOnCloseRequest(windowEvent -> {
            if (gameManager != null)
                gameManager.disconnect();
        });
    }

    public boolean joinServer(String ipStr, String playerName) {
        String[] ipArr = ipStr.split(":", 2);
        int port = 25565;
        if (ipArr.length == 2)
            try {
                port = Integer.parseInt(ipArr[1]);
            } catch (NumberFormatException exception) {
                FXGL.getDialogService().showMessageBox("Port format error: " + ipArr[1]);
                return false;
            }
        // Connect server
        gameManager = new ClientGameManager(ipArr[0], port, gamePlay);
        gamePlay.setPlayerName(playerName);
        ServerResponse result = gameManager.connect(playerName);
        if (result.success) {
            gamePlay.initGame(gameManager);
//            initGameUI();
            return true;
        }

        FXGL.getDialogService().showMessageBox(result.message);
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
