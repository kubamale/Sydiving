package jakub.malewicz.skydiving.Repositories;

import jakub.malewicz.skydiving.Models.Departure;
import jakub.malewicz.skydiving.Models.DepartureUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartureUserRepository extends JpaRepository<DepartureUser, Long> {
    List<DepartureUser> getByDepartureId(long id);

    @Query(value = "SELECT d.id, d.jump_type, d.customer_id, d.skydiver_id, d.departure_id FROM departure_users d WHERE d.departure_id = :dep AND d.skydiver_id IN :skydivers", nativeQuery = true)
    List<DepartureUser> findByDepartureIdWhereInSkydiverId(@Param("dep") Long departure_id, @Param("skydivers") List<Long> skydiver_id);

    List<DepartureUser> findByDepartureIdAndSkydiverLicence(long Id, String name);
}
