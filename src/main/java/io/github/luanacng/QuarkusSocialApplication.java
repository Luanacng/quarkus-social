package io.github.luanacng;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(
        title = "API Quarkus Social",
        version = "1.0",
        contact = @Contact(
            name = "Luana Gama",
            email = "luanagama8@gmail.com"
        )
    )
)
public class QuarkusSocialApplication extends Application{
    
}
