# Quick run

Have a docker service running and just run this script.
Set the docker host in the Makefile under the REGISTRY_HOST tag.
Simply run: 

    chmod +x run.sh
    ./run.sh

Test that everything is working by running some client side commands.
Working directory is considered the top level.

    mvn test
        
# Swarm 

Start (or join) a Docker Swarm

    docker swarm init --advertise-addr 127.0.0.1
    
        
#Build and Run
The following instruction can be used to deploy the entire application in a Docker
Swarm environment. The following steps assume that the path is at the top-level
directory of the project (same as this README).


Build the project
    
    mvn clean package -DskipTests=true


Build the docker images 
    
    make
    
    
Set the redis flags

    docker node update --label-add redis-master=true docker-desktop
    docker node update --label-add redis-replica=true docker-desktop
    docker node update --label-add redis-sentinel=true docker-desktop
    docker node update --label-add spring=true docker-desktop
    docker node update --label-add kafka=true docker-desktop
    docker node update --label-add zookeeper=true docker-desktop
    docker node update --label-add mysql=true docker-desktop


Deploy the FULL stack

    docker stack deploy -c swarm.yml dge

    
Deploy just the Databases and Kafka (useful for testing)

    docker stack deploy -c auxiliary.yml dge


Change Spring properties on runtime

    java -jar myproject.jar --spring.config.name=myproject
    
or through Java System properties (System.getProperties()).

# Docker commands (run from PowerShell)

Stop a deployed stack

    docker stack rm dge
    

Update the service images 

    docker service update --force dge_game-master


Stop all services

    docker service rm $(docker service ls -q)
    
    
Stop and remove all EXITED containers

    docker rm $(docker ps --filter "status=exited" -q)


Remove all unused images

    docker image prune -f
    
Remove all dangling images (none tags)
    
    docker rmi $(docker images -aq --filter dangling=true)


Stop and remove all containers

    docker stop $(docker ps -a -q)
    docker rm $(docker ps -a -q)


Remove all instances and containers

    docker rm -f $(docker ps -a -q)
    docker rmi -f $(docker images -q)

# Registry

Deploy a Docker registry so other nodes can access our images

    docker service create --name registry --publish published=5000,target=5000 registry:2

Verify that it works (should return '{}')

    curl http://localhost:5000/v2/
    
Push something

    make build 
    make push
    
Remove the registry

    docker service rm registry
    
# Summary

## User authentication with JWT

JSON Web Token (JWT) is an open standard (RFC 7519) that defines a compact and self-contained way for securely transmitting information between parties as a JSON object. This information can be verified and trusted because it is digitally signed. JWTs can be signed using a secret (with the HMAC algorithm) or a public/private key pair using RSA.

Let's explain some concepts of this definition further.

**Compact**: Because of their smaller size, JWTs can be sent through a URL, POST parameter, or inside an HTTP header. Additionally, the smaller size means transmission is fast.

**Self-contained**: The payload contains all the required information about the user, avoiding the need to query the database more than once.

## When should we use JSON Web Tokens?

Here are some scenarios where JSON Web Tokens are useful:

**Authentication**: This is the most common scenario for using JWT. Once the user is logged in, each subsequent request will include the JWT, allowing the user to access routes, services, and resources that are permitted with that token. Single Sign On is a feature that widely uses JWT nowadays, because of its small overhead and its ability to be easily used across different domains.

**Information Exchange**: JSON Web Tokens are a good way of securely transmitting information between parties. Because JWTs can be signed—for example, using public/private key pairs—we can be sure the senders are who they say they are. Additionally, as the signature is calculated using the header and the payload, we can also verify that the content hasn't been tampered with.

## What is the JSON Web Token structure?

JSON Web Tokens consist of three parts separated by dots **(.)**, which are:

1. Header
2. Payload
3. Signature

Therefore, a JWT typically looks like the following.

`xxxxx`.`yyyyy`.`zzzzz`

Let's break down the different parts.

**Header**

