package edu.uw.edm.contentapi2.controller.content.v4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/content/v4/{profileId}/profile")
@Slf4j
public class ProfileDefinitionControllerV4 {

    private ProfileFacade profileFacade;

    @Autowired
    public ProfileDefinitionControllerV4(ProfileFacade profileFacade) {
        this.profileFacade = profileFacade;
    }


    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<ProfileDefinitionV4> getProfileDefinition(
            @PathVariable("profileId") String profileId,
            @AuthenticationPrincipal User user) throws NoSuchProfileException {

        ProfileDefinitionV4 profileDefinition = profileFacade.getProfileDefinition(profileId, user);
        return new ResponseEntity<>(profileDefinition, HttpStatus.OK);
    }
}


