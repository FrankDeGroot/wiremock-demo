package fjtdg.demo.encrypted_url_parameter;

import fjtdg.demo.Weather;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EncryptedWeatherClient {

    private final RestTemplate restTemplate;
    private final Encryptor encryptor;

    public EncryptedWeatherClient(RestTemplate restTemplate, Encryptor encryptor) {
        this.restTemplate = restTemplate;
        this.encryptor = encryptor;
    }

    public String getWeather(final String location) {
        final String key = encryptor.key();
        final String encryptedLocation = encryptor.encrypt(key, location);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-EncryptionKey", key);

        final HttpEntity httpEntity = new HttpEntity(null, httpHeaders);

        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl("http://localhost:8800/weather");
        uriComponentsBuilder.queryParam("location", encryptedLocation);

        return restTemplate.exchange(uriComponentsBuilder.build(true).toUriString(),
            HttpMethod.GET,
            httpEntity,
            Weather.class,
            encryptedLocation).getBody().getSky();
    }
}
