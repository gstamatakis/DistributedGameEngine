package pm.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class PMController {

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping(value = "/ping/{value}")
    @ApiResponses(value = @ApiResponse(code = 400, message = "Something went wrong"))
    public String ping(@PathVariable String value) {
        return value;
    }
}
