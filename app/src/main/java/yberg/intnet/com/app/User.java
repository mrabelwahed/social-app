package yberg.intnet.com.app;

/**
 * Created by Viktor on 2016-03-10.
 */
public class User {

    private int image;
    private String username, firstName, lastName, email;

    public User(String username, String firstName, String lastName, String email, int image) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.image = image;
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

    public void setImage(int image) {
        this.image = image;
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
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public int getImage() {
        return image;
    }
}
