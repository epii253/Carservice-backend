package domain.utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.valueObjects.ModelName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;


@Converter
public class ModelNameListConverter implements AttributeConverter<List<ModelName>, Object> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ModelName> attribute) {
        if (attribute == null) return null;
        try {
            return mapper.writeValueAsString(
                    attribute.stream().map(ModelName::getName).toList()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ModelName> convertToEntityAttribute(Object dbData) {
        try {
            String json = dbData.toString();

            List<String> list = mapper.readValue(json, new TypeReference<>() {});
            return list.stream().map(ModelName::new).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}