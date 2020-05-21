package lt.ktu.treespectator;

import java.io.Serializable;

public class UserTree implements Serializable {
    String ID, name, age, height, diameter, kind, startDate, edited, description, owner, imageUrl, location;

    public UserTree() {
    }

    public UserTree(String ID, String name, String age, String height, String diameter, String kind, String startDate, String edited, String description, String owner, String imageUrl, String location) {
        this.ID = ID;
        this.name = name;
        this.age = age;
        this.height = height;
        this.diameter = diameter;
        this.kind = kind;
        this.startDate = startDate;
        this.edited = edited;
        this.description = description;
        this.owner = owner;
        this.imageUrl = imageUrl;
        this.location = location;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getDiameter() {
        return diameter;
    }

    public void setDiameter(String diameter) {
        this.diameter = diameter;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEdited() {
        return edited;
    }

    public void setEdited(String edited) {
        this.edited = edited;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
