package com.quartztop.bonus.servises;

import jakarta.annotation.PostConstruct;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {

    private ResourceBundleMessageSource messageSource;

    @PostConstruct
    public void init() {
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");  // Устанавливаем кодировку
    }

    public String getHost() {
        return messageSource.getMessage("app.host",null,Locale.getDefault());
    }

    public String getPointCreateUser() {return messageSource.getMessage("app.pointCreateUser",null,Locale.getDefault());}
    public String getWelcomeMessage() {
        return messageSource.getMessage("app.welcome-message", null, Locale.getDefault());
    }

    public String getEmailSubject() {
        return messageSource.getMessage("app.email.subject", null, Locale.getDefault());
    }

    public String getEmailFrom() {
        return messageSource.getMessage("app.email.emailFrom",null,Locale.getDefault());
    }

    public String getTextCreateAccount() {return  messageSource.getMessage("app.email.create-password-message",null,Locale.getDefault());}

    public String getErrorMessageEmail() {return  messageSource.getMessage("app.error.email-exist",null,Locale.getDefault());}

    public String getErrorMessagePhone() {return  messageSource.getMessage("app.error.phone-exist",null,Locale.getDefault());}

    public String getTextNameSalon() {return  messageSource.getMessage("app.name.salon",null,Locale.getDefault());}

    public String getTextAdressSalon() {return  messageSource.getMessage("app.address.salon",null,Locale.getDefault());}

    public String getTextFio() {return  messageSource.getMessage("app.text.name",null,Locale.getDefault());}

}
