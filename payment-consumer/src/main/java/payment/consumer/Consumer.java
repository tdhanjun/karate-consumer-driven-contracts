package payment.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import payment.producer.Payment;
import payment.producer.QueueConsumer;
import com.intuit.karate.JsonUtils;
import javax.jms.TextMessage;

/**
 *
 * @author pthomas3
 */
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private final String paymentServiceUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    private final QueueConsumer queueConsumer;

    public Consumer(String paymentServiceUrl) {
        this(paymentServiceUrl,"test");
    }

    public Consumer(String paymentServiceUrl, String queueName) {
        this.paymentServiceUrl = paymentServiceUrl;
        queueConsumer = new QueueConsumer(queueName);
    }

    private HttpURLConnection getConnection(String path) throws Exception {
        URL url = new URL(paymentServiceUrl + path);
        return (HttpURLConnection) url.openConnection();
    }

    public Payment create(Payment payment) {
        try {
            HttpURLConnection con = getConnection("/payments");
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            String json = mapper.writeValueAsString(payment);
            IOUtils.write(json, con.getOutputStream(), "utf-8");
            int status = con.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("status code was " + status);
            }
            String content = IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
            return mapper.readValue(content, Payment.class);
//            return JsonUtils.fromJson(content, Payment.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void listen(java.util.function.Consumer<String> handler) {
        queueConsumer.setMessageListener(message -> {
            try {
                TextMessage tm = (TextMessage) message;
                String json = tm.getText();
                logger.info("*** received message: {}", json);
                handler.accept(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void stopQueueConsumer() {
        queueConsumer.setMessageListener(null);
        queueConsumer.stop();
    }



}
