package ly.stealth.kafkahttp;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import com.codahale.metrics.health.HealthCheck;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.eclipse.jetty.servlets.CrossOriginFilter;

public class KafkaApplication extends Application<KafkaConfiguration> implements Managed {
    public static void main(String[] args) throws Exception {
        new KafkaApplication().run(args);
    }

    private Producer<String, String> producer;

    @Override
    public String getName() {
        return "kafka-http";
    }

    @Override
    public void initialize(Bootstrap<KafkaConfiguration> bootstrap) {

    }

    @Override
    public void run(KafkaConfiguration configuration, Environment environment) {
        ProducerConfig config = new ProducerConfig(configuration.producer.asProperties());
        producer = new Producer<String, String>(config);

        // Open to Cross-Domain requests
        environment.servlets().addFilter("/*", CrossOriginFilter.class);

        environment.lifecycle().manage(this);
        environment.jersey().register(new MessageResource(producer, configuration.consumer));
        environment.healthChecks().register("empty", new EmptyHealthCheck());
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        producer.close();
    }

    private static class EmptyHealthCheck extends HealthCheck {
        public EmptyHealthCheck() {
            //super("empty");
        }

        @Override
        protected Result check() throws Exception {
            return Result.healthy();
        }
    }
}
