package session;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import javax.naming.InitialContext;
import rental.Car;
import rental.CarType;
import rental.Reservation;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type, int carId);
    
    public int getNumberOfReservations(String company, String type);
    
    public void addCompany(String name, List<String> regions, List<Car> cars);
    
    public void removeCompany(String ccName);
    
    public Set<String> getBestClients();

    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year);

    public int getNumberOfReservationsBy(String clientName);

    public int getNumberOfReservationsByCarType( String carRentalName, String carType);
 
}