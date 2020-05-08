package dto;

import io.swagger.annotations.ApiModelProperty;
import model.Role;

public class UserResponseDTO {

    @ApiModelProperty()
    private Integer id;
    @ApiModelProperty()
    private String username;
    @ApiModelProperty()
    private String email;
    @ApiModelProperty()
    Role role;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
