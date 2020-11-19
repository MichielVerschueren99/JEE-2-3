package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarData;
import rental.CarType;
import rental.TypeAlreadyExistsException;
import rental.TypeNotInCrCException;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type, int carId);
    
    public void addCompany(String name, List<String> regions, List<CarData> cars);
    
    public Set<String> getBestClients();

    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year);

    public int getNumberOfReservationsBy(String clientName);

    public int getNumberOfReservationsByCarType( String carRentalName, String carType);

    public void addCar(int id, String typeName, String crcName) throws TypeNotInCrCException;

    public void addCarType(CarType ct, String crcName) throws TypeAlreadyExistsException;
 
}