package fjtdg.demo.response_template;

import fjtdg.demo.Weather;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ResponseTemplateWeatherClient {

    private final RestTemplate restTemplate;

    public ResponseTemplateWeatherClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Weather getWeather(final long id, final String location) {
        return restTemplate.getForObject("http://localhost:8800/weather?location=" + location + "&id=" + id, Weather.class);
    }
}
