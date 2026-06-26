package application.contracts.mappers.testdrive;

import application.contracts.dataobjects.testdrive.AddTestsDriveCar;
import application.contracts.dataobjects.testdrive.TestDriveCarInfo;
import application.repositories.rows.TestDriveCar;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ITestDriveCarMapper {

    default AddTestsDriveCar.Response toAddTestsDriveCarResponse(TestDriveCar testDriveCar) {
        return new AddTestsDriveCar.Response(toInfo(testDriveCar));
    }
    @Mapping(source = "carModel", target = "modelId")
    @Mapping(source = "id", target = "id")
    TestDriveCarInfo toInfo(TestDriveCar testDriveCar);
}
