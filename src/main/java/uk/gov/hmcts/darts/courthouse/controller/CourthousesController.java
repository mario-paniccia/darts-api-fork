package uk.gov.hmcts.darts.courthouse.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.darts.courthouse.api.CourthousesApi;
import uk.gov.hmcts.darts.courthouse.mapper.CourthouseToCourthouseEntityMapper;
import uk.gov.hmcts.darts.courthouse.model.Courthouse;
import uk.gov.hmcts.darts.courthouse.model.ExtendedCourthouse;
import uk.gov.hmcts.darts.courthouse.service.CourthouseService;

import java.util.List;
import javax.validation.Valid;

@RestController
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CourthousesController implements CourthousesApi {

    @Autowired
    CourthouseService courthouseService;

    @Autowired
    CourthouseToCourthouseEntityMapper mapper;

    @Override
    public ResponseEntity<Void> courthousesCourthouseIdDelete(
        @Parameter(name = "courthouse_id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("courthouse_id") Integer courthouseId
    ) {
        courthouseService.deleteCourthouseById(courthouseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @Override
    public ResponseEntity<ExtendedCourthouse> courthousesCourthouseIdGet(
        @Parameter(name = "courthouse_id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("courthouse_id") Integer courthouseId
    ) {
        try {
            uk.gov.hmcts.darts.common.entity.Courthouse courtHouseEntity = courthouseService.getCourtHouseById(
                courthouseId);
            ExtendedCourthouse responseEntity = mapper.mapFromEntityToExtendedCourthouse(courtHouseEntity);
            return new ResponseEntity<>(responseEntity, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }


    }

    @Override
    public ResponseEntity<Void> courthousesCourthouseIdPut(
        @Parameter(name = "courthouse_id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("courthouse_id") Integer courthouseId,
        @Parameter(name = "Courthouse", description = "", required = true) @Valid @RequestBody Courthouse courthouse
    ) {
        try {
            courthouseService.amendCourthouseById(courthouse, courthouseId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException exception) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }


    }

    @Override
    public ResponseEntity<List<ExtendedCourthouse>> courthousesGet(

    ) {
        List<uk.gov.hmcts.darts.common.entity.Courthouse> courtHouseEntities = courthouseService.getAllCourthouses();
        List<ExtendedCourthouse> responseEntities = mapper.mapFromListEntityToListExtendedCourthouse(courtHouseEntities);
        return new ResponseEntity<>(responseEntities, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ExtendedCourthouse> courthousesPost(
        @Parameter(name = "Courthouse", description = "", required = true) @Valid @RequestBody Courthouse courthouse
    ) {
        uk.gov.hmcts.darts.common.entity.Courthouse addedCourtHouse = courthouseService.addCourtHouse(courthouse);
        ExtendedCourthouse extendedCourthouse = mapper.mapFromEntityToExtendedCourthouse(addedCourtHouse);
        return new ResponseEntity<>(extendedCourthouse, HttpStatus.CREATED);
    }
}