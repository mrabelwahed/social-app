package yberg.intnet.com.app;

/**
 * Created by Viktor on 2016-03-10.
 */
public class User {

    private int uid;
    private String username, firstName, lastName, name, email, image;

    public User(int uid, String username, String firstName, String lastName, String email, String image) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = firstName + " " + lastName;
        this.email = email;
        this.image = image;
    }

    public User(int uid, String username, String name, String image) {
        this(uid, username, "", "", "", image);
        this.name = name;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = "@" + username;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }
}
