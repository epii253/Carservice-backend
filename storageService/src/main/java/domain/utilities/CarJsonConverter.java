package domain.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entities.Car;
import domain.entities.Pair;
import domain.valueObjects.ModelName;
import domain.valueObjects.PartType;
import domain.valueObjects.Price;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Converter
public class CarJsonConverter implements AttributeConverter<Car, String> {

    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public String convertToDatabaseColumn(Car car) {
        if (car == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(car);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при записи Car в JSON", e);
        }
    }

    @Override
    public Car convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return null;
        }
        try {
            return mapper.readValue(dbData, Car.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при чтении Car из JSON", e);
        }
    }
}