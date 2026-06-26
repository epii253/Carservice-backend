package application.contracts.ports;

import application.contracts.dataobjects.cars.*;

public interface ICarService {
    GetCertainCar.Response GetCertainCar(GetCertainCar.Request request);

    NewCar.Response AddNewCarModel(NewCar.Request request);

    GetAllCars.Response GetAllCars(GetAllCars.Request request);

    GetCarsBy.Response GetWithFilter(GetCarsBy.Request request);

    GetCarById.Response GetCarById(GetCarById.Request request);
    CheckPossibilityToCreateAnOrder.Response CheckPossibilityToCreateAnOrder(CheckPossibilityToCreateAnOrder.Request request);

    CheckAvailableForTestDriven.Response CheckAvailableForTestDriven(CheckAvailableForTestDriven.Request request);

}