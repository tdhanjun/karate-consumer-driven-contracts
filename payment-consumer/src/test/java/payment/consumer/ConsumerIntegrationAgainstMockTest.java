package payment.consumer;

import com.intuit.karate.core.MockServer;
import java.io.File;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import payment.producer.Payment;
import payment.producer.Shipment;
import com.intuit.karate.JsonUtils;

/**
 *
 * @author pthomas3
 */
class ConsumerIntegrationAgainstMockTest {

    static MockServer server;
    static Consumer consumer;

    @BeforeAll
    static void beforeAll() {
        String queueName = "test";
        File file = new File("../contract-broker/payment-service-mock.feature");
//        File file = new File("../payment-producer/src/test/java/payment/producer/mock/payment-mock.feature");
        server = MockServer.feature(file)
                .arg("queueName", queueName)
                .http(0).build();
        String paymentServiceUrl = "http://localhost:" + server.getPort();
        consumer = new Consumer(paymentServiceUrl, queueName);
//        consumer = new Consumer(paymentServiceUrl);
    }

    @Test
    void testPaymentCreate() throws Exception {
        Payment payment = new Payment();
        payment.setAmount(5.67);
        payment.setDescription("test one");
        payment = consumer.create(payment);
        assertTrue(payment.getId() > 0);
        assertEquals(payment.getAmount(), 5.67, 0);
        assertEquals(payment.getDescription(), "test one");
        Payment finalPayment = payment;
        consumer.listen(json -> {
            Shipment shipment = JsonUtils.fromJson(json, Shipment.class);
            assertEquals(finalPayment.getId(), shipment.getPaymentId());
            assertEquals("shipped", shipment.getStatus());
            synchronized(this) {
                notify();
            }
        });
        synchronized(this) {
            wait(10000);
        }
    }

    @AfterAll
    static void afterAll() {
        server.stop();
        consumer.stopQueueConsumer();
    }

}
