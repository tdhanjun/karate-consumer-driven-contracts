package payment.producer.mock;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import payment.producer.QueueConsumer;
import payment.producer.QueueUtils;

import static org.junit.jupiter.api.Assertions.*;

public class QueueUtilsTest {
    static final Logger logger = LoggerFactory.getLogger(QueueUtilsTest.class);

    boolean passed = false;

    @Test
    void testQueueOperations() throws Exception {
        String queueName = "DEMO.TEST";
        QueueUtils.send(queueName, "first", 0);
        QueueConsumer consumer = new QueueConsumer(queueName);
        String text = consumer.waitForNextMessage();
        assertEquals("first", text);
        QueueUtils.send(queueName, "second", 0);
        QueueUtils.send(queueName, "third", 0);
        consumer.purgeMessages();
        QueueUtils.send(queueName, "foo", 25);
        consumer.setMessageListener(m -> {
            TextMessage tm = (TextMessage) m;
            try {
                logger.info("*** received message: {}", tm.getText());
                assertEquals("foo", tm.getText());
                passed = true;
                synchronized (consumer) {
                    consumer.notify();
                }
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });
        synchronized (consumer) {
            consumer.wait(10000);
        }
        assertTrue(passed);
    }

}
