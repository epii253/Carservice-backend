package application.contracts.ports;

import application.contracts.dataobjects.carparts.*;
import application.services.exceptions.UnauthorizedException;
public interface ICarPartService {
    GetAllPartsResponse GetAllParts(GetAllPartsRequest request);

    AddCarPartResponse AddRuderPart(AddRudderRequest request)
            throws UnauthorizedException;

    AddCarPartResponse AddWheelPart(
            AddWheelRequest request
    );

    AddCarPartResponse AddTransmissionPart(
            AddTransmissionRequest request
    );

    AddCarPartResponse AddInteriorPart(
            AddInteriorRequest request
    );

    AddCarPartResponse AddEnginePart(
            AddEngineRequest request
    );
}
