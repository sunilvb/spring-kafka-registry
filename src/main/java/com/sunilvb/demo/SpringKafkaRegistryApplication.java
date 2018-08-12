package com.sunilvb.demo;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.confluent.kafka.serializers.KafkaAvroSerializer;

@SpringBootApplication
@RestController
public class SpringKafkaRegistryApplication {

	final static Logger logger = Logger.getLogger(SpringKafkaRegistryApplication.class);
	@Value("${bootstrap.url}")
	String bootstrap;
	@Value("${registry.url}")
	String registry;
	
	public static void main(String[] args) {
		SpringApplication.run(SpringKafkaRegistryApplication.class, args);
			
	}
	
	@RequestMapping("/orders")
	public String doIt(@RequestParam(value="name", defaultValue="Order-avro") String name)
	{
		
		String ret=name;
		try
		{
			ret += "<br>Using Bootstrap : " + bootstrap;
			ret += "<br>Using Bootstrap : " + registry;
			
			Properties properties = new Properties();
			// Kafka Properties
			properties.setProperty("bootstrap.servers", bootstrap);
			properties.setProperty("acks", "all");
			properties.setProperty("retries", "10");
			// Avro properties
			properties.setProperty("key.serializer", StringSerializer.class.getName());
			properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());
			properties.setProperty("schema.registry.url", registry);
			
			ret += sendMsg(properties, name);
		}
		catch(Exception ex){ ret+="<br>"+ex.getMessage();}
		
		return ret;
	}
	
	private Order sendMsg(Properties properties, String topic)
	{
		Producer<String, Order> producer = new KafkaProducer<String, Order>(properties);

        Order order = Order.newBuilder()
        		.setOrderId("OId234")
        		.setCustomerId("CId432")
        		.setSupplierId("SId543")
                .setItems(4)
                .setFirstName("Sunil")
                .setLastName("V")
                .setPrice(178f)
                .setWeight(75f)
                .build();

        ProducerRecord<String, Order> producerRecord = new ProducerRecord<String, Order>(topic, order);

        
        producer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception == null) {
                    logger.info(metadata); 
                } else {
                	logger.error(exception.getMessage());
                }
            }
        });

        producer.flush();
        producer.close();
        
        return order;
	}
	
	
}
