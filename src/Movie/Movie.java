package Movie;
import java.io.Serializable;


public class Movie implements Serializable {
    private String title;
    private int amount;

    public Movie(String title, int amount) {
        this.title = title;
        this.amount = amount;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
}