# Spring Boot + Kafka + Schema Registry - Tutorial

## What is Schema Registry?

According to [Confluent.io](https://docs.confluent.io/current/schema-registry/docs/index.html) : The Schema Registry stores a versioned history of all schemas and allows for the evolution of schemas according to the configured compatibility settings and expanded Avro support.

## Why do we need a Schema Registry?

Simply put, we want to avoid garbage-in-garbage-out scenarios. Schema Registry enables message producers to comply to a JSON schema and avoid producers from pushing message that are bad in to topics. This saves a lot of headache for down-stream consumer. Schema Registry is a critical component in enforcing data governance in a messaging platform. 

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

In our sample application we will build a Spring Boot microservice that produces messages and uses Avro to serialize messages and push them into Kafka.
For this tutorial we will be using the open source components of confluent platform. All of our microservices and infrastructure components will be dockerized and run using docker-compose.  


### Get the code and tools

Download and install Maven from https://maven.apache.org/download.cgi
Download and install JDK 1.8 from http://www.oracle.com/technetwork/java/javase/downloads/index.html

Clone this repo to your machine and change directory to spring-kafka-registry. Build the docker image referenced in the compose file

```
git clone https://github.com/sunilvb/spring-kafka-registry.git

cd spring-kafka-registry

mvn clean package

```

Let's look at the source code. 
Open the order.avsc file from src\main\resources\avro

```
{
     "type": "record",
     "namespace": "com.sunilvb.demo",
     "name": "Order",
     "version": "1",
     "fields": [
       { "name": "order_id", "type": "string", "doc": "Id of the order filed" },
       { "name": "customer_id", "type": "string", "doc": "Id of the customer" },
       { "name": "supplier_id", "type": "string", "doc": "Id of the supplier" },
       { "name": "first_name", "type": "string", "doc": "First Name of Customer" },
       { "name": "last_name", "type": "string", "doc": "Last Name of Customer" },
       { "name": "items", "type": "int", "doc": "Totla number of items in the order" },
       { "name": "price", "type": "float", "doc": "Total price of the order" },
       { "name": "weight", "type": "float", "doc": "Weight of the items" },
       { "name": "automated_email", "type": "boolean", "default": true, "doc": "Field indicating if the user is enrolled in marketing emails" }
     ]
}
```
This is a simple Avro Schema file that describes the Order message structure.
Following are the two types of data supported in Avro:

Primitive type: Primitive type are used to define the data types of fields in our message schema. All premetive types are supported in Avro. In our Order example, we are using string, int, float in the Order message schema.
Complex type: We could also use these six complex data types supported in Avro to define our schema: records, enums, arrays, maps, unions and fixed. In our Order example, we are using the 'record' complex type to define order message.

Once we define the schema, we then generate the Java source code from the schema using the maven plugin:
```
<plugin>
				<groupId>org.apache.avro</groupId>
				<artifactId>avro-maven-plugin</artifactId>
				<version>${avro.version}</version>
				<executions>
					<execution>
						<phase>generate-sources</phase>
						<goals>
							<goal>schema</goal>
						</goals>
						<configuration>
							<sourceDirectory>${project.basedir}/src/main/resources/avro/</sourceDirectory>
							<outputDirectory>${project.build.directory}/generated/avro</outputDirectory>
						</configuration>
					</execution>
				</executions>
			</plugin>
			
```
The following command in maven lifecycle phase will do the trick and put the generated classes in : spring-kafka-registry\target\generated\avro\

```
mvn generate-sources
```

We can then compile and build the jar file and create a docker container as below:

```
mvn package

mv target/*.jar app.jar
 
docker build -t spring-kafka-registry .
```
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

* [Spring Boot](https://projects.spring.io/spring-boot/) - The web framework used
* [Apache Kafka](https://maven.apache.org/) - Dependency Management
* [Confluent Schema Registry](https://maven.apache.org/) - Dependency Management
* [Maven](https://maven.apache.org/) - Dependency Management

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
