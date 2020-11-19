package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import rental.Car;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import session.ManagerSessionRemote;
import session.ReservationSessionRemote;

public class Main extends AbstractTestManagement<ReservationSessionRemote, ManagerSessionRemote> {
    
    @EJB
    private static ManagerSessionRemote managerSession;
    
    public Main(String scriptFile) {
        super(scriptFile);
        
    }

    public static void main(String[] args) throws Exception {
        // use updated manager interface to load cars into companies
        
        CrcData hertzData = loadRental("hertz.csv");
        CrcData dockxData = loadRental("dockx.csv");
        managerSession.addCompany(hertzData.name, hertzData.regions, new ArrayList<>());
        managerSession.addCompany(dockxData.name, dockxData.regions, new ArrayList<>());
        
        Set<CarType> allTypes1 = new HashSet<>();
        
        Set<CarType> allTypes2 = new HashSet<>();
        
        for (Car c : hertzData.cars) {
            allTypes1.add(c.getType());
        }
        
        for (Car c : dockxData.cars) {
            allTypes2.add(c.getType());
        }
        
        for (CarType ct : allTypes1) {
            managerSession.addCarType(ct, "Hertz");
        }
        
        for (CarType ct : allTypes2) {
            managerSession.addCarType(ct, "Dockx");
        }
        
        for (Car c : hertzData.cars) {
            managerSession.addCar(c.getId(), c.getType().getName(), "Hertz");
        }
        
        for (Car c : dockxData.cars) {
            managerSession.addCar(c.getId(), c.getType().getName(), "Dockx");
        }
        
        new Main("trips").run();               
    }

    @Override
    protected Set<String> getBestClients(ManagerSessionRemote ms) throws Exception {
        return ms.getBestClients();
    }

    @Override
    protected String getCheapestCarType(ReservationSessionRemote session, Date start, Date end, String region) throws Exception {
        return session.getCheapestCarType(start, end, region);
    }

    @Override
    protected CarType getMostPopularCarTypeIn(ManagerSessionRemote ms, String carRentalCompanyName, int year) throws Exception {
        //TEST OP GEEN ERRORS
       System.out.println(ms.getCarTypes(carRentalCompanyName));
       System.out.println(ms.getCarIds(carRentalCompanyName, "MPV"));
       System.out.println(ms.getNumberOfReservations(carRentalCompanyName, "Compact", 1));
       ms.addCarType(new CarType("CHECK", 666, 900000, 10000, true), carRentalCompanyName);
       ms.addCar(420, "CHECK", carRentalCompanyName);
       return ms.getMostPopularCarTypeIn(carRentalCompanyName, year);
    }

    @Override
    protected ReservationSessionRemote getNewReservationSession(String name) throws Exception {
        InitialContext context = new InitialContext();
        return (ReservationSessionRemote) context.lookup(ReservationSessionRemote.class.getName());
    }

    @Override
    protected ManagerSessionRemote getNewManagerSession(String name) throws Exception {
        return managerSession;
    }

    @Override
    protected void getAvailableCarTypes(ReservationSessionRemote session, Date start, Date end) throws Exception {
        System.out.println(session.getAvailableCarTypes(start, end));
    }

    @Override
    protected void createQuote(ReservationSessionRemote session, String name, Date start, Date end, String carType, String region) throws Exception {
        ReservationConstraints constraints = new ReservationConstraints(start, end, carType, region);
        session.createQuote(name, constraints);
    }

    @Override
    protected List<Reservation> confirmQuotes(ReservationSessionRemote session, String name) throws Exception {
        return session.confirmQuotes();
    }

    @Override
    protected int getNumberOfReservationsBy(ManagerSessionRemote ms, String clientName) throws Exception {
        return ms.getNumberOfReservationsBy(clientName);
    }

    @Override
    protected int getNumberOfReservationsByCarType(ManagerSessionRemote ms, String carRentalName, String carType) throws Exception {
        return ms.getNumberOfReservationsByCarType(carRentalName, carType);
    }
    
    protected void addCompany(ManagerSessionRemote ms, String name, List<String> regions, List<Car> cars) {
        ms.addCompany(name, regions, cars);
    }
    
    private static CrcData loadRental(String datafile) {
        CrcData data = null;
        try {
            data =loadData(datafile);
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Loaded {0} from file {1}", new Object[]{data.name, datafile});
        } catch (NumberFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    private static CrcData loadData(String datafile)
            throws NumberFormatException, IOException {

        CrcData out = new CrcData();
        StringTokenizer csvReader;
        int nextuid = 0;
       
        //open file from jar
        BufferedReader in = new BufferedReader(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream(datafile)));
        
        try {
            while (in.ready()) {
                String line = in.readLine();
                
                if (line.startsWith("#")) {
                    // comment -> skip					
                } else if (line.startsWith("-")) {
                    csvReader = new StringTokenizer(line.substring(1), ",");
                    out.name = csvReader.nextToken();
                    out.regions = Arrays.asList(csvReader.nextToken().split(":"));
                } else {
                    csvReader = new StringTokenizer(line, ",");
                    //create new car type from first 5 fields
                    CarType type = new CarType(csvReader.nextToken(),
                            Integer.parseInt(csvReader.nextToken()),
                            Float.parseFloat(csvReader.nextToken()),
                            Double.parseDouble(csvReader.nextToken()),
                            Boolean.parseBoolean(csvReader.nextToken()));
                    //create N new cars with given type, where N is the 5th field
                    for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
                        out.cars.add(new Car(nextuid++, type));
                    }        
                }
            } 
        } finally {
            in.close();
        }

        return out;
    }
    
    static class CrcData {
            public List<Car> cars = new LinkedList<Car>();
            public String name;
            public List<String> regions =  new LinkedList<String>();
    }
}