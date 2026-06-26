package presentation.mappers;

import domain.valueObjects.*;
import org.springframework.stereotype.Component;

@Component
public class VoMapper {
    public ModelName toModelName(String name) {
        return name == null ? null : new ModelName(name);
    }
    public CarCase toCarCase(String name) {
        return name == null ? null : new CarCase(name);
    }
    public BrandName toBrandName(String name) {
        return name == null ? null : new BrandName(name);
    }
    public Color toColor(String name) {
        return name == null ? null : new Color(name);
    }
    public Price toPrice(Float value) {
        return value == null ? null : new Price(value);
    }
}
