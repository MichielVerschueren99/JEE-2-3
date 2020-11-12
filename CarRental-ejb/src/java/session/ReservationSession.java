package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class ReservationSession implements ReservationSessionRemote {
    
    @PersistenceContext
    private EntityManager em;

    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    //@Override
    //public Set<String> getAllRentalCompanies() {
    //    return new HashSet<String>(RentalStore.getRentals().keySet());
    //} (NI GEBRUIKT)
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarType> result = em.createQuery(
                "SELECT ct FROM CarType ct "
              + "WHERE EXISTS (SELECT c FROM Car c "
                           +  "WHERE c.type = ct AND NOT EXISTS (SELECT r FROM Reservation r, IN(c.reservations) q " 
                                                               + "WHERE NOT (r.startDate > :givenEndDate OR r.endDate < :givenStartDate)))")
                .setParameter("givenEndDate", end)
                .setParameter("givenStartDate", start)
                .getResultList();
        System.out.println(result);
        return result;
    } 
        
    //    List<CarType> availableCarTypes = new LinkedList<CarType>();
    //    List<CarRentalCompany> CCList = em.createQuery("SELECT cc FROM CarRentalCompany cc").getResultList();
    //    for(CarRentalCompany crc : CCList) {
    //        for(CarType ct : crc.getAvailableCarTypes(start, end)) {
    //            if(!availableCarTypes.contains(ct))
    //                availableCarTypes.add(ct);
    //        }
    //    }
     //   return availableCarTypes;
    //}

    @Override
    public Quote createQuote(String company, ReservationConstraints constraints) throws ReservationException {
        try {
            CarRentalCompany requestedCompany = em.find(CarRentalCompany.class, company);
            Quote out = requestedCompany.createQuote(constraints, renter);
            quotes.add(out);
            return out;
        } catch(Exception e) {
            throw new ReservationException(e);
        }
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<>();
        try {
            for (Quote quote : quotes) {
                CarRentalCompany companyOfQuote = em.find(CarRentalCompany.class, quote.getRentalCompany());
                done.add(companyOfQuote.confirmQuote(quote));
            }
        } catch (Exception e) {
            for(Reservation r:done) {
                CarRentalCompany companyOfReservation = em.find(CarRentalCompany.class, r.getRentalCompany());
                companyOfReservation.cancelReservation(r);
            }   
            throw new ReservationException(e);
        }
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
        em.createQuery("SELECT cc FROM CarRentalCompany cc");
        return "unssupported";
    }
}