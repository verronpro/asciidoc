package pro.verron.asciidoc.core.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("AsciiDocModel") class AsciiDocModelTest {
    @Nested @DisplayName("getAttribute") class GetAttribute {

        @Test
        @DisplayName(
                "should return empty optional when attribute does not exist")
        void shouldReturnEmptyWhenAttributeNotFound() {
            var model = AsciiDocModel.of();
            assertEquals(Optional.empty(), model.getAttribute("nonexistent"));
        }

        @Test
        @DisplayName("should return attribute value when attribute exists")
        void shouldReturnValueWhenAttributeExists() {
            var model = AsciiDocModel.of(Map.of("attrName", "attrValue"));
            assertEquals(Optional.of("attrValue"),
                    model.getAttribute("attrName"));
        }
    }
}
