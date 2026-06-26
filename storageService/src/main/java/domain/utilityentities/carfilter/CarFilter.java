package domain.utilityentities.carfilter;

import domain.entities.CarModel;
import domain.entities.GearBoxType;
import domain.valueObjects.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class CarFilter {

    // price.value (Price.value -> BigDecimal)
    public static Specification<CarModel> hasPriceLowerBound(Price bound) {
        return (root, query, cb) ->
                bound == null ? null : cb.ge(root.get("price").get("value"), BigDecimal.valueOf(bound.getValue().floatValue()));
    }

    public static Specification<CarModel> hasPriceUpperBound(Price bound) {
        return (root, query, cb) ->
                bound == null ? null : cb.le(root.get("price").get("value"), BigDecimal.valueOf(bound.getValue().floatValue()));
    }

    // brandName.name (embedded)
    public static Specification<CarModel> hasBrandName(BrandName brandName) {
        return (root, query, cb) ->
                brandName == null ? null : cb.equal(cb.lower(root.get("brandName").get("name")), brandName.getName().toLowerCase());
    }

    // modelName.name (embedded)
    public static Specification<CarModel> hasModelName(ModelName modelName) {
        return (root, query, cb) ->
                modelName == null ? null : cb.equal(cb.lower(root.get("modelName").get("name")), modelName.getName().toLowerCase());
    }

    // carCase.name (embedded)
    public static Specification<CarModel> hasCarCase(CarCase carCase) {
        return (root, query, cb) ->
                carCase == null ? null : cb.equal(cb.lower(root.get("carCase").get("name")), carCase.getName().toLowerCase());
    }

    // engine.engineType (enum) - сравниваем с Enum.valueOf, как в твоём фрагменте
    public static Specification<CarModel> hasEngineType(EngineType engineType) {
        return (root, query, cb) ->
                engineType == null ? null : cb.equal(root.get("engine").get("engineType"), engineType);
    }

    // engine.power bounds (число)
    public static Specification<CarModel> hasEnginePowerLowerBound(Float bound) {
        return (root, query, cb) ->
                bound == null ? null : cb.ge(root.get("engine").get("power"), bound);
    }

    public static Specification<CarModel> hasEnginePowerUpperBound(Float bound) {
        return (root, query, cb) ->
                bound == null ? null : cb.le(root.get("engine").get("power"), bound);
    }

    // engine.volume bounds (число)
    public static Specification<CarModel> hasEngineVolumeLowerBound(Float bound) {
        return (root, query, cb) ->
                bound == null ? null : cb.ge(root.get("engine").get("volume"), bound);
    }

    public static Specification<CarModel> hasEngineVolumeUpperBound(Float bound) {
        return (root, query, cb) ->
                bound == null ? null : cb.le(root.get("engine").get("volume"), bound);
    }

    // gearBoxType (enum on CarModel) - compare by Enum.valueOf
    public static Specification<CarModel> hasGearBoxType(GearBoxType gearBoxType) {
        return (root, query, cb) ->
                gearBoxType == null ? null : cb.equal(root.get("gearBoxType"), gearBoxType);
    }

    // wheel drive (enum)
    public static Specification<CarModel> hasWheelDrive(Wheeldrive wheelDrive) {
        return (root, query, cb) ->
                wheelDrive == null ? null : cb.equal(root.get("wheeldrive"), wheelDrive);
    }

    // color.name (embedded)
    public static Specification<CarModel> hasColor(Color color) {
        return (root, query, cb) ->
                color == null ? null : cb.equal(cb.lower(root.get("color").get("name")), color.getName().toLowerCase());
    }
}