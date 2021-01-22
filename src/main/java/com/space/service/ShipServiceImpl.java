package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ShipServiceImpl implements ShipService {
    @Autowired
    private ShipRepository shipRepository;

    @Override
    public List<Ship> getAllShips(Specification<Ship> specification, Pageable pageable) {
        return shipRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public void saveShip(Ship ship) {
        shipRepository.save(ship);
    }

    @Override
    public Optional<Ship> getShipById(Long id) {
        return shipRepository.findById(id);
    }

    @Override
    public void deleteShip(Long id) {
        shipRepository.deleteById(id);
    }

    @Override
    public Specification<Ship> filterByName(String name) {
        return getShipSpecification("name", name);
    }

    @Override
    public Specification<Ship> filterByPlanet(String planet) {
        return getShipSpecification("planet", planet);
    }

    @Override
    public Specification<Ship> filterByShipType(ShipType shipType) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return shipType == null ? null : criteriaBuilder.equal(root.get("shipType"), shipType);
            }
        };
    }

    @Override
    public Specification<Ship> filterByDate(Long dateAfterMs, Long dateBeforeMs) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                if (dateAfterMs == null && dateBeforeMs == null)
                    return null;
                if (dateAfterMs == null) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"),
                            new Date(dateBeforeMs));
                }
                if (dateBeforeMs == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"),
                            new Date(dateAfterMs));
                }

                return criteriaBuilder.between(root.get("prodDate"),
                        new Date(dateAfterMs),
                        new Date(dateBeforeMs));
            }
        };
    }

    @Override
    public Specification<Ship> filterByUsage(Boolean isUsed) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (isUsed == null) {
                    return null;
                }
                return isUsed ? criteriaBuilder.isTrue(root.get("isUsed")) : criteriaBuilder.isFalse(root.get("isUsed"));
            }
        };
    }

    @Override
    public Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (minSpeed == null && maxSpeed == null)
                    return null;
                if (minSpeed == null)
                    return criteriaBuilder.lessThanOrEqualTo(root.get("speed"), maxSpeed);
                if (maxSpeed == null)
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), minSpeed);

                return criteriaBuilder.between(root.get("speed"), minSpeed, maxSpeed);
            }
        };
    }

    @Override
    public Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (minCrewSize == null && maxCrewSize == null)
                    return null;
                if (minCrewSize == null)
                    return criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize);
                if (maxCrewSize == null)
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), minCrewSize);

                return criteriaBuilder.between(root.get("crewSize"), minCrewSize, maxCrewSize);
            }
        };
    }

    @Override
    public Specification<Ship> filterByRating(Double min, Double max) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (min == null && max == null)
                    return null;
                if (min == null)
                    return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), max);
                if (max == null)
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), min);

                return criteriaBuilder.between(root.get("rating"), min, max);
            }
        };
    }

    private Specification<Ship> getShipSpecification(String fieldName, String stringValue) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return stringValue == null ? null : cb.like(root.get(fieldName), "%" + stringValue + "%");
            }
        };
    }

    private Date normalizeDateFromFrontend (Long unixDate) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(unixDate);
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR),Calendar.JANUARY, 1, 0, 0, 0);
        return calendar.getTime();
    }

}
