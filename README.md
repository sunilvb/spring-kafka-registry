# Spring Boot + Kafka + Schema Registry - Tutorial

## What is Schema Registry?

According to [Confluent.io](https://docs.confluent.io/current/schema-registry/docs/index.html) : Schema Registry is part of the Confluent Open Source and Confluent Enterprise distributions. The Schema Registry stores a versioned history of all schemas and allows for the evolution of schemas according to the configured compatibility settings and expanded Avro support.

## What is Avro?

According to [Avro.Apache.org](https://avro.apache.org/docs/current/) : Apache Avro™ is a data serialization system.

Avro provides:

 - Rich data structures.
 - A compact, fast, binary data format.
 - A container file, to store persistent data.
 - Remote procedure call (RPC).
 - Simple integration with dynamic languages. Code generation is not required to read or write data files nor to use or implement RPC protocols. Code generation as an optional optimization, only worth implementing for statically typed languages.

 ## What will we build in this tutorial

This is a tutorial for creating a simple Spring Boot application with Kafka and Schema Registry.
The following topics are covered in this tutorial:
1. Working with Confluent.io components
2. Creating a Kafka Avro Producer using Spring Boot
3. Creating Avro schema and generating Java classes  
4. A REST interface to send messages to a Kafka topic with Avro schema
5. View the messages from a Kafka Avro Consumer

## Getting Started

![alt text](docs/SchemaRegistry.jpg)

Most applications interact with the Stellar network through Horizon, a RESTful HTTP API server. Horizon gives you a straightforward way to submit transactions, check accounts, and subscribe to events. The Java Stellar Sdk library provides APIs to build transactions and connect to Horizon.

For this tutorial we will be using the test network :  https://horizon-testnet.stellar.org. 
The test network allows us to create new accounts and seed them with 10,000 Lumens(XLM) to play with.

### Download the SDK 

Download the SDK from [Stellar.org](https://github.com/stellar/java-stellar-sdk) to your local file system.

Notice that this a jar file that needs to be imported into your local Maven repo before we can use it in our project as a dependency.


### Import the jar into your local Maven repo

Use the following command to import the SDK jar file into your Maven repo:

```
mvn install:install-file -Dfile=/<path to the sdk jar> -DgroupId=<package name> -DartifactId=<packageId> -Dversion=<version> -Dpackaging=jar

```

For example :

```
mvn install:install-file -Dfile=/Users/sunil_vishnubhotla/Downloads/stellar-sdk.jar -DgroupId=com.stellar.code -DartifactId=stellar -Dversion=0.1.14 -Dpackaging=jar

```


Then add the dependancy in your pom.xml file like so :

```
<dependency>
     <groupId>com.stellar.code</groupId>
     <artifactId>stellar</artifactId>
     <version>0.1.14</version>
</dependency>

```

Note: this dependancy is already added in the source pom.xml.

### Installing and Running

To run the sample install Java 1.8+, Maven,  MySql for your OS and download the code. 
Edit the application.properties file to setup your DB connection.

And simply run this command in the source root


```
mvn springboot:run
```

And point your browser to 

```
http://localhost:8080
```

login screen :

![alt text](docs/login.png)

Asuming your MySql DB is up and running, you should see the login screen. As a first time user, go ahead and click the "Join us" link to create a new user with your email and a password. Use these credentials to login after you finish registering. 

### User Registration

The following method in the is used to accomplish this in LoginController.java :
```
...
@RequestMapping(value = "/registration", method = RequestMethod.POST)
public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) {
	ModelAndView modelAndView = new ModelAndView();
	User userExists = userService.findUserByEmail(user.getEmail());
	if (userExists != null) {
		bindingResult.rejectValue("email", "error.user",
					"There is already a user registered with the email provided");
	}
	if (bindingResult.hasErrors()) {
		System.out.println("There was an error...");
		modelAndView.setViewName("registration");
	} else {
		userService.saveUser(user);
		modelAndView.addObject("successMessage", "User registered successfully. Please login.");
		modelAndView.addObject("user", new User());
		modelAndView.setViewName("login");

	}
	return modelAndView;
}
...
```
### Creating a Stellar account
We will use the following two properties for communicating with the network :
```
@Value("${stellar.network.url}")
private String network;

@Value("${stellar.network.friendbot}")
private String friendbot;
```
![alt text](docs/home.png)

After you login click the "Open a New Account" link to create a new account associated with your User credentials.

![alt text](docs/create.png)

Give your account a nick name and click the Open Account button.

At this point the AccountService is called to create a new account as shown below:

```
...
KeyPair pair = KeyPair.random();
String seed = new String(pair.getSecretSeed());
key = pair.getAccountId();
String friendbotUrl = String.format(friendbot, key);

response = new URL(friendbotUrl).openStream();
String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
System.out.println("New Stellar account created :)\n" + body);

Account acc = new Account(key, seed, name, email);
accountRepository.save(acc);
...
```
We start by calling the Stellar SDK's KeyPair object's random() method that generates and assigns our accout a unique key pair.
Each account has a privete key also called the secret seed and a public key that is assigned when you create a Stellsr account.
As with any blockchain implementation, you need a private key to sign all your transactions to ensure they orignate from you and that  no one else can tamper with it. You do not share your privete key(hence the word private) with anyone but you do send the public key along with the transaction for the system to verify it was you who started the transaction and that you have the needed funds to carry out the transaction.

We then seed this account using the Stellar's Friendbot service to give us 10,000 XLMs.

Note down your account information as it is printed on the console output.

![alt text](docs/debug.png)
You can then see this account information live on  https://stellarchain.io/

![alt text](docs/stellarchain.png)
Make sure you switch the network to TESTNET as shown and then enter the account number. 
Then enter to see 10,000 Lumens and their equivalent values in othe currencies. Know that this is just testing money and not real :-)


### Querying the balance

After we create and seed the account we can query the network to get the upto date account details:



## Built With

* [Stellar SDK](https://github.com/stellar/java-stellar-sdk) - Stellar's Java SDK
* [Spring Boot](https://projects.spring.io/spring-boot/) - The web framework used
* [Spring Security](https://projects.spring.io/spring-security/) - User authentication
* [Spring Data JPA](https://projects.spring.io/spring-data-jpa//) - Data access layer
* [Maven](https://maven.apache.org/) - Dependency Management
* [MySql](https://rometools.github.io/rome/) - RDBMS

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [TBD](http://tbd.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Sunil Vishnubhotla** - *Initial work* - [sunilvb](https://github.com/sunilvb)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone who's code was used
* Inspiration
* etc