package pro.verron.asciidoc.preview;

import java.util.Map;
import java.util.function.Function;

public class Attributes {
    private final Map<String, Object> map;

    public Attributes(Map<String, Object> map) {
        this.map = map;
    }

    public String get(String key, String defaultValue) {
        return (String) map.getOrDefault(key, defaultValue);
    }

    public <T> T get(String key, T defaultValue, Function<String, T> parser) {
        var value = (String) map.getOrDefault(key, defaultValue);
        return parser.apply(value);
    }
}
