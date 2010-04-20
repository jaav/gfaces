package controllers;

import play.mvc.*;

import java.net.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
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
import org.apache.commons.io.IOUtils;
import models.Contact;
import models.SearchFilter;
import models.Image;

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

    public static void doSubmitImages(){
        Map imagesInfo = params.allSimple();
        int imageCount = (int)Math.floor(imagesInfo.size()/3);
        Image[] images = new Image[imageCount];
        for (Object key : imagesInfo.keySet()) {
            String[] parts = ((String)key).split("_");
            if(parts.length == 2){
                Image current = new Image();
                if("id".equals(parts[0])) current.contact_id = (String)imagesInfo.get(key);
                else if("image".equals(parts[0])) try {
                    current.url = URLDecoder.decode((String)imagesInfo.get(key), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                else if("style".equals(parts[0])) current.style = ((String)imagesInfo.get(key)).replaceAll(" ", "");
                else System.out.println("This shouldn't happen! Check Application.class code line 191");
                images[Integer.parseInt(parts[1])] = current;
            }
        }
        processImagesTest();
        //processImages(images);
    }

    private static void processImages(Image[] images){
        for (int i = 0; i < images.length; i++) {
            Image image = images[i];
            //download the image, give it a unique name and store it locally with the extension set to the corresponding mime type

            //process the image

            //add the unique name to the object
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

    private static String getExtension(String contentType){
        if(contentType.indexOf("jpeg")>0) return ".jpg";
        if(contentType.indexOf("png")>0) return ".png";
        if(contentType.indexOf("gif")>0) return ".gif";
        else return "";
    }


    /****************************************************************
     *
     * Tests
     *
     ****************************************************************/

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

    private static String getImageDimension(String path, String image){
        //identify -format '%k' tree.gif
        String command="C:/jwmcwt6/downloads/ImageMagick-6.6.1-4/identify -format %w "+path+image;
        try{
            Process proc =Runtime.getRuntime().exec(command);
            InputStream stream = proc.getInputStream();
            String dim = IOUtils.toString(stream);
            System.out.println("command = " + command);
            System.out.println("Image Dimension: "+dim);
            return dim;
        }catch(Exception e){
            System.out.println("error");
        }
        return null;
    }

    private static String getStylesWidth(String style){
        int widthStart = style.indexOf("width:")+6;
        return style.substring(widthStart, style.indexOf("px", widthStart));
    }

    private static void runIMProcess(String path, Image[] images){
        for (int i = 0; i < images.length; i++) {
            Image image = images[i];
            try{
                String test =getImageDimension(path, image.name).replaceAll("\\D", "");
                int realWidth = Integer.parseInt(getImageDimension(path, image.name).replaceAll("\\D", ""));
                int stylesWidth = Integer.parseInt(getStylesWidth(image.style));
                int scale = Math.round(stylesWidth*100/realWidth);
                //String command="C:/jwmcwt6/downloads/ImageMagick-6.6.1-4/convert "+path+"im.jpg -resize 150%  "+path +"new_im.jpg -crop 180x180+10+10  "+path+"crop.gif";
                String command="C:/jwmcwt6/downloads/ImageMagick-6.6.1-4/convert "+path+image.name+" -resize "+scale+"%  "+path+image.name+" -crop 96x96+0+0 "+path+image.name;
                Process proc =Runtime.getRuntime().exec(command);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void processImagesTest(){
        String path = "C:/jwmcwt6/toDelete/";
        Image[] images = new Image[3];
        images[0] = new Image();
        images[1] = new Image();
        images[2] = new Image();
        images[0].contact_id = "1";
        images[0].style = "width:291px;height:205px;margin-left:-170px;margin-top:0px;";
        images[0].url = "http://localhost/nodecaster/images/one.jpg";
        images[1].contact_id = "2";
        images[1].style = "width:373px;height:505px;margin-left:-242px;margin-top:-115px;";
        images[1].url = "http://localhost/nodecaster/images/two.jpg";
        images[2].contact_id = "3";
        images[2].style = "width:291px;height:194px;margin-left:-59px;margin-top:-39px;";
        images[2].url = "http://localhost/nodecaster/images/three.jpg";
        for (int i = 0; i < images.length; i++) {
            Image image = images[i];
            String name = UUID.randomUUID().toString();
            //download the image, give it a unique name and store it locally with the extension set to the corresponding mime type
            try {
                URL url = new URL(image.url);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                String strStatus = conn.getResponseMessage() + " (" + conn.getResponseCode() + ")";
                String strContentType = conn.getContentType();
                InputStream is = conn.getInputStream();
                String ext = getExtension(conn.getContentType());
                FileOutputStream fos = new FileOutputStream(new File(path+name+ext));
                image.name = name+ext;
                IOUtils.copy(is, fos);
                is.close();
                fos.close();
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        //process the images
        runIMProcess(path, images);
        //add the unique name to the object

    }

    
}