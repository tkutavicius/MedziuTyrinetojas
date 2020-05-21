package lt.ktu.treespectator;

public class CatalogTree {
    String name;
    String latinName;
    String description;
    String imageUrl;

    public CatalogTree(String name, String latinName, String description, String imageUrl) {
        this.name = name;
        this.latinName = latinName;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatinName() {
        return latinName;
    }

    public void setLatinName(String latinName) {
        this.latinName = latinName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
