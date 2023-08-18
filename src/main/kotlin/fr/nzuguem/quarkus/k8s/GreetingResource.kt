package fr.nzuguem.quarkus.k8s

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.config.inject.ConfigProperty

@Path("/hello")
class GreetingResource {

    @ConfigProperty(name = "greeting.name", defaultValue = "RESTEasy Reactive Default")
    var greetingName: String? = null;

    @ConfigProperty(name = "greeting.name.cm", defaultValue = "RESTEasy Reactive CM Default")
    var greetingNameFromCm: String? = null;

    @ConfigProperty(name = "greeting.name.secret", defaultValue = "RESTEasy Reactive SECRET Default")
    var greetingNameFromSecret: String? = null;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "Hello from $greetingName"

    @GET
    @Path("cm")
    @Produces(MediaType.TEXT_PLAIN)
    fun helloFromCm() = "Hello from $greetingNameFromCm"

    @GET
    @Path("secret")
    @Produces(MediaType.TEXT_PLAIN)
    fun helloFromSecret() = "Hello from $greetingNameFromSecret"
}