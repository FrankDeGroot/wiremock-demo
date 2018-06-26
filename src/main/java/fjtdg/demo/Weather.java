package fjtdg.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class Weather {

    public Weather(final String sky) {
        this.id = 0;
        this.sky = sky;
    }

    private long id;
    private String sky;
}
