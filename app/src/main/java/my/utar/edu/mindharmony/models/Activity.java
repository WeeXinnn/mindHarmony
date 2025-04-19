package my.utar.edu.mindharmony.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "activities")
public class Activity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String description;
    private String category;

    public Activity(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}