package controllers;

import play.mvc.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Query;
import com.google.gdata.client.Service;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.client.authn.oauth.*;
import com.google.gdata.data.*;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.NoLongerAvailableException;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.XmlBlob;
import exceptions.NotAuthenticatedException;
import org.omg.PortableInterceptor.RequestInfo;
import org.apache.commons.lang.StringUtils;
import models.Contact;
import models.SearchFilter;

public class Application extends Controller {

    private static final String FILTER = "filter";
    private static final String RESULT = "result";
    //git test6
    private static GoogleService googleService = null;
    private static final String ROOT_URL = "http://www.google.com/m8/feeds/";
    private static final String DEFAULT_FEED = ROOT_URL+"contacts/default/full";
    //private static final String DEFAULT_HOME = "http://localhost:8080/";
    private static final String DEFAULT_HOME = "http://www.nodecaster.com:8080/";
    private static int filterQuantity = 32;

    public static void index() {
        render();
    }

    public static void pay() {
        render();
    }

    public static void show(String page) {
        if(page.equals(FILTER)) render("Application/filter.html");
        if(page.equals(RESULT)) render("Application/result.html");
    }


    public static void initFilterSearch(SearchFilter searchFilter){
        StringBuilder filter = new StringBuilder();
        if("Y".equals(searchFilter.mailFilter)) filter.append("M");
        if("Y".equals(searchFilter.nameFilter)) filter.append("N");
        if("Y".equals(searchFilter.pictureFilter)) filter.append("P");
        session.clear();
        session.put("filter", filter.toString());
        String next = DEFAULT_HOME+"backToFilter";
        boolean secure = false;
        boolean session = true;
        String authSubLogin = AuthSubUtil.getRequestUrl(next, ROOT_URL, secure, session);
        redirect(authSubLogin);
    }


    public static void initBasicSearch(){
        session.clear();
        String next = DEFAULT_HOME+"backToBasic";
        boolean secure = false;
        boolean session = true;
        String authSubLogin = AuthSubUtil.getRequestUrl(next, ROOT_URL, secure, session);
        redirect(authSubLogin);
    }

    public static void addImage() throws NotAuthenticatedException {
        //if authenticated --> store image
        //else
        //throw NotAuthenticatedException
        //or
        //authenticate

    }

