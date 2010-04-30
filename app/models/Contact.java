package models;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: Apr 7, 2010
 * Time: 5:29:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Contact {          
    public String name;
    //public String iimage;
    public String email;
    public String image_link_href;
    public String image_edit_link_href;
    public String image_etag;

    public Contact() {
    }

    public Contact(String[] contactValues) {
        this.name = contactValues[0];
        this.image_link_href = contactValues[1];
        this.email = contactValues[2];
        this.image_edit_link_href = contactValues[1];
    }
}
