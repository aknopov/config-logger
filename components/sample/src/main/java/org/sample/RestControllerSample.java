package org.sample;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Just version + extra stuff
 */
@RestController
public class RestControllerSample {

    private final String version;
    private final String extra1;
    private final String extra2;

    public RestControllerSample(@Value("${APP_VERSION:UNKNOWN}") String version,
            @Value("${EXTRA:}") String extra1, @Value("EXTRA2") String extra2) {
        this.version = version;
        this.extra1 = extra1;
        this.extra2 = extra2;
    }

    @GetMapping("/version")
    public ResponseEntity<String> getVersion() {
        return ResponseEntity.ok(this.version);
    }


    @GetMapping("/extra")
    public ResponseEntity<String> getExtra1() {
        return ResponseEntity.ok(this.extra1);
    }
}