    public static void backToBasic(String token) {
        /*String next = "http://localhost:9009/backFromGoogle";
        String scope = "http://www.google.com/m8/feeds/";
        boolean secure = false;
        boolean session = true;
        String authSubLogin = AuthSubUtil.getRequestUrl(next, scope, secure, session);
        String aToken = AuthSubUtil.getTokenFromReply(authSubLogin);*/
        try {
            String sessionToken = AuthSubUtil.exchangeForSessionToken(token, null);
            session.put("token", sessionToken);
            getContacts(1, 100, "");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (GeneralSecurityException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public static void backToFilter(String token) {
        /*String next = "http://localhost:9009/backFromGoogle";
        String scope = "http://www.google.com/m8/feeds/";
        boolean secure = false;
        boolean session = true;
        String authSubLogin = AuthSubUtil.getRequestUrl(next, scope, secure, session);
        String aToken = AuthSubUtil.getTokenFromReply(authSubLogin);*/
        try {
            String sessionToken = AuthSubUtil.exchangeForSessionToken(token, null);
            session.put("token", sessionToken);
            getContacts(1, 100, session.get("filter"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (GeneralSecurityException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public static void getContactsTest(String filter) {
        renderArgs.put("entries", queryEntriesTest(filter));
        render("Application/result.html");
    }

    public static void getContacts(int from, int quantity, String filter) {
        initService();
        if(from == 0) from = 1; 
        if(quantity == 0) quantity = 100;
        googleService.setAuthSubToken(session.get("token"), null);
        try {
            renderArgs.put("entries", queryEntries(from, quantity, filter));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        render("Application/result.html");
    }

    public static void getProfilePic(String url){
        initService();
        googleService.setAuthSubToken(session.get("token"), null);
        Link l = new Link();
        l.setHref(ROOT_URL+url);
        InputStream in = null;
        try {
            Service.GDataRequest request = googleService.createLinkQueryRequest(l);
            request.execute();
            renderBinary(request.getResponseStream());
            request.end();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private static List<Contact> queryEntries(int from, int quantity, String filter)
            throws IOException, ServiceException {
        Query myQuery = new Query(new URL(DEFAULT_FEED));
        myQuery.setMaxResults(quantity);
        myQuery.setStartIndex(from);
        //myQuery.setStringCustomParameter("sortorder", "ascending");
        //myQuery.setStringCustomParameter("orderby", "lastmodified");
        List<Contact> contacts = new ArrayList<Contact>();
        SearchFilter searchFilter = new SearchFilter(filter);
        try {
            ContactFeed resultFeed = googleService.query(myQuery, ContactFeed.class);
            for (int i = 0; i < resultFeed.getEntries().size(); i++) {
                ContactEntry entry = resultFeed.getEntries().get(i);
                Contact c = new Contact();
                c.id = entry.getId();
                c.name = entry.getTitle().getPlainText();
                if(entry.getContactPhotoLink() != null){
                    if(StringUtils.isNotBlank(entry.getContactPhotoLink().getHref())){
                        c.image = entry.getContactPhotoLink().getHref().substring(ROOT_URL.length());
                        if(searchFilter.filterPicture) continue;
                    }
                }
                if(searchFilter.filterName && !isValidName(c.name)) continue;
                c.email = getPrimaryEmail(entry.getXmlBlob());
                if(searchFilter.filterMail && !isValidPrimaryEmail(c.email)) continue;
                if(!searchFilter.filterName && !isValidName(c.name) && StringUtils.isNotBlank(c.email)) c.name = c.email.substring(0, c.email.indexOf('@')).replace(".", " ");
                contacts.add(c);

            }
            return contacts;
        } catch (NoLongerAvailableException ex) {
            System.err.println(
            "Not all placehorders of deleted entries are available");
            return null;
        }
  }

    private static void initService(){
        googleService =
            new GoogleService("cp",
                "ContactsProfilePicsApp");
    }

    private static String getPrimaryEmail(XmlBlob blob){
        if(blob.getBlob() != null){
            String email = "";
            Pattern p = Pattern.compile("<gd:email primary='true' address='([^']+)");
            Matcher m = null; // get a matcher object
            m = p.matcher(blob.getBlob());
            if(m.find()) {
                email = m.group(1);
                System.out.println("Primary email "+email);
            }
            return email;

        }
        else return "";

    }

    private static boolean isValidPrimaryEmail(String address){
        if(address.indexOf("info@") >= 0) return false;
        return true;
    }

    private static boolean isValidName(String name){
        if(StringUtils.isBlank(name)) return false;
        return true;
    }

    private static List<Contact> queryEntriesTest(String filter){
        List<Contact> theContacts = new ArrayList<Contact>();
        String[][] contacts = {
                {"1","Jef, Waumans","http://media.linkedin.com/mpr/mpr/shrink_80_80/p/2/000/033/19e/0ad1aef.jpg", "jef@waumans.net"},
                {"2","Test test","","test@test.com "},
                {"3","Els Verreck","","els.verreck@telenet.be"},
                {"4","Pieter Wuyts","","pieter@8seconds.net"},
                {"5","Frank Salliau","","frank@boek.be"}
        };
        SearchFilter searchFilter = new SearchFilter(filter);
        for (int i = 0; i < contacts.length; i++) {
            String[] contact = contacts[i];
            if(searchFilter.filterName && !isValidName(contact[1])) continue;
            if(searchFilter.filterPicture && StringUtils.isNotBlank(contact[2])) continue;
            theContacts.add(new Contact(contact));

        }
        return theContacts;
    }
}