The header typically consists of two parts: the type of the token, which is JWT, and the hashing algorithm being used, such as HMAC SHA256 or RSA.

For example:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

Then, this JSON is Base64Url encoded to form the first part of the JWT.

**Payload**

The second part of the token is the payload, which contains the claims. Claims are statements about an entity (typically, the user) and additional metadata. There are three types of claims: reserved, public, and private claims.

- **Reserved claims**: These are a set of predefined claims which are not mandatory but recommended, to provide a set of useful, interoperable claims. Some of them are: iss (issuer), exp (expiration time), sub (subject), aud (audience), and others.

> Notice that the claim names are only three characters long as JWT is meant to be compact.

- **Public claims**: These can be defined at will by those using JWTs. But to avoid collisions they should be defined in the IANA JSON Web Token Registry or be defined as a URI that contains a collision resistant namespace.

- **Private claims**: These are the custom claims created to share information between parties that agree on using them.

An example of payload could be:

```json
{
  "sub": "1234567890",
  "name": "John Doe",
  "admin": true
}
```

The payload is then Base64Url encoded to form the second part of the JSON Web Token.

**Signature**

To create the signature part we have to take the encoded header, the encoded payload, a secret, the algorithm specified in the header, and sign that.

For example if we want to use the HMAC SHA256 algorithm, the signature will be created in the following way:

```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret)
```

The signature is used to verify that the sender of the JWT is who it says it is and to ensure that the message wasn't changed along the way.
Putting all together

The output is three Base64 strings separated by dots that can be easily passed in HTML and HTTP environments, while being more compact when compared to XML-based standards such as SAML.

The following shows a JWT that has the previous header and payload encoded, and it is signed with a secret. Encoded JWT

