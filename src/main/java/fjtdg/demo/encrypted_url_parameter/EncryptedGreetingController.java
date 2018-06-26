package fjtdg.demo.encrypted_url_parameter;

import java.util.concurrent.atomic.AtomicLong;

import fjtdg.demo.Greeting;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EncryptedGreetingController {

    private static final String template = "Hello, %s %s!";
    private final AtomicLong counter = new AtomicLong();

    private final EncryptedWeatherClient encryptedWeatherClient;

    public EncryptedGreetingController(EncryptedWeatherClient encryptedWeatherClient) {
        this.encryptedWeatherClient = encryptedWeatherClient;
    }

    @RequestMapping("/encrypted/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        final String location = "World".equals(name) ? "Here": "There";
        return new Greeting(counter.incrementAndGet(),
            String.format(template, encryptedWeatherClient.getWeather(location), name));
    }
}