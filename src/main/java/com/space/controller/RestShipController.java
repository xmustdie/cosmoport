package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/rest")
public class RestShipController {

    @Autowired
    private ShipService shipService;

    @GetMapping("/ships")
    public @ResponseBody
    List<Ship> showAllShips(
            @RequestParam(value = "order", defaultValue = "ID") String shipOrder,
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "3") int pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long prodAfter,
            @RequestParam(value = "before", required = false) Long prodBefore,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating
    ) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(ShipOrder.valueOf(shipOrder).getFieldName()));
        return geShipsWithSpecification(name, planet, shipType, prodAfter, prodBefore, isUsed,
                minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, pageable);
    }

    @GetMapping("ships/count")
    public Integer getShipsCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long prodAfter,
            @RequestParam(value = "before", required = false) Long prodBefore,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating
    ) {
        Pageable pageable = Pageable.unpaged();
        List<Ship> shipList = geShipsWithSpecification(name, planet, shipType, prodAfter, prodBefore, isUsed,
                minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, pageable);
        return shipList.size();
    }

    @GetMapping("ships/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable Long id) {
        Optional<Ship> optionalShip = shipService.getShipById(id);
        if (id < 1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!optionalShip.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(optionalShip.get(), HttpStatus.OK);
    }

    @PostMapping("ships/")
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        if (ship == null
            || ship.getName() == null
            || ship.getPlanet() == null
            || ship.getShipType() == null
            || ship.getProdDate() == null
            || ship.getSpeed() == null
            || ship.getCrewSize() == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (ship.isUsed() == null) {
            ship.setUsed(false);
        }
        if (validateShipFields(ship)) {
            ship.setRating(ship.calculateRating());
            shipService.saveShip(ship);
            return new ResponseEntity<>(ship, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


    }

    @PostMapping("ships/{id}")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Ship> updateShip(@PathVariable Long id, @RequestBody Ship updatedShip) {
        if (!validateShipFields(updatedShip)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Ship> optionalShip = shipService.getShipById(id);
        if (id < 1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!optionalShip.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Ship editedShip = optionalShip.get();
        if (updatedShip.getName() != null) {
            editedShip.setName(updatedShip.getName());
        }
        if (updatedShip.getPlanet() != null) {
            editedShip.setPlanet(updatedShip.getPlanet());
        }
        if (updatedShip.getShipType() != null) {
            editedShip.setShipType(updatedShip.getShipType());
        }
        if (updatedShip.getProdDate() != null) {
            editedShip.setProdDate(updatedShip.getProdDate());
        }
        if (updatedShip.isUsed() != null) {
            editedShip.setUsed(updatedShip.isUsed());
        }
        if (updatedShip.getSpeed() != null) {
            editedShip.setSpeed(updatedShip.getSpeed());
        }
        if (updatedShip.getCrewSize() != null) {
            editedShip.setCrewSize(updatedShip.getCrewSize());
        }
        editedShip.setRating(editedShip.calculateRating());

        shipService.saveShip(editedShip);

        return new ResponseEntity<>(editedShip, HttpStatus.OK);
    }

    @DeleteMapping("/ships/{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable Long id) {
        Optional<Ship> optionalShip = shipService.getShipById(id);
        if (id < 1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!optionalShip.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        shipService.deleteShip(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private List<Ship> geShipsWithSpecification(String name, String planet, ShipType shipType, Long prodAfter, Long prodBefore,
                                                Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                                                Double minRating, Double maxRating, Pageable pageable) {
        Specification<Ship> specification =
                Specification.where(
                        shipService.filterByName(name)).and(
                        shipService.filterByPlanet(planet)).and(
                        shipService.filterByShipType(shipType)).and(
                        shipService.filterByDate(prodAfter, prodBefore)).and(
                        shipService.filterByUsage(isUsed)).and(
                        shipService.filterBySpeed(minSpeed, maxSpeed)).and(
                        shipService.filterByCrewSize(minCrewSize, maxCrewSize)).and(
                        shipService.filterByRating(minRating, maxRating));

        return shipService.getAllShips(specification, pageable);
    }

    private boolean validateShipFields(Ship ship) {

        if (ship.getName() != null && (ship.getName().length() < 1 || ship.getName().length() > 50))
            return false;

        if (ship.getPlanet() != null && (ship.getPlanet().length() < 1 || ship.getPlanet().length() > 50))
            return false;

        if (ship.getCrewSize() != null && (ship.getCrewSize() < 1 || ship.getCrewSize() > 9999))
            return false;

        if (ship.getSpeed() != null && (ship.getSpeed() < 0.01D || ship.getSpeed() > 0.99D))
            return false;

        if (ship.getProdDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ship.getProdDate());
            return calendar.get(Calendar.YEAR) >= 2800 && calendar.get(Calendar.YEAR) <= 3019;
        }
        return true;
    }

}

