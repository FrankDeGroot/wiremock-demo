package fjtdg.demo.plain;

import fjtdg.demo.Greeting;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class PlainGreetingController {

    private static final String template = "Hello, %s %s!";
    private final AtomicLong counter = new AtomicLong();

    private final PlainWeatherClient plainWeatherClient;

    public PlainGreetingController(PlainWeatherClient plainWeatherClient) {
        this.plainWeatherClient = plainWeatherClient;
    }

    @RequestMapping("/plain/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        final String location = "World".equals(name) ? "Here" : "There";
        return new Greeting(counter.incrementAndGet(),
            String.format(template, plainWeatherClient.getWeather(location), name));
    }
}