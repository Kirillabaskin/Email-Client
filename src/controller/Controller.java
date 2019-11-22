package controller;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Properties;

import com.sun.mail.util.MailSSLSocketFactory;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Controller extends Application {

    private Task getMail;
    private Runnable sendMail;
    @FXML
    private TextArea mailDisplayTa;
    @FXML
    private Button fetchBtn;
    @FXML
    private AnchorPane root;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private TextField subjectTf; // Label would not listen (?!)
    @FXML
    private TextField fromTf;
    @FXML
    private TextArea mailComposeTa;
    @FXML
    private TextField inputSubjectTf;
    @FXML
    private TextField inputRecipientTf;
    @FXML
    private Label sentLbl;

    private final String PORT = "587";
    private final String EMAIL = "javabootcampecology@gmail.com";
    private final String PASSWORD = "Javabootcamp2019";

    public Controller() {
    }

    @FXML
    public void initialize() {
        mailDisplayTa.prefWidthProperty().bind(root.widthProperty());
        mailComposeTa.prefWidthProperty().bind(root.widthProperty());
        progressIndicator.setVisible(false);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlUrl = getClass().getClassLoader().getResource("view/mailMenu.fxml");
        AnchorPane root = FXMLLoader.<AnchorPane>load(fxmlUrl);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bebica MailClient");
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /*     * GET AND DISPLAY LAST MAIL RECIEVED     */
    @FXML
    private void getMail() throws IOException {
        getMail = recieve();
        progressIndicator.setVisible(true);
        new Thread(getMail).start();
    }

    private Properties getRecieveProp() throws GeneralSecurityException {
        Properties properties = new Properties();
        properties.put(String.format("mail.%s.host", "imap"), "imap.gmail.com");
        properties.put(String.format("mail.%s.port", "imap"), "993");
        properties.setProperty(String.format("mail.%s.socketFactory.class", "imap"), "javax.net.ssl.SSLSocketFactory");
        properties.setProperty(String.format("mail.%s.socketFactory.fallback", "imap"), "false");
        properties.setProperty(String.format("mail.%s.socketFactory.port", "imap"), String.valueOf(993));
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        properties.put("mail.imap.ssl.trust", "*");
        properties.put("mail.imap.ssl.socketFactory", sf);
        return properties;
    }

    private Task recieve() throws IOException {
        return new Task() {
            @Override
            protected Object call() throws Exception {

                Properties props = getRecieveProp();
                props.setProperty("mail.store.protocol", "imap");
                Session session = Session.getDefaultInstance(props);
                // fetching the last recieved mail in inbox folder
                try {
                    Store store = session.getStore();
                    Message msg = null;
                    store.connect("javabootcampecology@gmail.com", "Javabootcamp2019");
                    Folder inbox = store.getFolder("Inbox");
                    inbox.open(Folder.READ_ONLY);
                    msg = inbox.getMessage(inbox.getMessageCount());

                    StringBuilder from = new StringBuilder();
                    for (Address adr : msg.getFrom()) {
                        from.append(adr + "");
                    }
                    // reading content - I've kept it as it is for future usage
                    if (msg.isMimeType("text/plain")) {
                        System.out.println("TEXT/PLAIN");
                        mailDisplayTa.setText(msg.getContent().toString());
                        progressIndicator.setVisible(false);
                        subjectTf.setText("Subject: " + msg.getSubject());
                        fromTf.setText("From: " + msg.getFrom()[0].toString());
                    } else if (msg.isMimeType("multipart/*")) {
                        System.out.println("MULTI");
                        MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();
                        String result = "";
                        int count = mimeMultipart.getCount();
                        for (int i = 0; i < count; i++) {
                            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                            if (bodyPart.isMimeType("text/plain")) {
                                result = result + "\n" + bodyPart.getContent();
                                System.out.println("PLAIN");
                            } else if (bodyPart.isMimeType("text/html")) {
                                String html = (String) bodyPart.getContent();
                                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
                                System.out.println("WITH HTML");
                            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                                result = result + (MimeMultipart) bodyPart.getContent();
                            }
                        }
                        subjectTf.setText("Subject: " + msg.getSubject());
                        fromTf.setText("From: " + msg.getFrom()[0].toString());
                        mailDisplayTa.setText(result);
                        progressIndicator.setVisible(false);
                    } else {
                        mailDisplayTa.setText("Your e-mail client can not read this type of message");
                    }
                }
                catch (MessagingException ex) {
                    System.out.println("Mail fetching error: " + ex);
                }
                return true;
            }
        };
    }

    /*
     * SEND MAIL
     */
    @FXML
    private void sendMail() throws IOException, InterruptedException {
        sendMail = send();
        progressIndicator.setVisible(true);
        Thread sending = new Thread(sendMail);
        sending.start();
//        sending.join();
//        progressIndicator.setVisible(false);

    }

    private Runnable send() throws IOException {
        return new Runnable() {
            @Override
            public void run() {

                String to = "javabootcampecology@gmail.com";//change accordingly

                // Sender's email ID needs to be mentioned
                String from = "javabootcampecology@gmail.com";//change accordingly

                final String username = "javabootcampecology@gmail.com";//change accordingly
                final String password = "Javabootcamp2019";//change accordingly

                // GMail's SMTP server
                String host = "smtp.gmail.com";

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.ssl.trust", host);

                // Get the Session object.
                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });

                try {
                    // Create a default MimeMessage object.
                    Message message = new MimeMessage(session);

                    // Set From: header field of the header.
                    message.setFrom(new InternetAddress(from));

                    // Set To: header field of the header.
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(to));

                    // Set Subject: header field
                    message.setSubject("Gmail - Email Test");

                    // Now set the actual message
                    message.setText("Hello, this is sample email to check/send "
                            + "email using JavaMailAPI from GMAIL");

                    // Send message
                    Transport.send(message);

                    System.out.println("MAIL SENT");
                    progressIndicator.setVisible(false);
                    sentLbl.setVisible(true);
                }
                catch (MessagingException ex) {
                    System.out.println("Cannot send email. " + ex);
                }
                catch (Exception ex){
                    System.out.println("Exception"+ ex.toString());
                }
            }
        };
    }

    public static void main(String[] args) {
        launch(args);
    }

}