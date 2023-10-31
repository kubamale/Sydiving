package jakub.malewicz.skydiving.Services;

import jakub.malewicz.skydiving.DTOs.*;
import jakub.malewicz.skydiving.Models.Departure;
import jakub.malewicz.skydiving.Models.DepartureUser;
import jakub.malewicz.skydiving.Models.Plane;
import jakub.malewicz.skydiving.Repositories.DepartureRepository;
import jakub.malewicz.skydiving.Repositories.DepartureUserRepository;
import jakub.malewicz.skydiving.Repositories.PlaneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DepartureService {

    private final DepartureRepository departureRepository;
    private final PlaneRepository planeRepository;
    private final DepartureUserRepository departureUserRepository;

    public ResponseEntity<List<DepartureDetailsDTO>> getDepartures(String date, int page) throws ParseException {

        List<Departure> departures = departureRepository.getDepartures(date, page);

        Map<Departure,List<DepartureUser>> departureSkydivers = new HashMap<>();

        for (Departure departure : departures){
            if (!departureSkydivers.containsKey(departure)){
                departureSkydivers.put(departure,new ArrayList<>());
            }

            List<DepartureUser> departureUser = departureUserRepository.getByDepartureId(departure.getId());

            departureSkydivers.put(departure,departureUser);
        }

        List<DepartureDetailsDTO> result = new ArrayList<>();
        departureSkydivers.forEach((departure, departureUserList) -> result.add(Mappers.mapToDTO(departure, departureUserList)));

        return ResponseEntity.ok(result);
    }

    ///TODO: Create logic to block adding new departure with the same plane earlier the 30 min after previous flight
    public ResponseEntity<DepartureDetailsDTO> createDeparture(DepartureCreateDTO departure) {
        Optional<Plane> plane = planeRepository.findById(departure.planeId());

        if (plane.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Departure createdDeparture = departureRepository.save(new Departure(
                departure.date(),
                departure.time(),
                departure.allowStudents(),
                departure.allowAFF(),
                plane.get()
        ));


        return ResponseEntity.ok(Mappers.mapToDTO(createdDeparture, departureUserRepository.getByDepartureId(createdDeparture.getId())));
    }

    public ResponseEntity deleteDeparture(long id) {

        Optional<Departure> departure = departureRepository.findById(id);

        if (departure.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        departureRepository.delete(departure.get());

        return ResponseEntity.ok().build();

    }

    public ResponseEntity<DepartureDetailsDTO> updateDeparture(DepartureUpdateDTO departure) {
        Optional<Departure> savedDeparture = departureRepository.findById(departure.id());

        if (savedDeparture.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        if (departure.date() != null){
            savedDeparture.get().setDate(departure.date());
        }

        if (departure.time() != null){
            savedDeparture.get().setTime(departure.time());
        }

        if (departure.allowStudents() != savedDeparture.get().isAllowStudents()){
            savedDeparture.get().setAllowStudents(departure.allowStudents());
        }

        if (departure.allowAFF() != savedDeparture.get().isAllowAFF()){
            savedDeparture.get().setAllowAFF(departure.allowAFF());
        }

        ///TODO: Don't allow to change planes if its already used on that time
        if (departure.planeId() != savedDeparture.get().getPlane().getId()){
            Optional<Plane> plane = planeRepository.findById(departure.planeId());
            if (plane.isEmpty()){
                return ResponseEntity.badRequest().build();
            }

            savedDeparture.get().setPlane(plane.get());
        }

        Departure updatedDeparture = departureRepository.save(savedDeparture.get());

        List<DepartureUser> departureUser = departureUserRepository.findByDepartureIdWhereInSkydiverId(departure.id(),  departure.usersId());
        departureUserRepository.deleteAll(departureUser);
        departureUser = departureUserRepository.getByDepartureId(departure.id());
        return ResponseEntity.ok(Mappers.mapToDTO(updatedDeparture, departureUser));

    }

    ///TODO: Check if request was not sent less then 1 hour before flight if its sent by USER
    public ResponseEntity<DepartureDetailsDTO> deleteUserFromDeparture(DeleteUsersDTO userId, long departureId) {

        List<DepartureUser> departureUser = departureUserRepository.findByDepartureIdWhereInSkydiverId(departureId, userId.ids());

        if (departureUser.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        departureUserRepository.deleteAll(departureUser);
        List<DepartureUser> departureUserList = departureUserRepository.getByDepartureId(departureId);
        Optional<Departure> myDeparture = departureRepository.findById(departureId);

        return myDeparture.map(departure -> ResponseEntity.ok(Mappers.mapToDTO(departure, departureUserList))).orElseGet(() -> ResponseEntity.badRequest().build());

    }

    public ResponseEntity<List<String>> getDeparturesDates(String startDate, String endDate) {
        return ResponseEntity.ok(departureRepository.getDates(startDate, endDate));
    }
}
