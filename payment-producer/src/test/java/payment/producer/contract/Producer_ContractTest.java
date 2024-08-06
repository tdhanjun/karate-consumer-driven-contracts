package payment.producer.contract;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import payment.producer.PaymentService;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author pthomas3
 */
class Producer_ContractTest {
    static ConfigurableApplicationContext context;

    @BeforeAll
    static void beforeAll() {
        context = PaymentService.start(0);
    }

    @Test
    void testReal() {
        String paymentServiceUrl = "http://localhost:" + PaymentService.getPort(context);
        Results results = Runner.path("classpath:payment/producer/contract/producer.feature")
                .systemProperty("payment.service.url", paymentServiceUrl)
                .parallel(1);
        assertTrue(results.getFailCount() == 0, results.getErrorMessages());
    }

    @AfterAll
    static void afterAll() {
        PaymentService.stop(context);
    }

}
