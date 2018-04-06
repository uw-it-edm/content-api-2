package edu.uw.edm.contentapi2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class ContentApi2Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ContentApi2Application.class);
        app.addListeners(new ApplicationPidFileWriter());

        app.run(args);
    }
}
