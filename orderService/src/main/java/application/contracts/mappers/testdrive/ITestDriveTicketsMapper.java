package application.contracts.mappers.testdrive;

import application.contracts.dataobjects.testdrive.CreateTestDrive;
import application.contracts.dataobjects.testdrive.GetAllTickets;
import application.contracts.dataobjects.testdrive.TestDriveTicketInfo;
import domain.entities.TestDriveTicket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ITestDriveTicketsMapper {

    default CreateTestDrive.Response toCreateTestDriveResponse(TestDriveTicket ticket) {
        return new CreateTestDrive.Response(toInfo(ticket));
    }

    default GetAllTickets.Response toGetAllTicketsResponse(List<TestDriveTicket> ticket) {
        return new GetAllTickets.Response(toTestDriveTicketInfoList(ticket));
    }

    List<TestDriveTicketInfo> toTestDriveTicketInfoList(List<TestDriveTicket> tickets);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "testDriveCar.id", target = "testsDriveModelId")
    @Mapping(source = "testDate", target = "testDate")
    @Mapping(source = "testDriveCar.carModel", target = "modelId")
    TestDriveTicketInfo toInfo(TestDriveTicket ticket);
}
