
import java.io.Serializable;

public class Item implements Serializable {
    private String title;
    private String dateAdded;
    private String type;
    private String dueDate;
    // Default Constructor
    public Item() {
        this.title = "";
        this.dateAdded = "";
        this.type = "";
        this.dueDate="";
    }
    public Item(String title){
        this.title = title;
        this.dateAdded = "";
        this.type="";
        this.dueDate="";
    }
    public Item(String title, String dateAdded){
        this.title = title;
        this.dateAdded = dateAdded;
        this.type="";
    }
    public Item(String title, String dateAdded, String type){
        this.title = title;
        this.dateAdded = dateAdded;
        this.type = type;
        this.dueDate="";
    }
    public String getTitle(){
        return title;
    }
    public String getDateAdded(){
        return dateAdded;
    }
    @Override
    public String toString() {
        return "Item {" +
                "title='" + getTitle() + '\'' +
                ", dateAdded='" + getDateAdded() + '\'' +
                '}';
    }
    public String getType(){
        return type;
    }
    public void setDateAdded(String date){
        dateAdded = date;
    }
    public void setDueDate(String date){
        dueDate = date;
    }
    public String getDueDate(){
        return dueDate;
    }
}
class Book extends Item{
    private String author;
    public Book(){
        super();
        this.author = "";
    }
    public Book(String author){
        super();
        this.author = author;
    }
    @Override
    public String toString() {
        return "Book {" +
                "title='" + getTitle() + '\'' +
                ", dateAdded='" + getDateAdded() + '\'' +
                ", author='" + author + '\'' +
                '}';
    }

    public String getAuthor() {
        return author;
    }
}
class DVD extends Item{
    private String productionCompany;
    private String director;
    public DVD(){
        super();
        productionCompany = "";
        director = "";
    }
    public DVD(String productionCompany, String director){
        super();
        this.productionCompany = productionCompany;
        this.director = director;
    }
    @Override
    public String toString() {
        return "DVD {" +
                "title='" + getTitle() + '\'' +
                ", dateAdded='" + getDateAdded() + '\'' +
                ", productionCompany='" + productionCompany + '\'' +
                ", director='" + director + '\'' +
                '}';
    }
    public String getDirector(){
        return director;
    }
    public String getProductionCompany(){
        return productionCompany;
    }
}
class Audiobook extends Item{
    private String narrator;
    public Audiobook(){
        super();
        narrator = "";
    }
    public Audiobook(String narrator){
        super();
        this.narrator = narrator;
    }
    @Override
    public String toString() {
        return "Audiobook {" +
                "title='" + getTitle() + '\'' +
                ", dateAdded='" + getDateAdded() + '\'' +
                ", narrator='" + narrator + '\'' +
                '}';
    }
    public String getNarrator(){
        return narrator;
    }
}
class Game extends Item{
    private String gameDesigner;
    public Game(){
        super();
        gameDesigner = "";
    }
    public Game(String gameDesigner){
        super();
        this.gameDesigner = gameDesigner;
    }
    @Override
    public String toString() {
        return "Game {" +
                "title='" + getTitle() + '\'' +
                ", dateAdded='" + getDateAdded() + '\'' +
                ", gameDesigner='" + gameDesigner + '\'' +
                '}';
    }
    public String getGameDesigner(){
        return gameDesigner;
    }
}
class comicBook extends Item{
    private String illustrator;
    public comicBook(){
        super();
        illustrator = "";
    }
    public comicBook(String illustrator){
        this.illustrator = illustrator;
    }
    @Override
    public String toString() {
        return "comicBook {" +
                "title='" + getTitle() + '\'' +
                ", dateAdded='" + getDateAdded() + '\'' +
                ", illustrator='" + illustrator + '\'' +
                '}';
    }
    public String getIllustrator(){
        return illustrator;
    }
}
