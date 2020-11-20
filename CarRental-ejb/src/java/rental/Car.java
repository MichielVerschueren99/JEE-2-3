package rental;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static javax.persistence.CascadeType.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.AUTO;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Car {

   
    private int id;
    
    @ManyToOne(cascade={MERGE, PERSIST, REFRESH, DETACH})
    private CarType type;
    
    @OneToMany(cascade=ALL)
    private Set<Reservation> reservations;
    
    @Id
    @GeneratedValue(strategy=AUTO)
    private long globalID;

    /***************
     * CONSTRUCTOR *
     ***************/
    
    public Car() {}
    
    public Car(int uid, CarType type) {
    	this.id = uid;
        this.type = type;
        this.reservations = new HashSet<Reservation>();
    }

    /******
     * ID *
     ******/
    
    public int getId() {
    	return id;
    }
    
    /************
     * CAR TYPE *
     ************/
    
    
    public CarType getType() {
        return type;
    }
	
    /****************
     * RESERVATIONS *
     ****************/

    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        for(Reservation reservation : reservations) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    public void addReservation(Reservation res) {
        reservations.add(res);
    }
    
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }

    
    public Set<Reservation> getReservations() {
        return reservations;
    }
    
}