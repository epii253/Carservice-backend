package domain.untilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entities.Pair;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Converter
public class PartsListConverter implements AttributeConverter<List<Pair<String, UUID>>, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Pair<String, UUID>> attribute) {
        if (attribute == null || attribute.isEmpty())
            return "[]";

        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при записи List<Pair> в JSON", e);
        }
    }

    @Override
    public List<Pair<String, UUID>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.toString().isBlank())
            return new ArrayList<>();

        try {
            String json = dbData.toString();

            return mapper.readValue(json, new TypeReference<List<Pair<String, UUID>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении JSON в List<Pair>. Данные в БД: " + dbData, e);
        }
    }
}
