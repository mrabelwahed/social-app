package yberg.intnet.com.app;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Viktor on 2016-03-06.
 */
public class Post implements Serializable {

    int pid, numberOfComments, upvotes, downvotes, voted;
    private User user;
    private String text, posted, image;
    private ArrayList<Comment> comments;

    public Post(int pid, User user, String text, String posted, int numberOfComments, ArrayList<Comment> comments, int upvotes, int downvotes, int voted, String image) {
        this.pid = pid;
        this.user = user;
        this.text = text;
        this.posted = posted;
        this.numberOfComments = numberOfComments;
        this.comments = comments;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.voted = voted;
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

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public void setVoted(int voted) {
        this.voted = voted;
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

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public int getVoted() {
        return voted;
    }

    public String getImage() {
        return image;
    }
}
