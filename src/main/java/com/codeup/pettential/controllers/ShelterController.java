package com.codeup.pettential.controllers;

import com.codeup.pettential.models.Pet;
import com.codeup.pettential.models.Program;
import com.codeup.pettential.models.Shelter;
import com.codeup.pettential.models.User;
import com.codeup.pettential.repositories.PetRepository;
import com.codeup.pettential.repositories.ProgramRepository;
import com.codeup.pettential.repositories.ShelterRepository;
import com.codeup.pettential.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ShelterController {

    private final ShelterRepository shelterDao;
    private final UserRepository userDao;
    private final ProgramRepository programDao;
    private final PetRepository petDao;

    public ShelterController(ShelterRepository shelterDao, UserRepository userDao, ProgramRepository programDao, PetRepository petDao) {
        this.shelterDao = shelterDao;
        this.userDao = userDao;
        this.programDao = programDao;
        this.petDao = petDao;
    }

    @GetMapping ("adopter/{id}")
    public String findUser(@PathVariable long id, Model model) {
        model.addAttribute("user", userDao.findOne(id));
        return "views/user_info";
    }

    @GetMapping("shelter/{id}")
    public String findShelter(@PathVariable long id, Model model) {
        Shelter currentShelter = shelterDao.findOne(id);
        List<Pet> pets = petDao.findAllByShelter(currentShelter);
        List<Pet> pets1 = new ArrayList<>();
        for (Pet pet : pets){
            if (!pet.getName().equals("deleted")){
                pets1.add(pet);
            }
        }
        List<Program> programs = programDao.findAllByShelter(currentShelter);
        model.addAttribute("shelter", currentShelter);
        model.addAttribute("pets", pets1);
        model.addAttribute("programs", programs);
        return "views/shelter_view";
    }

    @GetMapping("shelter/home")
    public String getShelterHome(Model model){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Shelter shelter = shelterDao.findByUser(user);

        model.addAttribute("programs", programDao.findOne(shelter.getId()));
        model.addAttribute("apps", shelter.getApplicants());
        return "views/shelter_home";
    }

    @GetMapping("shelter/edit/{id}")
    public String editShelter(Model model, @PathVariable long id){
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() == "anonymousUser"){
            return "redirect:/home";
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.getIsShelter()){
            return "redirect:/home";
        }
        Shelter shelter = shelterDao.findByUser(user);
        model.addAttribute("shelter", shelter);
        return "edit_pages/shelter_edit";
    }

    @PostMapping("shelter/edit/{id}")
    public String saveUser(@PathVariable long id, @RequestParam(name = "name") String name,
                           @RequestParam(name = "location") String location, @RequestParam(name = "number") String number,
                           @RequestParam(name = "email") String email) {
        Shelter shelter = shelterDao.findOne(id);
        shelter.setName(name);
        shelter.setLocation(location);
        shelter.setNumber(number);
        shelter.setEmail(email);
        shelterDao.save(shelter);
        return "redirect:/home";
    }
}
