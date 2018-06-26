package fjtdg.demo.plain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fjtdg.demo.Greeting;
import fjtdg.demo.Weather;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.boot.test.json.JacksonTester.initFields;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScenarioGreetingIntegrationTest {

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
                .inScenario("Changing Weather")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(weatherJson.write(new Weather("Cloudy")).getJson())
                )
                .willSetStateTo("Summer"));
        wireMockRule.stubFor(
            get(urlPathEqualTo("/weather"))
                .withQueryParam("location", equalTo("Here"))
                .inScenario("Changing Weather")
                .whenScenarioStateIs("Summer")
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(weatherJson.write(new Weather("Rainy")).getJson())
                )
                .willSetStateTo("Autumn"));
        wireMockRule.stubFor(
            get(urlPathEqualTo("/weather"))
                .withQueryParam("location", equalTo("Here"))
                .inScenario("Changing Weather")
                .whenScenarioStateIs("Autumn")
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(weatherJson.write(new Weather("Rainier")).getJson())
                )
                .willSetStateTo("Winter"));
        wireMockRule.stubFor(
            get(urlPathEqualTo("/weather"))
                .withQueryParam("location", equalTo("Here"))
                .inScenario("Changing Weather")
                .whenScenarioStateIs("Winter")
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(weatherJson.write(new Weather("Wet Snow")).getJson())
                )
                .willSetStateTo("STARTED")
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
        assertThat(getWorldGreeting()).isEqualTo("Hello, Cloudy World!");
        assertThat(getWorldGreeting()).isEqualTo("Hello, Rainy World!");
        assertThat(getWorldGreeting()).isEqualTo("Hello, Rainier World!");
        assertThat(getWorldGreeting()).isEqualTo("Hello, Wet Snow World!");
    }

    private String getWorldGreeting() throws Exception {
        final String body = restTemplate.getForObject("/plain/greeting", String.class);
        return greetingJson.parseObject(body).getContent();
    }

    @Test
    public void shouldIncludeName() throws Exception {
        final String body = restTemplate.getForObject("/plain/greeting?name=Test", String.class);
        assertThat(greetingJson.parseObject(body).getContent()).isEqualTo("Hello, Sunny Test!");
    }
}
