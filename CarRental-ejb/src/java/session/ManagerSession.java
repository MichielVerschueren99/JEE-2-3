package session;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        List<CarType> result = em.createQuery("SELECT cc.carTypes FROM CarRentalCompany cc WHERE cc.name=:givenCompany")
                .setParameter("givenCompany", company)
                .getResultList();
        return  new HashSet<>(result);
        //try {
        //    CarRentalCompany requestedCompany = em.find(CarRentalCompany.class, company);
        //    return new HashSet<CarType>(requestedCompany.getAllTypes());
        //} catch (IllegalArgumentException ex) {
        //    Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
        //    return null;
        //}
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        List<Integer> result = em.createQuery("SELECT c.id FROM Car c "
                + "WHERE EXISTS (SELECT cc FROM CarRentalCompany WHERE cc.name = :givenCompany AND c IN(cc.cars)) "
                    + "AND EXISTS (SELECT ct FROM CarType WHERE ct.type = :givenType AND c.type=ct")
                        .setParameter("givenType", type)
                        .setParameter("givencompany", company)
                        .getResultList();
        return new HashSet<>(result);
        //Set<Integer> out = new HashSet<Integer>();
        //try {
        //    CarRentalCompany requestedCompany = em.find(CarRentalCompany.class, company);
        //    for(Car c: requestedCompany.getCars(type)){
        //        out.add(c.getId());
        //   }
        //} catch (IllegalArgumentException ex) {
        //    Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
        //    return null;
        //}
        //return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        try {
            CarRentalCompany requestedCompany = em.find(CarRentalCompany.class, company);
            return requestedCompany.getCar(id).getReservations().size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        Set<Reservation> out = new HashSet<Reservation>();
        try {
            CarRentalCompany requestedCompany = em.find(CarRentalCompany.class, company);
            for(Car c: requestedCompany.getCars(type)){
                out.addAll(c.getReservations());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return out.size();
    }

    public void addCompany(String name, List<String> regions, List<Car> cars) {
        CarRentalCompany newCC = new CarRentalCompany(name, regions, cars);
        em.persist(newCC);
    }
    
    public void removeCompany(String ccName) {
        CarRentalCompany cc = em.find(CarRentalCompany.class, ccName);
        em.remove(cc); 
    }

    @Override
    public Set<String> getBestClients() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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