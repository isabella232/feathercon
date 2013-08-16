FeatherCon lightweight servlet container
----------------------------------------

### Usage

FeatherCon(tainer) is a lightweight servlet container built around embedded Jetty.  The first use case for which FeatherCon
was designed was to host small production or tool servers for JAX-RS Jersey-based REST services.  Here's how to set
up a JAX-RS server, which is taken from one of the unit tests:

    String scanPackages = "com.xoom.oss.feathercon";
    FeatherCon server = new JerseyServerBuilder(scanPackages).build();
    server.start();

Here's how to setup a servlet that serves static content:

    ServletConfiguration.ServletConfigurationBuilder servletBuilder = new ServletConfiguration.ServletConfigurationBuilder();
    servletBuilder
            .withServletClass(DefaultServlet.class)
            .withPathSpec("/")
            .withInitParameter("resourceBase", "test-data/static/");

    FeatherCon.FeatherConBuilder serverBuilder = new FeatherCon.FeatherConBuilder();
    serverBuilder.withServletConfiguration(servletBuilder.build());
    FeatherCon server = serverBuilder.build();
    server.start();

Note that Jersey, the JAX-RS reference implementation, is not a formal dependency of FeatherCon and will therefore not appear
in the FeatherCon published POM.  If you want to use the JAX-RS JerseyServerBuilder your app must explicitly depend on
a Jersey implementation.

A JAX-RS servlet combined with a static content servlet can be produced using the WebappServerBuilder:

    String resourceBase = "/tmp";  // path to static content
    String restPathSpec = "/api/*";
    int serverPort = 8888;
    String jerseyScanPackages = "com.xoom.oss.feathercon";
    WebappServerBuilder webappServerBuilder = new WebappServerBuilder(jerseyScanPackages, restPathSpec, resourceBase, serverPort);
    FeatherCon server = webappServerBuilder.build();
    server.start();

So a FeatherConBuilder can be used to host a set of arbitrary servlets with the appropriate configs exercised
through the various builder withXXX methods. FeatherCon is also useful in unit tests, where one needs to quickly spin
up a fast backend on a per-test basis.

### Building

To build using the included [Gradle Wrapper](http://www.gradle.org/docs/current/userguide/gradle_wrapper.html):

    $ ./gradlew clean build

Installing to the local Maven repository proceeds thusly

    $ ./gradlew publishToMavenLocal
    ...
    :publishMavenJavaPublicationToMavenLocal
    Uploading: com/xoom/oss/feathercon/1.2.1/feathercon-1.2.1.jar to repository remote at file:/Users/petrovic/.m2/repository/
    Transferring 11K from remote
    Uploaded 11K
    Uploading: com/xoom/oss/feathercon/1.2.1/feathercon-1.2.1-source.jar to repository remote at file:/Users/petrovic/.m2/repository/

### Maven coordinates

The binary for this build can be used in a Maven build by specifying this repository in your POM

        <repositories>
            <repository>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
                <id>bintray</id>
                <name>bintray</name>
                <url>http://dl.bintray.com/xoom/xoomoss</url>
            </repository>
        </repositories>

with this dependency

        <dependencies>
            <dependency>
                <groupId>com.xoom.oss</groupId>
                <artifactId>feathercon</artifactId>
                <version>1.2.2</version>  <!-- or latest in the repository above -->
            </dependency>
        </dependencies>