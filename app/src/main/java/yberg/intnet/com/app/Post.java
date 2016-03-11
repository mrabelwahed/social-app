package yberg.intnet.com.app;

/**
 * Created by Viktor on 2016-03-06.
 */
public class Post {

    int pid;
    private User user;
    private String text, posted, image;

    public Post(int pid, User user, String text, String posted, String image) {
        this.pid = pid;
        this.user = user;
        this.text = text;
        this.posted = posted;
        this.image = image;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public User getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public String getPosted() {
        return posted;
    }

    public String getImage() {
        return image;
    }
}
