package fjtdg.demo.response_template;

import fjtdg.demo.Greeting;
import fjtdg.demo.Weather;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class ResponseTemplateGreetingController {

    private static final String template = "Hello, (%d) %s %s!";
    private final AtomicLong counter = new AtomicLong();

    private final ResponseTemplateWeatherClient responseTemplateWeatherClient;

    public ResponseTemplateGreetingController(ResponseTemplateWeatherClient responseTemplateWeatherClient) {
        this.responseTemplateWeatherClient = responseTemplateWeatherClient;
    }

    @RequestMapping("/template/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        final String location = "World".equals(name) ? "Here": "There";
        final long id = counter.incrementAndGet();
        final Weather weather = responseTemplateWeatherClient.getWeather(id, location);
        return new Greeting(id,
            String.format(template, weather.getId(), weather.getSky(), name));
    }
}