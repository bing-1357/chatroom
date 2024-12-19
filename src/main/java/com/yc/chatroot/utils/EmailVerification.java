package com.yc.chatroot.utils;

import lombok.extern.log4j.Log4j;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

@Log4j
public class EmailVerification {

    private final String fromEmail;
    private final String toEmail;
    private final Properties props;

    public EmailVerification(String fromEmail, String toEmail) {
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;


        // Load configuration from properties file
        ResourceBundle config = ResourceBundle.getBundle("email");
        this.props = new Properties();
        props.put("mail.smtp.host", config.getString("smtp.host"));
        props.put("mail.smtp.port", config.getString("smtp.port"));
        props.put("mail.smtp.auth", config.getString("smtp.auth"));
        props.put("mail.smtp.starttls.enable", config.getString("smtp.starttls.enable"));
        props.put("mail.smtp.ssl.trust", config.getString("smtp.ssl.trust"));

        // Optional: Enable debugging output (if needed)
        if ("true".equalsIgnoreCase(config.getString("debug"))) {
            props.put("mail.debug", "true");
        }
    }

    public String sendVerificationCode() throws MessagingException, IOException {
        // Generate a verification code
        String verificationCode = generateVerificationCode();

        // Read the HTML template and replace the placeholder with the verification code
        String htmlContent = readTemplateAndReplace(verificationCode);

        // Create a session with an authenticator
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                ResourceBundle config = ResourceBundle.getBundle("email");
                return new PasswordAuthentication(fromEmail, config.getString("from.email.password"));
            }
        });

        try {
            // Create a message
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("欢迎注册开发者bing的聊天室");       //主题
            msg.setContent(htmlContent, "text/html; charset=utf-8");

            // Send the message
            Transport.send(msg);

            System.out.println("Verification code sent to " + toEmail);
//            log.info("验证码："+verificationCode);
            return verificationCode;

        } catch (MessagingException e) {
            throw new MessagingException("Failed to send verification code: " + e.getMessage(), e);
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999)); // 6 digit verification code
    }

    private String readTemplateAndReplace(String verificationCode) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/test.html");
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        return contentBuilder.toString().replace("{verification_code}", verificationCode);
    }

    public static void main(String[] args) {
        try {
            // Example usage:
            EmailVerification emailVerification = new EmailVerification(
                    "1841248198@qq.com", // 发信人邮箱
                    "1841248198@qq.com"// 收信人邮箱
                     // HTML模板路径
            );
            String code = emailVerification.sendVerificationCode();
            log.info("验证码："+code);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}