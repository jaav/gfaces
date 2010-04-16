package models;

/**
 * Created by IntelliJ IDEA.
 * User: jwmcwt6
 * Date: 15-apr-2010
 * Time: 11:37:28
 * To change this template use File | Settings | File Templates.
 */
public class SearchFilter {
    public String nameFilter;
    public String pictureFilter;
    public String mailFilter;

    public boolean filterName = false;
    public boolean filterPicture = false;
    public boolean filterMail = false;

    public SearchFilter(String filter) {
        if(filter.indexOf("N") >= 0) this.filterName = true;
        if(filter.indexOf("P") >= 0) this.filterPicture = true;
        if(filter.indexOf("M") >= 0) this.filterMail = true;
    }
}
