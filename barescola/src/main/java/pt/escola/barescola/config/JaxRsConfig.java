package pt.escola.barescola.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** Ativa o JAX-RS: todos os recursos REST ficam em /barescola/api/... */
@ApplicationPath("api")
public class JaxRsConfig extends Application {
}
