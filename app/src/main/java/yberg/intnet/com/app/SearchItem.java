package yberg.intnet.com.app;

/**
 * Created by Viktor on 2016-03-24.
 *
 * An item to be displayed in the search view or in PeopleActivity.
 */
public class SearchItem {

    int uid;
    String username, name, image;

    public SearchItem(int uid, String username, String name, String image) {
        this.uid = uid;
        this.username = username;
        this.name = name;
        this.image = image;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }
}
