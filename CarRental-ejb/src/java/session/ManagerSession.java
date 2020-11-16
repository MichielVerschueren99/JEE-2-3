package session;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.Lob;
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
        Object result = em.createQuery("SELECT COUNT(r) FROM Reservation r WHERE r.rentalCompany= :givenCompany AND r.carType = :givenType AND r.carId=:givenId")
                .setParameter("givenCompany", company)
                .setParameter("givenType", type)
                .setParameter("givenId", id)
                .getSingleResult();
        return ((Long) result).intValue();
    }
        //try {
        //    CarRentalCompany requestedCompany = em.find(CarRentalCompany.class, company);
        //    return requestedCompany.getCar(id).getReservations().size();
        //} catch (IllegalArgumentException ex) {
        //    Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
        //    return 0;
        //}
    //}

    @Override
    public int getNumberOfReservations(String company, String type) {   
        Object result = em.createQuery("SELECT COUNT(r) FROM Reservation r WHERE r.rentalCompany= :givenCompany AND r.carType = :givenType")
                .setParameter("givenCompany", company)
                .setParameter("givenType", type)
                .getSingleResult();
        return ((Long) result).intValue();
    }
        //Set<Reservation> out = new HashSet<Reservation>();
        //try {
        //    CarRentalCompany requestedCompany = em.find(CarRentalCompany.class, company);
        //    for(Car c: requestedCompany.getCars(type)){
        //        out.addAll(c.getReservations());
        //    }
        //} catch (IllegalArgumentException ex) {
        //    Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
        //    return 0;
       // }
        //return out.size();
    //}

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
        List<String> result = 
                em.createQuery("SELECT r.carRenter FROM Reservation r "
                             + "WHERE NOT EXISTS (SELECT r2.carRenter FROM Reservation r2 "
                                               + "WHERE (SELECT COUNT(r3) FROM Reservation r3 "
                                                      + "WHERE r3.carRenter = r2.carRenter) > (SELECT COUNT(r4) FROM Reservation r4 "
                                                                                            + "WHERE r4.carRenter = r.carRenter))")
                .getResultList();
        return new HashSet<>(result);
    }
    
    
//    @Override
//    public Set<String> getBestClients() {
//        List<Long> counts = em.createQuery("SELECT COUNT(r2) FROM Reservation r2 GROUP BY r2.carRenter").getResultList();
//        Long max = Collections.max(counts);
//        //ik vind geen manier om dit als subquery te doen. de counts query mag ni gedefinieerd worden in FROM en lezen van een lijst is ook vaag
//        System.out.println(counts);
//        List<String> result = em.createQuery("SELECT r.carRenter FROM Reservation r GROUP BY r.carRenter "
//                + "HAVING COUNT(r.carRenter) = :givenCounts")
//               .setParameter("givenCounts", max)
//                .getResultList();
//        System.out.println(result);        
//        return new HashSet<>(result);
//    }

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

        List<Long> counts = em.createQuery("SELECT COUNT(r2) FROM Reservation r2 WHERE r2.startDate <= :givenEndDate AND r2.endDate >= :givenStartDate "
                                                + "AND r2.rentalCompany = :givenCompany  "
                                                + "GROUP BY r2.carType")
                                .setParameter("givenEndDate", endDate)
                                .setParameter("givenStartDate", startDate)
                                .setParameter("givenCompany", carRentalCompanyName )
                                .getResultList();
        Long max = Collections.max(counts);
        List<String> types = em.createQuery("SELECT r.carType FROM Reservation r "
                                            + "WHERE r.startDate <= :givenEndDate AND r.endDate >= :givenStartDate AND r.rentalCompany = :givenCompany "
                                            + "GROUP BY r.carType HAVING COUNT(r.carType)= :givenCounts")
                                                .setParameter("givenEndDate", endDate)
                                                .setParameter("givenStartDate", startDate)
                                                .setParameter("givenCompany", carRentalCompanyName)
                                                .setParameter("givenCounts", max)
                                                .getResultList();
        System.out.println(types);
        String type = types.get(0);
        List<CarType> result = em.createQuery("SELECT ct FROM CarType ct "
                                + "WHERE EXISTS (SELECT cc FROM CarRentalCompany cc WHERE ct IN(cc.carTypes) AND cc.name = :givenCompany) "
                                + "AND ct.name = :givenType")
                .setParameter("givenType", type)    
                .setParameter("givenCompany", carRentalCompanyName )
                .getResultList();
        System.out.println(result);
        return result.get(0);
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