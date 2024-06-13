package com.javaworld.clientapp;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;


public class GameFactory implements EntityFactory {

    @Spawns("stone")
    public Entity newRockTile(SpawnData data) {
        return createTileEntity(data, "stone");
    }

    @Spawns("grass")
    public Entity newGrassTile(SpawnData data) {
        return createTileEntity(data, "grass");
    }

    @Spawns("soil")
    public Entity newGroundTile(SpawnData data) {
        return createTileEntity(data, "soil");
    }

    @Spawns("plant")
    public Entity newPlant(SpawnData data) {
        return createPlant(data);
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return createPlayer(data);
    }


    private Map<String, Image> imageCache = new HashMap<>();

    public GameFactory() {
        imageCache.put("stone", new Image("com/javaworld/clientapp/rock_pixel.png"));
        imageCache.put("grass", new Image("com/javaworld/clientapp/grass_pixel.png"));
        imageCache.put("soil", new Image("com/javaworld/clientapp/soil_pixel.png"));
    }

    private Entity createTileEntity(SpawnData data, String type) {
        Image image = imageCache.get(type);
        Rectangle view = new Rectangle(32, 32);
        view.setFill(new ImagePattern(image));

        return FXGL.entityBuilder(data)
                .view(view)
                .at(data.getX(), data.getY())
                .zIndex((int) data.getZ())
                .build();
    }

    private static Entity createPlayer(SpawnData data) {
        Image image = new Image("com/javaworld/clientapp/role.png");
        Rectangle view = new Rectangle(32, 32);
        view.setFill(new ImagePattern(image));


        // 从SpawnData中读取玩家名
        String playerName = data.get("name").toString();
        playerName = playerName.substring(0, Math.min(2, playerName.length()));
        // Create a Text object for the player name using JavaFX
        Text nameTag = new Text(playerName);
        nameTag.setFont(Font.font("Verdana", 14)); // Set the font and size
        nameTag.setFill(Color.WHITE); // Set the text color

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(view, nameTag);
        stackPane.setAlignment(Pos.TOP_CENTER);
//        StackPane.setAlignment(nameTag, Pos.TOP_CENTER); // Position the text above the center of the view


        return FXGL.entityBuilder(data)
                .view(stackPane) // Set the StackPane as the view of the entity
                .at(data.getX(), data.getY())
                .zIndex(3) // Set the z-index if necessary
                .build();

    }

    private static Entity createPlant(SpawnData data) {
        Image image = new Image("com/javaworld/clientapp/plant.png");
        Rectangle view = new Rectangle(32, 32);
        view.setFill(new ImagePattern(image));
        return FXGL.entityBuilder(data)
                .view(view)
                .at(data.getX(), data.getY())
                .zIndex(3)
                .build();
    }
}
