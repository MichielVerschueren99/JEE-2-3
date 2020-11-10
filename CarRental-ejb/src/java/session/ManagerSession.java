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
        try {
            CarRentalCompany requestedCompany = em.find(CarRentalCompany.class, company);
            return new HashSet<CarType>(requestedCompany.getAllTypes());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            CarRentalCompany requestedCompany = em.find(CarRentalCompany.class, company);
            for(Car c: requestedCompany.getCars(type)){
                out.add(c.getId());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
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
}