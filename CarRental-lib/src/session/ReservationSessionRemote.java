package session;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Remote
public interface ReservationSessionRemote {
    
    public void setRenterName(String name);
    
    public String getRenterName();
    
    public List<CarType> getAvailableCarTypes(Date start, Date end);
    
    public void createQuote(String name, ReservationConstraints constraints) throws ReservationException;
    
    public List<Quote> getCurrentQuotes();
    
    public List<Reservation> confirmQuotes() throws ReservationException;
    
    public String getCheapestCarType(Date start, Date end, String region) throws Exception;
    
    public Set<String> getAllRentalCompanies();
}