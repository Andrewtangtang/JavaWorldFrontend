package com.javaworld.clientapp;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.image.Image;
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

    @Spawns("dirt")
    public Entity newGroundTile(SpawnData data) {
        return createTileEntity(data, "dirt");
    }

    @Spawns("plant")
    public Entity newPlant(SpawnData data) {
        return createPlant(data);
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return createPlayer(data);
    }

    @Spawns("playerNameTag")
    public Entity newPlayerNameTag(SpawnData data) {
        return createPlayerNameTag(data);
    }

    private Map<String, Image> imageCache = new HashMap<>();

    public GameFactory() {
        imageCache.put("stone", new Image("rock_pixel.png"));
        imageCache.put("grass", new Image("grass_pixel.png"));
        imageCache.put("dirt", new Image("dirt_pixel.png"));
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
        Image image = new Image("role.png");
        Rectangle view = new Rectangle(32, 32);
        view.setFill(new ImagePattern(image));

        Entity e = FXGL.entityBuilder(data)
                .view(view) // Set the StackPane as the view of the entity
                .anchorFromCenter()
                .at(data.getX(), data.getY())
                .zIndex(4) // Set the z-index if necessary
                .build();
        return e;
    }

    private static Entity createPlayerNameTag(SpawnData data) {
        String playerName = data.get("name").toString();
        Text nameTag = new Text(playerName);
        nameTag.setFont(Font.font("Verdana", 14)); // Set the font and size
        nameTag.setFill(Color.WHITE); // Set the text color

        Entity e = FXGL.entityBuilder(data)
                .view(nameTag) // Set the StackPane as the view of the entity
                .at(data.getX(), data.getY())
                .zIndex(5) // Set the z-index if necessary
                .build();
        return e;
    }

    private static Entity createPlant(SpawnData data) {
        Image image = new Image("plant.png");
        Rectangle view = new Rectangle(32, 32);
        view.setFill(new ImagePattern(image));
        return FXGL.entityBuilder(data)
                .view(view)
                .at(data.getX(), data.getY())
                .zIndex(3)
                .build();
    }
}
