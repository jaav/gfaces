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
import com.google.gdata.util.*;
import exceptions.NotAuthenticatedException;
import org.omg.PortableInterceptor.RequestInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;
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
    private static final String DEFAULT_HOME = "http://localhost:8080/";
    //private static final String DEFAULT_HOME = "http://www.nodecaster.com:8080/";
    //private static final String IM_PATH = "C:/jwmcwt6/downloads/ImageMagick-6.6.1-4/";
    private static final String TMP_PATH = play.Play.configuration.getProperty("gfaces.tmp");

    private static final String IM_PATH = play.Play.configuration.getProperty("gfaces.magick");
    private static final String LOCAL_WEB_HTTP = play.Play.configuration.getProperty("gfaces.local.web.http");
    private static final String LOCAL_WEB_FILE = play.Play.configuration.getProperty("gfaces.local.web.file");
    private static int filterQuantity = 32;
    

    /**************************************************************************************
    * -------------------------------- Public Methods ---------------------------------- *
    ***************************************************************************************/

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
        if (filter == null) filter = "";
        if(from == 0) from = 1; 
        if(quantity == 0) quantity = 100;
        session.put("from", from);
        session.put("quantity", quantity);
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
        for(int i = 0; i<imageCount; i++){
            images[i] = new Image();
        }
        for (Object key : imagesInfo.keySet()) {
            String[] parts = ((String)key).split("_");
            if(parts.length == 2){
                if("editLink".equals(parts[0])) images[Integer.parseInt(parts[1])].link_href = (String)imagesInfo.get(key);
                else if("etag".equals(parts[0])) images[Integer.parseInt(parts[1])].etag = (String)imagesInfo.get(key);
                else if("image".equals(parts[0])) try {
                    images[Integer.parseInt(parts[1])].remote_url = URLDecoder.decode((String)imagesInfo.get(key), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                else if("style".equals(parts[0])) images[Integer.parseInt(parts[1])].style = ((String)imagesInfo.get(key)).replaceAll(" ", "");
                else System.out.println("This shouldn't happen! Check Application.class code line 207 why "+parts[0]+" isn't processed");
            }
        }
        //processImagesTest();
        processImages(images);
        System.out.println("PROCESSING DONE, NOW SHOW SAME PAGE AGAIN");
        getContacts(Integer.parseInt(session.get("from")), Integer.parseInt(session.get("quantity")), session.get("filter"));
    }


    /**************************************************************************************
    * -------------------------------- Private Methods ---------------------------------- *
    ***************************************************************************************/

    private static void processImages(Image[] images){
        List<Image> validImages = new ArrayList<Image>();
        for (int i = 0; i < images.length; i++) {
            Image image = images[i];
            String inName = UUID.randomUUID().toString();
            String outName = UUID.randomUUID().toString();
            //download the image, give it a unique name and store it locally with the extension set to the corresponding mime type
            try {
                URL url = new URL(image.remote_url);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                image.mime = conn.getContentType();
                InputStream is = conn.getInputStream();
                String ext = getExtension(conn.getContentType());
                FileOutputStream fos = new FileOutputStream(new File(TMP_PATH+inName+ext));
                image.inName = inName+ext;
                image.outName = outName+ext;
                IOUtils.copy(is, fos);
                is.close();
                fos.close();
                conn.disconnect();
                validImages.add(image);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        runIMProcess(validImages);
        uploadImagesToContacts(validImages);
    }

    private static void uploadImagesToContacts(List<Image> validImages) {
        for (Image validImage : validImages) {
            URL photoUrl = null;
            try {

                photoUrl = new URL(validImage.link_href);
                Service.GDataRequest request = googleService.createRequest(Service.GDataRequest.RequestType.UPDATE,
                    photoUrl, new ContentType(validImage.mime));
                if(validImage.etag != null) request.setEtag(validImage.etag);

                /*URL url = new URL(LOCAL_WEB+validImage.outName);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();*/
                FileInputStream fis = new FileInputStream(new File(TMP_PATH, validImage.outName));
                
                OutputStream requestStream = request.getRequestStream();

                IOUtils.copy(fis, requestStream);
                fis.close();
                request.execute();
                //conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ServiceException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

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
                c.image_edit_link_href = entry.getContactEditPhotoLink().getHref();
                if(entry.getContactPhotoLink() != null){
                    c.image_link_href = StringUtils.isNotBlank(entry.getContactPhotoLink().getHref()) ? getLocalPicture(entry.getContactPhotoLink().getHref()) : "";
                    c.image_etag = entry.getContactPhotoLink().getEtag();
                }
                c.name = entry.getTitle().getPlainText();
                if(searchFilter.filterPicture) continue;
                if(searchFilter.filterName && !isValidName(c.name)) continue;
                c.email = getPrimaryEmail(entry.getXmlBlob());
                if(searchFilter.filterMail && !isValidPrimaryEmail(c.email)) continue;
                if(!searchFilter.filterName && !isValidName(c.name) && StringUtils.isNotBlank(c.email)) c.name = c.email.substring(0, c.email.indexOf('@')).replace(".", " ");
                contacts.add(c);

            }
            return contacts;
        } catch (NoLongerAvailableException ex) {
            ex.printStackTrace();
            return null;
        }
  }

    private static String getLocalPicture(String googleUrl){

        try {
            URL photoUrl = new URL(googleUrl);
            String outName = UUID.randomUUID().toString()+".jpg";

            Link l = new Link();
            l.setHref(googleUrl);

            Service.GDataRequest request = googleService.createLinkQueryRequest(l);
            request.execute();

            FileOutputStream fos = new FileOutputStream(new File(LOCAL_WEB_FILE, outName));

            IOUtils.copy(request.getResponseStream(), fos);
            request.end();
            fos.close();
            return LOCAL_WEB_HTTP+outName;
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    private static String getStylesWidth(String style){
        int widthStart = style.indexOf("width:")+6;
        return style.substring(widthStart, style.indexOf("px", widthStart));
    }

    private static String getImageDimension(String path, String image){
        //identify -format '%k' tree.gif
        String command=IM_PATH+"identify -format %w "+path+image;
        try{
            Process proc =Runtime.getRuntime().exec(command);
            InputStream stream = proc.getInputStream();
            String dim = IOUtils.toString(stream);
            return dim;
        }catch(Exception e){
            System.out.println("error");
        }
        return null;
    }

    private static String getMargins(String style){
        StringBuilder margins = new StringBuilder();
        int leftStart = style.indexOf("margin-left:")+12;
        int topStart = style.indexOf("margin-top:")+11;
        String left = style.substring(leftStart, style.indexOf("px", leftStart));
        String top = style.substring(topStart, style.indexOf("px", topStart));
        margins.append("+").append(left.startsWith("-") ? left.substring(1) : left);
        margins.append("+").append(top.startsWith("-") ? top.substring(1) : top);//.append(" ");
        return margins.toString();
    }

    private static void runIMProcess(List<Image> images){
        for (Image image : images) {
            try{
                String test =getImageDimension(TMP_PATH, image.inName).replaceAll("\\D", "");
                int realWidth = Integer.parseInt(getImageDimension(TMP_PATH, image.inName).replaceAll("\\D", ""));
                int stylesWidth = Integer.parseInt(getStylesWidth(image.style));
                String margins = getMargins(image.style);
                int scale = stylesWidth>realWidth ? Math.round(stylesWidth*100/realWidth) : 100;
                //String command="C:/jwmcwt6/downloads/ImageMagick-6.6.1-4/convert "+path+image.name+" -resize "+scale+"%  "+path+image.name+" -crop 96x96"+margins+path+image.name;
                //String command="C:/jwmcwt6/downloads/ImageMagick-6.6.1-4/convert "+path+image.name+" -resize "+scale+"%  "+path+image.name+" -crop 96x96"+margins+path+image.name;
                String command=IM_PATH+"convert "+TMP_PATH+image.inName+" -resize "+scale+"% - | "+IM_PATH+"convert - -crop 96x96"+margins+TMP_PATH+image.outName;
                System.out.println("command = " + command);
                ProcessBuilder pb = new ProcessBuilder(IM_PATH+"convert",
                        TMP_PATH+image.inName,
                        "-resize",
                        scale+"%",
                        TMP_PATH+"tmp.jpg");
                        /*"-",
                        "|",
                        IM_PATH+"convert",
                        "-",
                        "-crop",
                        "96x96",
                        margins,
                        TMP_PATH+image.outName);*/
                pb.redirectErrorStream(true);

                Process p = pb.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                while((line=br.readLine())!=null){
                    System.out.println(line);
                }
                System.out.println(p.waitFor());
                System.out.println("margins = *" + margins + "*");

                pb = new ProcessBuilder(IM_PATH+"convert",
                        TMP_PATH+"tmp.jpg",
                        "-crop",
                        "96x96"+margins,
                        TMP_PATH+image.outName);
                pb.redirectErrorStream(true);

                p = pb.start();
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                line = null;
                while((line=br.readLine())!=null){
                    System.out.println(line);
                }
                System.out.println(p.waitFor());


                //String[] testt = {"", ""};
                //Process proc =Runtime.getRuntime().exec(testt);
                //int exitVal = proc.waitFor(); 
                String trest = "0";
                //System.out.println("proc = " + IOUtils.toString(proc.getInputStream()));
                //System.out.println("proc error = " + IOUtils.toString(proc.getErrorStream()));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    /****************************************************************
     *
     * Tests
     *
     ****************************************************************/

    private static List<Contact> queryEntriesTest(String filter){
        List<Contact> theContacts = new ArrayList<Contact>();
        String[][] contacts = {
                {"Jef, Waumans","http://media.linkedin.com/mpr/mpr/shrink_80_80/p/2/000/033/19e/0ad1aef.jpg", "jef@waumans.net"},
                {"Test test","","test@test.com "},
                {"Els Verreck","","els.verreck@telenet.be"},
                {"Pieter Wuyts","","pieter@8seconds.net"},
                {"Frank Salliau","","frank@boek.be"}
        };
        SearchFilter searchFilter = new SearchFilter(filter);
        for (int i = 0; i < contacts.length; i++) {
            String[] contact = contacts[i];
            theContacts.add(new Contact(contact));
        }
        return theContacts;
    }

    private static void processImagesTest(){
        String path = "C:/jwmcwt6/toDelete/";
        Image[] images = new Image[3];
        List<Image> validImages = new ArrayList<Image>();
        images[0] = new Image();
        images[1] = new Image();
        images[2] = new Image();
        images[0].style = "width:291px;height:205px;margin-left:-170px;margin-top:0px;";
        images[0].link_href = "http://localhost/nodecaster/images/one.jpg";
        images[0].remote_url = "http://localhost/nodecaster/images/one.jpg";
        images[1].link_href = "2";
        images[1].style = "width:373px;height:505px;margin-left:-242px;margin-top:-115px;";
        images[1].link_href = "http://localhost/nodecaster/images/two.jpg";
        images[1].remote_url = "http://localhost/nodecaster/images/two.jpg";
        images[2].link_href = "3";
        images[2].style = "width:291px;height:194px;margin-left:-59px;margin-top:-39px;";
        images[2].link_href = "http://localhost/nodecaster/images/three.jpg";
        images[2].remote_url = "http://localhost/nodecaster/images/three.jpg";
        for (int i = 0; i < images.length; i++) {
            Image image = images[i];                      
            String inName = UUID.randomUUID().toString();
            String outName = UUID.randomUUID().toString();
            //download the image, give it a unique name and store it locally with the extension set to the corresponding mime type
            try {
                URL url = new URL(image.remote_url);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                String strStatus = conn.getResponseMessage() + " (" + conn.getResponseCode() + ")";
                String strContentType = conn.getContentType();
                InputStream is = conn.getInputStream();
                String ext = getExtension(conn.getContentType());
                FileOutputStream fos = new FileOutputStream(new File(path+inName+ext));
                image.inName = inName+ext;
                image.outName = outName+ext;
                IOUtils.copy(is, fos);
                is.close();
                fos.close();
                conn.disconnect();
                validImages.add(image);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        //process the images
        runIMProcess(validImages);

    }

    
}