package payment.consumer;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.core.MockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import payment.producer.PaymentService;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Consumer_ContractTest {

    private static MockServer server;

    @BeforeAll
    static void beforeAll() {
//        context = PaymentService.start(0);
        server = MockServer.feature("classpath:payment/consumer/payment-mock.feature").http(0).build();
    }

    @Test
    void testPaymentService() {
        String paymentServiceUrl = "http://localhost:" + server.getPort();
        Results results = Runner.path("classpath:payment/consumer/consumer.feature")
                .configDir("classpath:payment/consumer/payment-mock.feature")
                .systemProperty("payment.service.url", paymentServiceUrl)
//                .systemProperty("shipping.queue.name", queueName)
                .parallel(1);
        assertTrue(results.getFailCount() == 0, results.getErrorMessages());
    }
}
