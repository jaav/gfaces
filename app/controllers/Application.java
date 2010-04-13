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

public class Application extends Controller {

    private static final String START = "start";
    //git test
    private static GoogleService googleService = null;
    private static final String ROOT_URL = "http://www.google.com/m8/feeds/";
    private static final String DEFAULT_FEED = ROOT_URL+"contacts/default/full";

    public static void index() {
        render();
    }

    public static void pay() {
        render();
    }

    public static void imageMatrix() {
        render("Application/result.html");
    }

    public static void show(String page) {
        if(page.equals(START)) render("Application/start.html");
    }


    public static void authenticate(){
        String next = "http://localhost:9009/backFromGoogle";
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

    public static void backFromGoogle(String token) {
        /*String next = "http://localhost:9009/backFromGoogle";
        String scope = "http://www.google.com/m8/feeds/";
        boolean secure = false;
        boolean session = true;
        String authSubLogin = AuthSubUtil.getRequestUrl(next, scope, secure, session);
        String aToken = AuthSubUtil.getTokenFromReply(authSubLogin);*/
        try {
            String sessionToken = AuthSubUtil.exchangeForSessionToken(token, null);
            session.put("token", sessionToken);
            getContacts();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (GeneralSecurityException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public static void getContacts() {
        initService();
        googleService.setAuthSubToken(session.get("token"), null);
        try {
            renderArgs.put("entries", queryEntries());
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

    private static List<Contact> queryEntries()
            throws IOException, ServiceException {
        Query myQuery = new Query(new URL(DEFAULT_FEED));
        myQuery.setMaxResults(32);
        myQuery.setStringCustomParameter("sortorder", "ascending");
        myQuery.setStringCustomParameter("orderby", "lastmodified");
        List<Contact> contacts = new ArrayList<Contact>();

        try {
            ContactFeed resultFeed = googleService.query(myQuery, ContactFeed.class);
            for (int i = 0; i < resultFeed.getEntries().size(); i++) {
                ContactEntry entry = resultFeed.getEntries().get(i);
                Contact c = new Contact();
                c.name = entry.getTitle().getPlainText();
                if(entry.getContactPhotoLink() != null){
                    Link l = entry.getContactPhotoLink();
                    if(StringUtils.isNotBlank(entry.getContactPhotoLink().getHref()))
                        c.image = entry.getContactPhotoLink().getHref().substring(ROOT_URL.length());
                }
                c.email = getPrimaryEmail(entry.getXmlBlob());
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
}