![](https://camo.githubusercontent.com/a56953523c443d6a97204adc5e39b4b8c195b453/68747470733a2f2f63646e2e61757468302e636f6d2f636f6e74656e742f6a77742f656e636f6465642d6a7774332e706e67)

## How do JSON Web Tokens work?

In authentication, when the user successfully logs in using their credentials, a JSON Web Token will be returned and must be saved locally (typically in local storage, but cookies can be also used), instead of the traditional approach of creating a session in the server and returning a cookie.

Whenever the user wants to access a protected route or resource, the user agent should send the JWT, typically in the Authorization header using the Bearer schema. The content of the header should look like the following:

`Authorization: Bearer <token>`

This is a stateless authentication mechanism as the user state is never saved in server memory. The server's protected routes will check for a valid JWT in the Authorization header, and if it's present, the user will be allowed to access protected resources. As JWTs are self-contained, all the necessary information is there, reducing the need to query the database multiple times.

This allows us to fully rely on data APIs that are stateless and even make requests to downstream services. It doesn't matter which domains are serving wer APIs, so Cross-Origin Resource Sharing (CORS) won't be an issue as it doesn't use cookies.

The following diagram shows this process:

![](https://camo.githubusercontent.com/5871e9f0234542cd89bab9b9c100b20c9eb5b789/68747470733a2f2f63646e2e61757468302e636f6d2f636f6e74656e742f6a77742f6a77742d6469616772616d2e706e67) 

# JWT Authentication Summary

Token based authentication schema's became immensely popular in recent times, as they provide important benefits when compared to sessions/cookies:

- CORS
- No need for CSRF protection
- Better integration with mobile
- Reduced load on authorization server
- No need for distributed session store

Some trade-offs have to be made with this approach:

- More vulnerable to XSS attacks
- Access token can contain outdated authorization claims (e.g when some of the user privileges are revoked)
- Access tokens can grow in size in case of increased number of claims
- File download API can be tricky to implement
- True statelessness and revocation are mutually exclusive

**JWT Authentication flow is very simple**

1. User obtains Refresh and Access tokens by providing credentials to the Authorization server
2. User sends Access token with each request to access protected API resource
3. Access token is signed and contains user identity (e.g. user id) and authorization claims.

It's important to note that authorization claims will be included with the Access token. Why is this important? Well, let's say that authorization claims (e.g user privileges in the database) are changed during the life time of Access token. Those changes will not become effective until new Access token is issued. In most cases this is not big issue, because Access tokens are short-lived. Otherwise go with the opaque token pattern.

# Implementation Details

Let's see how can we implement the JWT token based authentication using Java and Spring, while trying to reuse the Spring security default behavior where we can. The Spring Security framework comes with plug-in classes that already deal with authorization mechanisms such as: session cookies, HTTP Basic, and HTTP Digest. Nevertheless, it lacks from native support for JWT, and we need to get our hands dirty to make it work.

## MySQL DB

This demo is currently using a MySQL database called **user_db** that's automatically configured by Spring Boot. If we want to connect to another database we have to specify the connection in the `application.yml` file inside the resource directory. Note that `hibernate.hbm2ddl.auto=create-drop` will drop and create a clean database each time we deploy (we may want to change it if we are using this in a real project). Here's the example from the project:

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_db
    username: root
    password: null
  tomcat:
    max-wait: 20000
    max-active: 50
    max-idle: 20
    min-idle: 15
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
        id:
          new_generator_mappings: false
```

## Core Code

1. `JwtTokenFilter`
2. `JwtTokenFilterConfigurer`
3. `JwtTokenProvider`
4. `MyUserDetails`
5. `WebSecurityConfig`

**JwtTokenFilter**

The `JwtTokenFilter` filter is applied to each API (`/**`) with exception of the signin token endpoint (`/users/signin`) and singup endpoint (`/users/signup`).

This filter has the following responsibilities:

1. Check for access token in Authorization header. If Access token is found in the header, delegate authentication to `JwtTokenProvider` otherwise throw authentication exception
2. Invokes success or failure strategies based on the outcome of authentication process performed by JwtTokenProvider

Please ensure that `chain.doFilter(request, response)` is invoked upon successful authentication. We want processing of the request to advance to the next filter, because very last one filter *FilterSecurityInterceptor#doFilter* is responsible to actually invoke method in our controller that is handling requested API resource.

```java
String token = jwtTokenProvider.resolveToken((HttpServletRequest) req);
if (token != null && jwtTokenProvider.validateToken(token)) {
  Authentication auth = jwtTokenProvider.getAuthentication(token);
  SecurityContextHolder.getContext().setAuthentication(auth);
}
filterChain.doFilter(req, res);
```

**JwtTokenFilterConfigurer**

Adds the `JwtTokenFilter` to the `DefaultSecurityFilterChain` of spring boot security.

```java
JwtTokenFilter customFilter = new JwtTokenFilter(jwtTokenProvider);
http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
```

**JwtTokenProvider**

The `JwtTokenProvider` has the following responsibilities:

1. Verify the access token's signature
2. Extract identity and authorization claims from Access token and use them to create UserContext
3. If Access token is malformed, expired or simply if token is not signed with the appropriate signing key Authentication exception will be thrown

**MyUserDetails**

Implements `UserDetailsService` in order to define our own custom *loadUserbyUsername* function. The `UserDetailsService` interface is used to retrieve user-related data. It has one method named *loadUserByUsername* which finds a user entity based on the username and can be overridden to customize the process of finding the user.

It is used by the `DaoAuthenticationProvider` to load details about the user during authentication.

**WebSecurityConfig**

The `WebSecurityConfig` class extends `WebSecurityConfigurerAdapter` to provide custom security configuration.

Following beans are configured and instantiated in this class:

1. `JwtTokenFilter`
3. `PasswordEncoder`

Also, inside `WebSecurityConfig#configure(HttpSecurity http)` method we'll configure patterns to define protected/unprotected API endpoints. Please note that we have disabled CSRF protection because we are not using Cookies.

```java
// Disable CSRF (cross site request forgery)
http.csrf().disable();

// No session will be created or used by spring security
http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

// Entry points
http.authorizeRequests()
  .antMatchers("/users/signin").permitAll()
  .antMatchers("/users/signup").permitAll()
  // Disallow everything else..
  .anyRequest().authenticated();

// If a user try to access a resource without having enough permissions
http.exceptionHandling().accessDeniedPage("/login");

// Apply JWT
http.apply(new JwtTokenFilterConfigurer(jwtTokenProvider));

// Optional, if we want to test the API from a browser
// http.httpBasic();
```

# Message passing, WebSockets and STOMP

The WebSocket protocol is one of the ways to make your application handle real-time messages. The most common alternatives are long polling and server-sent events. Each of these solutions has its advantages and drawbacks. In this article, I am going to show you how to implement WebSockets with the Spring Boot Framework. I will cover both the server-side and the client-side setup, and we will use STOMP over WebSocket protocol to communicate with each other.

The server-side will be coded purely in Java. But, in the case of the client, I will show snippets written both in Java and in JavaScript (SockJS) since, typically, WebSockets clients are embedded in front-end applications. The code examples will demonstrate how to broadcast messages to multiple users using the pub-sub model as well as how to send messages only to a single user. In a further part of the article, I will briefly discuss securing WebSockets and how we can ensure that our WebSocket-based solution will stay operational even when the environment does not support the WebSocket protocol.

Please note that the topic of securing WebSockets will only briefly be touched on here since it is a complex enough topic for a separate article. Due to this, and several other factors that I touch on in the WebSocket in Production? section in the end, I recommend making modifications before using this setup in production, read until the end for a production-ready setup with security measures in place.

## WebSocket and STOMP Protocols
The WebSocket protocol allows you to implement bidirectional communication between applications. It is important to know that HTTP is used only for the initial handshake. After it happens, the HTTP connection is upgraded to a newly opened TCP/IP connection that is used by a WebSocket.

The WebSocket protocol is a rather low-level protocol. It defines how a stream of bytes is transformed into frames. A frame may contain a text or a binary message. Because the message itself does not provide any additional information on how to route or process it, It is difficult to implement more complex applications without writing additional code. Fortunately, the WebSocket specification allows using of sub-protocols that operate on a higher, application level. One of them, supported by the Spring Framework, is STOMP.

STOMP is a simple text-based messaging protocol that was initially created for scripting languages such as Ruby, Python, and Perl to connect to enterprise message brokers. Thanks to STOMP, clients and brokers developed in different languages can send and receive messages to and from each other. The WebSocket protocol is sometimes called TCP for Web. Analogically, STOMP is called HTTP for Web. It defines a handful of frame types that are mapped onto WebSockets frames, e.g., CONNECT, SUBSCRIBE, UNSUBSCRIBE, ACK, or SEND. On one hand, these commands are very handy to manage communication while, on the other, they allow us to implement solutions with more sophisticated features like message acknowledgment.

## The Server-side: Spring Boot and WebSockets
To build the WebSocket server-side, we will utilize the Spring Boot framework which significantly speeds up the development of standalone and web applications in Java. Spring Boot includes the spring-WebSocket module, which is compatible with the Java WebSocket API standard (JSR-356).

Implementing the WebSocket server-side with Spring Boot is not a very complex task and includes only a couple of steps, which we will walk through one by one.

<h3>Step 1</h3> First, we need to add the WebSocket library dependency.

```
<dependency>
  <groupId>org.springframework.boot</groupId>            
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

If you plan to use JSON format for transmitted messages, you may want to include also the GSON or Jackson dependency. Quite likely, you may additionally need a security framework, for instance, Spring Security.

<h3>Step 2</h3> Then, we can configure Spring to enable WebSocket and STOMP messaging.
```java

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry
   registry) {
    registry.addEndpoint("/mywebsockets")
        .setAllowedOrigins("mydomain.com").withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config){ 
    config.enableSimpleBroker("/topic/", "/queue/");
    config.setApplicationDestinationPrefixes("/app");
  }
}
```

The method configureMessageBroker does two things:

Creates the in-memory message broker with one or more destinations for sending and receiving messages. In the example above, there are two destination prefixes defined: topic and queue. They follow the convention that destinations for messages to be carried on to all subscribed clients via the pub-sub model should be prefixed with topic. On the other hand, destinations for private messages are typically prefixed by queue.
Defines the prefix app that is used to filter destinations handled by methods annotated with @MessageMapping which you will implement in a controller. The controller, after processing the message, will send it to the broker.


Going back to the snippet above—probably you have noticed a call to the method withSockJS()—it enables SockJS fallback options. To keep things short, it will let our WebSockets work even if the WebSocket protocol is not supported by an internet browser. I will discuss this topic in greater detail a bit further.

There is one more thing that needs clarifying—why we call setAllowedOrigins() method on the endpoint. It is often required because the default behavior of WebSocket and SockJS is to accept only same-origin requests. So, if your client and the server-side use different domains, this method needs to be called to allow the communication between them.

<h3>Step 3</h3> Implement a controller that will handle user requests. It will broadcast a received message to all users subscribed to a given topic.

Here is a sample method that sends messages to the destination /topic/news.

```java
@MessageMapping("/news")
@SendTo("/topic/news")
public void broadcastNews(@Payload String message) {
  return message;
}
```

Instead of the annotation @SendTo, you can also use SimpMessagingTemplate which you can autowire inside your controller.

```java
@MessageMapping("/news")
public void broadcastNews(@Payload String message) {
  this.simpMessagingTemplate.convertAndSend("/topic/news", message)
}
```

In later steps, you may want to add some additional classes to secure your endpoints, like ResourceServerConfigurerAdapter or WebSecurityConfigurerAdapter from the Spring Security framework. Also, it is often beneficial to implement the message model so that transmitted JSON can be mapped to objects.

## Building the WebSocket Client
Implementing a client is an even simpler task.

Step 1. Autowire Spring STOMP client.0

```java
@Autowired
private WebSocketStompClient stompClient;
```
Step 2. Open a connection.

```java
StompSessionHandler sessionHandler = new CustmStompSessionHandler();

StompSession stompSession = stompClient.connect(loggerServerQueueUrl, 
sessionHandler).get();
//Once this is done, it is possible to send a message to a destination. The message will be sent to all users subscribed to a topic.

stompSession.send("topic/greetings", "Hello new user");
```

It is also possible to subscribe for messages.

```java
session.subscribe("topic/greetings", this);

@Override
public void handleFrame(StompHeaders headers, Object payload) {
    Message msg = (Message) payload;
    logger.info("Received : " + msg.getText()+ " from : " + 
    msg.getFrom());
}
```

Sometimes it is needed to send a message only to a dedicated user (for instance when implementing a chat). Then, the client and the server-side must use a separate destination dedicated to this private conversation. The name of the destination may be created by appending a unique identifier to a general destination name, e.g., /queue/chat-user123. HTTP Session or STOMP session identifiers can be utilized for this purpose.

Spring makes sending private messages a lot easier. We only need to annotate a Controller’s method with @SendToUser. Then, this destination will be handled by UserDestinationMessageHandler, which relies on a session identifier. On the client-side, when a client subscribes to a destination prefixed with /user, this destination is transformed into a destination unique for this user. On the server-side, a user destination is resolved based on a user’s Principal.

Although we won't use a fancy user-interface here is a sample server-side code with @SendToUser annotation:

```java
@MessageMapping("/greetings")
@SendToUser("/queue/greetings")
public String reply(@Payload String message,
   Principal user) {
 return  "Hello " + message;
}
```

Or you can use SimpMessagingTemplate:
```java
String username = ...
this.simpMessagingTemplate.convertAndSendToUser();
   username, "/queue/greetings", "Hello " + username);
```

Let’s now look at how to implement a JavaScript (SockJS) client capable of receiving private messages which could be sent by the Java code in the example above. It is worth knowing that WebSockets are a part of HTML5 specification and are supported by most modern browsers (Internet Explorer supports them since version 10).

```javascript
function connect() {
 var socket = new SockJS('/greetings');
 stompClient = Stomp.over(socket);
 stompClient.connect({}, function (frame) {
   stompClient.subscribe('/user/queue/greetings', function (greeting) {
     showGreeting(JSON.parse(greeting.body).name);
   });
 });
}

function sendName() {
 stompClient.send("/app/greetings", {}, $("#name").val());
}
```

# User - Spring interaction


# Spring - Kafka interaction

Spring Cloud Stream framework enables application developers to write event-driven applications that use the strong foundations of Spring Boot and Spring Integration. The underpinning of all these is the binder implementation, which is responsible for communication between the application and the message broker. These binders are MessageChannel-based implementations.

Enter Kafka Streams Binder
While the contracts established by Spring Cloud Stream are maintained from a programming model perspective, Kafka Streams binder does not use MessageChannel as the target type. The binder implementation natively interacts with Kafka Streams “types” - KStream or KTable. Applications can directly use the Kafka Streams primitives and leverage Spring Cloud Stream and the Spring ecosystem without any compromise.

Note: The Kafka Streams binder is not a replacement for using the library itself.

Getting Started
A quick way to generate a project with the necessary components for a Spring Cloud Stream Kafka Streams application is through the Spring Initializr - see below.

kafka streams initializr
Simple Example
Here is a simple word-count application written in Spring Cloud Stream and Kafka Streams.

```java
@EnableBinding(KafkaStreamsProcessor.class)
public static class WordCountProcessorApplication {

  @StreamListener("input")
  @SendTo("output")
  public KStream<?, WordCount> process(KStream<Object, String> input) {

     return input
           .flatMapValues(
              value -> Arrays.asList(value.toLowerCase().split("\\W+")))
           .map((key, value) -> new KeyValue<>(value, value))
           .groupByKey()
           .windowedBy(TimeWindows.of(5000)
                   .count(Materialized.as("wordcounts"))
           .toStream()
           .map((key, value) ->
             new KeyValue<>(null, new WordCount(key.key(), value));
  }
}
```
@EnableBinding annotation with KafkaStreamsProcessor convey the framework to perform binding on Kafka Streams targets. You can have your own interfaces with multiple “input” and “output” bindings, too.

@StreamListener instructs the framework to allow the application to consume events as KStream from a topic that is bound on the "input" target.

process() - a handler that receives events from the KStream containing textual data. The business logic counts the number of each word and stores the total count over a time-window (5 seconds in this case) in a state store. The resulting KStream contains the word and its corresponding count in that time window.

Here is a complete version of this example.

Josh Long (@starbuxman) has put together a screencast that goes into much detail about the various features of the Kafka Streams binding support.


<h3>Benefits</h3>
Developers familiar with Spring Cloud Stream (eg: @EnableBinding and @StreamListener), can extend it to building stateful applications by using the Kafka Streams API.

Developers can leverage the framework’s content-type conversion for inbound and outbound conversion or switch to the native SerDe’s provided by Kafka.

Port existing Kafka Streams workloads into a standalone cloud-native application and be able to orchestrate them as coherent data pipelines using Spring Cloud Data Flow.

An application runs as-is - no lock-in with any cloud platform vendor.

<h3>Features</h3>
Interoperability between Kafka Streams and Kafka binder’s MessageChannel bindings

Multiple Kafka Streams types (such as KStream and KTable) as Handler arguments

Content-type conversion for inbound and outbound streams

Property toggles to switch between framework vs. native Kafka SerDe’s for inbound and outbound message conversion

Error handling support

Dead Letter Queue (DLQ) support for records in deserialization error

Branching support

Interactive-query support

Multiple Output Bindings (aka Branching)
Kafka Streams binder lets you send to multiple output topics (Branching API in Kafka Streams).

Here is the outline for such a method.

```java
@StreamListener("input")
@SendTo({"output1","output2","output3"})
public KStream<String, String>[] process(KStream<Object, String> input) {
    //...
}
```
Notice that the return type on the method is KStream[]. See this example for more details on how this works.

Multiple Input Bindings
The Kafka Streams binder also let you bind to multiple inputs of KStream and KTable target types, as the following example shows:
```java
  @StreamListener
  public void process(@Input("input") KStream<String, PlayEvent> playEvents,
                         @Input("inputX") KTable<Long, Song> songTable) {
}
```
Notice the use of multiple inputs on the method argument list. Here you can see two @Input annotations - one for KStream and another for KTable.

Here is a working version of this example.

Framework Content-type vs. Native Kafka SerDe
Similar to MessageChannel based binder implementations, Kafka Streams binder also supports content-type conversion on the incoming and outgoing streams. Any other type of data serialization is entirely handled by Kafka Streams itself. The framework-provided content-type conversion on the edges can be disabled. Instead, you can delegate the responsibilities entirely to Kafka, using the SerDe facilities provided by Kafka Streams.

When relying on the Kafka Streams binder for the content-type conversion, it is applied only for “value” (that is, the payload) in the message. The “keys” are always converted by Kafka SerDe’s.

Please refer to the documentation for detailed information about how content-type negotiation and serialization is addressed in the Kafka Streams binder.

Error Handling
Kafka Streams library has built-in support for handling deserialization exceptions (KIP-161). In addition to native deserialization error-handling support, the Kafka Streams binder also provides support to route errored payloads to a DLQ. See this documentation section for details.

Here is a sample that demonstrates DLQ facilities in the Kafka Streams binder.

<h3>Interactive Query</h3>
Kafka Streams lets you query state stores interactively from the applications, which can be used to gain insights into ongoing streaming data. The Kafka Streams binder API exposes a class called QueryableStoreRegistry. You can access this as a Spring bean in your application by injecting this bean (possibly by autowiring), as the following example shows:

```java
@Autowired
QueryableStoreRegistry queryableStoreRegistry;

ReadOnlyKeyValueStore<Object, Object> keyValueStore =
	queryableStoreRegistry.getQueryableStoreType("my-store",
                         QueryableStoreTypes.keyValueStore());
```

Here are basic and an advanced examples demonstrating the interactive query capabilities through the binder.

Mixing Kafka Streams and MessageChannel based binders
If the application use case requires the usage of both the MessageChannel-based Kafka binder and the Kafka Streams binder, both of them can be used in the same application. In that case, you can have multiple StreamListener methods or a combination of source and sink/processor type methods. The following example of an application shows how multiple StreamListener methods can be used to target various types of bindings:

```java
@StreamListener("binding2")
@SendTo("output")
public KStream<?, WordCount> process(KStream<Object, String> input) {
}

@StreamListener("binding1")
public void sink(String input) {

}

interface MultipleProcessor {

	String BINDING_1 = "binding1";
	String BINDING_2 = "binding2";
	String OUTPUT = "output";

	@Input(BINDING_1)
	SubscribableChannel binding1();

	@Input(BINDING_2)
	KStream<?, ?> binding2();

	@Output(OUTPUT)
	KStream<?, ?> output();
}
```

In this example, the first method is a Kafka Streams processor, and the second method is a regular MessageChannel-based consumer. Although you can have multiple methods with differing target types (MessageChannel vs Kafka Stream type), it is not possible to mix the two within a single method.

# Game client

See the client module for more details on how a User can interact with Spring UI.
A STOMP session with Spring is used for exchanging gameplay related infor (eg. player moves)
and a REST client is used for interacting with Spring services (eg. join a tournament).
There are tests in the client module that concurrently interact with our service in order
to stress test the system.

# Sources

### Docker / Docker Swarm

    https://docs.docker.com/engine/swarm/
    
### Authentication

    https://blog.ngopal.com.np/2017/10/10/spring-boot-with-jwt-authentication-using-redis/

    https://medium.com/@xoor/jwt-authentication-service-44658409e12c
    
### STOMP

    https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#websocket-stomp-clientSTOMPMessage-flow
    
### Kafka

    https://github.com/wurstmeister/kafka-docker
    
### MySQL

    https://github.com/robinong79/docker-swarm-mysql-masterslave-failover
    
### Other

    https://www.markdowntopdf.com/