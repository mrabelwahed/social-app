package yberg.intnet.com.app;

import java.io.Serializable;

/**
 * Created by Viktor on 2016-03-06.
 */
public class Post implements Serializable {

    int pid, comments, upvotes, downvotes;
    private User user;
    private String text, posted, image;

    public Post(int pid, User user, String text, String posted, int comments, int upvotes, int downvotes, String image) {
        this.pid = pid;
        this.user = user;
        this.text = text;
        this.posted = posted;
        this.comments = comments;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.image = image;
    }

    public void setPid(int pid) {
        this.pid = pid;
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

    public void setComments(int comments) {
        this.comments = comments;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPid() {
        return pid;
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

    public int getComments() {
        return comments;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public String getImage() {
        return image;
    }
}
