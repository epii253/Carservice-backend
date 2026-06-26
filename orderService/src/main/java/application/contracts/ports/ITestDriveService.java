package application.contracts.ports;

import application.contracts.dataobjects.testdrive.AddTestsDriveCar;
import application.contracts.dataobjects.testdrive.CreateTestDrive;
import application.contracts.dataobjects.testdrive.GetAllTickets;
import application.services.exceptions.NotFoundException;
import application.services.exceptions.UnauthorizedException;

public interface ITestDriveService {
    CreateTestDrive.Response TestDriveRequest(CreateTestDrive.Request request)
            throws UnauthorizedException, NotFoundException;

    GetAllTickets.Response GetAllTestsDriveTickets(GetAllTickets.Request request)
            throws UnauthorizedException;

    AddTestsDriveCar.Response AddToTestDrive(AddTestsDriveCar.Request request) throws Exception;
}
