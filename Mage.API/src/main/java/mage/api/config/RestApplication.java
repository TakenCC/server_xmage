package mage.api.config;

import mage.api.resources.AuthResource;
import mage.api.resources.RoomResource;
import mage.api.resources.TableResource;
import mage.api.resources.GameResource;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class RestApplication extends ResourceConfig {

    public RestApplication() {
        // Register REST resources
        register(AuthResource.class);
        register(RoomResource.class);
        register(TableResource.class);
        register(GameResource.class);
        
        // Register filters
        register(JwtAuthFilter.class);
        register(CorsFilter.class);
        
        // Enable Jackson for JSON
        packages("com.fasterxml.jackson.jaxrs.json");
    }
    
    // DependencyBinder will be registered externally via Main.java
}

