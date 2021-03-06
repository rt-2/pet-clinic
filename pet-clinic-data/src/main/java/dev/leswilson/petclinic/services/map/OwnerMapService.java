package dev.leswilson.petclinic.services.map;

import dev.leswilson.petclinic.model.Owner;
import dev.leswilson.petclinic.model.Pet;
import dev.leswilson.petclinic.services.OwnerService;
import dev.leswilson.petclinic.services.PetService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile("map")
public class OwnerMapService extends AbstractMapService<Owner, Long> implements OwnerService {

    private final PetService petService;

    public OwnerMapService(PetService petService) {
        this.petService = petService;
    }

    @Override
    public Owner save(Owner owner) {
        // add logic to iterate through pets and save any that don't exist
        if(owner != null) {
            Set<Pet> pets = owner.getPets();
            if(!CollectionUtils.isEmpty(pets)) {
                pets.stream().filter(Pet::isNew).forEach(pet -> {
                    pet.setOwner(owner);
                    petService.save(pet);
                });
            }
            return super.save(owner);
        }
        return null;
    }

    @Override
    public Owner findByLastName(String lastName) {
        return this.findAll()
                .stream()
                .filter(entry -> lastName.equalsIgnoreCase(entry.getLastName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Owner> findAllByLastNameLike(String lastName) {

        return this.findAll()
                .stream()
                .filter(entry -> StringUtils.contains(entry.getLastName(), lastName))
                .collect(Collectors.toList());
    }
}
