package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
@TransactionAttribute(NOT_SUPPORTED)
public class ReservationSession implements ReservationSessionRemote {
    
    @Resource
    javax.ejb.SessionContext ctx;
    
    @PersistenceContext
    private EntityManager em;

    private String renter;
    private List<Quote> quotes = new LinkedList<>();

    @Override
    public Set<String> getAllRentalCompanies() { //NG
        List<String> companies = em.createQuery("SELECT cc.name FROM CarRentalCompany cc").getResultList();
        return new HashSet<>(companies);
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) { //NG
        List<CarType> result = em.createQuery(
                "SELECT ct FROM CarType ct "
              + "WHERE EXISTS (SELECT c FROM Car c "
                           +  "WHERE c.type = ct AND NOT EXISTS (SELECT r FROM c.reservations r " 
                                                              + "WHERE r.startDate < :givenEndDate AND r.endDate > :givenStartDate))")
                .setParameter("givenEndDate", end)
                .setParameter("givenStartDate", start)
                .getResultList();
        return result;
    }
    
    @Override
    public void createQuote(String name, ReservationConstraints constraints) throws ReservationException {
        List<CarRentalCompany> allCCs = em.createQuery("SELECT cc FROM CarRentalCompany cc").getResultList();
	for (CarRentalCompany cc : allCCs) {
            try {
		Quote out = cc.createQuote(constraints, name);
                quotes.add(out);
                return;
            } catch (Exception e) {
		//deze cc heeft geen auto voor gevraagde constraints => volgende cc proberen
            }
        }
        throw new ReservationException("no quote found");
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    @TransactionAttribute(REQUIRED)
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<>();
        try {
            for (Quote quote : quotes) {
                CarRentalCompany companyOfQuote = em.find(CarRentalCompany.class, quote.getRentalCompany());
                done.add(companyOfQuote.confirmQuote(quote));
            }
        } catch (Exception e) {
            ctx.setRollbackOnly();
            throw new ReservationException(e);
        }
        this.quotes= new LinkedList();
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }

    @Override
    public String getRenterName() {
        return renter;
    }

    @Override
    public String getCheapestCarType(Date start, Date end, String region) throws Exception {
         Object carType = em.createQuery("SELECT ct.name FROM CarType ct "
                 + "WHERE ct.rentalPricePerDay = (SELECT MIN(ct.rentalPricePerDay) FROM CarType ct "
                + "WHERE EXISTS (SELECT cc FROM CarRentalCompany cc "
                    + "WHERE ct IN(cc.carTypes) AND :givenRegion IN(cc.regions) AND EXISTS (SELECT c FROM Car c "
                      +  "WHERE c.type = ct AND NOT EXISTS (SELECT r FROM Reservation r, IN (c.reservations) AS q " 
                                                                 + "WHERE NOT (r.startDate >= :givenEndDate OR r.endDate <= :givenStartDate)))))")
                .setParameter("givenEndDate", end)
                .setParameter("givenStartDate", start)
                .setParameter("givenRegion", region)
                .getSingleResult();
         
        System.out.println("cheapest" + carType);
        return carType.toString();
    }

}
