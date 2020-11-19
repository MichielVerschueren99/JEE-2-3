package rental;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static javax.persistence.CascadeType.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;

@Entity
public class Car implements Serializable {

   
    private int id;
    
    @ManyToOne(cascade={MERGE, PERSIST, REFRESH, DETACH})
    private CarType type;
    
    @OneToMany(cascade=ALL)
    private Set<Reservation> reservations;
    
    @TableGenerator(name="carGen",
        table="CAR_ID_GENERATOR",
        pkColumnName="GEN_KEY",
        valueColumnName="GEN_VALUE",
        pkColumnValue="CAR_ID",
        allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE,generator="carGen")
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