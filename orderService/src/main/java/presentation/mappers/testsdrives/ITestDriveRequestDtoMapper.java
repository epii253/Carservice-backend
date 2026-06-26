package presentation.mappers.testsdrives;

import application.contracts.dataobjects.testdrive.CreateTestDrive;
import org.mapstruct.Mapper;
import presentation.entryobjects.testsdrive.TestDriveRequestDto;
import presentation.mappers.VoMapper;

@Mapper(
        componentModel = "spring",
        uses = {VoMapper.class}
)
public interface ITestDriveRequestDtoMapper {
    CreateTestDrive.Request toRequest(TestDriveRequestDto dto);
}
