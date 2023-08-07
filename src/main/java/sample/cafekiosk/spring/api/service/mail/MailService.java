package sample.cafekiosk.spring.api.service.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sample.cafekiosk.spring.client.mail.MailSendClient;

@RequiredArgsConstructor
@Service
public class MailService {

    private final MailSendClient mailSendClient;

    public boolean sendMail(String fromEmail, String toEmail, String subject, String content) {

        boolean result = mailSendClient.sendMail(fromEmail, toEmail, subject, content);
        return false;
    }
}
