package presentation.mappers.testsdrives;

import application.contracts.dataobjects.testdrive.AddTestsDriveCar;
import org.mapstruct.Mapper;
import presentation.entryobjects.testsdrive.AddCarForTestDrivesDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface IAddCarForTestDriveMapper {
    AddTestsDriveCar.Request toRequest(AddCarForTestDrivesDto dto);

}
