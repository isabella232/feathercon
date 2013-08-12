FeatherCon lightweight servlet container
----------------------------------------

### Usage

FeatherCon(tainer) is a lightweight servlet container built around embedded Jetty.  The first use case for which FeatherCon
was designed was to host small production or tool servers for JAX-RS Jersey-based REST services.  Here's how to set
one up, which is taken from one of the unit tests:

    String scanPackages = "com.xoom.feathercon";
    FeatherCon server = new JerseyServerBuilder(scanPackages).build();
    server.start();

    ClientConfig clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    Client client = Client.create(clientConfig);
    WebResource resource = client.resource("http://localhost:8080/users");
    User user = resource.accept(MediaType.APPLICATION_JSON_TYPE).get(User.class);
    System.out.println(user);

    server.stop();

We've also found FeatherCon to be useful in unit tests, where spinning up a fast backend on a per-test basis to test
REST clients was needed.

The FeatherCon builder com.xoom.feathercon.FeatherCon.FeatherConBuilder can be used to host an arbitrary servlet
with the appropriate configs exercised through the various builder withXXX methods.

### Building

To build using the included Gradle Wrapper (http://www.gradle.org/docs/current/userguide/gradle_wrapper.html):

$ ./gradlew clean build