package yberg.intnet.com.app;

/**
 * Created by Viktor on 2016-03-06.
 */
public class Post {

    private int image;
    private String username, name, text, posted;

    public Post(String username, String name, String text, String posted, int image) {
        this.username = "@" + username;
        this.name = name;
        this.text = text;
        this.posted = posted;
        this.image = image;
    }

    public void setUsername(String username) {
        this.username = "@" + username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getPosted() {
        return posted;
    }

    public int getImage() {
        return image;
    }
}
