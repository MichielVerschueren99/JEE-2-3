package rental;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static javax.persistence.CascadeType.ALL;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import rental.CarType;
import rental.Reservation;

@Entity
public class Car implements Serializable {

   
    private int id;
    
    private CarType type;
    private Set<Reservation> reservations;
    
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
   
    @TableGenerator(name="carGen",
        table="CAR_ID_GENERATOR",
        pkColumnName="GEN_KEY",
        valueColumnName="GEN_VALUE",
        pkColumnValue="CAR_ID",
        allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE,generator="carGen")
    public long getGlobalID() {
    	return this.globalID;
    }
    
    public void setGlobalID(long newID) {
    	this.globalID = newID;
    }
    
    public int getId() {
    	return id;
    }
    
    public void setId(int newId) {
    	this.id = newId;
    }
    
    /************
     * CAR TYPE *
     ************/
    
    @ManyToOne
    public CarType getType() {
        return type;
    }
	
    public void setType(CarType type) {
        this.type = type;
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

    @OneToMany(cascade=ALL)
    public Set<Reservation> getReservations() {
        return reservations;
    }
    
    public void setReservations(Set<Reservation> newSet) {
        this.reservations = newSet;
    }
}