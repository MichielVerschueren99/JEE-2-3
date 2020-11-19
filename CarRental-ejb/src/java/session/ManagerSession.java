package session;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.Car;
import rental.CarData;
import rental.CarRentalCompany;
import rental.CarType;
import rental.TypeAlreadyExistsException;
import rental.TypeNotInCrCException;

@Stateless
@RolesAllowed("Manager")
@TransactionAttribute(NOT_SUPPORTED)
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Set<CarType> getCarTypes(String company) { //NG
        List<CarType> result = em.createQuery("SELECT cc.carTypes FROM CarRentalCompany cc "
                                            + "WHERE cc.name = :givenCompany")
                .setParameter("givenCompany", company)
                .getResultList();
        return  new HashSet<>(result);

    }

    @Override
    public Set<Integer> getCarIds(String company, String type) { //NG
        List<Integer> result =
                em.createQuery("SELECT c.id FROM Car c "
                             + "WHERE EXISTS (SELECT cc FROM CarRentalCompany cc WHERE cc.name = :givenCompany AND c IN (cc.cars)) "
                             + "AND c.type.name = :givenType")
                        .setParameter("givenType", type)
                        .setParameter("givenCompany", company)
                        .getResultList();
        return new HashSet<>(result);

    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) { //NG
        Object result = em.createQuery("SELECT COUNT(r) FROM Reservation r "
                                     + "WHERE r.rentalCompany = :givenCompany AND r.carType = :givenType AND r.carId = :givenId")
                .setParameter("givenCompany", company)
                .setParameter("givenType", type)
                .setParameter("givenId", id)
                .getSingleResult();
        return ((Long) result).intValue();
    }


    @Override
    @TransactionAttribute(REQUIRED)
    public void addCompany(String name, List<String> regions, List<CarData> cars) {
        List initialCars = new LinkedList<>();
        for (CarData cd : cars) {
            initialCars.add(new Car(cd.id, cd.type));
        }
        
        CarRentalCompany newCC = new CarRentalCompany(name, regions, initialCars);
        em.persist(newCC);
    }
    
    @Override
    @TransactionAttribute(REQUIRED)
    public void addCarType(CarType ct, String crcName) throws TypeAlreadyExistsException {
        CarRentalCompany crc = em.find(CarRentalCompany.class, crcName);
        crc.addType(ct);
    }
    
    
    @Override
    @TransactionAttribute(REQUIRED)
    public void addCar(int carID, String typeName, String crcName) throws TypeNotInCrCException {
        CarRentalCompany crc = em.find(CarRentalCompany.class, crcName);
        crc.addCar(carID, typeName);
    }

    @Override
    public Set<String> getBestClients() {
        List<String> result = 
                em.createQuery("SELECT DISTINCT r.carRenter FROM Reservation r "
                             + "WHERE NOT EXISTS (SELECT r2 FROM Reservation r2 "
                                               + "WHERE (SELECT COUNT(r3) FROM Reservation r3 "
                                                      + "WHERE r3.carRenter = r2.carRenter) > (SELECT COUNT(r4) FROM Reservation r4 "
                                                                                            + "WHERE r4.carRenter = r.carRenter))")
                .getResultList();
        return new HashSet<>(result);
    }

    @Override
    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year) {
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = new SimpleDateFormat("dd/MM/yyyy").parse("01/01/" + year);
            endDate = new SimpleDateFormat("dd/MM/yyyy").parse("31/12/" + year);
        } catch (ParseException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<CarType> result =
                em.createQuery("SELECT c FROM CarType c "
                             + "WHERE EXISTS (SELECT r.carType from Reservation r "
                                              + "WHERE c.name = r.carType AND r.startDate <= :givenEndDate AND r.endDate >= :givenStartDate AND r.rentalCompany = :givenCompany "
                                              + "AND NOT EXISTS (SELECT r2 FROM Reservation r2 "
                                                              + "WHERE (SELECT COUNT(r3) FROM Reservation r3 "
                                                                     + "WHERE r3.carType = r2.carType AND r3.startDate <= :givenEndDate AND r3.endDate >= :givenStartDate AND r3.rentalCompany = :givenCompany) "
                                                                     + "> (SELECT COUNT(r4) FROM Reservation r4 "
                                                                        + "WHERE r4.carType = r.carType AND r4.startDate <= :givenEndDate AND r4.endDate >= :givenStartDate AND r4.rentalCompany = :givenCompany)))")
                .setParameter("givenEndDate", endDate)
                .setParameter("givenStartDate", startDate)
                .setParameter("givenCompany", carRentalCompanyName )
                .setMaxResults(1)
                .getResultList();
        if (result.isEmpty()) return null;
        else return result.get(0);
    }

    @Override
    public int getNumberOfReservationsBy(String clientName) {
        Object result = em.createQuery("SELECT COUNT(r) FROM Reservation r WHERE r.carRenter = :givenName ")
                .setParameter("givenName", clientName)
                .getSingleResult();
        return ((Long) result).intValue();
    }

    @Override
    public int getNumberOfReservationsByCarType(String carRentalName, String carType) {
        Object result = em.createQuery("SELECT COUNT(r) FROM Reservation r WHERE r.rentalCompany= :givenCompany AND r.carType = :givenType")
                .setParameter("givenCompany", carRentalName)
                .setParameter("givenType", carType)
                .getSingleResult();
        return ((Long) result).intValue();
    }
}