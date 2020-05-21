package gm.controller;

import exception.CustomException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@RestController
@RequestMapping("/game")
public class GMController {

    @Autowired
    ModelMapper modelMapper;

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<String> handleConflict(CustomException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @GetMapping(value = "/test/{value}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    @ApiOperation(value = "${UserController.test}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "The user doesn't exist"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String ping(@PathVariable String value) {
        return "Sent " + value;
    }
}
