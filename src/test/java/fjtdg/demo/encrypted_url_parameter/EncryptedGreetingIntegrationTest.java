package fjtdg.demo.encrypted_url_parameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import fjtdg.demo.Greeting;
import fjtdg.demo.Weather;
import fjtdg.demo.encrypted_url_parameter.Encryptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMatching;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.boot.test.json.JacksonTester.initFields;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EncryptedGreetingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Encryptor encryptor;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8800);

    private JacksonTester<Greeting> greetingJson;
    private JacksonTester<Weather> weatherJson;

    @Before
    public void setUp() throws Exception {
        initFields(this, objectMapper);
        wireMockRule.stubFor(
            requestMatching(encryptedQueryParamMatcher("/weather?location=Here"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(weatherJson.write(new Weather("Cloudy")).getJson())
                )
        );
        wireMockRule.stubFor(
            requestMatching(encryptedQueryParamMatcher("/weather?location=There"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(weatherJson.write(new Weather("Sunny")).getJson())
                )
        );
    }

    @Test
    public void shouldDefaultToWorld() throws Exception {
        final String body = restTemplate.getForObject("/encrypted/greeting", String.class);
        assertThat(greetingJson.parseObject(body).getContent()).isEqualTo("Hello, Cloudy World!");
    }

    @Test
    public void shouldIncludeName() throws Exception {
        final String body = restTemplate.getForObject("/encrypted/greeting?name=Test", String.class);
        assertThat(greetingJson.parseObject(body).getContent()).isEqualTo("Hello, Sunny Test!");
    }

    private ValueMatcher<Request> encryptedQueryParamMatcher(final String expectedUrl) {
        return request -> {
            // Get Key
            final String key = request.header("X-EncryptionKey").firstValue();

            // Get encrypted "location" query parameter from url
            final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(request.getUrl());
            final UriComponents uriComponents = uriComponentsBuilder.build();
            final String encryptedLocation = uriComponents.getQueryParams().getFirst("location");

            // Create a new URL with a decrypted location
            final String location = encryptor.decrypt(key, encryptedLocation);
            uriComponentsBuilder.replaceQueryParam("location", location);
            final String url = uriComponentsBuilder.build().toUriString();

            // And match it
            return UrlPattern.fromOneOf(expectedUrl, null, null, null).match(url);
        };
    }
}
