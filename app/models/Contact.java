package models;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: Apr 7, 2010
 * Time: 5:29:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Contact {
    public String id;
    public String name;
    public String image;
    public String email;

    public Contact() {
    }

    public Contact(String name, String image, String email) {
        this.name = name;
        this.image = image;
        this.email = email;
    }

    public Contact(String[] contactValues) {
        this.id = contactValues[0];
        this.name = contactValues[1];
        this.image = contactValues[2];
        this.email = contactValues[3];
    }
}
