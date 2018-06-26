package fjtdg.demo.plain;

import fjtdg.demo.Weather;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PlainWeatherClient {

    private final RestTemplate restTemplate;

    public PlainWeatherClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getWeather(final String location) {
        return restTemplate.getForObject("http://localhost:8800/weather?location=" + location, Weather.class).getSky();
    }
}
