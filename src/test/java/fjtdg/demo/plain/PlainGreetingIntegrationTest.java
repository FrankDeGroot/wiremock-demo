package fjtdg.demo.plain;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.boot.test.json.JacksonTester.initFields;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlainGreetingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8800);

    private JacksonTester<Greeting> greetingJson;
    private JacksonTester<Weather> weatherJson;

    @Before
    public void setUp() throws Exception {
        initFields(this, objectMapper);
        wireMockRule.stubFor(
            get(urlPathEqualTo("/weather"))
                .withQueryParam("location", equalTo("Here"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(weatherJson.write(new Weather("Cloudy")).getJson())
                )
        );
        wireMockRule.stubFor(
            get(urlEqualTo("/weather?location=There"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(weatherJson.write(new Weather("Sunny")).getJson())
                )
        );
    }

    @Test
    public void shouldDefaultToWorld() throws Exception {
        final String body = restTemplate.getForObject("/plain/greeting", String.class);
        assertThat(greetingJson.parseObject(body).getContent()).isEqualTo("Hello, Cloudy World!");
    }

    @Test
    public void shouldIncludeName() throws Exception {
        final String body = restTemplate.getForObject("/plain/greeting?name=Test", String.class);
        assertThat(greetingJson.parseObject(body).getContent()).isEqualTo("Hello, Sunny Test!");
    }
}
