package sample.cafekiosk.spring.api.service.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sample.cafekiosk.spring.client.mail.MailSendClient;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistory;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 이게 있어야 @Mock 에너테이션이 붙은 객체 만들어줌
class MailServiceTest {

    @Mock
    private MailSendClient mailSendClient;
    //MailSendClient mailSendClient = mock(MailSendClient.class); 이것과 같음

    @Mock
    private MailSendHistoryRepository mailSendHistoryRepository;
    //MailSendHistoryRepository mailSendHistoryRepository = mock(MailSendHistoryRepository.class); 이것과 같음

    @InjectMocks
    private MailService mailService;
    // MailService mailService = new MailService(mailSendClient, mailSendHistoryRepository); 이것과 같음

    @DisplayName("메일 전송 테스트")
    @Test
    void sendMail() {
        // give
        when(mailSendClient.sendMail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        // when
        boolean result = mailService.sendMail("", "", "", "");

        // then
        assertThat(result).isTrue();
        verify(mailSendHistoryRepository, times(1)).save(any(MailSendHistory.class));
    }
}